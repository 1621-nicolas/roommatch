import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  ImagenHabitacionService
} from '../../../core/services/imagen-habitacion.service';

import {
  ImagenHabitacionResponse
} from '../../../core/models/imagen-habitacion-response';

import {
  ImagenHabitacionRequest
} from '../../../core/models/imagen-habitacion-request';

import {
  HabitacionResponse
} from '../../../core/models/habitacion-response';


@Component({
  selector: 'app-image-manager-modal',

  imports: [
    FormsModule
  ],

  templateUrl: './image-manager-modal.html',

  styleUrl: './image-manager-modal.css'
})
export class ImageManagerModal implements OnChanges {


  /*
   * =========================================================
   * INPUTS
   * =========================================================
   */

  @Input()
  abierta = false;


  @Input()
  habitacion: HabitacionResponse | null = null;


  /*
   * =========================================================
   * OUTPUTS
   * =========================================================
   */

  @Output()
  cerrar = new EventEmitter<void>();


  @Output()
  imagenesActualizadas =
    new EventEmitter<ImagenHabitacionResponse[]>();


  /*
   * =========================================================
   * ESTADO
   * =========================================================
   */

  imagenes: ImagenHabitacionResponse[] = [];


  urlImagen = '';


  marcarPrincipalAlAgregar = false;


  cargando = false;


  agregando = false;


  idImagenProcesando: number | null = null;


  mensajeError = '';


  mensajeExito = '';


  imagenEliminar: ImagenHabitacionResponse | null = null;


  /*
   * =========================================================
   * CONSTRUCTOR
   * =========================================================
   */

  constructor(
    private imagenHabitacionService:
      ImagenHabitacionService
  ) {}


  /*
   * =========================================================
   * CAMBIOS INPUT
   * =========================================================
   */

  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['abierta'] ||
      changes['habitacion']
    ) {

      if (
        this.abierta &&
        this.habitacion
      ) {

        this.prepararModal();

        this.cargarImagenes();
      }
    }
  }


  /*
   * =========================================================
   * PREPARAR MODAL
   * =========================================================
   */

  private prepararModal(): void {

    this.urlImagen = '';

    this.marcarPrincipalAlAgregar = false;

    this.mensajeError = '';

    this.mensajeExito = '';

    this.imagenEliminar = null;

    this.idImagenProcesando = null;
  }


  /*
   * =========================================================
   * CARGAR IMÁGENES
   * =========================================================
   */

  cargarImagenes(): void {

    if (!this.habitacion) {
      return;
    }


    this.cargando = true;

    this.mensajeError = '';


    this.imagenHabitacionService
      .listarImagenes(
        this.habitacion.idHabitacion
      )
      .subscribe({

        next: response => {

          if (
            response.status === 'success'
          ) {

            this.imagenes =
              Array.isArray(response.data)
                ? response.data
                : [];

          } else {

            this.imagenes = [];

            this.mensajeError =
              response.message ||
              'No se pudieron cargar las imágenes';
          }


          this.cargando = false;
        },


        error: error => {

          this.imagenes = [];

          this.cargando = false;


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las imágenes de la habitación';


          console.error(
            '[RoomMatch imágenes] Error al listar:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * AGREGAR IMAGEN
   * =========================================================
   */

  agregarImagen(): void {

    if (!this.habitacion) {
      return;
    }


    this.limpiarMensajes();


    const url =
      this.urlImagen.trim();


    if (!url) {

      this.mensajeError =
        'Ingresa la URL de una imagen';

      return;
    }


    if (!this.esUrlValida(url)) {

      this.mensajeError =
        'Ingresa una URL válida que comience con http:// o https://';

      return;
    }


    if (
      this.imagenes.length >= 5
    ) {

      this.mensajeError =
        'Solo puedes registrar hasta 5 imágenes por habitación';

      return;
    }


    const request:
      ImagenHabitacionRequest = {

        urlImagen: url,

        orden:
          this.imagenes.length + 1,

        principal:
          this.marcarPrincipalAlAgregar

      };


    this.agregando = true;


    this.imagenHabitacionService
      .agregarImagen(
        this.habitacion.idHabitacion,
        request
      )
      .subscribe({

        next: response => {

          this.agregando = false;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo agregar la imagen';

            return;
          }


          this.urlImagen = '';

          this.marcarPrincipalAlAgregar = false;


          this.mensajeExito =
            'Imagen agregada correctamente';


          this.cargarImagenesYNotificar();
        },


        error: error => {

          this.agregando = false;


          this.mensajeError =

            error.error?.message ||

            'No se pudo agregar la imagen';


          console.error(
            '[RoomMatch imágenes] Error al agregar:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * MARCAR PRINCIPAL
   * =========================================================
   */

  marcarPrincipal(
    imagen: ImagenHabitacionResponse
  ): void {

    if (
      imagen.principal ||
      this.idImagenProcesando !== null
    ) {

      return;
    }


    this.limpiarMensajes();


    this.idImagenProcesando =
      imagen.idImagen;


    this.imagenHabitacionService
      .marcarComoPrincipal(
        imagen.idImagen
      )
      .subscribe({

        next: response => {

          this.idImagenProcesando = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo cambiar la imagen principal';

            return;
          }


          this.mensajeExito =
            'Imagen principal actualizada';


          this.cargarImagenesYNotificar();
        },


        error: error => {

          this.idImagenProcesando = null;


          this.mensajeError =

            error.error?.message ||

            'No se pudo cambiar la imagen principal';


          console.error(
            '[RoomMatch imágenes] Error principal:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * SOLICITAR ELIMINACIÓN
   * =========================================================
   */

  solicitarEliminar(
    imagen: ImagenHabitacionResponse
  ): void {

    this.limpiarMensajes();


    this.imagenEliminar =
      imagen;
  }


  /*
   * =========================================================
   * CANCELAR ELIMINACIÓN
   * =========================================================
   */

  cancelarEliminar(): void {

    this.imagenEliminar = null;
  }


  /*
   * =========================================================
   * CONFIRMAR ELIMINACIÓN
   * =========================================================
   */

  confirmarEliminar(): void {

    if (!this.imagenEliminar) {
      return;
    }


    const idImagen =
      this.imagenEliminar.idImagen;


    this.idImagenProcesando =
      idImagen;


    this.imagenHabitacionService
      .eliminarImagen(
        idImagen
      )
      .subscribe({

        next: response => {

          this.idImagenProcesando = null;

          this.imagenEliminar = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo eliminar la imagen';

            return;
          }


          this.mensajeExito =
            'Imagen eliminada correctamente';


          this.cargarImagenesYNotificar();
        },


        error: error => {

          this.idImagenProcesando = null;


          this.mensajeError =

            error.error?.message ||

            'No se pudo eliminar la imagen';


          console.error(
            '[RoomMatch imágenes] Error al eliminar:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * CARGAR Y NOTIFICAR PADRE
   * =========================================================
   */

  private cargarImagenesYNotificar(): void {

    if (!this.habitacion) {
      return;
    }


    this.imagenHabitacionService
      .listarImagenes(
        this.habitacion.idHabitacion
      )
      .subscribe({

        next: response => {

          if (
            response.status === 'success'
          ) {

            this.imagenes =
              Array.isArray(response.data)
                ? response.data
                : [];


            this.imagenesActualizadas.emit(
              this.imagenes
            );
          }
        },


        error: error => {

          console.error(
            '[RoomMatch imágenes] Error al actualizar:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * IMAGEN PRINCIPAL
   * =========================================================
   */

  obtenerImagenPrincipal():
    ImagenHabitacionResponse | null {

    return (

      this.imagenes.find(
        imagen =>
          imagen.principal === true
      ) ??

      this.imagenes[0] ??

      null

    );
  }


  /*
   * =========================================================
   * ERROR DE IMAGEN
   * =========================================================
   */

  imagenConError(
    event: Event
  ): void {

    const elemento =
      event.target as HTMLImageElement;


    elemento.style.display =
      'none';
  }


  /*
   * =========================================================
   * VALIDAR URL
   * =========================================================
   */

  private esUrlValida(
    url: string
  ): boolean {

    try {

      const urlValidada =
        new URL(url);


      return (

        urlValidada.protocol === 'http:' ||

        urlValidada.protocol === 'https:'

      );

    } catch {

      return false;
    }
  }


  /*
   * =========================================================
   * LIMPIAR MENSAJES
   * =========================================================
   */

  private limpiarMensajes(): void {

    this.mensajeError = '';

    this.mensajeExito = '';
  }


  /*
   * =========================================================
   * CERRAR MODAL
   * =========================================================
   */

  cerrarModal(): void {

    if (
      this.agregando ||
      this.idImagenProcesando !== null
    ) {

      return;
    }


    this.prepararModal();

    this.imagenes = [];


    this.cerrar.emit();
  }
}