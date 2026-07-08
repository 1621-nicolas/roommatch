import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { FavoritoResponse } from '../models/favorito-response';

@Injectable({
  providedIn: 'root'
})
export class FavoritoService {

  private readonly apiUrl =
    'http://localhost:8081/api/favoritos';

  constructor(
    private http: HttpClient
  ) {}

  listarFavoritos():
    Observable<ApiResponse<FavoritoResponse[]>> {

    return this.http.get<
      ApiResponse<FavoritoResponse[]>
    >(
      this.apiUrl
    );
  }

  agregarFavorito(
    idUsuarioFavorito: number
  ): Observable<ApiResponse<unknown>> {

    return this.http.post<ApiResponse<unknown>>(
      `${this.apiUrl}/${idUsuarioFavorito}`,
      {}
    );
  }

  eliminarFavorito(
    idUsuarioFavorito: number
  ): Observable<ApiResponse<unknown>> {

    return this.http.delete<ApiResponse<unknown>>(
      `${this.apiUrl}/${idUsuarioFavorito}`
    );
  }
}