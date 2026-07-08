import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  RouterLink
} from '@angular/router';

import {
  catchError,
  forkJoin,
  of
} from 'rxjs';

import {
  HabitacionService,
  HabitacionPayload
} from '../../../core/services/habitacion.service';

import {
  PropietarioService
} from '../../../core/services/propietario.service';

import {
  ImagenHabitacionService
} from '../../../core/services/imagen-habitacion.service';

import {
  HabitacionResponse
} from '../../../core/models/habitacion-response';

import {
  PropietarioResponse
} from '../../../core/models/propietario-response';

import {
  ImagenHabitacionResponse
} from '../../../core/models/imagen-habitacion-response';

import {
  ImageManagerModal
} from '../../../shared/components/image-manager-modal/image-manager-modal';


interface HabitacionFormulario {

  titulo: string;

  descripcion: string;

  distrito: string;

  direccionReferencial: string;

  precio: number | null;

  areaM2: number | null;

  amoblado: boolean;

  banoPrivado: boolean;

  internetIncluido: boolean;

  aguaIncluida: boolean;

  luzIncluida: boolean;

  permiteMascotas: boolean;

  disponibleDesde: string;

  destacada: boolean;
}


@Component({
  selector: 'app-mis-habitaciones',

  imports: [
    FormsModule,
    RouterLink,
    ImageManagerModal
  ],

  templateUrl: './mis-habitaciones.html',

  styleUrl: './mis-habitaciones.css'
})
export class MisHabitaciones implements OnInit {


  /*
   * =========================================================
   * PROPIETARIO
   * =========================================================
   */

  propietario:
    PropietarioResponse | null = null;


  /*
   * =========================================================
   * HABITACIONES
   * =========================================================
   */

  habitaciones:
    HabitacionResponse[] = [];


  /*
   * =========================================================
   * IMÁGENES POR HABITACIÓN
   * =========================================================
   */

  imagenesPorHabitacion:
    Record<
      number,
      ImagenHabitacionResponse[]
    > = {};


  /*
   * =========================================================
   * MODAL DE IMÁGENES
   * =========================================================
   */

  modalImagenesAbierto = false;


  habitacionGestionImagenes:
    HabitacionResponse | null = null;


  /*
   * =========================================================
   * ESTADO GENERAL
   * =========================================================
   */

  cargando = true;

  guardando = false;


  /*
   * =========================================================
   * FORMULARIO
   * =========================================================
   */

  mostrarFormulario = false;


  idHabitacionEditando:
    number | null = null;


  formulario:
    HabitacionFormulario =
      this.crearFormularioVacio();


  /*
   * =========================================================
   * MENSAJES
   * =========================================================
   */

  mensajeError = '';

  mensajeExito = '';


  /*
   * =========================================================
   * CONSTRUCTOR
   * =========================================================
   */

  constructor(

    private habitacionService:
      HabitacionService,

    private propietarioService:
      PropietarioService,

    private imagenHabitacionService:
      ImagenHabitacionService

  ) {}


  /*
   * =========================================================
   * INICIALIZACIÓN
   * =========================================================
   */

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

      propietario:
        this.propietarioService
          .obtenerMiPerfil(),

      habitaciones:
        this.habitacionService
          .listarMisHabitaciones()

    })
      .subscribe({

        next: response => {

          /*
           * =================================================
           * PROPIETARIO
           * =================================================
           */

          if (
            response.propietario.status === 'success' &&
            response.propietario.data
          ) {

            this.propietario =
              response.propietario.data;

          } else {

            this.propietario = null;
          }


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

              Array.isArray(
                response.habitaciones.data.content
              )

                ? response.habitaciones.data.content

                : [];

          } else {

            this.habitaciones = [];
          }


          /*
           * =================================================
           * CARGAR IMÁGENES
           * =================================================
           */

          this.cargarImagenesHabitaciones();


          this.cargando = false;
        },


        error: error => {

          this.cargando = false;


          this.habitaciones = [];

          this.imagenesPorHabitacion = {};


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar tus habitaciones';


          console.error(
            '[RoomMatch habitaciones] Error al cargar:',
            error
          );
        }

      });
  }


  /*
   * =========================================================
   * CARGAR IMÁGENES DE TODAS LAS HABITACIONES
   * =========================================================
   */

  cargarImagenesHabitaciones(): void {

    if (
      !Array.isArray(
        this.habitaciones
      ) ||
      this.habitaciones.length === 0
    ) {

      this.imagenesPorHabitacion = {};

      return;
    }


    const peticiones =

      this.habitaciones.map(

        habitacion =>

          this.imagenHabitacionService

            .listarImagenes(
              habitacion.idHabitacion
            )

            .pipe(

              catchError(
                error => {

                  console.error(

                    '[RoomMatch imágenes] Error en habitación:',

                    habitacion.idHabitacion,

                    error

                  );


                  return of({

                    status: 'error',

                    message:
                      'No se pudieron cargar las imágenes',

                    data:
                      [] as ImagenHabitacionResponse[]

                  });

                }
              )

            )

      );


    forkJoin(
      peticiones
    )
      .subscribe({

        next: responses => {

          const nuevasImagenes:
            Record<
              number,
              ImagenHabitacionResponse[]
            > = {};


          responses.forEach(

            (
              response,
              index
            ) => {

              const habitacion =

                this.habitaciones[index];


              if (!habitacion) {

                return;
              }


              nuevasImagenes[
                habitacion.idHabitacion
              ] =

                response.status === 'success' &&
                Array.isArray(response.data)

                  ? response.data

                  : [];

            }

          );


          this.imagenesPorHabitacion =
            nuevasImagenes;


          console.log(

            '[RoomMatch habitaciones] Imágenes:',

            this.imagenesPorHabitacion

          );
        },


        error: error => {

          this.imagenesPorHabitacion = {};


          console.error(

            '[RoomMatch habitaciones] Error general de imágenes:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * OBTENER IMAGEN PRINCIPAL
   * =========================================================
   */

  obtenerImagenPrincipal(
    idHabitacion: number
  ): string | null {

    const imagenes =

      this.imagenesPorHabitacion[
        idHabitacion
      ] ?? [];


    if (
      imagenes.length === 0
    ) {

      return null;
    }


    const imagenPrincipal =

      imagenes.find(

        imagen =>

          imagen.principal === true

      );


    if (
      imagenPrincipal
    ) {

      return imagenPrincipal.urlImagen;
    }


    return (

      imagenes[0]?.urlImagen ??

      null

    );
  }


  /*
   * =========================================================
   * ABRIR GESTOR DE IMÁGENES
   * =========================================================
   */

  abrirGestorImagenes(
    habitacion: HabitacionResponse
  ): void {

    this.limpiarMensajes();


    this.habitacionGestionImagenes =
      habitacion;


    this.modalImagenesAbierto =
      true;
  }


  /*
   * =========================================================
   * CERRAR GESTOR DE IMÁGENES
   * =========================================================
   */

  cerrarGestorImagenes(): void {

    this.modalImagenesAbierto =
      false;


    this.habitacionGestionImagenes =
      null;
  }


  /*
   * =========================================================
   * ACTUALIZAR IMÁGENES DESDE EL MODAL
   * =========================================================
   */

  actualizarImagenesHabitacion(
    imagenes: ImagenHabitacionResponse[]
  ): void {

    if (
      !this.habitacionGestionImagenes
    ) {

      return;
    }


    const idHabitacion =

      this.habitacionGestionImagenes
        .idHabitacion;


    this.imagenesPorHabitacion = {

      ...this.imagenesPorHabitacion,


      [idHabitacion]:

        Array.isArray(imagenes)

          ? imagenes

          : []

    };
  }


  /*
   * =========================================================
   * NUEVA HABITACIÓN
   * =========================================================
   */

  abrirNuevaHabitacion(): void {

    this.limpiarMensajes();


    if (
      this.alcanzoLimitePlan()
    ) {

      this.mensajeError =

        `Has alcanzado el límite de ${
          this.propietario?.limiteHabitaciones ?? 0
        } habitación(es) de tu plan ${
          this.obtenerNombrePlan()
        }. Mejora tu plan para publicar más espacios.`;


      return;
    }


    this.idHabitacionEditando = null;


    this.formulario =
      this.crearFormularioVacio();


    this.mostrarFormulario = true;


    this.irAlFormulario();
  }


  /*
   * =========================================================
   * EDITAR HABITACIÓN
   * =========================================================
   */

  editarHabitacion(
    habitacion: HabitacionResponse
  ): void {

    this.limpiarMensajes();


    this.idHabitacionEditando =
      habitacion.idHabitacion;


    this.formulario = {

      titulo:
        habitacion.titulo ?? '',

      descripcion:
        habitacion.descripcion ?? '',

      distrito:
        habitacion.distrito ?? '',

      direccionReferencial:
        habitacion.direccionReferencial ?? '',

      precio:
        habitacion.precio ?? null,

      areaM2:
        habitacion.areaM2 ?? null,

      amoblado:
        habitacion.amoblado === true,

      banoPrivado:
        habitacion.banoPrivado === true,

      internetIncluido:
        habitacion.internetIncluido === true,

      aguaIncluida:
        habitacion.aguaIncluida === true,

      luzIncluida:
        habitacion.luzIncluida === true,

      permiteMascotas:
        habitacion.permiteMascotas === true,

      disponibleDesde:
        habitacion.disponibleDesde ?? '',

      destacada:
        habitacion.destacada === true

    };


    this.mostrarFormulario = true;


    this.irAlFormulario();
  }


  /*
   * =========================================================
   * IR AL FORMULARIO
   * =========================================================
   */

  private irAlFormulario(): void {

    setTimeout(
      () => {

        document
          .getElementById(
            'formulario-habitacion'
          )
          ?.scrollIntoView({

            behavior: 'smooth',

            block: 'start'

          });

      },
      50
    );
  }


  /*
   * =========================================================
   * GUARDAR HABITACIÓN
   * =========================================================
   */

  guardarHabitacion(): void {

    this.limpiarMensajes();


    if (
      !this.validarFormulario()
    ) {

      return;
    }


    const request:
      HabitacionPayload = {

      titulo:

        this.formulario
          .titulo
          .trim(),


      descripcion:

        this.formulario
          .descripcion
          .trim(),


      distrito:

        this.formulario
          .distrito
          .trim(),


      direccionReferencial:

        this.formulario
          .direccionReferencial
          .trim() ||

        null,


      precio:

        this.formulario.precio!,


      areaM2:

        this.formulario.areaM2,


      amoblado:

        this.formulario.amoblado,


      banoPrivado:

        this.formulario.banoPrivado,


      internetIncluido:

        this.formulario.internetIncluido,


      aguaIncluida:

        this.formulario.aguaIncluida,


      luzIncluida:

        this.formulario.luzIncluida,


      permiteMascotas:

        this.formulario.permiteMascotas,


      disponibleDesde:

        this.formulario.disponibleDesde ||

        null,


      destacada:

        this.propietario
          ?.permiteDestacar === true

          ? this.formulario.destacada

          : false

    };


    this.guardando = true;


    const esNuevaHabitacion =

      this.idHabitacionEditando === null;


    const operacion =

      esNuevaHabitacion

        ? this.habitacionService
            .crear(
              request
            )

        : this.habitacionService
            .actualizar(

              this.idHabitacionEditando!,

              request

            );


    operacion.subscribe({

      next: response => {

        this.guardando = false;


        if (
          response.status !== 'success'
        ) {

          this.mensajeError =

            response.message ||

            'No se pudo guardar la habitación';


          return;
        }


        this.mensajeExito =

          esNuevaHabitacion

            ? 'Habitación publicada correctamente'

            : 'Habitación actualizada correctamente';


        this.cancelarFormulario();


        this.cargarDatos();
      },


      error: error => {

        this.guardando = false;


        this.mensajeError =

          error.error?.message ||

          'No se pudo guardar la habitación';


        console.error(

          '[RoomMatch habitaciones] Error al guardar:',

          error

        );
      }

    });
  }


  /*
   * =========================================================
   * PAUSAR HABITACIÓN
   * =========================================================
   */

  pausarHabitacion(
    habitacion: HabitacionResponse
  ): void {

    this.limpiarMensajes();


    this.habitacionService
      .pausar(
        habitacion.idHabitacion
      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo pausar la habitación';


            return;
          }


          this.mensajeExito =

            'Habitación pausada correctamente';


          this.cargarDatos();
        },


        error: error => {

          this.mensajeError =

            error.error?.message ||

            'No se pudo pausar la habitación';


          console.error(

            '[RoomMatch habitaciones] Error al pausar:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * ACTIVAR HABITACIÓN
   * =========================================================
   */

  activarHabitacion(
    habitacion: HabitacionResponse
  ): void {

    this.limpiarMensajes();


    if (
      this.alcanzoLimitePlan()
    ) {

      this.mensajeError =

        `Tu plan ${
          this.obtenerNombrePlan()
        } permite ${
          this.propietario?.limiteHabitaciones ?? 0
        } habitación(es) activa(s).`;


      return;
    }


    this.habitacionService
      .activar(
        habitacion.idHabitacion
      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo activar la habitación';


            return;
          }


          this.mensajeExito =

            'Habitación activada correctamente';


          this.cargarDatos();
        },


        error: error => {

          this.mensajeError =

            error.error?.message ||

            'No se pudo activar la habitación';


          console.error(

            '[RoomMatch habitaciones] Error al activar:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * CANCELAR FORMULARIO
   * =========================================================
   */

  cancelarFormulario(): void {

    this.mostrarFormulario = false;


    this.idHabitacionEditando = null;


    this.formulario =
      this.crearFormularioVacio();
  }


  /*
   * =========================================================
   * VALIDAR FORMULARIO
   * =========================================================
   */

  private validarFormulario(): boolean {

    if (
      !this.formulario.titulo.trim()
    ) {

      this.mensajeError =

        'Ingresa un título para la habitación';


      return false;
    }


    if (
      !this.formulario.descripcion.trim()
    ) {

      this.mensajeError =

        'Describe la habitación';


      return false;
    }


    if (
      !this.formulario.distrito.trim()
    ) {

      this.mensajeError =

        'Ingresa el distrito';


      return false;
    }


    if (
      this.formulario.precio === null ||
      this.formulario.precio <= 0
    ) {

      this.mensajeError =

        'Ingresa un precio válido';


      return false;
    }


    if (
      this.formulario.areaM2 !== null &&
      this.formulario.areaM2 <= 0
    ) {

      this.mensajeError =

        'El área de la habitación debe ser mayor a cero';


      return false;
    }


    return true;
  }


  /*
   * =========================================================
   * HABITACIONES ACTIVAS
   * =========================================================
   */

  contarActivas(): number {

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

  contarPausadas(): number {

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
   * ALCANZÓ LÍMITE DEL PLAN
   * =========================================================
   */

  alcanzoLimitePlan(): boolean {

    const limite =

      this.propietario
        ?.limiteHabitaciones ?? 0;


    if (
      limite <= 0
    ) {

      return false;
    }


    return (

      this.contarActivas() >=
      limite

    );
  }


  /*
   * =========================================================
   * PORCENTAJE DE USO DEL PLAN
   * =========================================================
   */

  obtenerPorcentajePlan(): number {

    const limite =

      this.propietario
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
          this.contarActivas() /
          limite
        ) * 100

      )

    );
  }


  /*
   * =========================================================
   * HABITACIONES DISPONIBLES
   * =========================================================
   */

  obtenerHabitacionesDisponibles(): number {

    const limite =

      this.propietario
        ?.limiteHabitaciones ?? 0;


    return Math.max(

      0,

      limite -
      this.contarActivas()

    );
  }


  /*
   * =========================================================
   * NOMBRE DEL PLAN
   * =========================================================
   */

  obtenerNombrePlan(): string {

    return (

      this.propietario
        ?.nombrePlan ??

      this.propietario
        ?.planActual ??

      'Gratis'

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
   * FORMATEAR FECHA
   * =========================================================
   */

  formatearFecha(
    fecha: string | null
  ): string {

    if (
      !fecha
    ) {

      return 'Disponible ahora';
    }


    return new Date(
      `${fecha}T00:00:00`
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
   * LIMPIAR MENSAJES
   * =========================================================
   */

  private limpiarMensajes(): void {

    this.mensajeError = '';

    this.mensajeExito = '';
  }


  /*
   * =========================================================
   * CREAR FORMULARIO VACÍO
   * =========================================================
   */

  private crearFormularioVacio():
    HabitacionFormulario {

    return {

      titulo: '',

      descripcion: '',

      distrito: '',

      direccionReferencial: '',

      precio: null,

      areaM2: null,

      amoblado: false,

      banoPrivado: false,

      internetIncluido: false,

      aguaIncluida: false,

      luzIncluida: false,

      permiteMascotas: false,

      disponibleDesde: '',

      destacada: false

    };
  }
}