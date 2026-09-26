import { test, expect } from '@playwright/test';
import { fixtures, paged, session } from './fixtures';

const surfaces = [
  {name: 'home', path: '/', signedIn: false}, {name: 'login', path: '/login', signedIn: false},
  {name: 'register', path: '/register', signedIn: false}, {name: 'signed-home', path: '/', signedIn: true},
  {name: 'contacts', path: '/contactos', signedIn: true}, {name: 'admin-dashboard', path: '/admin/dashboard', signedIn: true},
  {name: 'admin-reports', path: '/admin/reportes', signedIn: true}
];
for (const surface of surfaces) {
  test(`${surface.name}: renders under CSP without page overflow or script errors`, async ({page}, testInfo) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    page.on('console', message => {if (message.type() === 'error') errors.push(message.text());});
    await fixtures(page); if (surface.signedIn) await session(page);
    await page.goto(surface.path); await expect(page.locator('main h1')).toHaveCount(1);
    // Static production server + finite intercepted requests; no polling or websocket connections.
    await page.waitForLoadState('networkidle');
    expect(errors).toEqual([]);
    const width = await page.evaluate(() => ({viewport: innerWidth, content: document.documentElement.scrollWidth}));
    expect(width.content).toBeLessThanOrEqual(width.viewport);
    const unlabelled = await page.locator('input,select,textarea').evaluateAll(elements => elements.filter(element =>
      !(element as HTMLInputElement).labels?.length && !element.getAttribute('aria-label')).length);
    expect(unlabelled).toBe(0);
    await testInfo.attach(surface.name, {body: await page.screenshot({fullPage: true}), contentType: 'image/png'});
  });
}

test('home recovers from a malformed collection through the visible retry', async ({page}) => {
  await fixtures(page); let malformed = true;
  await page.route('**/api/habitaciones?*', route => route.fulfill({contentType: 'application/json', body: JSON.stringify({status: 'success', data: {...paged([]), content: malformed ? {} : []}})}));
  await page.goto('/'); await expect(page.getByRole('button', {name: 'Reintentar habitaciones'})).toBeVisible();
  await expect(page.getByText('No hay habitaciones disponibles')).toHaveCount(0);
  malformed = false; await page.getByRole('button', {name: 'Reintentar habitaciones'}).click();
  await expect(page.getByText('No hay habitaciones disponibles')).toBeVisible();
});

test('keyboard opens account links, Escape returns focus, and mobile navigation expands', async ({page}) => {
  await fixtures(page); await session(page); await page.goto('/');
  const trigger = page.getByRole('button', {name: 'Mi cuenta', exact: true});
  await trigger.focus(); await page.keyboard.press('Enter');
  await expect(trigger).toHaveAttribute('aria-expanded', 'true');
  await page.keyboard.press('Tab'); await expect(page.getByRole('link', {name: 'Mi cuenta y privacidad'})).toBeFocused();
  await page.keyboard.press('Escape'); await expect(trigger).toBeFocused(); await expect(trigger).toHaveAttribute('aria-expanded', 'false');
  if (page.viewportSize()!.width < 1050) {
    await page.getByRole('button', {name: 'Menú', exact: true}).click();
    await expect(page.locator('#primary-links').getByRole('link', {name: 'Habitaciones'})).toBeVisible();
  }
});

test('moderation dialog focuses its reason, cancels with Escape and retains a rejected draft', async ({page}, testInfo) => {
  await fixtures(page); await session(page); await page.goto('/admin/reportes');
  await page.getByRole('button', {name: 'Ver reporte 1', exact: true}).click();
  const suspend = page.getByRole('button', {name: 'Suspender cuenta', exact: true}); await suspend.click();
  await expect(page.getByLabel('Motivo de la decisión')).toBeFocused();
  await expect(page.getByRole('button', {name: 'Confirmar decisión'})).toBeDisabled();
  await page.keyboard.press('Escape'); await expect(page.locator('dialog')).not.toBeVisible(); await expect(suspend).toBeFocused();
  await suspend.click(); await page.getByLabel('Motivo de la decisión').fill('Motivo de prueba que debe conservarse.');
  let decisions = 0;
  await page.route('**/api/reportes/admin/usuarios/1/sancionar', route => {decisions++; return route.fulfill({status: 409, contentType: 'application/json', body: JSON.stringify({status: 'error', message: 'Otro administrador resolvió el caso.', data: null})});});
  await page.getByRole('button', {name: 'Confirmar decisión'}).click();
  await expect(page.locator('dialog [role=alert]')).toContainText('Otro administrador');
  await expect(page.getByLabel('Motivo de la decisión')).toHaveValue('Motivo de prueba que debe conservarse.');
  await expect(page.getByRole('button', {name: 'Confirmar decisión'})).toHaveCount(0);
  await page.getByLabel('Motivo de la decisión').press('Control+Enter');
  expect(decisions).toBe(1);
  await testInfo.attach('moderation-conflict', {body: await page.screenshot(), contentType: 'image/png'});
  await page.getByRole('button', {name: 'Actualizar estado', exact: true}).click();
  await expect(page.locator('dialog')).not.toBeVisible();
  await expect(page.getByRole('heading', {name: 'Borrador sin enviar · Reporte #1 de usuarios'})).toBeVisible();
  await expect(page.getByText('Motivo de prueba que debe conservarse.', {exact: true})).toBeVisible();
  await expect(page.getByLabel('Estado del reporte')).toHaveValue('');
  expect(decisions).toBe(1);
});

test('login reports rejected credentials without leaving the form', async ({page}) => {
  await fixtures(page);
  await page.route('**/api/auth/login', route => route.fulfill({status: 401, contentType: 'application/json', body: JSON.stringify({status: 'error', message: 'Credenciales inválidas', data: null})}));
  await page.goto('/login'); await page.getByLabel('Correo electrónico').fill('test@example.test');
  await page.getByLabel('Contraseña', {exact: true}).fill('test-password');
  await page.getByRole('button', {name: 'Iniciar sesión', exact: true}).click();
  await expect(page.getByRole('alert')).toContainText('Credenciales inválidas');
  await expect(page).toHaveURL(/\/login$/); await expect(page.getByRole('button', {name: 'Iniciar sesión', exact: true})).toBeEnabled();
});
