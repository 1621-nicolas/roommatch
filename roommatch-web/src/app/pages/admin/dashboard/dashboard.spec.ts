import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { Dashboard } from './dashboard';
import { AdminService } from '../../../core/services/admin.service';
import { AdminDashboard } from '../../../core/models/admin';

const empty: AdminDashboard={"totalUsuarios":0,"usuariosActivos":0,"usuariosSuspendidos":0,"totalPropietarios":0,"totalPerfilesConvivencia":0,"totalHabitaciones":0,"habitacionesActivas":0,"habitacionesPausadas":0,"totalPublicacionesRoomie":0,"publicacionesActivas":0,"totalMatches":0,"solicitudesPendientes":0,"leadsPendientes":0,"reportesUsuariosPendientes":0,"reportesHabitacionesPendientes":0,"notificacionesNoLeidas":0};
describe('Administrative dashboard states', () => {
  function create(dashboard: ReturnType<typeof vi.fn>) {
    TestBed.configureTestingModule({imports:[Dashboard],providers:[provideRouter([]),{provide:AdminService,useValue:{dashboard}}]});
    const fixture=TestBed.createComponent(Dashboard);fixture.detectChanges();return fixture;
  }
  it('renders loading before a response and an honest empty review queue', () => {
    const pending=new Subject<AdminDashboard>();const fixture=create(vi.fn().mockReturnValue(pending));
    expect(fixture.nativeElement.textContent).toContain('Consultando');
    pending.next(empty);pending.complete();fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No hay reportes nuevos pendientes');
    expect(fixture.nativeElement.textContent).toContain('Matches almacenados históricos');
  });
  it('renders an API error and recovers through the retry control', () => {
    const dashboard=vi.fn().mockReturnValueOnce(throwError(()=>new Error('Servidor no disponible'))).mockReturnValue(of({...empty,totalUsuarios:12}));
    const fixture=create(dashboard);
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Servidor no disponible');
    const buttons=Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
    buttons.find(button=>button.textContent?.includes('Reintentar'))!.click();fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('12');
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeNull();
  });
});
