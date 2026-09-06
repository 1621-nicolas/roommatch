import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PlanPropietarioResponse } from '../models/plan-propietario-response';
import { MiPlanResponse } from '../models/mi-plan-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class PlanPropietarioService {

  private readonly apiUrl = `${API_BASE_URL}/planes`;

  constructor(private http: HttpClient) {}

  listarPlanes(): Observable<ApiResponse<PlanPropietarioResponse[]>> {
    return this.http.get<ApiResponse<PlanPropietarioResponse[]>>(this.apiUrl);
  }

  obtenerMiPlan(): Observable<ApiResponse<MiPlanResponse>> {
    return this.http.get<ApiResponse<MiPlanResponse>>(`${this.apiUrl}/mi-plan`);
  }

  cambiarPlan(idPlan: number): Observable<ApiResponse<MiPlanResponse>> {
    return this.http.put<ApiResponse<MiPlanResponse>>(
      `${this.apiUrl}/cambiar/${idPlan}`,
      {}
    );
  }
}
