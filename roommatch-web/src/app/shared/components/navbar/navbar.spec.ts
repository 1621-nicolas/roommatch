import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Navbar } from './navbar';
import { AuthService } from '../../../core/services/auth.service';
import { PropietarioContextService } from '../../../core/services/propietario-context.service';
import { PropietarioService } from '../../../core/services/propietario.service';

describe('Navigation disclosure and recovery', () => {
  function setup(role = 'USUARIO') {
    const obtenerMiPerfil = vi.fn().mockReturnValue(of({status: 'success', data: null}));
    const logout = vi.fn();
    TestBed.configureTestingModule({imports: [Navbar], providers: [provideRouter([]), PropietarioContextService,
      {provide: PropietarioService, useValue: {obtenerMiPerfil}},
      {provide: AuthService, useValue: {estaAutenticado: () => true, getUsuario: () => ({nombres: 'Ana', apellidos: 'Prueba', email: 'ana@example.test'}), getRol: () => role, cerrarSesion: logout}}]});
    const fixture = TestBed.createComponent(Navbar);
    return {fixture, obtenerMiPerfil};
  }
  it('opens the account disclosure and returns focus to its trigger on Escape', () => {
    const {fixture} = setup(); fixture.detectChanges();
    const trigger = fixture.nativeElement.querySelector('.account-toggle') as HTMLButtonElement;
    trigger.click(); fixture.detectChanges(); expect(trigger.getAttribute('aria-expanded')).toBe('true');
    const link = fixture.nativeElement.querySelector('a[href="/contactos"]') as HTMLAnchorElement;
    link.focus(); link.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape', bubbles: true})); fixture.detectChanges();
    expect(trigger.getAttribute('aria-expanded')).toBe('false'); expect(document.activeElement).toBe(trigger);
    expect(fixture.nativeElement.querySelector('#account-links')).toBeNull();
  });
  it('keeps mobile and account disclosures mutually exclusive', () => {
    const {fixture} = setup(); fixture.detectChanges();
    const mobile = fixture.nativeElement.querySelector('.mobile-toggle') as HTMLButtonElement;
    mobile.click(); fixture.detectChanges(); expect(mobile.getAttribute('aria-expanded')).toBe('true');
    (fixture.nativeElement.querySelector('.account-toggle') as HTMLButtonElement).click(); fixture.detectChanges();
    expect(mobile.getAttribute('aria-expanded')).toBe('false');
    document.body.click(); fixture.detectChanges(); expect(fixture.nativeElement.querySelector('#account-links')).toBeNull();
  });
  it('shows an owner lookup error and retries instead of suggesting registration', () => {
    const {fixture, obtenerMiPerfil} = setup();
    obtenerMiPerfil.mockReturnValue(throwError(() => ({status: 503}))); fixture.detectChanges();
    (fixture.nativeElement.querySelector('.account-toggle') as HTMLButtonElement).click(); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('a[href="/propietario/registro"]')).toBeNull();
    obtenerMiPerfil.mockReturnValue(of({status: 'success', data: {idPropietario: 1}}));
    const retry = [...fixture.nativeElement.querySelectorAll('button')].find((b: any) => b.textContent.includes('Reintentar')) as HTMLButtonElement;
    retry.click(); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('a[href="/propietario/habitaciones"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeNull();
  });
  it('retains administration and all personal destinations in the account menu', () => {
    const {fixture} = setup('ADMIN'); fixture.detectChanges();
    (fixture.nativeElement.querySelector('.account-toggle') as HTMLButtonElement).click(); fixture.detectChanges();
    for (const route of ['/admin/dashboard', '/mi-cuenta', '/perfil', '/favoritos', '/contactos', '/notificaciones', '/mis-publicaciones']) {
      expect(fixture.nativeElement.querySelector(`a[href="${route}"]`)).not.toBeNull();
    }
  });
});
