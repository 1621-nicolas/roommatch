import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  finalize
} from 'rxjs/operators';

import {
  HabitacionService
} from '../../core/services/habitacion.service';

import {
  LeadHabitacionService
} from '../../core/services/lead-habitacion.service';

import {
  AuthService
} from '../../core/services/auth.service';

import {
  HabitacionResponse
} from '../../core/models/habitacion-response';

import {
  LeadHabitacionRequest
} from '../../core/models/lead-habitacion-request';


@Component({
  selector: 'app-habitacion-detalle',

  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],

  templateUrl: './habitacion-detalle.html',

  styleUrl: './habitacion-detalle.css'
})
export class HabitacionDetalle implements OnInit {

  /*
   * =========================================================
   * HABITACIÓN
   * =========================================================
   */
  habitacion: HabitacionResponse | null = null;


  /*
   * =========================================================
   * LEAD
   * =========================================================
   */
  mensajeInteres = '';


  /*
   * =========================================================
   * ESTADOS
   * =========================================================
   */
  cargando = true;

  enviandoInteres = false;

  interesEnviado = false;


  mensajeError = '';

  mensajeExito = '';


  /*
   * =========================================================
   * ID
   * =========================================================
   */
  idHabitacion = 0;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private habitacionService: HabitacionService,
    private leadHabitacionService: LeadHabitacionService,
    private authService: AuthService
  ) {}


  ngOnInit(): void {

    this.obtenerIdHabitacion();

  }


  /*
   * =========================================================
   * OBTENER ID DESDE LA URL
   * =========================================================
   */
  private obtenerIdHabitacion(): void {

    const idParametro =
      this.route.snapshot.paramMap.get(
        'idHabitacion'
      );


    if (!idParametro) {

      this.mensajeError =
        'No se encontró la habitación solicitada';

      this.cargando = false;

      return;

    }


    const idHabitacion =
      Number(idParametro);


    if (
      Number.isNaN(idHabitacion) ||
      idHabitacion <= 0
    ) {

      this.mensajeError =
        'El identificador de la habitación no es válido';

      this.cargando = false;

      return;

    }


    this.idHabitacion =
      idHabitacion;


    this.cargarHabitacion();

  }


  /*
   * =========================================================
   * CARGAR HABITACIÓN
   * =========================================================
   */
  cargarHabitacion(): void {

    this.cargando = true;

    this.mensajeError = '';


    this.habitacionService
      .obtenerPorId(
        this.idHabitacion
      )
      .pipe(

        finalize(() => {

          this.cargando = false;

        })

      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.habitacion = null;


            this.mensajeError =
              response.message ||
              'No se pudo obtener la habitación';

            return;

          }


          this.habitacion =
            response.data;

        },


        error: error => {

          console.error(
            'Error cargando habitación:',
            error
          );


          this.habitacion = null;


          this.mensajeError =

            error.error?.message ||

            'No se pudo cargar la habitación';

        }

      });

  }


  /*
   * =========================================================
   * ENVIAR INTERÉS
   * =========================================================
   */
  enviarInteres(): void {

    this.mensajeError = '';

    this.mensajeExito = '';


    /*
     * Usuario no autenticado.
     */
    if (
      !this.authService.estaAutenticado()
    ) {

      this.router.navigate(
        ['/login']
      );

      return;

    }


    /*
     * Validar mensaje.
     */
    const mensaje =
      this.mensajeInteres.trim();


    if (!mensaje) {

      this.mensajeError =
        'Escribe un mensaje para el propietario';

      return;

    }


    if (
      mensaje.length > 500
    ) {

      this.mensajeError =
        'El mensaje no puede superar los 500 caracteres';

      return;

    }


    if (
      this.enviandoInteres ||
      this.interesEnviado
    ) {

      return;

    }


    this.enviandoInteres = true;


    const request: LeadHabitacionRequest = {

      mensaje

    };


    this.leadHabitacionService
      .crearLead(
        this.idHabitacion,
        request
      )
      .pipe(

        finalize(() => {

          this.enviandoInteres = false;

        })

      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo enviar tu interés';

            return;

          }


          this.interesEnviado = true;


          this.mensajeExito =

            'Tu mensaje fue enviado al propietario correctamente';

        },


        error: error => {

          console.error(
            'Error enviando interés:',
            error
          );


          this.mensajeError =

            error.error?.message ||

            'No se pudo enviar tu interés';

        }

      });

  }


  /*
   * =========================================================
   * PRECIO
   * =========================================================
   */
  formatearPrecio(
    precio: number
  ): string {

    return new Intl.NumberFormat(
      'es-PE',
      {

        style: 'currency',

        currency: 'PEN',

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

      return 'Disponibilidad inmediata';

    }


    return new Date(
      `${fecha}T00:00:00`
    )
    .toLocaleDateString(
      'es-PE',
      {

        day: '2-digit',

        month: 'long',

        year: 'numeric'

      }
    );

  }


  /*
   * =========================================================
   * CONTADOR MENSAJE
   * =========================================================
   */
  contarCaracteres(): number {

    return this.mensajeInteres.length;

  }

}