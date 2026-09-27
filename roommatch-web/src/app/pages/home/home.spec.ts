import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { Home } from './home';
import { AuthService } from '../../core/services/auth.service';
import { HomeService } from '../../core/services/home.service';
import { MatchService } from '../../core/services/match.service';
import { SolicitudService } from '../../core/services/solicitud.service';
import { PerfilService } from '../../core/services/perfil.service';
import { ContactoService } from '../../core/services/contacto.service';

const empty = {status: 'success', data: {content: [], totalElements: 0, totalPages: 0, number: 0, size: 3}};
describe('Home real data and recovery', () => {
  function setup(authenticated = false) {
    const rooms = vi.fn().mockReturnValue(of(empty));
    const publications = vi.fn().mockReturnValue(of(empty));
    const matches = vi.fn().mockReturnValue(of(empty));
    const requests = vi.fn().mockReturnValue(of({status: 'success', data: []}));
    const profile = vi.fn().mockReturnValue(throwError(() => ({status: 404})));
    const contact = vi.fn().mockReturnValue(of({status: 'success', data: null}));
    TestBed.configureTestingModule({imports: [Home], providers: [provideRouter([]),
      {provide: AuthService, useValue: {estaAutenticado: () => authenticated, getUsuario: () => ({nombres: 'Ana'})}},
      {provide: HomeService, useValue: {listarHabitacionesDestacadas: rooms, listarPublicacionesRoomie: publications}},
      {provide: MatchService, useValue: {listarMatches: matches}},
      {provide: SolicitudService, useValue: {listarRecibidas: requests}},
      {provide: PerfilService, useValue: {obtenerMiPerfil: profile}},
      {provide: ContactoService, useValue: {obtenerMiContacto: contact}}]});
    const fixture = TestBed.createComponent(Home);
    return {fixture, rooms, publications, matches, requests, profile, contact};
  }
  it('shows loading exclusively, then empty results without invented listings or profiles', () => {
    const {fixture, rooms, matches, profile} = setup();
    const pending = new Subject<typeof empty>(); rooms.mockReturnValue(pending);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Cargando habitaciones');
    expect(fixture.nativeElement.textContent).not.toContain('No hay habitaciones disponibles');
    pending.next(empty); pending.complete(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No hay habitaciones disponibles');
    expect(fixture.nativeElement.textContent).not.toMatch(/Camila|Diego|1,250|95%|89%/);
    expect(matches).not.toHaveBeenCalled(); expect(profile).not.toHaveBeenCalled();
  });
  it('does not turn malformed arrays into empty success and retries through the visible button', () => {
    const {fixture, rooms} = setup();
    rooms.mockReturnValueOnce(of({...empty, data: {...empty.data, content: {value: []}}})).mockReturnValue(of(empty));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('No hay habitaciones disponibles');
    const buttons = [...fixture.nativeElement.querySelectorAll('button')] as HTMLButtonElement[];
    buttons.find(button => button.textContent?.includes('Reintentar habitaciones'))!.click(); fixture.detectChanges();
    expect(rooms).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('No hay habitaciones disponibles');
  });
  it('honors profile 404 and optional contact null without claiming server failure', () => {
    const {fixture} = setup(true); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Crear perfil');
    expect(fixture.nativeElement.textContent).toContain('Configurar contacto');
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeNull();
  });
  it('keeps errors visible instead of reporting a missing profile or zero requests', () => {
    const {fixture, profile, requests, contact, matches} = setup(true);
    profile.mockReturnValue(throwError(() => ({status: 503})));
    contact.mockReturnValue(throwError(() => ({status: 503})));
    requests.mockReturnValue(of({status: 'success', data: {value: []}}));
    matches.mockReturnValue(throwError(() => ({status: 503})));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No disponible');
    expect(fixture.nativeElement.textContent).toContain('Parte del resumen no está disponible');
    expect(fixture.nativeElement.textContent).not.toContain('Crear perfil');
    expect(fixture.nativeElement.textContent).not.toContain('Aún no hay recomendaciones');
  });
  it('cancels superseded requests and all remaining requests on navigation away', () => {
    const {fixture, rooms, publications} = setup();
    const old = new Subject<typeof empty>(), next = new Subject<typeof empty>(), pubs = new Subject<typeof empty>();
    rooms.mockReturnValueOnce(old).mockReturnValueOnce(next); publications.mockReturnValue(pubs);
    fixture.detectChanges(); fixture.componentInstance.cargarHabitaciones();
    expect(old.observed).toBe(false); expect(next.observed).toBe(true); expect(pubs.observed).toBe(true);
    fixture.destroy(); expect(next.observed).toBe(false); expect(pubs.observed).toBe(false);
  });
});
