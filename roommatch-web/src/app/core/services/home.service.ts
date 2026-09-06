import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';
import { HabitacionResponse } from '../models/habitacion-response';
import { PublicacionRoomieResponse } from '../models/publicacion-roomie-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class HomeService {

  constructor(private http: HttpClient) {}

  listarHabitacionesDestacadas(): Observable<ApiResponse<PageResponse<HabitacionResponse>>> {
    return this.http.get<ApiResponse<PageResponse<HabitacionResponse>>>(
      `${API_BASE_URL}/habitaciones?page=0&size=3`
    );
  }

  listarPublicacionesRoomie(): Observable<ApiResponse<PageResponse<PublicacionRoomieResponse>>> {
    return this.http.get<ApiResponse<PageResponse<PublicacionRoomieResponse>>>(
      `${API_BASE_URL}/publicaciones-roomie?page=0&size=3`
    );
  }
}
