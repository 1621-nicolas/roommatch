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
  RouterLink
} from '@angular/router';

import {
  PublicacionRoomieService
} from '../../../core/services/publicacion-roomie.service';

import {
  HabitacionService
} from '../../../core/services/habitacion.service';

import {
  PublicacionRoomieResponse
} from '../../../core/models/publicacion-roomie-response';

import {
  PublicacionRoomieRequest
} from '../../../core/models/publicacion-roomie-request';

import {
  HabitacionResponse
} from '../../../core/models/habitacion-response';


@Component({
  selector: 'app-mis-publicaciones',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],

  templateUrl: './mis-publicaciones.html',

  styleUrl: './mis-publicaciones.css'
})
export class MisPublicaciones implements OnInit {


  /*
   * =========================================================
   * PUBLICACIONES
   * =========================================================
   */

  publicaciones: PublicacionRoomieResponse[] = [];


  /*
   * =========================================================
   * HABITACIONES ROOMMATCH
   * =========================================================
   */

  habitacionesDisponibles: HabitacionResponse[] = [];

  cargandoHabitaciones = false;


  /*
   * =========================================================
   * FORMULARIO
   * =========================================================
   */

  formularioVisible = false;

  editando = false;

  idPublicacionEditando: number | null = null;


  formulario: PublicacionRoomieRequest =
    this.crearFormularioVacio();


  /*
   * =========================================================
   * ESTADO
   * =========================================================
   */

  cargando = true;

  guardando = false;

  accionandoId: number | null = null;

  mensajeError = '';

  mensajeExito = '';


  /*
   * =========================================================
   * ELIMINACIÓN
   * =========================================================
   */

  publicacionEliminar:
    PublicacionRoomieResponse | null = null;


  constructor(

    private publicacionService:
      PublicacionRoomieService,

    private habitacionService:
      HabitacionService

  ) {}


  /*
   * =========================================================
   * INICIO
   * =========================================================
   */

  ngOnInit(): void {

    this.cargarPublicaciones();

  }


  /*
   * =========================================================
   * FORMULARIO VACÍO
   * =========================================================
   */

  private crearFormularioVacio():
    PublicacionRoomieRequest {

    return {

      tipoPublicacion: '',

      titulo: '',

      descripcion: '',

      distrito: '',

      presupuestoMin: null,

      presupuestoMax: null,

      tipoVinculacionVivienda: null,

      idHabitacion: null,

      viviendaExternaTitulo: null,

      viviendaExternaDireccion: null,

      viviendaExternaPrecio: null

    };

  }


  /*
   * =========================================================
   * CARGAR PUBLICACIONES
   * =========================================================
   */

  cargarPublicaciones(): void {

    this.cargando = true;

    this.mensajeError = '';


    this.publicacionService
      .listarMisPublicaciones(
        0,
        50
      )
      .subscribe({

        next: response => {

          this.cargando = false;


          if (
            response.status !== 'success'
            ||
            !response.data
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudieron cargar tus publicaciones';

            return;

          }


          this.publicaciones =

            response.data.content ?? [];

        },


        error: error => {

          this.cargando = false;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudieron cargar tus publicaciones';

        }

      });

  }


  /*
   * =========================================================
   * CARGAR HABITACIONES
   * =========================================================
   */

  cargarHabitaciones(): void {

    if (
      this.habitacionesDisponibles.length > 0
    ) {

      return;

    }


    this.cargandoHabitaciones = true;


    this.habitacionService
      .listarDisponibles(
        0,
        50
      )
      .subscribe({

        next: response => {

          this.cargandoHabitaciones = false;


          if (
            response.status !== 'success'
            ||
            !response.data
          ) {

            return;

          }


          this.habitacionesDisponibles =

            response.data.content ?? [];

        },


        error: () => {

          this.cargandoHabitaciones = false;

        }

      });

  }


  /*
   * =========================================================
   * CAMBIAR TIPO DE PUBLICACIÓN
   * =========================================================
   */

  cambiarTipoPublicacion(): void {

    if (
      this.formulario.tipoPublicacion
      !== 'busco_compartir'
    ) {

      this.limpiarVinculacionVivienda();

    }

  }


  /*
   * =========================================================
   * CAMBIAR TIPO DE VINCULACIÓN
   * =========================================================
   */

  seleccionarVinculacion(
    tipo: string | null
  ): void {

    this.formulario.tipoVinculacionVivienda =
      tipo;


    this.formulario.idHabitacion = null;


    this.formulario.viviendaExternaTitulo =
      null;

    this.formulario.viviendaExternaDireccion =
      null;

    this.formulario.viviendaExternaPrecio =
      null;


    if (
      tipo === 'roommatch'
    ) {

      this.cargarHabitaciones();

    }

  }


  /*
   * =========================================================
   * SELECCIONAR HABITACIÓN
   * =========================================================
   */

  seleccionarHabitacion(
    habitacion: HabitacionResponse
  ): void {

    this.formulario.idHabitacion =

      habitacion.idHabitacion;


    this.mensajeError = '';

  }


  /*
   * =========================================================
   * OBTENER HABITACIÓN SELECCIONADA
   * =========================================================
   */

  obtenerHabitacionSeleccionada():
    HabitacionResponse | null {

    if (
      this.formulario.idHabitacion === null
    ) {

      return null;

    }


    return (

      this.habitacionesDisponibles
        .find(
          habitacion =>
            habitacion.idHabitacion
            === this.formulario.idHabitacion
        )

      ?? null

    );

  }


  /*
   * =========================================================
   * NUEVA PUBLICACIÓN
   * =========================================================
   */

  nuevaPublicacion(): void {

    this.editando = false;

    this.idPublicacionEditando = null;


    this.limpiarFormulario();


    this.formularioVisible = true;


    this.limpiarMensajes();


    this.irAlFormulario();

  }


  /*
   * =========================================================
   * EDITAR
   * =========================================================
   */

  editarPublicacion(

    publicacion: PublicacionRoomieResponse

  ): void {

    this.editando = true;


    this.idPublicacionEditando =

      publicacion.idPublicacion;


    this.formulario = {

      tipoPublicacion:

        publicacion.tipoPublicacion ?? '',


      titulo:

        publicacion.titulo ?? '',


      descripcion:

        publicacion.descripcion ?? '',


      distrito:

        publicacion.distrito ?? '',


      presupuestoMin:

        publicacion.presupuestoMin ?? null,


      presupuestoMax:

        publicacion.presupuestoMax ?? null,


      tipoVinculacionVivienda:

        publicacion.tipoVinculacionVivienda
        ?? null,


      idHabitacion:

        publicacion.idHabitacion
        ?? null,


      viviendaExternaTitulo:

        publicacion.viviendaExternaTitulo
        ?? null,


      viviendaExternaDireccion:

        publicacion.viviendaExternaDireccion
        ?? null,


      viviendaExternaPrecio:

        publicacion.viviendaExternaPrecio
        ?? null

    };


    if (
      this.formulario.tipoVinculacionVivienda
      === 'roommatch'
    ) {

      this.cargarHabitaciones();

    }


    this.formularioVisible = true;


    this.limpiarMensajes();


    this.irAlFormulario();

  }


  /*
   * =========================================================
   * GUARDAR
   * =========================================================
   */

  guardarPublicacion(): void {

    this.limpiarMensajes();


    if (!this.validarFormulario()) {

      return;

    }


    this.prepararRequest();


    this.guardando = true;


    if (
      this.editando
      &&
      this.idPublicacionEditando !== null
    ) {

      this.actualizarPublicacion();

      return;

    }


    this.crearPublicacion();

  }


  /*
   * =========================================================
   * PREPARAR REQUEST
   * =========================================================
   */

  private prepararRequest(): void {

    if (
      this.formulario.tipoPublicacion
      !== 'busco_compartir'
    ) {

      this.limpiarVinculacionVivienda();

      return;

    }


    if (
      this.formulario.tipoVinculacionVivienda
      === 'roommatch'
    ) {

      this.formulario.viviendaExternaTitulo =
        null;

      this.formulario.viviendaExternaDireccion =
        null;

      this.formulario.viviendaExternaPrecio =
        null;

      return;

    }


    if (
      this.formulario.tipoVinculacionVivienda
      === 'externa'
    ) {

      this.formulario.idHabitacion = null;

      return;

    }


    this.limpiarVinculacionVivienda();

  }


  /*
   * =========================================================
   * CREAR
   * =========================================================
   */

  private crearPublicacion(): void {

    this.publicacionService
      .crearPublicacion(
        this.formulario
      )
      .subscribe({

        next: response => {

          this.guardando = false;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo crear la publicación';

            return;

          }


          this.mensajeExito =

            'Tu publicación fue creada correctamente';


          this.formularioVisible = false;


          this.limpiarFormulario();


          this.cargarPublicaciones();

        },


        error: error => {

          this.guardando = false;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo crear la publicación';

        }

      });

  }


  /*
   * =========================================================
   * ACTUALIZAR
   * =========================================================
   */

  private actualizarPublicacion(): void {

    if (
      this.idPublicacionEditando === null
    ) {

      this.guardando = false;

      return;

    }


    this.publicacionService
      .actualizarPublicacion(

        this.idPublicacionEditando,

        this.formulario

      )
      .subscribe({

        next: response => {

          this.guardando = false;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo actualizar la publicación';

            return;

          }


          this.mensajeExito =

            'Tu publicación fue actualizada correctamente';


          this.formularioVisible = false;


          this.editando = false;


          this.idPublicacionEditando = null;


          this.limpiarFormulario();


          this.cargarPublicaciones();

        },


        error: error => {

          this.guardando = false;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo actualizar la publicación';

        }

      });

  }


  /*
   * =========================================================
   * CANCELAR
   * =========================================================
   */

  cancelarFormulario(): void {

    this.formularioVisible = false;

    this.editando = false;

    this.idPublicacionEditando = null;

    this.guardando = false;


    this.limpiarFormulario();


    this.mensajeError = '';

  }


  /*
   * =========================================================
   * PAUSAR
   * =========================================================
   */

  pausarPublicacion(

    publicacion: PublicacionRoomieResponse

  ): void {

    this.accionandoId =

      publicacion.idPublicacion;


    this.limpiarMensajes();


    this.publicacionService
      .pausarPublicacion(
        publicacion.idPublicacion
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo pausar la publicación';

            return;

          }


          this.mensajeExito =

            'La publicación fue pausada';


          this.cargarPublicaciones();

        },


        error: error => {

          this.accionandoId = null;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo pausar la publicación';

        }

      });

  }


  /*
   * =========================================================
   * ACTIVAR
   * =========================================================
   */

  activarPublicacion(

    publicacion: PublicacionRoomieResponse

  ): void {

    this.accionandoId =

      publicacion.idPublicacion;


    this.limpiarMensajes();


    this.publicacionService
      .activarPublicacion(
        publicacion.idPublicacion
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo activar la publicación';

            return;

          }


          this.mensajeExito =

            'La publicación está nuevamente activa';


          this.cargarPublicaciones();

        },


        error: error => {

          this.accionandoId = null;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo activar la publicación';

        }

      });

  }


  /*
   * =========================================================
   * CERRAR
   * =========================================================
   */

  cerrarPublicacion(

    publicacion: PublicacionRoomieResponse

  ): void {

    this.accionandoId =

      publicacion.idPublicacion;


    this.limpiarMensajes();


    this.publicacionService
      .cerrarPublicacion(
        publicacion.idPublicacion
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo cerrar la publicación';

            return;

          }


          this.mensajeExito =

            'La publicación fue cerrada';


          this.cargarPublicaciones();

        },


        error: error => {

          this.accionandoId = null;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo cerrar la publicación';

        }

      });

  }


  /*
   * =========================================================
   * ELIMINACIÓN
   * =========================================================
   */

  abrirEliminar(

    publicacion: PublicacionRoomieResponse

  ): void {

    this.publicacionEliminar = publicacion;

    this.limpiarMensajes();

  }


  cancelarEliminar(): void {

    this.publicacionEliminar = null;

  }


  confirmarEliminar(): void {

    if (!this.publicacionEliminar) {

      return;

    }


    const publicacion =

      this.publicacionEliminar;


    this.accionandoId =

      publicacion.idPublicacion;


    this.publicacionService
      .eliminarPublicacion(
        publicacion.idPublicacion
      )
      .subscribe({

        next: response => {

          this.accionandoId = null;

          this.publicacionEliminar = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message
              ||
              'No se pudo eliminar la publicación';

            return;

          }
            this.publicaciones =
          this.publicaciones.filter(
            item =>
              item.idPublicacion !==
              publicacion.idPublicacion
                 );



          this.mensajeExito =

          `La publicación "${publicacion.titulo}" fue eliminada correctamente`;
        },


        error: error => {

          this.accionandoId = null;


          this.mensajeError =

            error.error?.message
            ||
            'No se pudo eliminar la publicación';

        }

      });

  }


  /*
   * =========================================================
   * VALIDAR FORMULARIO
   * =========================================================
   */

  private validarFormulario(): boolean {

    if (
      !this.formulario.tipoPublicacion
    ) {

      this.mensajeError =

        'Selecciona el tipo de publicación';

      return false;

    }


    if (
      !this.formulario.titulo?.trim()
    ) {

      this.mensajeError =

        'Ingresa un título para tu publicación';

      return false;

    }


    if (
      !this.formulario.descripcion?.trim()
    ) {

      this.mensajeError =

        'Describe qué estás buscando';

      return false;

    }


    if (
      !this.formulario.distrito?.trim()
    ) {

      this.mensajeError =

        'Ingresa el distrito';

      return false;

    }


    if (
      this.formulario.presupuestoMin !== null
      &&
      this.formulario.presupuestoMax !== null
      &&
      Number(
        this.formulario.presupuestoMax
      )
      <
      Number(
        this.formulario.presupuestoMin
      )
    ) {

      this.mensajeError =

        'El presupuesto máximo no puede ser menor que el mínimo';

      return false;

    }


    if (
      this.formulario.tipoPublicacion
      === 'busco_compartir'
    ) {

      if (
        this.formulario.tipoVinculacionVivienda
        === 'roommatch'
        &&
        this.formulario.idHabitacion === null
      ) {

        this.mensajeError =

          'Selecciona una habitación de RoomMatch';

        return false;

      }


      if (
        this.formulario.tipoVinculacionVivienda
        === 'externa'
      ) {

        if (
          !this.formulario
            .viviendaExternaTitulo
            ?.trim()
        ) {

          this.mensajeError =

            'Ingresa un título para la vivienda';

          return false;

        }


        if (
          !this.formulario
            .viviendaExternaDireccion
            ?.trim()
        ) {

          this.mensajeError =

            'Ingresa una dirección referencial';

          return false;

        }


        if (
          this.formulario
            .viviendaExternaPrecio
          === null
          ||
          Number(
            this.formulario
              .viviendaExternaPrecio
          )
          <= 0
        ) {

          this.mensajeError =

            'Ingresa el precio mensual de la vivienda';

          return false;

        }

      }

    }


    return true;

  }


  /*
   * =========================================================
   * LIMPIAR VINCULACIÓN
   * =========================================================
   */

  private limpiarVinculacionVivienda(): void {

    this.formulario.tipoVinculacionVivienda =
      null;

    this.formulario.idHabitacion =
      null;

    this.formulario.viviendaExternaTitulo =
      null;

    this.formulario.viviendaExternaDireccion =
      null;

    this.formulario.viviendaExternaPrecio =
      null;

  }


  /*
   * =========================================================
   * LIMPIAR FORMULARIO
   * =========================================================
   */

  private limpiarFormulario(): void {

    this.formulario =

      this.crearFormularioVacio();

  }


  /*
   * =========================================================
   * CONTADORES
   * =========================================================
   */

  contarActivas(): number {

    return this.publicaciones
      .filter(
        publicacion =>
          publicacion.estado?.toLowerCase()
          === 'activa'
      )
      .length;

  }


  contarPausadas(): number {

    return this.publicaciones
      .filter(
        publicacion =>
          publicacion.estado?.toLowerCase()
          === 'pausada'
      )
      .length;

  }


  contarCerradas(): number {

    return this.publicaciones
      .filter(
        publicacion =>
          publicacion.estado?.toLowerCase()
          === 'cerrada'
      )
      .length;

  }


  /*
   * =========================================================
   * TEXTO DEL TIPO
   * =========================================================
   */

  obtenerTipo(

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

      return 'Busco habitación';

    }


    if (
      tipo === 'busco_compartir'
    ) {

      return 'Busco compartir';

    }


    return tipo;

  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */

  formatearFecha(

    fecha: string | null | undefined

  ): string {

    if (!fecha) {

      return 'Sin fecha';

    }


    return new Date(
      fecha
    ).toLocaleDateString(
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
   * AUXILIARES
   * =========================================================
   */

  private limpiarMensajes(): void {

    this.mensajeError = '';

    this.mensajeExito = '';

  }


  private irAlFormulario(): void {

    setTimeout(
      () => {

        document
          .getElementById(
            'formulario-publicacion'
          )
          ?.scrollIntoView({

            behavior: 'smooth',

            block: 'start'

          });

      },
      50
    );

  }

}