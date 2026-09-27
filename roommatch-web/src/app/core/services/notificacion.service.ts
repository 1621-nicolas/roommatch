import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';
import { NotificacionResponse } from '../models/notificacion-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class NotificacionService {

  private readonly apiUrl = `${API_BASE_URL}/notificaciones`;

  constructor(private http: HttpClient) {}

  listar(
    page: number = 0,
    size: number = 10,
    leido: boolean | null = null
  ): Observable<ApiResponse<PageResponse<NotificacionResponse>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (leido !== null) params = params.set('leido', String(leido));

    return this.http.get<ApiResponse<PageResponse<NotificacionResponse>>>(
      this.apiUrl,
      { params }
    );
  }

  contarNoLeidas(): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/no-leidas/count`);
  }

  marcarComoLeida(
    idNotificacion: number
  ): Observable<ApiResponse<NotificacionResponse>> {
    return this.http.put<ApiResponse<NotificacionResponse>>(
      `${this.apiUrl}/${idNotificacion}/leer`,
      {}
    );
  }

  marcarTodasComoLeidas(): Observable<ApiResponse<number>> {
    return this.http.put<ApiResponse<number>>(
      `${this.apiUrl}/leer-todas`,
      {}
    );
  }
}
