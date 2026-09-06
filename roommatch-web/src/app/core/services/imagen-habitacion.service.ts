import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { ImagenHabitacionRequest } from '../models/imagen-habitacion-request';
import { ImagenHabitacionResponse } from '../models/imagen-habitacion-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class ImagenHabitacionService {

  private readonly apiUrl = `${API_BASE_URL}/imagenes-habitacion`;

  constructor(private http: HttpClient) {}

  agregarImagen(
    idHabitacion: number,
    request: ImagenHabitacionRequest
  ): Observable<ApiResponse<ImagenHabitacionResponse>> {
    return this.http.post<ApiResponse<ImagenHabitacionResponse>>(
      `${this.apiUrl}/habitacion/${idHabitacion}`,
      request
    );
  }

  listarImagenes(
    idHabitacion: number
  ): Observable<ApiResponse<ImagenHabitacionResponse[]>> {
    return this.http.get<ApiResponse<ImagenHabitacionResponse[]>>(
      `${this.apiUrl}/habitacion/${idHabitacion}`
    );
  }

  marcarComoPrincipal(
    idImagen: number
  ): Observable<ApiResponse<ImagenHabitacionResponse>> {
    return this.http.put<ApiResponse<ImagenHabitacionResponse>>(
      `${this.apiUrl}/${idImagen}/principal`,
      {}
    );
  }

  eliminarImagen(idImagen: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${idImagen}`);
  }
}
