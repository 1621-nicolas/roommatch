import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { UsuarioResponse } from '../models/usuario-response';
import { ActualizarUsuarioRequest } from '../models/actualizar-usuario-request';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private readonly apiUrl = `${API_BASE_URL}/usuarios`;

  constructor(private http: HttpClient) {}

  obtenerMiUsuario(): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.get<ApiResponse<UsuarioResponse>>(`${this.apiUrl}/me`);
  }

  actualizarMiUsuario(
    request: ActualizarUsuarioRequest
  ): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.put<ApiResponse<UsuarioResponse>>(
      `${this.apiUrl}/me`,
      request
    );
  }
}
