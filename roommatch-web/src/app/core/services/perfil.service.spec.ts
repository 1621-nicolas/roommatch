import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PerfilService } from './perfil.service';

describe('Versioned profile editing', () => {
  it('PATCH sends only description and version without a preliminary GET', () => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(PerfilService).actualizarDescripcion('', 3).subscribe();
    const request = http.expectOne('/api/perfil/me/descripcion');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({descripcionPersonal: '', version: 3});
    request.flush({status: 'success', data: {descripcionPersonal: null, version: 4}});
    http.verify();
  });
});
