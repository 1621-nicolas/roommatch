import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SolicitudService } from './solicitud.service';
import { API_BASE_URL } from '../config/api.config';

describe('SolicitudService', () => {
  let service: SolicitudService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(SolicitudService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('cancels only the selected request with the backend contract', () => {
    service.cancelarSolicitud(10).subscribe(response => expect(response.data.estado).toBe('cancelada'));
    const request = http.expectOne(`${API_BASE_URL}/solicitudes/10/cancelar`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({});
    request.flush({status: 'success', data: {idSolicitud: 10, estado: 'cancelada'}});
  });

  it('propagates a conflict so the UI can explain the retry window', () => {
    service.enviarSolicitud(2, {mensaje: 'Hola'}).subscribe({error: error => expect(error.status).toBe(409)});
    const request = http.expectOne(`${API_BASE_URL}/solicitudes/2`);
    expect(request.request.body).toEqual({mensaje: 'Hola'});
    request.flush({message: 'Podrás volver a enviar una solicitud desde la fecha indicada'}, {status: 409, statusText: 'Conflict'});
  });
});
