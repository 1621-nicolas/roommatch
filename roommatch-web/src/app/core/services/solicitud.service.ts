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
  SolicitudContactoRequest
} from '../models/solicitud-contacto-request';

import {
  SolicitudContactoResponse
} from '../models/solicitud-contacto-response';


@Injectable({
  providedIn: 'root'
})
export class SolicitudService {

  private readonly apiUrl =
    'http://localhost:8081/api/solicitudes';


  constructor(
    private http: HttpClient
  ) {}


  /*
   * =========================================================
   * ENVIAR SOLICITUD DE CONTACTO
   * =========================================================
   */

  enviarSolicitud(
    idUsuarioReceptor: number,
    request: SolicitudContactoRequest | string
  ): Observable<
    ApiResponse<SolicitudContactoResponse>
  > {

    const body: SolicitudContactoRequest =

      typeof request === 'string'

        ? {
            mensaje: request
          }

        : request;


    return this.http.post<
      ApiResponse<SolicitudContactoResponse>
    >(
      `${this.apiUrl}/${idUsuarioReceptor}`,
      body
    );
  }


  /*
   * =========================================================
   * LISTAR SOLICITUDES RECIBIDAS
   * =========================================================
   */

  listarRecibidas(): Observable<
    ApiResponse<SolicitudContactoResponse[]>
  > {

    return this.http.get<
      ApiResponse<SolicitudContactoResponse[]>
    >(
      `${this.apiUrl}/recibidas`
    );
  }


  /*
   * =========================================================
   * LISTAR SOLICITUDES ENVIADAS
   * =========================================================
   */

  listarEnviadas(): Observable<
    ApiResponse<SolicitudContactoResponse[]>
  > {

    return this.http.get<
      ApiResponse<SolicitudContactoResponse[]>
    >(
      `${this.apiUrl}/enviadas`
    );
  }


  /*
   * =========================================================
   * ACEPTAR SOLICITUD
   * =========================================================
   */

  aceptarSolicitud(
    idSolicitud: number
  ): Observable<
    ApiResponse<SolicitudContactoResponse>
  > {

    return this.http.put<
      ApiResponse<SolicitudContactoResponse>
    >(
      `${this.apiUrl}/${idSolicitud}/aceptar`,
      {}
    );
  }


  /*
   * =========================================================
   * RECHAZAR SOLICITUD
   * =========================================================
   */

  rechazarSolicitud(
    idSolicitud: number
  ): Observable<
    ApiResponse<SolicitudContactoResponse>
  > {

    return this.http.put<
      ApiResponse<SolicitudContactoResponse>
    >(
      `${this.apiUrl}/${idSolicitud}/rechazar`,
      {}
    );
  }

}