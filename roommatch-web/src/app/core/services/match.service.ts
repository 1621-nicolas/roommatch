import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';
import { MatchResponse } from '../models/match-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class MatchService {

  private readonly apiUrl = `${API_BASE_URL}/matches`;

  constructor(private http: HttpClient) {}

  calcularMatches(): Observable<ApiResponse<MatchResponse[]>> {
    return this.http.post<ApiResponse<MatchResponse[]>>(
      `${this.apiUrl}/calcular`,
      {}
    );
  }

  listarMatches(
    porcentajeMinimo: number | null,
    page: number,
    size: number
  ): Observable<ApiResponse<PageResponse<MatchResponse>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (porcentajeMinimo !== null) {
      params = params.set('porcentajeMinimo', porcentajeMinimo.toString());
    }

    return this.http.get<ApiResponse<PageResponse<MatchResponse>>>(
      this.apiUrl,
      { params }
    );
  }
}
