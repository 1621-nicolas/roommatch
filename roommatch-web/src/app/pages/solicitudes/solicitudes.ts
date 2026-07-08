import {
  Component,
  OnInit
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

import {
  SolicitudService
} from '../../core/services/solicitud.service';

import {
  LeadHabitacionService
} from '../../core/services/lead-habitacion.service';

import {
  SolicitudContactoResponse
} from '../../core/models/solicitud-contacto-response';

import {
  LeadHabitacionResponse
} from '../../core/models/lead-habitacion-response';


@Component({
  selector: 'app-solicitudes',

  imports: [
    RouterLink
  ],

  templateUrl: './solicitudes.html'
})
export class Solicitudes implements OnInit {

  /*
   * =========================================================
   * SECCIÓN PRINCIPAL
   * =========================================================
   */

  seccionActiva:
    'contactos' | 'habitaciones' =
    'contactos';


  /*
   * =========================================================
   * SOLICITUDES DE CONTACTO
   * =========================================================
   */

  solicitudesRecibidas:
    SolicitudContactoResponse[] = [];

  solicitudesEnviadas:
    SolicitudContactoResponse[] = [];


  pestanaActiva:
    'recibidas' | 'enviadas' =
    'recibidas';


  /*
   * =========================================================
   * INTERESES EN HABITACIONES
   * =========================================================
   */

  interesesHabitaciones:
    LeadHabitacionResponse[] = [];


  /*
   * =========================================================
   * ESTADOS DE CARGA
   * =========================================================
   */

  cargando = true;

  cargandoIntereses = true;


  /*
   * =========================================================
   * MENSAJES
   * =========================================================
   */

  mensajeError = '';

  mensajeExito = '';


  /*
   * =========================================================
   * ACCIONES DE SOLICITUD
   * =========================================================
   */

  accionandoId: number | null = null;


  solicitudRechazar:
    SolicitudContactoResponse | null = null;


  constructor(
    private solicitudService: SolicitudService,
    private leadHabitacionService: LeadHabitacionService
  ) {}


  /*
   * =========================================================
   * INICIO
   * =========================================================
   */

  ngOnInit(): void {

    this.cargarSolicitudes();

    this.cargarInteresesHabitaciones();

  }


  /*
   * =========================================================
   * CAMBIAR SECCIÓN PRINCIPAL
   * =========================================================
   */

  cambiarSeccion(
    seccion: 'contactos' | 'habitaciones'
  ): void {

    this.seccionActiva = seccion;

    this.mensajeError = '';

    this.mensajeExito = '';

  }


  /*
   * =========================================================
   * CARGAR SOLICITUDES DE CONTACTO
   * =========================================================
   */

  cargarSolicitudes(): void {

    this.cargando = true;

    this.mensajeError = '';


    let completadas = 0;


    const comprobarCarga = (): void => {

      completadas++;


      if (completadas === 2) {

        this.cargando = false;

      }

    };


    /*
     * SOLICITUDES RECIBIDAS
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

          } else {

            this.solicitudesRecibidas = [];

          }


          comprobarCarga();

        },


        error: error => {

          console.error(
            'Error cargando solicitudes recibidas:',
            error
          );


          this.solicitudesRecibidas = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las solicitudes recibidas';


          comprobarCarga();

        }

      });


    /*
     * SOLICITUDES ENVIADAS
     */
    this.solicitudService
      .listarEnviadas()
      .subscribe({

        next: response => {

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.solicitudesEnviadas =
              response.data;

          } else {

            this.solicitudesEnviadas = [];

          }


          comprobarCarga();

        },


        error: error => {

          console.error(
            'Error cargando solicitudes enviadas:',
            error
          );


          this.solicitudesEnviadas = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las solicitudes enviadas';


          comprobarCarga();

        }

      });

  }


  /*
   * =========================================================
   * CARGAR INTERESES EN HABITACIONES
   * =========================================================
   */

  cargarInteresesHabitaciones(): void {

    this.cargandoIntereses = true;


    this.leadHabitacionService
      .listarMisIntereses()
      .subscribe({

        next: response => {

          this.cargandoIntereses = false;


          if (
            response.status === 'success' &&
            response.data
          ) {

            this.interesesHabitaciones =
              response.data;

            return;

          }


          this.interesesHabitaciones = [];


          this.mensajeError =

            response.message ||

            'No se pudieron cargar tus intereses en habitaciones';

        },


        error: error => {

          this.cargandoIntereses = false;


          console.error(
            'Error cargando intereses en habitaciones:',
            error
          );


          this.interesesHabitaciones = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar tus intereses en habitaciones';

        }

      });

  }


  /*
   * =========================================================
   * CAMBIAR PESTAÑA DE CONTACTOS
   * =========================================================
   */

  cambiarPestana(
    pestana: 'recibidas' | 'enviadas'
  ): void {

    this.pestanaActiva = pestana;

    this.mensajeError = '';

    this.mensajeExito = '';

  }


  /*
   * =========================================================
   * ACEPTAR SOLICITUD
   * =========================================================
   */

  aceptarSolicitud(
    solicitud: SolicitudContactoResponse
  ): void {

    if (
      solicitud.estado !== 'pendiente'
    ) {

      return;

    }


    this.accionandoId =
      solicitud.idSolicitud;


    this.mensajeError = '';

    this.mensajeExito = '';


    this.solicitudService
      .aceptarSolicitud(
        solicitud.idSolicitud
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;


          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo aceptar la solicitud';

            return;

          }


          this.actualizarSolicitudRecibida(
            solicitud.idSolicitud,
            response.data
          );


          this.mensajeExito =

            `Aceptaste la solicitud de ${solicitud.nombreEmisor}. El contacto ya está desbloqueado.`;

        },


        error: error => {

          this.accionandoId = null;


          console.error(
            'Error aceptando solicitud:',
            error
          );


          this.mensajeError =

            error.error?.message ||

            'No se pudo aceptar la solicitud';

        }

      });

  }


  /*
   * =========================================================
   * ABRIR CONFIRMACIÓN DE RECHAZO
   * =========================================================
   */

  abrirRechazar(
    solicitud: SolicitudContactoResponse
  ): void {

    this.solicitudRechazar =
      solicitud;


    this.mensajeError = '';

    this.mensajeExito = '';

  }


  /*
   * =========================================================
   * CERRAR CONFIRMACIÓN DE RECHAZO
   * =========================================================
   */

  cerrarRechazar(): void {

    this.solicitudRechazar = null;

    this.accionandoId = null;

  }


  /*
   * =========================================================
   * CONFIRMAR RECHAZO
   * =========================================================
   */

  confirmarRechazo(): void {

    if (
      !this.solicitudRechazar
    ) {

      return;

    }


    const solicitud =
      this.solicitudRechazar;


    this.accionandoId =
      solicitud.idSolicitud;


    this.solicitudService
      .rechazarSolicitud(
        solicitud.idSolicitud
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;


          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo rechazar la solicitud';

            return;

          }


          this.actualizarSolicitudRecibida(
            solicitud.idSolicitud,
            response.data
          );


          this.mensajeExito =

            `La solicitud de ${solicitud.nombreEmisor} fue rechazada`;


          this.cerrarRechazar();

        },


        error: error => {

          this.accionandoId = null;


          console.error(
            'Error rechazando solicitud:',
            error
          );


          this.mensajeError =

            error.error?.message ||

            'No se pudo rechazar la solicitud';

        }

      });

  }


  /*
   * =========================================================
   * ACTUALIZAR SOLICITUD LOCALMENTE
   * =========================================================
   */

  private actualizarSolicitudRecibida(
    idSolicitud: number,
    solicitudActualizada:
      SolicitudContactoResponse
  ): void {

    this.solicitudesRecibidas =

      this.solicitudesRecibidas.map(

        solicitud => {

          if (
            solicitud.idSolicitud ===
            idSolicitud
          ) {

            return solicitudActualizada;

          }


          return solicitud;

        }

      );

  }


  /*
   * =========================================================
   * CONTADORES
   * =========================================================
   */

  contarPendientesRecibidas(): number {

    return this.solicitudesRecibidas
      .filter(
        solicitud =>
          solicitud.estado === 'pendiente'
      )
      .length;

  }


  contarPendientesEnviadas(): number {

    return this.solicitudesEnviadas
      .filter(
        solicitud =>
          solicitud.estado === 'pendiente'
      )
      .length;

  }


  contarInteresesPendientes(): number {

    return this.interesesHabitaciones
      .filter(
        interes =>
          interes.estado === 'pendiente'
      )
      .length;

  }


  /*
   * =========================================================
   * INICIALES
   * =========================================================
   */

  obtenerIniciales(
    nombreCompleto: string
  ): string {

    if (!nombreCompleto) {

      return '';

    }


    const partes =

      nombreCompleto
        .trim()
        .split(/\s+/);


    const primera =

      partes[0]?.charAt(0) ?? '';


    const segunda =

      partes.length > 1
        ? partes[1]?.charAt(0) ?? ''
        : '';


    return (
      primera +
      segunda
    )
    .toUpperCase();

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

      return 'Sin fecha';

    }


    return new Date(
      fecha
    )
    .toLocaleString(
      'es-PE',
      {

        day: '2-digit',

        month: 'short',

        year: 'numeric',

        hour: '2-digit',

        minute: '2-digit'

      }
    );

  }


  /*
   * =========================================================
   * FORMATEAR PRECIO
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
   * TEXTO ESTADO SOLICITUD DE CONTACTO
   * =========================================================
   */

  obtenerTextoEstado(
    estado: string
  ): string {

    if (
      estado === 'pendiente'
    ) {

      return 'Pendiente';

    }


    if (
      estado === 'aceptada'
    ) {

      return 'Aceptada';

    }


    if (
      estado === 'rechazada'
    ) {

      return 'Rechazada';

    }


    return estado;

  }


  /*
   * =========================================================
   * TEXTO ESTADO INTERÉS DE HABITACIÓN
   * =========================================================
   */

  obtenerTextoEstadoInteres(
    estado: string
  ): string {

    if (
      estado === 'pendiente'
    ) {

      return 'Esperando respuesta';

    }


    if (
      estado === 'contactado'
    ) {

      return 'Propietario contactado';

    }


    if (
      estado === 'cerrado'
    ) {

      return 'Proceso finalizado';

    }


    if (
      estado === 'rechazado'
    ) {

      return 'Interés rechazado';

    }


    return estado;

  }


  /*
   * =========================================================
   * DESCRIPCIÓN DEL ESTADO DEL INTERÉS
   * =========================================================
   */

  obtenerDescripcionEstadoInteres(
    estado: string
  ): string {

    if (
      estado === 'pendiente'
    ) {

      return 'Tu consulta fue enviada y está esperando ser revisada por el propietario.';

    }


    if (
      estado === 'contactado'
    ) {

      return 'El propietario revisó tu interés y marcó la consulta como contactada.';

    }


    if (
      estado === 'cerrado'
    ) {

      return 'El propietario dio por finalizado el proceso de esta consulta.';

    }


    if (
      estado === 'rechazado'
    ) {

      return 'El propietario decidió no continuar con esta consulta.';

    }


    return 'Consulta registrada en RoomMatch.';

  }


  /*
   * =========================================================
   * CLASE VISUAL DEL ESTADO
   * =========================================================
   */

  obtenerClaseEstadoInteres(
    estado: string
  ): string {

    if (
      estado === 'pendiente'
    ) {

      return 'pending';

    }


    if (
      estado === 'contactado'
    ) {

      return 'contacted';

    }


    if (
      estado === 'cerrado'
    ) {

      return 'closed';

    }


    if (
      estado === 'rechazado'
    ) {

      return 'rejected';

    }


    return '';

  }

}