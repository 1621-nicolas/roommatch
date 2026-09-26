import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, Subscription, finalize, map, of, catchError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { HomeService } from '../../core/services/home.service';
import { MatchService } from '../../core/services/match.service';
import { SolicitudService } from '../../core/services/solicitud.service';
import { PerfilService } from '../../core/services/perfil.service';
import { ContactoService } from '../../core/services/contacto.service';
import { HabitacionResponse } from '../../core/models/habitacion-response';
import { PublicacionRoomieResponse } from '../../core/models/publicacion-roomie-response';
import { MatchResponse } from '../../core/models/match-response';
import { SolicitudContactoResponse } from '../../core/models/solicitud-contacto-response';
import { ApiResponse } from '../../core/models/api-response';
import { requirePage } from '../../core/validation/api-page';

type Section = 'habitaciones' | 'publicaciones' | 'matches' | 'solicitudes' | 'perfil' | 'contacto';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit, OnDestroy {
  habitaciones: HabitacionResponse[] = [];
  publicaciones: PublicacionRoomieResponse[] = [];
  mejoresMatches: MatchResponse[] = [];
  solicitudesRecibidas: SolicitudContactoResponse[] = [];
  perfilCreado = false;
  contactoConfigurado = false;
  readonly loading: Partial<Record<Section, boolean>> = {};
  readonly errors: Partial<Record<Section, string>> = {};
  private readonly requests = new Map<Section, Subscription>();
  private readonly changeDetector = inject(ChangeDetectorRef);

  constructor(public authService: AuthService, private homeService: HomeService,
    private matchService: MatchService, private solicitudService: SolicitudService,
    private perfilService: PerfilService, private contactoService: ContactoService) {}

  ngOnInit(): void {
    this.cargarHabitaciones();
    this.cargarPublicaciones();
    if (this.authService.estaAutenticado()) this.cargarResumen();
  }

  ngOnDestroy(): void { this.requests.forEach(request => request.unsubscribe()); }

  private load<T>(section: Section, source: Observable<T>, assign: (value: T) => void): void {
    this.requests.get(section)?.unsubscribe();
    this.loading[section] = true;
    this.errors[section] = '';
    this.requests.set(section, source.pipe(finalize(() => {
      this.loading[section] = false;
      this.changeDetector.markForCheck();
    })).subscribe({
      next: value => { assign(value); this.changeDetector.markForCheck(); },
      error: () => this.errors[section] = 'No pudimos cargar esta información. Vuelve a intentarlo.'
    }));
  }

  cargarHabitaciones(): void {
    this.load('habitaciones', this.homeService.listarHabitacionesDestacadas().pipe(
      map(response => requirePage<HabitacionResponse>(response).content)), data => this.habitaciones = data);
  }

  cargarPublicaciones(): void {
    this.load('publicaciones', this.homeService.listarPublicacionesRoomie().pipe(
      map(response => requirePage<PublicacionRoomieResponse>(response).content)), data => this.publicaciones = data);
  }

  cargarMatches(): void {
    this.load('matches', this.matchService.listarMatches(null, 0, 3).pipe(
      map(response => requirePage<MatchResponse>(response).content.slice(0, 3))), data => this.mejoresMatches = data);
  }

  cargarResumen(): void {
    this.cargarMatches();
    this.load('solicitudes', this.solicitudService.listarRecibidas().pipe(map(response => {
      if (response?.status !== 'success' || !Array.isArray(response.data)) throw new Error('Formato inesperado');
      return response.data;
    })), data => this.solicitudesRecibidas = data);
    this.load('perfil', this.exists(this.perfilService.obtenerMiPerfil()), exists => this.perfilCreado = exists);
    this.load('contacto', this.exists(this.contactoService.obtenerMiContacto(), true), exists => this.contactoConfigurado = exists);
  }

  /** The contact endpoint also explicitly supports a successful null response. */
  private exists(source: Observable<ApiResponse<unknown>>, allowNull = false): Observable<boolean> {
    return source.pipe(map(response => {
      if (allowNull && response?.status === 'success' && response.data === null) return false;
      if (response?.status !== 'success' || !response.data) throw new Error('Formato inesperado');
      return true;
    }), catchError(error => {
      if (error?.status === 404) return of(false);
      throw error;
    }));
  }

  obtenerPrimerNombre(): string { return this.authService.getUsuario()?.nombres?.trim().split(/\s+/)[0] ?? ''; }
  obtenerNombreMatch(match: MatchResponse): string { return `${match.nombres} ${match.apellidos}`.trim(); }
  obtenerInicialesMatch(match: MatchResponse): string { return `${match.nombres?.charAt(0) ?? ''}${match.apellidos?.charAt(0) ?? ''}`.toUpperCase(); }
  obtenerSolicitudesPendientes(): number { return this.solicitudesRecibidas.filter(row => row.estado === 'pendiente').length; }
  obtenerTipoPublicacion(tipo: string): string {
    return ({busco_roomie: 'Busco roomie', busco_cuarto: 'Busco cuarto', busco_compartir: 'Busco compartir'} as Record<string, string>)[tipo] ?? tipo;
  }
}
