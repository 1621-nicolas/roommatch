import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { SolicitudContactoRequest } from '../models/solicitud-contacto-request';
import { SolicitudContactoResponse } from '../models/solicitud-contacto-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class SolicitudService {

  private readonly apiUrl = `${API_BASE_URL}/solicitudes`;

  constructor(private http: HttpClient) {}

  enviarSolicitud(
    idUsuarioReceptor: number,
    request: SolicitudContactoRequest | string
  ): Observable<ApiResponse<SolicitudContactoResponse>> {
    const body: SolicitudContactoRequest =
      typeof request === 'string' ? { mensaje: request } : request;

    return this.http.post<ApiResponse<SolicitudContactoResponse>>(
      `${this.apiUrl}/${idUsuarioReceptor}`,
      body
    );
  }

  listarRecibidas(): Observable<ApiResponse<SolicitudContactoResponse[]>> {
    return this.http.get<ApiResponse<SolicitudContactoResponse[]>>(
      `${this.apiUrl}/recibidas`
    );
  }

  listarEnviadas(): Observable<ApiResponse<SolicitudContactoResponse[]>> {
    return this.http.get<ApiResponse<SolicitudContactoResponse[]>>(
      `${this.apiUrl}/enviadas`
    );
  }

  aceptarSolicitud(
    idSolicitud: number
  ): Observable<ApiResponse<SolicitudContactoResponse>> {
    return this.http.put<ApiResponse<SolicitudContactoResponse>>(
      `${this.apiUrl}/${idSolicitud}/aceptar`,
      {}
    );
  }

  rechazarSolicitud(
    idSolicitud: number
  ): Observable<ApiResponse<SolicitudContactoResponse>> {
    return this.http.put<ApiResponse<SolicitudContactoResponse>>(
      `${this.apiUrl}/${idSolicitud}/rechazar`,
      {}
    );
  }
}
