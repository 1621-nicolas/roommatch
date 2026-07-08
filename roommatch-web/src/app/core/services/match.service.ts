import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';
import { MatchResponse } from '../models/match-response';

@Injectable({
  providedIn: 'root'
})
export class MatchService {

  private readonly apiUrl =
    'http://localhost:8081/api/matches';

  constructor(
    private http: HttpClient
  ) {}

  calcularMatches():
    Observable<ApiResponse<MatchResponse[]>> {

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

    let url =
      `${this.apiUrl}?page=${page}&size=${size}`;

    if (porcentajeMinimo !== null) {
      url +=
        `&porcentajeMinimo=${porcentajeMinimo}`;
    }

    return this.http.get<
      ApiResponse<PageResponse<MatchResponse>>
    >(url);
  }
}