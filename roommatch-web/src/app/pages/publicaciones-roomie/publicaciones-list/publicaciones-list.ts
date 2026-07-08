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
  PublicacionRoomieService
} from '../../../core/services/publicacion-roomie.service';

import {
  PublicacionRoomieResponse
} from '../../../core/models/publicacion-roomie-response';


@Component({
  selector: 'app-publicaciones-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './publicaciones-list.html',
  styleUrls: [
    './publicaciones-list.css'
  ]
})
export class PublicacionesList implements OnInit {

  publicaciones: PublicacionRoomieResponse[] = [];

  publicacionesVisibles: PublicacionRoomieResponse[] = [];


  /*
   * =========================================================
   * FILTROS
   * =========================================================
   */

  tipoSeleccionado = '';

  distrito = '';

  presupuestoMin: number | null = null;

  presupuestoMax: number | null = null;

  compatibilidadMinima = 0;

  ordenSeleccionado = 'compatibilidad';


  /*
   * =========================================================
   * PAGINACIÓN
   * =========================================================
   */

  paginaActual = 0;

  tamanioPagina = 6;

  totalPaginas = 0;

  totalElementos = 0;


  /*
   * =========================================================
   * ESTADOS
   * =========================================================
   */

  cargando = true;

  mensajeError = '';


  constructor(
    private publicacionService: PublicacionRoomieService
  ) {}


  ngOnInit(): void {

    this.cargarPublicaciones();

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
      .listar(
        this.tipoSeleccionado || null,
        this.distrito || null,
        this.presupuestoMin,
        this.presupuestoMax,
        this.paginaActual,
        this.tamanioPagina
      )
      .subscribe({

        next: response => {

          this.cargando = false;


          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =
              response.message ||
              'No se pudieron cargar las publicaciones';

            return;
          }


          this.publicaciones =
            response.data.content ?? [];

          this.totalPaginas =
            response.data.totalPages ?? 0;

          this.totalElementos =
            response.data.totalElements ?? 0;


          this.aplicarFiltrosLocales();

        },


        error: error => {

          this.cargando = false;

          this.publicaciones = [];

          this.publicacionesVisibles = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las publicaciones Roomie';

        }

      });

  }


  /*
   * =========================================================
   * BUSCAR
   * =========================================================
   */

  buscar(): void {

    this.paginaActual = 0;

    this.cargarPublicaciones();

  }


  /*
   * =========================================================
   * CAMBIAR TIPO
   * =========================================================
   */

  cambiarTipo(
    tipo: string
  ): void {

    this.tipoSeleccionado = tipo;

    this.paginaActual = 0;

    this.cargarPublicaciones();

  }


  /*
   * =========================================================
   * FILTROS LOCALES
   * =========================================================
   */

  aplicarFiltrosLocales(): void {

    let resultado = [
      ...this.publicaciones
    ];


    /*
     * COMPATIBILIDAD MÍNIMA
     */

    if (
      this.compatibilidadMinima > 0
    ) {

      resultado = resultado.filter(
        publicacion =>

          publicacion.porcentajeCompatibilidad !== null

          &&

          publicacion.porcentajeCompatibilidad >=
          this.compatibilidadMinima
      );

    }


    /*
     * ORDENAMIENTO
     */

    if (
      this.ordenSeleccionado === 'compatibilidad'
    ) {

      resultado.sort(
        (
          publicacionA,
          publicacionB
        ) => {

          const porcentajeA =
            publicacionA.porcentajeCompatibilidad ?? -1;

          const porcentajeB =
            publicacionB.porcentajeCompatibilidad ?? -1;


          return porcentajeB - porcentajeA;

        }
      );

    }


    if (
      this.ordenSeleccionado === 'recientes'
    ) {

      resultado.sort(
        (
          publicacionA,
          publicacionB
        ) =>

          new Date(
            publicacionB.fechaPublicacion
          ).getTime()

          -

          new Date(
            publicacionA.fechaPublicacion
          ).getTime()
      );

    }


    if (
      this.ordenSeleccionado === 'presupuesto'
    ) {

      resultado.sort(
        (
          publicacionA,
          publicacionB
        ) => {

          const presupuestoA =
            publicacionA.presupuestoMin ?? 0;

          const presupuestoB =
            publicacionB.presupuestoMin ?? 0;


          return presupuestoA - presupuestoB;

        }
      );

    }


    this.publicacionesVisibles =
      resultado;

  }


  /*
   * =========================================================
   * LIMPIAR FILTROS
   * =========================================================
   */

  limpiarFiltros(): void {

    this.tipoSeleccionado = '';

    this.distrito = '';

    this.presupuestoMin = null;

    this.presupuestoMax = null;

    this.compatibilidadMinima = 0;

    this.ordenSeleccionado =
      'compatibilidad';

    this.paginaActual = 0;


    this.cargarPublicaciones();

  }


  /*
   * =========================================================
   * PAGINACIÓN
   * =========================================================
   */

  paginaAnterior(): void {

    if (
      this.paginaActual <= 0
    ) {

      return;

    }


    this.paginaActual--;

    this.cargarPublicaciones();

  }


  paginaSiguiente(): void {

    if (
      this.paginaActual >=
      this.totalPaginas - 1
    ) {

      return;

    }


    this.paginaActual++;

    this.cargarPublicaciones();

  }


  /*
   * =========================================================
   * INICIALES
   * =========================================================
   */

  obtenerIniciales(
    nombreCompleto: string
  ): string {

    if (
      !nombreCompleto
    ) {

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
    ).toUpperCase();

  }


  /*
   * =========================================================
   * TEXTO TIPO
   * =========================================================
   */

  obtenerTextoTipo(
    tipo: string
  ): string {

    switch (
      tipo
    ) {

      case 'busco_roomie':

        return 'Busco roomie';


      case 'busco_cuarto':

        return 'Busco habitación';


      case 'busco_compartir':

        return 'Busco compartir';


      default:

        return tipo;

    }

  }


  /*
   * =========================================================
   * CLASE COMPATIBILIDAD
   * =========================================================
   */

  obtenerClaseCompatibilidad(
    porcentaje: number | null
  ): string {

    if (
      porcentaje === null
    ) {

      return 'unknown';

    }


    if (
      porcentaje >= 90
    ) {

      return 'excellent';

    }


    if (
      porcentaje >= 75
    ) {

      return 'good';

    }


    if (
      porcentaje >= 50
    ) {

      return 'moderate';

    }


    return 'low';

  }


  /*
   * =========================================================
   * COINCIDENCIAS
   * =========================================================
   */

  obtenerCoincidencias(
    publicacion: PublicacionRoomieResponse
  ): string[] {

    if (
      !publicacion.coincidencias
    ) {

      return [];

    }


    return publicacion.coincidencias
      .split(',')
      .map(
        coincidencia =>
          coincidencia.trim()
      )
      .filter(
        coincidencia =>
          coincidencia.length > 0
      )
      .slice(
        0,
        3
      );

  }


  /*
   * =========================================================
   * PRESUPUESTO
   * =========================================================
   */

  formatearPrecio(
    precio: number | null
  ): string {

    if (
      precio === null
    ) {

      return 'Sin definir';

    }


    return new Intl.NumberFormat(
      'es-PE',
      {
        style: 'currency',
        currency: 'PEN',
        maximumFractionDigits: 0
      }
    ).format(
      precio
    );

  }


  obtenerPresupuesto(
    publicacion: PublicacionRoomieResponse
  ): string {

    if (
      publicacion.presupuestoMin === null

      &&

      publicacion.presupuestoMax === null
    ) {

      return 'Presupuesto por conversar';

    }


    if (
      publicacion.presupuestoMin !== null

      &&

      publicacion.presupuestoMax !== null
    ) {

      return (
        this.formatearPrecio(
          publicacion.presupuestoMin
        )

        +

        ' - '

        +

        this.formatearPrecio(
          publicacion.presupuestoMax
        )
      );

    }


    if (
      publicacion.presupuestoMin !== null
    ) {

      return (
        'Desde '

        +

        this.formatearPrecio(
          publicacion.presupuestoMin
        )
      );

    }


    return (
      'Hasta '

      +

      this.formatearPrecio(
        publicacion.presupuestoMax
      )
    );

  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */

  formatearFecha(
    fecha: string
  ): string {

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
}