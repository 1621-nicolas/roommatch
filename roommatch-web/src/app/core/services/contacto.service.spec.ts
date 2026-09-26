import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ContactoService } from './contacto.service';

describe('ContactoService privacy boundary', () => {
  let service: ContactoService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(ContactoService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('loads one page without requesting arbitrary user IDs or all requests', () => {
    const next = vi.fn(); service.listarDesbloqueados(2, 20).subscribe(next);
    const request = http.expectOne('/api/contactos/desbloqueados?page=2&size=20');
    request.flush({status: 'success', data: {content: [], number: 2, size: 20, totalPages: 3, totalElements: 40}});
    expect(next).toHaveBeenCalledWith(expect.objectContaining({number: 2, content: []}));
    http.expectNone('/api/solicitudes/recibidas');
  });
  it('reports a broken array contract instead of failing later in map', () => {
    const error = vi.fn(); service.listarDesbloqueados().subscribe({error});
    http.expectOne('/api/contactos/desbloqueados?page=0&size=20').flush({status: 'success', data: {content: {}, number: 0, size: 20, totalPages: 1, totalElements: 1}});
    expect(error).toHaveBeenCalledWith(expect.objectContaining({message: expect.stringContaining('formato inesperado')}));
  });
  it('keeps server failures visible rather than converting them into an empty page', () => {
    const error = vi.fn(); service.listarDesbloqueados().subscribe({error});
    http.expectOne('/api/contactos/desbloqueados?page=0&size=20').flush({message: 'No autorizado'}, {status: 403, statusText: 'Forbidden'});
    expect(error).toHaveBeenCalledWith(expect.objectContaining({status: 403}));
  });
});
