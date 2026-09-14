import { of, throwError } from 'rxjs';
import { MiCuenta } from './mi-cuenta';
import { PerfilService } from '../../core/services/perfil.service';

describe('MiCuenta description editing', () => {
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
