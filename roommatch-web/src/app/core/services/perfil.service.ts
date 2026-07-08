import {
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  ApiResponse
} from '../models/api-response';

import {
  PerfilConvivenciaRequest
} from '../models/perfil-convivencia-request';

import {
  PerfilConvivenciaResponse
} from '../models/perfil-convivencia-response';


@Injectable({
  providedIn: 'root'
})
export class PerfilService {

  private readonly apiUrl =
    'http://localhost:8081/api/perfil';


  constructor(
    private http: HttpClient
  ) {}


  /*
   * =========================================================
   * CREAR PERFIL
   * =========================================================
   */

  crearPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<
    ApiResponse<PerfilConvivenciaResponse>
  > {

    return this.http.post<
      ApiResponse<PerfilConvivenciaResponse>
    >(
      this.apiUrl,
      request
    );
  }


  /*
   * =========================================================
   * OBTENER MI PERFIL
   * =========================================================
   */

  obtenerMiPerfil(): Observable<
    ApiResponse<PerfilConvivenciaResponse>
  > {

    return this.http.get<
      ApiResponse<PerfilConvivenciaResponse>
    >(
      `${this.apiUrl}/me`
    );
  }


  /*
   * =========================================================
   * ACTUALIZAR MI PERFIL
   * =========================================================
   */

  actualizarMiPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<
    ApiResponse<PerfilConvivenciaResponse>
  > {

    return this.http.put<
      ApiResponse<PerfilConvivenciaResponse>
    >(
      `${this.apiUrl}/me`,
      request
    );
  }


  /*
   * =========================================================
   * COMPATIBILIDAD CON COMPONENTES EXISTENTES
   * =========================================================
   */

  actualizarPerfil(
    request: PerfilConvivenciaRequest
  ): Observable<
    ApiResponse<PerfilConvivenciaResponse>
  > {

    return this.actualizarMiPerfil(
      request
    );
  }

}