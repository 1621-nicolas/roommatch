import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { Reportes } from './reportes';
import { AdminService } from '../../../core/services/admin.service';
import { AdminReport } from '../../../core/models/admin';

const row: AdminReport={idReporte:7,idUsuarioReportante:1,nombreReportante:'Reportante de prueba',idUsuarioReportado:2,nombreReportado:'Usuario de prueba',motivo:'Incidencia de prueba',descripcion:'Descripción',estado:'pendiente',estadoObjetivo:'activo',fechaReporte:'2026-09-14T10:00:00',fechaRevision:null};
const page=(content: AdminReport[])=>({content,totalElements:content.length,totalPages:content.length?1:0,number:0,size:10,first:true,last:true,numberOfElements:content.length,empty:!content.length});

describe('Report review workflow', () => {
  function create(overrides: Record<string,unknown>={}) {
    const api={reportes:vi.fn().mockReturnValue(of(page([row]))),historial:vi.fn().mockReturnValue(of(page([]))),decidir:vi.fn(),...overrides};
    TestBed.configureTestingModule({imports:[Reportes],providers:[provideRouter([]),{provide:AdminService,useValue:api}]});
    const fixture=TestBed.createComponent(Reportes);fixture.detectChanges();
    const dialog=fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
    Object.defineProperty(dialog,'showModal',{configurable:true,value:vi.fn(()=>dialog.open=true)});
    Object.defineProperty(dialog,'close',{configurable:true,value:vi.fn(()=>dialog.open=false)});
    return {fixture,component:fixture.componentInstance,api,dialog};
  }
  it('shows real returned rows, detail and empty historical decisions', () => {
    const {fixture,component}=create();
    expect(fixture.nativeElement.textContent).toContain('Incidencia de prueba');
    component.select(row);fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Historial de decisiones');
    expect(fixture.nativeElement.textContent).toContain('No hay decisiones registradas');
  });
  it('requires confirmation and preserves a failed decision with its reason', () => {
    const decidir=vi.fn().mockReturnValue(throwError(()=>({status:409,error:{message:'El reporte ya fue resuelto'}})));
    const {component,dialog}=create({decidir});
    component.select(row);component.prepare('sancionado');
    component.confirm();expect(decidir).not.toHaveBeenCalled();
    component.motive='Evidencia revisada';component.confirm();
    expect(decidir).toHaveBeenCalledWith('usuarios',7,'sancionado','Evidencia revisada');
    expect(component.actionError()).toContain('ya fue resuelto');
    expect(component.motive).toBe('Evidencia revisada');
    expect(dialog.open).toBe(true);
    expect(component.saving()).toBe(false);
  });
  it('closes the confirmation only after a successful decision and reloads data', () => {
    const decidir=vi.fn().mockReturnValue(of({...row,estado:'rechazado'}));
    const {component,api,dialog}=create({decidir});
    component.select(row);component.prepare('rechazado');component.motive='No se confirma la incidencia';component.confirm();
    expect(dialog.open).toBe(false);expect(component.success()).toContain('Decisión guardada');
    expect(api.reportes).toHaveBeenCalledTimes(2);
  });
  it('cancels stale list requests when changing the report type', () => {
    const pending=new Subject<ReturnType<typeof page>>();
    const reportes=vi.fn().mockReturnValueOnce(pending).mockReturnValue(of(page([])));
    const {component,fixture}=create({reportes});
    component.changeKind('habitaciones');pending.next(page([row]));fixture.detectChanges();
    expect(component.kind()).toBe('habitaciones');
    expect(component.page()?.content).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('No hay reportes con este filtro');
  });
});
