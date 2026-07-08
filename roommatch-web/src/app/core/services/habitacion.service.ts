import {
  Injectable
} from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  ApiResponse
} from '../models/api-response';

import {
  PageResponse
} from '../models/page-response';

import {
  HabitacionResponse
} from '../models/habitacion-response';

import {
  HabitacionFiltro
} from '../models/habitacion-filtro';


export interface HabitacionPayload {

  titulo: string;

  descripcion: string;

  distrito: string;

  direccionReferencial: string | null;

  precio: number;

  areaM2: number | null;

  amoblado: boolean;

  banoPrivado: boolean;

  internetIncluido: boolean;

  aguaIncluida: boolean;

  luzIncluida: boolean;

  permiteMascotas: boolean;

  disponibleDesde: string | null;

  destacada: boolean;
}


@Injectable({
  providedIn: 'root'
})
export class HabitacionService {

  private readonly apiUrl =
    'http://localhost:8081/api/habitaciones';


  constructor(
    private http: HttpClient
  ) {}


  /*
   * =========================================================
   * LISTAR MIS HABITACIONES
   * =========================================================
   */

  listarMisHabitaciones(
    page: number = 0,
    size: number = 100
  ): Observable<
    ApiResponse<
      PageResponse<HabitacionResponse>
    >
  > {

    const params =
      new HttpParams()
        .set(
          'page',
          page.toString()
        )
        .set(
          'size',
          size.toString()
        );


    return this.http.get<
      ApiResponse<
        PageResponse<HabitacionResponse>
      >
    >(
      `${this.apiUrl}/mis`,
      {
        params
      }
    );
  }


  /*
   * =========================================================
   * CREAR HABITACIÓN
   * =========================================================
   */

  crear(
    request: HabitacionPayload
  ): Observable<
    ApiResponse<HabitacionResponse>
  > {

    return this.http.post<
      ApiResponse<HabitacionResponse>
    >(
      this.apiUrl,
      request
    );
  }


  /*
   * =========================================================
   * ACTUALIZAR HABITACIÓN
   * =========================================================
   */

  actualizar(
    idHabitacion: number,
    request: HabitacionPayload
  ): Observable<
    ApiResponse<HabitacionResponse>
  > {

    return this.http.put<
      ApiResponse<HabitacionResponse>
    >(
      `${this.apiUrl}/${idHabitacion}`,
      request
    );
  }


  /*
   * =========================================================
   * PAUSAR HABITACIÓN
   * =========================================================
   */

  pausar(
    idHabitacion: number
  ): Observable<
    ApiResponse<HabitacionResponse>
  > {

    return this.http.put<
      ApiResponse<HabitacionResponse>
    >(
      `${this.apiUrl}/${idHabitacion}/pausar`,
      {}
    );
  }


  /*
   * =========================================================
   * ACTIVAR HABITACIÓN
   * =========================================================
   */

  activar(
    idHabitacion: number
  ): Observable<
    ApiResponse<HabitacionResponse>
  > {

    return this.http.put<
      ApiResponse<HabitacionResponse>
    >(
      `${this.apiUrl}/${idHabitacion}/activar`,
      {}
    );
  }


  /*
   * =========================================================
   * BUSCAR HABITACIONES
   * =========================================================
   */

  buscar(
    filtros: HabitacionFiltro,
    page: number = 0,
    size: number = 6
  ): Observable<
    ApiResponse<
      PageResponse<HabitacionResponse>
    >
  > {

    let params =
      new HttpParams()
        .set(
          'page',
          page.toString()
        )
        .set(
          'size',
          size.toString()
        );


    if (
      filtros.distrito.trim()
    ) {

      params = params.set(
        'distrito',
        filtros.distrito.trim()
      );
    }


    if (
      filtros.precioMin !== null
    ) {

      params = params.set(
        'precioMin',
        filtros.precioMin.toString()
      );
    }


    if (
      filtros.precioMax !== null
    ) {

      params = params.set(
        'precioMax',
        filtros.precioMax.toString()
      );
    }


    if (
      filtros.amoblado !== ''
    ) {

      params = params.set(
        'amoblado',
        filtros.amoblado
      );
    }


    if (
      filtros.banoPrivado !== ''
    ) {

      params = params.set(
        'banoPrivado',
        filtros.banoPrivado
      );
    }


    if (
      filtros.permiteMascotas !== ''
    ) {

      params = params.set(
        'permiteMascotas',
        filtros.permiteMascotas
      );
    }


    return this.http.get<
      ApiResponse<
        PageResponse<HabitacionResponse>
      >
    >(
      this.apiUrl,
      {
        params
      }
    );
  }


  /*
   * =========================================================
   * OBTENER HABITACIÓN POR ID
   * =========================================================
   */

  obtenerPorId(
    idHabitacion: number
  ): Observable<
    ApiResponse<HabitacionResponse>
  > {

    return this.http.get<
      ApiResponse<HabitacionResponse>
    >(
      `${this.apiUrl}/${idHabitacion}`
    );
  }


  /*
   * =========================================================
   * LISTAR DISPONIBLES
   * =========================================================
   */

  listarDisponibles(
    page: number = 0,
    size: number = 50
  ): Observable<
    ApiResponse<
      PageResponse<HabitacionResponse>
    >
  > {

    const params =
      new HttpParams()
        .set(
          'page',
          page.toString()
        )
        .set(
          'size',
          size.toString()
        );


    return this.http.get<
      ApiResponse<
        PageResponse<HabitacionResponse>
      >
    >(
      this.apiUrl,
      {
        params
      }
    );
  }
}