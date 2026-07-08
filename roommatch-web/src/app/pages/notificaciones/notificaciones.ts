import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  finalize
} from 'rxjs/operators';

import {
  NotificacionService
} from '../../core/services/notificacion.service';

import {
  NotificacionResponse
} from '../../core/models/notificacion-response';


@Component({
  selector: 'app-notificaciones',

  imports: [
    CommonModule
  ],

  templateUrl: './notificaciones.html',

  styleUrl: './notificaciones.css'
})
export class Notificaciones implements OnInit {

  notificaciones: NotificacionResponse[] = [];

  filtro:
    'todas' |
    'no-leidas' = 'todas';


  cargando = false;

  procesandoTodas = false;


  paginaActual = 0;

  tamanioPagina = 10;

  totalPaginas = 0;

  totalElementos = 0;


  mensajeError = '';

  mensajeExito = '';


  constructor(
    private notificacionService: NotificacionService,
    private router: Router
  ) {}


  ngOnInit(): void {

    this.cargarNotificaciones();

  }


  /*
   * =========================================================
   * CARGAR NOTIFICACIONES
   * =========================================================
   */
  cargarNotificaciones(
    pagina: number = 0
  ): void {

    if (this.cargando) {

      return;

    }


    this.cargando = true;

    this.mensajeError = '';


    this.notificacionService
      .listar(
        pagina,
        this.tamanioPagina
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

            this.notificaciones = [];

            this.mensajeError =
              response.message ||
              'No se pudieron cargar las notificaciones';

            return;

          }


          this.notificaciones =
            response.data.content ?? [];


          this.paginaActual =
            response.data.number;


          this.totalPaginas =
            response.data.totalPages;


          this.totalElementos =
            response.data.totalElements;

        },


        error: error => {

          console.error(
            'Error cargando notificaciones:',
            error
          );


          this.notificaciones = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las notificaciones';

        }

      });

  }


  /*
   * =========================================================
   * NOTIFICACIONES VISIBLES
   * =========================================================
   */
  obtenerNotificacionesVisibles():
    NotificacionResponse[] {


    if (
      this.filtro === 'no-leidas'
    ) {

      return this.notificaciones
        .filter(
          notificacion =>
            !notificacion.leido
        );

    }


    return this.notificaciones;

  }


  /*
   * =========================================================
   * CONTADOR NO LEÍDAS
   * =========================================================
   */
  contarNoLeidas(): number {

    return this.notificaciones
      .filter(
        notificacion =>
          !notificacion.leido
      )
      .length;

  }


  /*
   * =========================================================
   * CAMBIAR FILTRO
   * =========================================================
   */
  cambiarFiltro(
    filtro:
      'todas' |
      'no-leidas'
  ): void {

    this.filtro = filtro;

  }


  /*
   * =========================================================
   * ABRIR NOTIFICACIÓN
   * =========================================================
   */
  abrirNotificacion(
    notificacion: NotificacionResponse
  ): void {


    if (notificacion.leido) {

      this.navegarDestino(
        notificacion
      );

      return;

    }


    this.notificacionService
      .marcarComoLeida(
        notificacion.idNotificacion
      )
      .subscribe({

        next: response => {

          if (
            response.status === 'success'
          ) {

            notificacion.leido = true;

          }


          this.navegarDestino(
            notificacion
          );

        },


        error: error => {

          console.error(
            'Error marcando notificación:',
            error
          );


          this.mensajeError =

            error.error?.message ||

            'No se pudo actualizar la notificación';

        }

      });

  }


  /*
   * =========================================================
   * MARCAR UNA COMO LEÍDA
   * =========================================================
   */
  marcarComoLeida(
    notificacion: NotificacionResponse,
    event: Event
  ): void {

    event.stopPropagation();


    if (notificacion.leido) {

      return;

    }


    this.mensajeError = '';


    this.notificacionService
      .marcarComoLeida(
        notificacion.idNotificacion
      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success'
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo marcar la notificación como leída';

            return;

          }


          notificacion.leido = true;

        },


        error: error => {

          this.mensajeError =

            error.error?.message ||

            'No se pudo marcar la notificación como leída';

        }

      });

  }


  /*
   * =========================================================
   * MARCAR TODAS COMO LEÍDAS
   * =========================================================
   */
  marcarTodasComoLeidas(): void {

    if (
      this.procesandoTodas ||
      this.contarNoLeidas() === 0
    ) {

      return;

    }


    this.procesandoTodas = true;

    this.mensajeError = '';

    this.mensajeExito = '';


    this.notificacionService
      .marcarTodasComoLeidas()
      .pipe(

        finalize(() => {

          this.procesandoTodas = false;

        })

      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success'
          ) {

            this.mensajeError =
              response.message ||
              'No se pudieron actualizar las notificaciones';

            return;

          }


          this.notificaciones =
            this.notificaciones.map(
              notificacion => ({

                ...notificacion,

                leido: true

              })
            );


          this.mensajeExito =
            'Todas las notificaciones fueron marcadas como leídas';

        },


        error: error => {

          this.mensajeError =

            error.error?.message ||

            'No se pudieron actualizar las notificaciones';

        }

      });

  }


  /*
   * =========================================================
   * NAVEGAR
   * =========================================================
   */
  private navegarDestino(
    notificacion: NotificacionResponse
  ): void {

    if (!notificacion.urlDestino) {

      return;

    }


    this.router.navigateByUrl(
      notificacion.urlDestino
    );

  }


  /*
   * =========================================================
   * ICONO
   * =========================================================
   */
  obtenerIcono(
    tipo: string
  ): string {


    switch (
      tipo?.toLowerCase()
    ) {

      case 'solicitud':
        return '👤';


      case 'contacto':
        return '💜';


      case 'match':
        return '♡';


      case 'habitacion':
        return '⌂';


      case 'lead':
        return '✉';


      case 'sistema':
        return 'ⓘ';


      default:
        return '●';

    }

  }


  /*
   * =========================================================
   * TEXTO DEL TIPO
   * =========================================================
   */
  obtenerNombreTipo(
    tipo: string
  ): string {


    switch (
      tipo?.toLowerCase()
    ) {

      case 'solicitud':
        return 'Solicitud';


      case 'contacto':
        return 'Contacto';


      case 'match':
        return 'Match';


      case 'habitacion':
        return 'Habitación';


      case 'lead':
        return 'Interés';


      case 'sistema':
        return 'RoomMatch';


      default:
        return 'Notificación';

    }

  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */
  formatearFecha(
    fecha: string
  ): string {


    if (!fecha) {

      return '';

    }


    const fechaNotificacion =
      new Date(fecha);


    const ahora =
      new Date();


    const diferencia =
      ahora.getTime() -
      fechaNotificacion.getTime();


    const minutos =
      Math.floor(
        diferencia / 60000
      );


    const horas =
      Math.floor(
        diferencia / 3600000
      );


    const dias =
      Math.floor(
        diferencia / 86400000
      );


    if (minutos < 1) {

      return 'Ahora';

    }


    if (minutos < 60) {

      return `Hace ${minutos} min`;

    }


    if (horas < 24) {

      return `Hace ${horas} h`;

    }


    if (dias === 1) {

      return 'Ayer';

    }


    if (dias < 7) {

      return `Hace ${dias} días`;

    }


    return fechaNotificacion
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
   * PAGINACIÓN
   * =========================================================
   */
  paginaAnterior(): void {

    if (this.paginaActual <= 0) {

      return;

    }


    this.cargarNotificaciones(
      this.paginaActual - 1
    );

  }


  paginaSiguiente(): void {

    if (
      this.paginaActual >=
      this.totalPaginas - 1
    ) {

      return;

    }


    this.cargarNotificaciones(
      this.paginaActual + 1
    );

  }

}