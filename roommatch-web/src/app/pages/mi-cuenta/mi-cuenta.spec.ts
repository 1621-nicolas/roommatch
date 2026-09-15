import { of, throwError } from 'rxjs';
import { MiCuenta } from './mi-cuenta';
import { PerfilService } from '../../core/services/perfil.service';

describe('MiCuenta description editing', () => {
  it('sends and adopts the contact version without silently opting into email sharing', () => {
    const actualizarMiContacto = vi.fn().mockReturnValue(of({status: 'success', data: {version: 4, mostrarEmail: false}}));
    const page = new MiCuenta({} as never, {} as never, {actualizarMiContacto} as never, {} as never);
    page.contactoExiste = true; page.contactoData.version = 3;
    page.guardarContacto();
    expect(actualizarMiContacto).toHaveBeenCalledWith(expect.objectContaining({version: 3, mostrarEmail: false}));
    expect(page.contactoData.version).toBe(4);
  });
  it('preserves the contact draft on conflict instead of automatically retrying stale consent', () => {
    const actualizarMiContacto = vi.fn().mockReturnValue(throwError(() => ({status: 409, error: {message: 'Recarga tu cuenta'}})));
    const page = new MiCuenta({} as never, {} as never, {actualizarMiContacto} as never, {} as never);
    page.contactoExiste = true; page.contactoData.version = 3; page.contactoData.emailContacto = 'draft@example.test';
    page.guardarContacto();
    expect(page.contactoData.emailContacto).toBe('draft@example.test');
    expect(page.contactoData.version).toBe(3); expect(page.mensajeError).toContain('Recarga');
    expect(actualizarMiContacto).toHaveBeenCalledTimes(1);
  });
  function pageWith(profileService: Partial<PerfilService>) {
    const page = new MiCuenta({} as never, profileService as PerfilService, {} as never, {} as never);
    page.perfilExiste = true; page.perfilVersion = 2; page.descripcionPersonal = 'Mi descripción';
    return page;
  }
  it('uses one PATCH and adopts the returned version', () => {
    const actualizarDescripcion = vi.fn().mockReturnValue(of({status: 'success', data: {descripcionPersonal: 'Mi descripción', version: 3}}));
    const obtenerMiPerfil = vi.fn();
    const page = pageWith({actualizarDescripcion, obtenerMiPerfil});
    page.guardarDescripcion();
    expect(actualizarDescripcion).toHaveBeenCalledWith('Mi descripción', 2);
    expect(obtenerMiPerfil).not.toHaveBeenCalled();
    expect(page.perfilVersion).toBe(3);
    expect(page.guardandoPerfil).toBe(false);
  });
  it('preserves the draft when another session changed the profile', () => {
    const page = pageWith({actualizarDescripcion: vi.fn().mockReturnValue(throwError(() => ({status: 409, error: {message: 'Recarga y revisa el perfil'}})))});
    page.guardarDescripcion();
    expect(page.descripcionPersonal).toBe('Mi descripción');
    expect(page.perfilVersion).toBe(2);
    expect(page.mensajeError).toContain('Recarga');
    expect(page.guardandoPerfil).toBe(false);
  });
});
