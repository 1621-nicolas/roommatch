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
  PlanPropietarioService
} from '../../../core/services/plan-propietario.service';

import {
  HabitacionService
} from '../../../core/services/habitacion.service';

import {
  PlanPropietarioResponse
} from '../../../core/models/plan-propietario-response';

import {
  MiPlanResponse
} from '../../../core/models/mi-plan-response';

import {
  HabitacionResponse
} from '../../../core/models/habitacion-response';


@Component({
  selector: 'app-planes',

  imports: [
    RouterLink
  ],

  templateUrl: './propietario-planes.html',

  styleUrl: './propietario-planes.css'
})
export class Planes implements OnInit {

  planes:
    PlanPropietarioResponse[] = [];


  miPlan:
    MiPlanResponse | null = null;


  habitaciones:
    HabitacionResponse[] = [];


  cargando = true;


  idPlanProcesando:
    number | null = null;


  planSeleccionado:
    PlanPropietarioResponse | null = null;


  mensajeError = '';

  mensajeExito = '';


  constructor(

    private planPropietarioService:
      PlanPropietarioService,

    private habitacionService:
      HabitacionService

  ) {}


  ngOnInit(): void {

    this.cargarDatos();
  }


  /*
   * =========================================================
   * CARGAR DATOS
   * =========================================================
   */

  cargarDatos(): void {

    this.cargando = true;

    this.mensajeError = '';


    forkJoin({

      planes:
        this.planPropietarioService
          .listarPlanes(),

      miPlan:
        this.planPropietarioService
          .obtenerMiPlan(),

      habitaciones:
        this.habitacionService
          .listarMisHabitaciones(
            0,
            100
          )

    })
      .subscribe({

        next: response => {

          this.planes =

            response.planes.status === 'success' &&
            Array.isArray(response.planes.data)

              ? response.planes.data

              : [];


          this.miPlan =

            response.miPlan.status === 'success'

              ? response.miPlan.data

              : null;


          this.habitaciones =

            response.habitaciones.status === 'success' &&
            response.habitaciones.data

              ? response.habitaciones.data.content ?? []

              : [];


          this.cargando = false;
        },


        error: error => {

          this.cargando = false;


          this.mensajeError =

            error.error?.message ||

            'No se pudo cargar la información de tus planes';


          console.error(

            '[RoomMatch planes] Error:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * SOLICITAR CAMBIO
   * =========================================================
   */

  solicitarCambioPlan(
    plan: PlanPropietarioResponse
  ): void {

    this.limpiarMensajes();


    if (
      this.esPlanActual(plan)
    ) {

      return;
    }


    this.planSeleccionado =
      plan;
  }


  /*
   * =========================================================
   * CANCELAR CAMBIO
   * =========================================================
   */

  cancelarCambioPlan(): void {

    this.planSeleccionado =
      null;
  }


  /*
   * =========================================================
   * CONFIRMAR CAMBIO
   * =========================================================
   */

  confirmarCambioPlan(): void {

    if (
      !this.planSeleccionado
    ) {

      return;
    }


    const plan =
      this.planSeleccionado;


    this.idPlanProcesando =
      plan.idPlan;


    this.planSeleccionado =
      null;


    this.planPropietarioService
      .cambiarPlan(
        plan.idPlan
      )
      .subscribe({

        next: response => {

          this.idPlanProcesando = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo actualizar el plan';


            return;
          }


          this.miPlan =
            response.data;


          this.mensajeExito =

            `Tu plan cambió correctamente a ${plan.nombrePlan}.`;


          this.cargarDatos();
        },


        error: error => {

          this.idPlanProcesando = null;


          this.mensajeError =

            error.error?.message ||

            'No se pudo actualizar el plan';


          console.error(

            '[RoomMatch planes] Error cambio:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * PLAN ACTUAL
   * =========================================================
   */

  esPlanActual(
    plan: PlanPropietarioResponse
  ): boolean {

    return (

      this.miPlan?.idPlan ===
      plan.idPlan

    );
  }


  /*
   * =========================================================
   * HABITACIONES ACTIVAS
   * =========================================================
   */

  contarHabitacionesActivas(): number {

    return this.habitaciones.filter(

      habitacion =>

        habitacion.estado
          .toLowerCase() === 'activa'

    ).length;
  }


  /*
   * =========================================================
   * PORCENTAJE USO
   * =========================================================
   */

  obtenerPorcentajeUso(): number {

    const limite =

      this.miPlan
        ?.limiteHabitaciones ?? 0;


    if (
      limite <= 0
    ) {

      return 0;
    }


    return Math.min(

      100,

      Math.round(

        (
          this.contarHabitacionesActivas() /
          limite
        ) * 100

      )

    );
  }


  /*
   * =========================================================
   * PRECIO
   * =========================================================
   */

  formatearPrecio(
    precio: number
  ): string {

    return new Intl
      .NumberFormat(
        'es-PE',
        {
          minimumFractionDigits: 0,
          maximumFractionDigits: 2
        }
      )
      .format(
        precio
      );
  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */

  formatearFecha(
    fecha: string | null
  ): string {

    if (!fecha) {

      return 'Sin fecha';
    }


    return new Date(
      fecha
    )
      .toLocaleDateString(
        'es-PE',
        {
          day: '2-digit',
          month: 'short',
          year: 'numeric'
        }
      );
  }


  /*
   * =========================================================
   * TEXTO BOTÓN
   * =========================================================
   */

  obtenerTextoBoton(
    plan: PlanPropietarioResponse
  ): string {

    if (
      this.esPlanActual(plan)
    ) {

      return 'Plan actual';
    }


    if (
      plan.precioMensual >
      (this.miPlan?.precioMensual ?? 0)
    ) {

      return `Mejorar a ${plan.nombrePlan}`;
    }


    return `Cambiar a ${plan.nombrePlan}`;
  }


  /*
   * =========================================================
   * PLAN PRO
   * =========================================================
   */

  esPlanPro(
    plan: PlanPropietarioResponse
  ): boolean {

    return (

      plan.nombrePlan
        .trim()
        .toLowerCase() === 'pro'

    );
  }


  /*
   * =========================================================
   * CONFIRMACIÓN
   * =========================================================
   */

  obtenerTituloConfirmacion(): string {

    return (

      `Cambiar a ${

        this.planSeleccionado
          ?.nombrePlan ?? ''

      }`

    );
  }


  obtenerTextoConfirmacion(): string {

    if (
      !this.planSeleccionado
    ) {

      return '';
    }


    return (

      `Tu suscripción actual al plan ${

        this.miPlan?.nombrePlan ?? ''

      } será finalizada y se activará el plan ${

        this.planSeleccionado.nombrePlan

      }.`

    );
  }


  /*
   * =========================================================
   * MENSAJES
   * =========================================================
   */

  private limpiarMensajes(): void {

    this.mensajeError = '';

    this.mensajeExito = '';
  }
}