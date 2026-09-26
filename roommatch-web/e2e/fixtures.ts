import { Page } from '@playwright/test';
// UI fixtures are never imported by src/ or included in the production bundle.
export const paged = (content: unknown[]) => ({content, number: 0, size: 20, totalElements: content.length,
  totalPages: content.length ? 1 : 0, first: true, last: true, numberOfElements: content.length, empty: !content.length});
export const report = {idReporte: 1, idUsuarioReportante: 2, nombreReportante: 'Reportante de prueba', idUsuarioReportado: 3,
  nombreReportado: 'Usuario de prueba', motivo: 'Contenido inapropiado', descripcion: 'Descripción para probar la revisión.',
  estado: 'pendiente', estadoObjetivo: 'activo', fechaReporte: '2026-09-26T10:00:00', fechaRevision: null};
export async function session(page: Page) {
  await page.addInitScript(() => {
    // Deliberately not a valid server JWT. All API requests are intercepted in these UI tests.
    const payload = btoa(JSON.stringify({exp: Math.floor(Date.now() / 1000) + 3600})).replace(/=/g, '');
    localStorage.setItem('roommatch_token', `test.${payload}.test`);
    localStorage.setItem('roommatch_usuario', JSON.stringify({idUsuario: 1, nombres: 'Cuenta', apellidos: 'de prueba', email: 'test@example.test', rol: 'ADMIN'}));
  });
}
export async function fixtures(page: Page) {
  await page.route('**/api/**', async route => {
    const pathname = new URL(route.request().url()).pathname;
    const data: Record<string, unknown> = {
      '/api/propietarios/me': null,
      '/api/contactos/me': null,
      '/api/perfil/me': {idPerfil: 1},
      '/api/solicitudes/recibidas': [],
      '/api/habitaciones': paged([{idHabitacion: 11, titulo: 'Habitación de prueba junto al parque', distrito: 'Trujillo', precio: 750, amoblado: true, banoPrivado: true, internetIncluido: true, destacada: false}]),
      '/api/publicaciones-roomie': paged([{idPublicacion: 12, nombreUsuario: 'Persona de prueba', titulo: 'Busco compartir un departamento', descripcion: 'Prefiero un hogar tranquilo y organizar los gastos de manera clara.', distrito: 'Trujillo', tipoPublicacion: 'busco_compartir'}]),
      '/api/matches': paged([{idUsuarioDestino: 2, nombres: 'Persona', apellidos: 'de prueba', ocupacion: 'Estudiante', porcentaje: 82, cobertura: 92, foto: null}]),
      '/api/contactos/desbloqueados': paged([{idUsuario: 2, nombreCompleto: 'Contacto de prueba con nombre extenso', fechaConexion: '2026-09-26T10:00:00', contacto: {telefono: null, whatsapp: null, instagram: null, facebook: null, emailContacto: 'contacto@example.test', mostrarTelefono: false, mostrarWhatsapp: false, mostrarInstagram: false, mostrarFacebook: false, mostrarEmail: true}}, {idUsuario: 3, nombreCompleto: 'Contacto sin datos compartidos', fechaConexion: '2026-09-26T10:00:00', contacto: null}]),
      '/api/admin/dashboard': {totalUsuarios: 40, usuariosActivos: 38, usuariosSuspendidos: 2, totalPropietarios: 5, totalPerfilesConvivencia: 30, totalHabitaciones: 12, habitacionesActivas: 10, habitacionesPausadas: 2, totalPublicacionesRoomie: 20, publicacionesActivas: 18, totalMatches: 5, solicitudesPendientes: 3, leadsPendientes: 4, reportesUsuariosPendientes: 1, reportesHabitacionesPendientes: 0, notificacionesNoLeidas: 5},
      '/api/reportes/admin/usuarios': paged([report]),
      '/api/reportes/admin/usuarios/1/historial': paged([])
    };
    if (!(pathname in data)) throw new Error(`Unexpected API request in UI fixture: ${route.request().method()} ${pathname}`);
    await route.fulfill({contentType: 'application/json', body: JSON.stringify({status: 'success', message: 'UI fixture', data: data[pathname]})});
  });
}
