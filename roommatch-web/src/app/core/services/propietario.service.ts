import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PropietarioRequest } from '../models/propietario-request';
import { PropietarioResponse } from '../models/propietario-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class PropietarioService {

  private readonly apiUrl = `${API_BASE_URL}/propietarios`;

  constructor(private http: HttpClient) {}

  convertirmeEnPropietario(
    request: PropietarioRequest
  ): Observable<ApiResponse<PropietarioResponse>> {
    return this.http.post<ApiResponse<PropietarioResponse>>(
      `${this.apiUrl}/me`,
      request
    );
  }

  obtenerMiPerfil(): Observable<ApiResponse<PropietarioResponse>> {
    return this.http.get<ApiResponse<PropietarioResponse>>(`${this.apiUrl}/me`);
  }
}
