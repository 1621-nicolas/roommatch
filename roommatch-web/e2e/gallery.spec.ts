import { test, expect, Page } from '@playwright/test';
import { fixtures, paged } from './fixtures';

const room = {idHabitacion: 11, idPropietario: 1, nombrePropietario: 'Propietario de prueba', propietarioVerificado: false,
  titulo: 'Habitación de prueba junto al parque', descripcion: 'Habitación de prueba con luz natural y acceso compartido a la cocina.',
  distrito: 'Trujillo', direccionReferencial: 'A dos cuadras del parque', precio: 750, areaM2: 18,
  amoblado: true, banoPrivado: true, internetIncluido: true, aguaIncluida: false, luzIncluida: false,
  permiteMascotas: false, disponibleDesde: null, destacada: false, estado: 'activa', bloqueada: false,
  imagenPrincipal: 'https://images.example/first.svg', fechaPublicacion: '2026-09-26T10:00:00', fechaActualizacion: '2026-09-26T10:00:00'};
const publication = {idPublicacion: 12, idUsuario: 2, nombreUsuario: 'Persona de prueba', edad: 24, ocupacion: 'Estudiante', foto: null,
  titulo: 'Busco compartir un departamento', descripcion: 'Prefiero un hogar tranquilo y organizar los gastos de manera clara.',
  distrito: 'Trujillo', tipoPublicacion: 'busco_compartir', presupuestoMin: 500, presupuestoMax: 900,
  estado: 'activa', esMiPublicacion: false, porcentajeCompatibilidad: null, tipoVinculacionVivienda: null,
  imagenPrincipal: 'https://images.example/first.svg', fechaPublicacion: '2026-09-26T10:00:00'};
const photos = [{idImagen: 1, orden: 1, principal: false, urlImagen: 'https://images.example/first.svg'},
  {idImagen: 2, orden: 2, principal: true, urlImagen: 'https://images.example/second.svg'}];

async function galleryFixtures(page: Page) {
  await fixtures(page);
  const calls: string[] = [], imageReferrers: (string | undefined)[] = [];
  const data: Record<string, unknown> = {
    '/api/habitaciones': paged([room]), '/api/habitaciones/11': room,
    '/api/publicaciones-roomie': paged([publication]), '/api/publicaciones-roomie/12': publication,
    '/api/imagenes-habitacion/habitacion/11': photos, '/api/publicaciones-roomie/12/imagenes': photos
  };
  await page.route('**/api/**', route => {
    const path = new URL(route.request().url()).pathname;
    if (!(path in data)) return route.fallback();
    calls.push(path);
    return route.fulfill({contentType: 'application/json', body: JSON.stringify({status: 'success', data: data[path]})});
  });
  await page.route('https://images.example/**', route => {
    imageReferrers.push(route.request().headers()['referer']);
    // Diagrammatic fixture, never used by the production application as a room photograph.
    return route.fulfill({contentType: 'image/svg+xml', body: '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="400"><rect width="640" height="400" fill="#e2e8f0"/><text x="320" y="200" text-anchor="middle" font-family="sans-serif" font-size="28" fill="#334155">Foto de prueba de galería</text></svg>'});
  });
  return {calls, imageReferrers};
}

for (const surface of [
  {name: 'rooms-with-photos', path: '/habitaciones', detail: false},
  {name: 'room-gallery', path: '/habitaciones/11', detail: true},
  {name: 'publications-with-photos', path: '/publicaciones-roomie', detail: false},
  {name: 'publication-gallery', path: '/publicaciones-roomie/12', detail: true}
]) {
  test(`${surface.name}: displays API photos without per-card API requests`, async ({page}, testInfo) => {
    const {calls, imageReferrers} = await galleryFixtures(page);
    const errors: string[] = []; page.on('pageerror', error => errors.push(error.message));
    await page.goto(surface.path);
    const image = page.locator('app-listing-image img').first();
    await expect(image).toBeVisible(); await expect(image).toHaveAttribute('referrerpolicy', 'no-referrer');
    await expect.poll(() => image.evaluate((element: HTMLImageElement) => element.naturalWidth)).toBeGreaterThan(0);
    if (surface.detail) {
      await expect(page.getByRole('button', {name: 'Foto 2', exact: true})).toHaveAttribute('aria-pressed', 'true');
      await page.getByRole('button', {name: 'Foto 1', exact: true}).click();
      await expect(image).toHaveAttribute('src', photos[0].urlImagen);
      expect(calls.filter(path => path.includes('imagenes'))).toHaveLength(1);
    } else { expect(calls.filter(path => path.includes('imagenes'))).toHaveLength(0); }
    expect(imageReferrers.every(value => value === undefined)).toBe(true);
    expect(errors).toEqual([]);
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(page.viewportSize()!.width);
    await testInfo.attach(surface.name, {body: await page.screenshot({fullPage: true}), contentType: 'image/png'});
  });
}

test('a gallery error remains distinct from no photos and can be retried', async ({page}) => {
  await galleryFixtures(page); let failing = true;
  await page.route('**/api/imagenes-habitacion/habitacion/11', route => route.fulfill({status: failing ? 503 : 200,
    contentType: 'application/json', body: JSON.stringify({status: failing ? 'error' : 'success', data: []})}));
  await page.goto('/habitaciones/11');
  await expect(page.locator('app-listing-gallery [role=alert]')).toBeVisible();
  await expect(page.getByRole('heading', {name: 'Sobre la habitación'})).toBeVisible();
  await expect(page.getByText('Este anuncio todavía no tiene fotos.')).toHaveCount(0);
  failing = false; await page.getByRole('button', {name: 'Reintentar fotos'}).click();
  await expect(page.getByText('Este anuncio todavía no tiene fotos.')).toBeVisible();
});

test('a broken external photo does not prevent switching to another image', async ({page}) => {
  await galleryFixtures(page);
  await page.route('https://images.example/second.svg', route => route.fulfill({status: 404, body: 'Unavailable'}));
  await page.goto('/habitaciones/11');
  await expect(page.getByText('No se pudo cargar esta foto')).toBeVisible();
  await page.getByRole('button', {name: 'Foto 1', exact: true}).click();
  await expect(page.locator('app-listing-gallery img')).toBeVisible();
  await expect(page.getByText('No se pudo cargar esta foto')).toHaveCount(0);
});
