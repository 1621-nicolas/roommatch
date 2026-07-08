import {
  Injectable
} from '@angular/core';

import {
  HttpClient
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
  NotificacionResponse
} from '../models/notificacion-response';


@Injectable({
  providedIn: 'root'
})
export class NotificacionService {

  private readonly apiUrl =
    'http://localhost:8081/api/notificaciones';


  constructor(
    private http: HttpClient
  ) {}


  listar(
    page: number = 0,
    size: number = 10
  ): Observable<
    ApiResponse<
      PageResponse<NotificacionResponse>
    >
  > {

    return this.http.get<
      ApiResponse<
        PageResponse<NotificacionResponse>
      >
    >(
      `${this.apiUrl}?page=${page}&size=${size}`
    );

  }


  marcarComoLeida(
    idNotificacion: number
  ): Observable<
    ApiResponse<NotificacionResponse>
  > {

    return this.http.put<
      ApiResponse<NotificacionResponse>
    >(
      `${this.apiUrl}/${idNotificacion}/leer`,
      null
    );

  }


  marcarTodasComoLeidas():
    Observable<ApiResponse<number>> {

    return this.http.put<
      ApiResponse<number>
    >(
      `${this.apiUrl}/leer-todas`,
      null
    );

  }

}