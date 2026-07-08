import {
  Component,
  OnInit
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  AuthService
} from '../../../core/services/auth.service';

import {
  PropietarioService
} from '../../../core/services/propietario.service';

import {
  HabitacionService
} from '../../../core/services/habitacion.service';

import {
  LeadHabitacionService
} from '../../../core/services/lead-habitacion.service';

import {
  PropietarioResponse
} from '../../../core/models/propietario-response';

import {
  HabitacionResponse
} from '../../../core/models/habitacion-response';

import {
  LeadHabitacionResponse
} from '../../../core/models/lead-habitacion-response';


@Component({
  selector: 'app-propietario-panel',

  imports: [
    RouterLink
  ],

  templateUrl: './propietario-panel.html',

  styleUrl: './propietario-panel.css'
})
export class PropietarioPanel implements OnInit {

  propietario:
    PropietarioResponse | null = null;


  habitaciones:
    HabitacionResponse[] = [];


  leads:
    LeadHabitacionResponse[] = [];


  cargando = true;

  mensajeError = '';


  constructor(

    public authService: AuthService,

    private propietarioService:
      PropietarioService,

    private habitacionService:
      HabitacionService,

    private leadHabitacionService:
      LeadHabitacionService

  ) {}


  ngOnInit(): void {

    this.cargarDashboard();
  }


  /*
   * =========================================================
   * CARGAR DASHBOARD
   * =========================================================
   */

  cargarDashboard(): void {

    this.cargando = true;

    this.mensajeError = '';


    this.propietarioService
      .obtenerMiPerfil()
      .subscribe({

        next: response => {

          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.propietario = null;

            this.cargando = false;

            return;
          }


          this.propietario =
            response.data;


          this.cargarInformacionPropietario();
        },


        error: error => {

          this.cargando = false;


          this.mensajeError =

            error.error?.message ||

            'No se pudo cargar el perfil de propietario';
        }

      });
  }


  /*
   * =========================================================
   * INFORMACIÓN COMERCIAL
   * =========================================================
   */

  private cargarInformacionPropietario(): void {

    forkJoin({

      habitaciones:
        this.habitacionService
          .listarMisHabitaciones(),

      leads:
        this.leadHabitacionService
          .listarLeadsPropietario(
            null,
            0,
            100
          )

    })
      .subscribe({

        next: response => {

          /*
           * =================================================
           * HABITACIONES
           * =================================================
           */

          if (
            response.habitaciones.status === 'success' &&
            response.habitaciones.data
          ) {

            this.habitaciones =
              response.habitaciones.data.content ?? [];

          } else {

            this.habitaciones = [];
          }


          /*
           * =================================================
           * LEADS
           * =================================================
           */

          if (
            response.leads.status === 'success' &&
            response.leads.data
          ) {

            this.leads =
              response.leads.data.content ?? [];

          } else {

            this.leads = [];
          }


          console.log(
            '[RoomMatch propietario] Habitaciones:',
            this.habitaciones
          );


          console.log(
            '[RoomMatch propietario] Leads:',
            this.leads
          );


          this.cargando = false;
        },


        error: error => {

          this.cargando = false;


          this.habitaciones = [];

          this.leads = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudo cargar el panel de propietario';


          console.error(
            '[RoomMatch propietario] Error dashboard:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * HABITACIONES ACTIVAS
   * =========================================================
   */

  contarHabitacionesActivas(): number {

    if (
      !Array.isArray(
        this.habitaciones
      )
    ) {

      return 0;
    }


    return this.habitaciones
      .filter(

        habitacion =>

          habitacion.estado
            .toLowerCase() === 'activa'

      )
      .length;
  }


  /*
   * =========================================================
   * HABITACIONES PAUSADAS
   * =========================================================
   */

  contarHabitacionesPausadas(): number {

    if (
      !Array.isArray(
        this.habitaciones
      )
    ) {

      return 0;
    }


    return this.habitaciones
      .filter(

        habitacion =>

          habitacion.estado
            .toLowerCase() === 'pausada'

      )
      .length;
  }


  /*
   * =========================================================
   * LEADS PENDIENTES
   * =========================================================
   */

  contarLeadsPendientes(): number {

    if (
      !Array.isArray(
        this.leads
      )
    ) {

      return 0;
    }


    return this.leads
      .filter(

        lead =>

          lead.estado
            .toLowerCase() === 'pendiente'

      )
      .length;
  }


  /*
   * =========================================================
   * LEADS CONTACTADOS
   * =========================================================
   */

  contarLeadsContactados(): number {

    if (
      !Array.isArray(
        this.leads
      )
    ) {

      return 0;
    }


    return this.leads
      .filter(

        lead =>

          lead.estado
            .toLowerCase() === 'contactado'

      )
      .length;
  }


  /*
   * =========================================================
   * HABITACIONES DESTACADAS
   * =========================================================
   */

  contarDestacadas(): number {

    if (
      !Array.isArray(
        this.habitaciones
      )
    ) {

      return 0;
    }


    return this.habitaciones
      .filter(

        habitacion =>

          habitacion.destacada === true

      )
      .length;
  }


  /*
   * =========================================================
   * PORCENTAJE DE USO DEL PLAN
   * =========================================================
   */

  obtenerPorcentajeUsoPlan(): number {

    const limite =

      this.propietario
        ?.limiteHabitaciones ?? 0;


    if (limite <= 0) {

      return 0;
    }


    const utilizadas =

      this.contarHabitacionesActivas();


    return Math.min(

      100,

      Math.round(

        (
          utilizadas /
          limite
        ) * 100

      )

    );
  }


  /*
   * =========================================================
   * VALIDAR LÍMITE DEL PLAN
   * =========================================================
   */

  alcanzoLimitePlan(): boolean {

    const limite =

      this.propietario
        ?.limiteHabitaciones ?? 0;


    if (limite <= 0) {

      return false;
    }


    return (

      this.contarHabitacionesActivas() >=
      limite

    );
  }


  /*
   * =========================================================
   * OBTENER PRIMER NOMBRE
   * =========================================================
   */

  obtenerPrimerNombre(): string {

    const usuario =

      this.authService
        .getUsuario();


    if (
      !usuario ||
      !usuario.nombres
    ) {

      return 'Propietario';
    }


    return usuario.nombres
      .trim()
      .split(/\s+/)[0];
  }


  /*
   * =========================================================
   * FORMATEAR FECHA
   * =========================================================
   */

  formatearFecha(
    fecha: string | null
  ): string {

    if (!fecha) {

      return '';
    }


    return new Date(fecha)
      .toLocaleString(

        'es-PE',

        {

          day: '2-digit',

          month: 'short',

          hour: '2-digit',

          minute: '2-digit'

        }

      );
  }
}