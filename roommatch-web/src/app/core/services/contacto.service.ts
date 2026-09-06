import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { ContactoUsuarioRequest } from '../models/contacto-usuario-request';
import { ContactoUsuarioResponse } from '../models/contacto-usuario-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class ContactoService {

  private readonly apiUrl = `${API_BASE_URL}/contactos`;

  constructor(private http: HttpClient) {}

  obtenerMiContacto(): Observable<ApiResponse<ContactoUsuarioResponse>> {
    return this.http.get<ApiResponse<ContactoUsuarioResponse>>(`${this.apiUrl}/me`);
  }

  crearMiContacto(
    request: ContactoUsuarioRequest
  ): Observable<ApiResponse<ContactoUsuarioResponse>> {
    return this.http.post<ApiResponse<ContactoUsuarioResponse>>(
      `${this.apiUrl}/me`,
      request
    );
  }

  actualizarMiContacto(
    request: ContactoUsuarioRequest
  ): Observable<ApiResponse<ContactoUsuarioResponse>> {
    return this.http.put<ApiResponse<ContactoUsuarioResponse>>(
      `${this.apiUrl}/me`,
      request
    );
  }

  obtenerContactoDesbloqueado(
    idUsuario: number
  ): Observable<ApiResponse<ContactoUsuarioResponse>> {
    return this.http.get<ApiResponse<ContactoUsuarioResponse>>(
      `${this.apiUrl}/desbloqueado/${idUsuario}`
    );
  }
}
