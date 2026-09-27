import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { Notificaciones } from './notificaciones';
import { NotificacionService } from '../../core/services/notificacion.service';
import { NotificacionResponse } from '../../core/models/notificacion-response';

const row: NotificacionResponse = {idNotificacion:1,idUsuario:7,titulo:'Solicitud recibida',mensaje:'Mensaje',tipo:'solicitud',leido:false,urlDestino:'/solicitudes',fechaCreacion:'2026-09-27T06:00:00'};
const ok = <T>(data: T) => ({status:'success',message:'ok',data});
const page = (content: NotificacionResponse[], number=0, total=content.length) => ok({content,number,size:10,totalElements:total,totalPages:Math.ceil(total/10)});

describe('Notification server filters and recovery', () => {
  function setup() {
    const api = {listar:vi.fn().mockReturnValue(of(page([row]))), contarNoLeidas:vi.fn().mockReturnValue(of(ok(13))),
      marcarComoLeida:vi.fn().mockReturnValue(of(ok({...row,leido:true}))),marcarTodasComoLeidas:vi.fn().mockReturnValue(of(ok(13)))};
    TestBed.configureTestingModule({imports:[Notificaciones],providers:[provideRouter([]),{provide:NotificacionService,useValue:api}]});
    const fixture=TestBed.createComponent(Notificaciones);
    return {fixture,component:fixture.componentInstance,api};
  }
  it('counts unread items outside the current page and applies the unread filter on the server', () => {
    const {fixture,component,api}=setup();
    api.listar.mockReturnValueOnce(of(page([{...row,leido:true}],0,23)));
    fixture.detectChanges();
    expect(component.noLeidas()).toBe(13);
    expect(fixture.nativeElement.querySelector('.mark-all-button').disabled).toBe(false);
    component.cambiarFiltro('no-leidas');
    expect(api.listar).toHaveBeenLastCalledWith(0,10,false);
    component.cambiarFiltro('todas');expect(api.listar).toHaveBeenLastCalledWith(0,10,null);
  });
  it('does not turn a failed or malformed list into an empty success and can retry', () => {
    const {fixture,component,api}=setup();
    api.listar.mockReturnValueOnce(of(ok({content:{value:[]},number:0,size:10,totalElements:0,totalPages:0})));
    fixture.detectChanges();
    expect(component.errorLista()).not.toBe('');
    expect(fixture.nativeElement.textContent).not.toContain('Todavía no tienes');
    expect(fixture.nativeElement.textContent).not.toContain('Estás al día');
    api.listar.mockReturnValue(of(page([])));
    (fixture.nativeElement.querySelector('.notifications-state button') as HTMLButtonElement).click();fixture.detectChanges();
    expect(component.errorLista()).toBe('');expect(fixture.nativeElement.textContent).toContain('Todavía no tienes');
  });
  it('keeps a failed count unavailable while preserving the list and retries the counter separately', () => {
    const {fixture,component,api}=setup();api.contarNoLeidas.mockReturnValueOnce(throwError(()=>({status:503})));
    fixture.detectChanges();expect(component.noLeidas()).toBeNull();expect(component.pagina()?.content).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('.mark-all-button').disabled).toBe(true);
    component.cargarContador();expect(component.noLeidas()).toBe(13);expect(api.listar).toHaveBeenCalledOnce();
  });
  it('cancels superseded lists and counters on filtering and route destruction', () => {
    const {fixture,component,api}=setup();
    const oldList=new Subject(),newList=new Subject(),oldCount=new Subject(),newCount=new Subject();
    api.listar.mockReturnValueOnce(oldList).mockReturnValueOnce(newList);api.contarNoLeidas.mockReturnValueOnce(oldCount).mockReturnValueOnce(newCount);
    fixture.detectChanges();component.cambiarFiltro('no-leidas');
    expect(oldList.observed).toBe(false);expect(oldCount.observed).toBe(false);expect(component.cargando()).toBe(true);
    fixture.destroy();expect(newList.observed).toBe(false);expect(newCount.observed).toBe(false);
  });
  it('prevents duplicate writes and recovers the previous page when reading removes the final unread page', () => {
    const {fixture,component,api}=setup();fixture.detectChanges();
    component.filtro.set('no-leidas');component.pagina.set(page([row],1,11).data as never);
    const pending=new Subject();api.marcarComoLeida.mockReturnValue(pending);
    api.listar.mockReturnValueOnce(of(page([],1,10))).mockReturnValueOnce(of(page([{...row,idNotificacion:2}],0,10)));
    component.marcarComoLeida(row);component.marcarComoLeida(row);expect(api.marcarComoLeida).toHaveBeenCalledOnce();
    pending.next(ok({...row,leido:true}));pending.complete();
    expect(api.listar).toHaveBeenLastCalledWith(0,10,false);expect(component.pagina()?.number).toBe(0);expect(component.procesando()).toBeNull();
  });
  it('reloads actual unread count after marking all instead of assuming zero when new activity arrives', () => {
    const {fixture,component,api}=setup();fixture.detectChanges();
    api.contarNoLeidas.mockReturnValue(of(ok(1)));
    component.marcarTodasComoLeidas();
    expect(component.noLeidas()).toBe(1);expect(component.exito()).toContain('13 notificaciones');expect(api.listar).toHaveBeenCalledTimes(2);
  });
  it('does not navigate or mark the row locally on HTTP failure, and keeps a retry possible', () => {
    const {fixture,component,api}=setup();fixture.detectChanges();
    const navigate=vi.spyOn(TestBed.inject(Router),'navigateByUrl');
    api.marcarComoLeida.mockReturnValueOnce(throwError(()=>({status:503})));
    component.marcarComoLeida(row,true);
    expect(navigate).not.toHaveBeenCalled();expect(component.pagina()?.content[0].leido).toBe(false);expect(component.errorAccion()).not.toBe('');
    component.marcarComoLeida(row,true);expect(navigate).toHaveBeenCalledWith('/solicitudes');
    for(const urlDestino of ['https://example.test','//example.test','javascript:alert(1)','/\\example.test','/bad\npath']) expect(component.destino({...row,urlDestino})).toBeNull();
  });
});
