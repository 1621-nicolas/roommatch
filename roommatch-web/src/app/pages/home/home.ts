import {
  Component,
  OnInit
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

import {
  AuthService
} from '../../core/services/auth.service';

import {
  HomeService
} from '../../core/services/home.service';

import {
  MatchService
} from '../../core/services/match.service';

import {
  SolicitudService
} from '../../core/services/solicitud.service';

import {
  PerfilService
} from '../../core/services/perfil.service';

import {
  ContactoService
} from '../../core/services/contacto.service';

import {
  HabitacionResponse
} from '../../core/models/habitacion-response';

import {
  PublicacionRoomieResponse
} from '../../core/models/publicacion-roomie-response';

import {
  MatchResponse
} from '../../core/models/match-response';

import {
  SolicitudContactoResponse
} from '../../core/models/solicitud-contacto-response';


@Component({
  selector: 'app-home',

  imports: [
    RouterLink
  ],

  templateUrl: './home.html'
})
export class Home implements OnInit {

  habitaciones: HabitacionResponse[] = [];

  publicaciones: PublicacionRoomieResponse[] = [];

  mejoresMatches: MatchResponse[] = [];

  solicitudesRecibidas: SolicitudContactoResponse[] = [];


  cargandoHabitaciones = false;

  cargandoPublicaciones = false;

  cargandoDashboard = false;


  errorHabitaciones = '';

  errorPublicaciones = '';

  mensajeDashboard = '';


  perfilCompleto = false;

  contactoConfigurado = false;


  constructor(
    public authService: AuthService,
    private homeService: HomeService,
    private matchService: MatchService,
    private solicitudService: SolicitudService,
    private perfilService: PerfilService,
    private contactoService: ContactoService
  ) {}


  ngOnInit(): void {

    if (
      this.authService.estaAutenticado()
    ) {

      this.cargarHomeAutenticado();

      return;
    }

    this.cargarHomePublico();
  }


  cargarHomePublico(): void {

    this.cargarHabitaciones();

    this.cargarPublicaciones();
  }


  cargarHomeAutenticado(): void {

    this.cargandoDashboard = true;

    this.cargarHabitaciones();

    let peticionesCompletadas = 0;

    const comprobarCarga = (): void => {

      peticionesCompletadas++;

      if (
        peticionesCompletadas === 4
      ) {

        this.cargandoDashboard = false;
      }
    };


    /*
     * Cargamos los mejores matches.
     */
    this.matchService
      .listarMatches(
        null,
        0,
        3
      )
      .subscribe({

        next: response => {

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.mejoresMatches =
              response.data.content.slice(
                0,
                3
              );
          }

          comprobarCarga();
        },

        error: () => {

          this.mejoresMatches = [];

          comprobarCarga();
        }

      });


    /*
     * Consultamos solicitudes recibidas.
     */
    this.solicitudService
      .listarRecibidas()
      .subscribe({

        next: response => {

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.solicitudesRecibidas =
              response.data;
          }

          comprobarCarga();
        },

        error: () => {

          this.solicitudesRecibidas = [];

          comprobarCarga();
        }

      });


    /*
     * Verificamos el perfil de convivencia.
     */
    this.perfilService
      .obtenerMiPerfil()
      .subscribe({

        next: response => {

          this.perfilCompleto =
            response.status === 'success' &&
            !!response.data;

          comprobarCarga();
        },

        error: () => {

          this.perfilCompleto = false;

          comprobarCarga();
        }

      });


    /*
     * Verificamos si tiene contacto configurado.
     */
    this.contactoService
      .obtenerMiContacto()
      .subscribe({

        next: response => {

          this.contactoConfigurado =
            response.status === 'success' &&
            !!response.data;

          comprobarCarga();
        },

        error: () => {

          this.contactoConfigurado = false;

          comprobarCarga();
        }

      });
  }


  cargarHabitaciones(): void {

    this.cargandoHabitaciones = true;

    this.errorHabitaciones = '';

    this.homeService
      .listarHabitacionesDestacadas()
      .subscribe({

        next: response => {

          this.cargandoHabitaciones = false;

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.habitaciones =
              response.data.content;
          }

        },

        error: () => {

          this.cargandoHabitaciones = false;

          this.errorHabitaciones =
            'No se pudieron cargar las habitaciones.';
        }

      });
  }


  cargarPublicaciones(): void {

    this.cargandoPublicaciones = true;

    this.errorPublicaciones = '';

    this.homeService
      .listarPublicacionesRoomie()
      .subscribe({

        next: response => {

          this.cargandoPublicaciones = false;

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.publicaciones =
              response.data.content;
          }

        },

        error: () => {

          this.cargandoPublicaciones = false;

          this.errorPublicaciones =
            'No se pudieron cargar las publicaciones Roomie.';
        }

      });
  }


  obtenerPrimerNombre(): string {

    const usuario =
      this.authService.getUsuario();

    if (!usuario) {
      return '';
    }

    return usuario.nombres
      .trim()
      .split(/\s+/)[0];
  }


  obtenerInicialesMatch(
    match: MatchResponse
  ): string {

    const nombre =
      match.nombres?.charAt(0) ?? '';

    const apellido =
      match.apellidos?.charAt(0) ?? '';

    return (
      nombre + apellido
    ).toUpperCase();
  }


  obtenerNombreMatch(
    match: MatchResponse
  ): string {

    return (
      `${match.nombres} ${match.apellidos}`
    ).trim();
  }


  obtenerSolicitudesPendientes(): number {

    return this.solicitudesRecibidas
      .filter(
        solicitud =>
          solicitud.estado === 'pendiente'
      )
      .length;
  }


  obtenerNivelCompatibilidad(
    porcentaje: number
  ): string {

    if (porcentaje >= 90) {
      return 'Excelente match';
    }

    if (porcentaje >= 80) {
      return 'Alta compatibilidad';
    }

    if (porcentaje >= 70) {
      return 'Buen match';
    }

    if (porcentaje >= 50) {
      return 'Compatibilidad media';
    }

    return 'Compatibilidad baja';
  }


  obtenerTipoPublicacion(
    tipo: string
  ): string {

    if (
      tipo === 'busco_roomie'
    ) {

      return 'Busco roomie';
    }

    if (
      tipo === 'busco_cuarto'
    ) {

      return 'Busco cuarto';
    }

    if (
      tipo === 'busco_compartir'
    ) {

      return 'Busco compartir';
    }

    return tipo;
  }
}