import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { PageResponse } from '../models/page-response';
import { ContactoDesbloqueadoView } from '../models/contacto-desbloqueado-view';
import { requirePage } from '../validation/api-page';

import { ApiResponse } from '../models/api-response';
import { ContactoUsuarioRequest } from '../models/contacto-usuario-request';
import { ContactoUsuarioResponse } from '../models/contacto-usuario-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class ContactoService {

  private readonly apiUrl = `${API_BASE_URL}/contactos`;

  constructor(private http: HttpClient) {}

  listarDesbloqueados(page = 0, size = 20): Observable<PageResponse<ContactoDesbloqueadoView>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<ContactoDesbloqueadoView>>>(`${this.apiUrl}/desbloqueados`, {params})
      .pipe(map(response => {
        const result = requirePage<ContactoDesbloqueadoView>(response);
        if (result.content.some(row => !row || !Number.isSafeInteger(row.idUsuario) || row.idUsuario < 1
            || typeof row.nombreCompleto !== 'string' || (row.contacto !== null && (typeof row.contacto !== 'object' || Array.isArray(row.contacto))))) {
          throw new Error('El servidor devolvió contactos con formato inesperado. Intenta recargar.');
        }
        return result;
      }));
  }

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
