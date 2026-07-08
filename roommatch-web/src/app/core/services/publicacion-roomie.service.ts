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
  PublicacionRoomieRequest
} from '../models/publicacion-roomie-request';

import {
  PublicacionRoomieResponse
} from '../models/publicacion-roomie-response';


@Injectable({
  providedIn: 'root'
})
export class PublicacionRoomieService {


  private readonly apiUrl =
    'http://localhost:8081/api/publicaciones-roomie';


  constructor(
    private http: HttpClient
  ) {}


  /*
   * =========================================================
   * LISTAR PUBLICACIONES PÚBLICAS
   * =========================================================
   */

  listar(
    tipo: string | null,
    distrito: string | null,
    presupuestoMin: number | null,
    presupuestoMax: number | null,
    page: number,
    size: number
  ): Observable<
    ApiResponse<
      PageResponse<PublicacionRoomieResponse>
    >
  > {

    let params = new HttpParams()
      .set(
        'page',
        page.toString()
      )
      .set(
        'size',
        size.toString()
      );


    if (
      tipo &&
      tipo.trim() !== ''
    ) {

      params = params.set(
        'tipo',
        tipo.trim()
      );

    }


    if (
      distrito &&
      distrito.trim() !== ''
    ) {

      params = params.set(
        'distrito',
        distrito.trim()
      );

    }


    if (
      presupuestoMin !== null
    ) {

      params = params.set(
        'presupuestoMin',
        presupuestoMin.toString()
      );

    }


    if (
      presupuestoMax !== null
    ) {

      params = params.set(
        'presupuestoMax',
        presupuestoMax.toString()
      );

    }


    return this.http.get<
      ApiResponse<
        PageResponse<PublicacionRoomieResponse>
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
   * OBTENER POR ID
   * =========================================================
   */

  obtenerPorId(
    idPublicacion: number
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.get<
      ApiResponse<PublicacionRoomieResponse>
    >(
      `${this.apiUrl}/${idPublicacion}`
    );

  }


  /*
   * =========================================================
   * MIS PUBLICACIONES
   * =========================================================
   */

  listarMisPublicaciones(
    page: number,
    size: number
  ): Observable<
    ApiResponse<
      PageResponse<PublicacionRoomieResponse>
    >
  > {

    const params = new HttpParams()
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
        PageResponse<PublicacionRoomieResponse>
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
   * CREAR
   * =========================================================
   */

  crearPublicacion(
    request: PublicacionRoomieRequest
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.post<
      ApiResponse<PublicacionRoomieResponse>
    >(
      this.apiUrl,
      request
    );

  }


  /*
   * =========================================================
   * ACTUALIZAR
   * =========================================================
   */

  actualizarPublicacion(
    idPublicacion: number,
    request: PublicacionRoomieRequest
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.put<
      ApiResponse<PublicacionRoomieResponse>
    >(
      `${this.apiUrl}/${idPublicacion}`,
      request
    );

  }


  /*
   * =========================================================
   * PAUSAR
   * =========================================================
   */

  pausarPublicacion(
    idPublicacion: number
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.put<
      ApiResponse<PublicacionRoomieResponse>
    >(
      `${this.apiUrl}/${idPublicacion}/pausar`,
      {}
    );

  }


  /*
   * =========================================================
   * ACTIVAR
   * =========================================================
   */

  activarPublicacion(
    idPublicacion: number
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.put<
      ApiResponse<PublicacionRoomieResponse>
    >(
      `${this.apiUrl}/${idPublicacion}/activar`,
      {}
    );

  }


  /*
   * =========================================================
   * CERRAR
   * =========================================================
   */

  cerrarPublicacion(
    idPublicacion: number
  ): Observable<
    ApiResponse<PublicacionRoomieResponse>
  > {

    return this.http.put<
      ApiResponse<PublicacionRoomieResponse>
    >(
      `${this.apiUrl}/${idPublicacion}/cerrar`,
      {}
    );

  }


  /*
   * =========================================================
   * ELIMINAR
   * =========================================================
   */

  eliminarPublicacion(
    idPublicacion: number
  ): Observable<
    ApiResponse<void>
  > {

    return this.http.delete<
      ApiResponse<void>
    >(
      `${this.apiUrl}/${idPublicacion}`
    );

  }

}