import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NotificacionService } from './notificacion.service';

describe('Notification HTTP integration', () => {
  beforeEach(() => TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]}));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('sends false explicitly for unread and omits the filter for all pages', () => {
    const api=TestBed.inject(NotificacionService),http=TestBed.inject(HttpTestingController);
    api.listar(2,10,false).subscribe();
    const unread=http.expectOne(r=>r.url==='/api/notificaciones' && r.params.get('leido')==='false' && r.params.get('page')==='2');
    expect(unread.request.params.get('size')).toBe('10');unread.flush({status:'success',data:{content:[]}});
    api.listar().subscribe();const all=http.expectOne('/api/notificaciones?page=0&size=10');expect(all.request.params.has('leido')).toBe(false);all.flush({status:'success',data:{content:[]}});
  });
  it('uses the authenticated account endpoints without accepting an arbitrary owner ID', () => {
    const api=TestBed.inject(NotificacionService),http=TestBed.inject(HttpTestingController);
    api.contarNoLeidas().subscribe();http.expectOne('/api/notificaciones/no-leidas/count').flush({status:'success',data:13});
    api.marcarTodasComoLeidas().subscribe();const update=http.expectOne('/api/notificaciones/leer-todas');expect(update.request.method).toBe('PUT');expect(update.request.body).toEqual({});update.flush({status:'success',data:13});
  });
});
