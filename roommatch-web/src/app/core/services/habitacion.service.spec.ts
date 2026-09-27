import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HabitacionService } from './habitacion.service';

describe('HabitacionService owner actions', () => {
  let service: HabitacionService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(HabitacionService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('marks a room rented using the state transition endpoint', () => {
    service.alquilar(7).subscribe(response => expect(response.data.estado).toBe('alquilada'));
    const request = http.expectOne(req => req.url.endsWith('/habitaciones/7/alquilar'));
    expect(request.request.method).toBe('PUT');
    request.flush({status: 'success', data: {estado: 'alquilada'}});
  });

  it('surfaces an ownership failure instead of removing a card optimistically', () => {
    service.archivar(9).subscribe({error: error => expect(error.status).toBe(404)});
    const request = http.expectOne(req => req.url.endsWith('/habitaciones/9'));
    expect(request.request.method).toBe('DELETE');
    request.flush({message: 'Habitación no encontrada o no te pertenece'}, {status: 404, statusText: 'Not Found'});
  });
});
