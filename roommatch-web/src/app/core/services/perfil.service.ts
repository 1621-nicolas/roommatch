import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PerfilConvivenciaRequest } from '../models/perfil-convivencia-request';
import { PerfilConvivenciaResponse } from '../models/perfil-convivencia-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class PerfilService {

  private readonly apiUrl = `${API_BASE_URL}/perfil`;

  constructor(private http: HttpClient) {}

  crearPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<ApiResponse<PerfilConvivenciaResponse>> {
    return this.http.post<ApiResponse<PerfilConvivenciaResponse>>(
      this.apiUrl,
      request
    );
  }

  obtenerMiPerfil(): Observable<ApiResponse<PerfilConvivenciaResponse>> {
    return this.http.get<ApiResponse<PerfilConvivenciaResponse>>(
      `${this.apiUrl}/me`
    );
  }

  actualizarMiPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<ApiResponse<PerfilConvivenciaResponse>> {
    return this.http.put<ApiResponse<PerfilConvivenciaResponse>>(
      `${this.apiUrl}/me`,
      request
    );
  }

  actualizarPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<ApiResponse<PerfilConvivenciaResponse>> {
    return this.actualizarMiPerfil(request);
  }
}
