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
  finalize
} from 'rxjs/operators';

import {
  HabitacionService
} from '../../core/services/habitacion.service';

import {
  HabitacionResponse
} from '../../core/models/habitacion-response';

import {
  HabitacionFiltro
} from '../../core/models/habitacion-filtro';


@Component({
  selector: 'app-habitaciones',

  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],

  templateUrl: './habitaciones.html',

  styleUrl: './habitaciones.css'
})
export class Habitaciones implements OnInit {

  /*
   * =========================================================
   * DATOS
   * =========================================================
   */
  habitaciones: HabitacionResponse[] = [];


  /*
   * =========================================================
   * FILTROS
   * =========================================================
   */
  filtros: HabitacionFiltro = {

    distrito: '',

    precioMin: null,

    precioMax: null,

    amoblado: '',

    banoPrivado: '',

    permiteMascotas: ''

  };


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
  cargando = false;

  mensajeError = '';


  constructor(
    private habitacionService: HabitacionService
  ) {}


  ngOnInit(): void {

    this.buscarHabitaciones();

  }


  /*
   * =========================================================
   * BUSCAR
   * =========================================================
   */
  buscarHabitaciones(
    pagina: number = 0
  ): void {

    if (this.cargando) {

      return;

    }


    this.mensajeError = '';


    /*
     * Validar precios.
     */
    if (
      this.filtros.precioMin !== null &&
      this.filtros.precioMin < 0
    ) {

      this.mensajeError =
        'El precio mínimo no puede ser negativo';

      return;

    }


    if (
      this.filtros.precioMax !== null &&
      this.filtros.precioMax < 0
    ) {

      this.mensajeError =
        'El precio máximo no puede ser negativo';

      return;

    }


    if (
      this.filtros.precioMin !== null &&
      this.filtros.precioMax !== null &&
      this.filtros.precioMin >
      this.filtros.precioMax
    ) {

      this.mensajeError =
        'El precio mínimo no puede ser mayor al precio máximo';

      return;

    }


    this.cargando = true;


    this.habitacionService
      .buscar(
        this.filtros,
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

            this.habitaciones = [];

            this.totalElementos = 0;

            this.totalPaginas = 0;


            this.mensajeError =
              response.message ||
              'No se pudieron cargar las habitaciones';

            return;

          }


          this.habitaciones =
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
            'Error cargando habitaciones:',
            error
          );


          this.habitaciones = [];

          this.totalElementos = 0;

          this.totalPaginas = 0;


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar las habitaciones';

        }

      });

  }


  /*
   * =========================================================
   * APLICAR FILTROS
   * =========================================================
   */
  aplicarFiltros(): void {

    this.buscarHabitaciones(0);

  }


  /*
   * =========================================================
   * LIMPIAR FILTROS
   * =========================================================
   */
  limpiarFiltros(): void {

    this.filtros = {

      distrito: '',

      precioMin: null,

      precioMax: null,

      amoblado: '',

      banoPrivado: '',

      permiteMascotas: ''

    };


    this.buscarHabitaciones(0);

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
   * FECHA DISPONIBLE
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
   * SERVICIOS INCLUIDOS
   * =========================================================
   */
  contarServiciosIncluidos(
    habitacion: HabitacionResponse
  ): number {

    let total = 0;


    if (
      habitacion.internetIncluido
    ) {

      total++;

    }


    if (
      habitacion.aguaIncluida
    ) {

      total++;

    }


    if (
      habitacion.luzIncluida
    ) {

      total++;

    }


    return total;

  }


  /*
   * =========================================================
   * PÁGINA ANTERIOR
   * =========================================================
   */
  paginaAnterior(): void {

    if (
      this.paginaActual <= 0
    ) {

      return;

    }


    this.buscarHabitaciones(
      this.paginaActual - 1
    );

  }


  /*
   * =========================================================
   * PÁGINA SIGUIENTE
   * =========================================================
   */
  paginaSiguiente(): void {

    if (
      this.paginaActual >=
      this.totalPaginas - 1
    ) {

      return;

    }


    this.buscarHabitaciones(
      this.paginaActual + 1
    );

  }

}