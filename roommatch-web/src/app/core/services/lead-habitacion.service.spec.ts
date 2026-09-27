import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LeadHabitacionService } from './lead-habitacion.service';

describe('Inquiry contact selection', () => {
  it('sends only the contact address explicitly included in the form', () => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    const service = TestBed.inject(LeadHabitacionService);
    const http = TestBed.inject(HttpTestingController);
    for (const emailContacto of [null, 'chosen@example.test']) {
      service.crearLead(4, {mensaje: 'Me interesa visitar', emailContacto}).subscribe();
      const request = http.expectOne('/api/leads/habitacion/4');
      expect(request.request.body).toEqual({mensaje: 'Me interesa visitar', emailContacto});
      request.flush({status: 'success', data: {emailInteresado: emailContacto}});
    }
    http.verify();
  });
});
