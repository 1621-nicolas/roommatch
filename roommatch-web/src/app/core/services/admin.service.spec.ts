import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';

describe('Administrative API integration', () => {
  let service: AdminService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service=TestBed.inject(AdminService); http=TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('sends typed report filters and zero-based pagination', () => {
    service.reportes('habitaciones','pendiente',2,10).subscribe(page => expect(page.content).toEqual([]));
    const req=http.expectOne(r => r.url==='/api/reportes/admin/habitaciones');
    expect(req.request.params.get('estado')).toBe('pendiente');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({status:'success',data:{content:[],totalElements:0,totalPages:0,number:2,size:10}});
  });
  it('maps each moderation action to the corresponding endpoint with its reason', () => {
    for (const action of ['revisado','rechazado','sancionado','restaurado'] as const) {
      service.decidir('usuarios',5,action,'Evidencia revisada').subscribe();
      const endpoint=action==='sancionado'?'sancionar':action==='restaurado'?'restaurar':'revisar';
      const req=http.expectOne(r=>r.url===`/api/reportes/admin/usuarios/5/${endpoint}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({motivo:'Evidencia revisada'});
      expect(req.request.params.get('estado')).toBe(endpoint==='revisar'?action:null);
      req.flush({status:'success',data:{idReporte:5,estado:action}});
    }
  });
  it('rejects malformed list envelopes instead of treating them as empty results', () => {
    let error: Error|undefined;
    service.reportes('usuarios','').subscribe({error:value=>error=value});
    http.expectOne(r=>r.url==='/api/reportes/admin/usuarios').flush({status:'success',data:{content:{value:[]},totalElements:0,totalPages:0,number:0,size:10}});
    expect(error?.message).toContain('formato inesperado');
  });
  it('does not conceal authorization failures', () => {
    service.historial('habitaciones',7).subscribe({error:error=>expect(error.status).toBe(403)});
    http.expectOne(r=>r.url==='/api/reportes/admin/habitaciones/7/historial').flush({message:'No tienes permisos'},{status:403,statusText:'Forbidden'});
  });
});
