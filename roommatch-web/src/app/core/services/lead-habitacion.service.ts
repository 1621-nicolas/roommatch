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
  LeadHabitacionRequest
} from '../models/lead-habitacion-request';

import {
  LeadHabitacionResponse
} from '../models/lead-habitacion-response';

import {
  PageResponse
} from '../models/page-response';


@Injectable({
  providedIn: 'root'
})
export class LeadHabitacionService {

  private readonly apiUrl =
    'http://localhost:8081/api/leads';


  constructor(
    private http: HttpClient
  ) {}


  /*
   * =========================================================
   * ENVIAR INTERÉS
   * =========================================================
   */

  crearLead(
    idHabitacion: number,
    request: LeadHabitacionRequest
  ): Observable<
    ApiResponse<LeadHabitacionResponse>
  > {

    return this.http.post<
      ApiResponse<LeadHabitacionResponse>
    >(
      `${this.apiUrl}/habitacion/${idHabitacion}`,
      request
    );
  }


  /*
   * =========================================================
   * MIS INTERESES EN HABITACIONES
   * =========================================================
   */

  listarMisIntereses(): Observable<
    ApiResponse<LeadHabitacionResponse[]>
  > {

    return this.http.get<
      ApiResponse<LeadHabitacionResponse[]>
    >(
      `${this.apiUrl}/mis`
    );
  }


  /*
   * =========================================================
   * LISTAR LEADS DEL PROPIETARIO
   * =========================================================
   */

  listarLeadsPropietario(
    estado: string | null = null,
    page: number = 0,
    size: number = 100
  ): Observable<
    ApiResponse<
      PageResponse<LeadHabitacionResponse>
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
      estado &&
      estado.trim() !== ''
    ) {

      params = params.set(
        'estado',
        estado.trim()
      );
    }


    return this.http.get<
      ApiResponse<
        PageResponse<LeadHabitacionResponse>
      >
    >(
      `${this.apiUrl}/propietario`,
      {
        params
      }
    );
  }


  /*
   * =========================================================
   * ACTUALIZAR ESTADO DEL LEAD
   * =========================================================
   */

  actualizarEstado(
    idLead: number,
    estado: string
  ): Observable<
    ApiResponse<LeadHabitacionResponse>
  > {

    const params =
      new HttpParams()
        .set(
          'estado',
          estado
        );


    return this.http.put<
      ApiResponse<LeadHabitacionResponse>
    >(
      `${this.apiUrl}/${idLead}/estado`,
      {},
      {
        params
      }
    );
  }
}