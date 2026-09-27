import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, map, of, Subscription, switchMap } from 'rxjs';
import { NotificacionService } from '../../core/services/notificacion.service';
import { NotificacionResponse } from '../../core/models/notificacion-response';
import { PageResponse } from '../../core/models/page-response';
import { requirePage } from '../../core/validation/api-page';
import { Icon, IconName } from '../../shared/components/icon/icon';

type Filter = 'todas' | 'no-leidas';

@Component({
  selector: 'app-notificaciones',
  imports: [DatePipe, Icon],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css'
})
export class Notificaciones implements OnInit {
  private readonly api = inject(NotificacionService);
  private readonly router = inject(Router);
  private readonly destroy = inject(DestroyRef);
  private listRequest?: Subscription;
  private countRequest?: Subscription;
  readonly pagina = signal<PageResponse<NotificacionResponse> | null>(null);
  readonly filtro = signal<Filter>('todas');
  readonly cargando = signal(false);
  readonly noLeidas = signal<number | null>(null);
  readonly contando = signal(false);
  readonly errorLista = signal('');
  readonly errorContador = signal('');
  readonly errorAccion = signal('');
  readonly exito = signal('');
  readonly procesando = signal<number | 'todas' | null>(null);
  readonly tamanioPagina = 10;

  ngOnInit(): void { this.cargarNotificaciones(); }

  cargarNotificaciones(page = 0): void {
    this.listRequest?.unsubscribe();
    this.cargando.set(true); this.errorLista.set('');
    const leido = this.filtro() === 'no-leidas' ? false : null;
    this.listRequest = this.api.listar(page, this.tamanioPagina, leido).pipe(
      map(requirePage<NotificacionResponse>),
      // Reading the final item (also in another tab) can remove the last unread page.
      switchMap(result => result.content.length === 0 && page > 0
        ? this.api.listar(Math.max(0, result.totalPages - 1), this.tamanioPagina, leido).pipe(map(requirePage<NotificacionResponse>))
        : of(result)),
      takeUntilDestroyed(this.destroy), finalize(() => this.cargando.set(false))
    ).subscribe({
      next: result => this.pagina.set(result),
      error: () => {this.pagina.set(null); this.errorLista.set('No se pudieron cargar tus notificaciones. Intenta de nuevo.');}
    });
    this.cargarContador();
  }

  cargarContador(): void {
    this.countRequest?.unsubscribe(); this.contando.set(true); this.errorContador.set('');
    this.countRequest = this.api.contarNoLeidas().pipe(
      map(response => {
        if (response.status !== 'success' || !Number.isSafeInteger(response.data) || response.data < 0) throw new Error('Invalid unread count');
        return response.data;
      }), takeUntilDestroyed(this.destroy), finalize(() => this.contando.set(false))
    ).subscribe({next: count => this.noLeidas.set(count), error: () => {
      this.noLeidas.set(null); this.errorContador.set('No se pudo obtener el total sin leer.');
    }});
  }

  cambiarFiltro(filter: Filter): void {
    if (this.procesando() !== null) return;
    this.filtro.set(filter); this.cargarNotificaciones();
  }

  marcarComoLeida(notification: NotificacionResponse, abrir = false): void {
    if (this.procesando() !== null) return;
    if (notification.leido) {if (abrir) this.navegar(notification); return;}
    this.procesando.set(notification.idNotificacion); this.errorAccion.set(''); this.exito.set('');
    this.api.marcarComoLeida(notification.idNotificacion).pipe(
      map(response => {
        if (response.status !== 'success' || response.data?.idNotificacion !== notification.idNotificacion || response.data.leido !== true) throw new Error('Invalid read response');
        return response.data;
      }), takeUntilDestroyed(this.destroy), finalize(() => this.procesando.set(null))
    ).subscribe({next: () => {
      this.exito.set('Notificación marcada como leída.');
      if (abrir && this.destino(notification)) this.navegar(notification);
      else this.cargarNotificaciones(this.pagina()?.number ?? 0);
    }, error: () => this.errorAccion.set('No se pudo marcar la notificación como leída. Puedes volver a intentarlo.')});
  }

  marcarTodasComoLeidas(): void {
    if (this.procesando() !== null || this.cargando() || this.contando() || !this.noLeidas()) return;
    this.procesando.set('todas'); this.errorAccion.set(''); this.exito.set('');
    this.api.marcarTodasComoLeidas().pipe(
      map(response => {
        if (response.status !== 'success' || !Number.isSafeInteger(response.data) || response.data < 0) throw new Error('Invalid update count');
        return response.data;
      }), takeUntilDestroyed(this.destroy), finalize(() => this.procesando.set(null))
    ).subscribe({next: count => {
      this.exito.set(count === 1 ? 'Se marcó 1 notificación como leída.' : `Se marcaron ${count} notificaciones como leídas.`);
      // Re-query: notifications arriving during the update must remain visible and unread.
      this.cargarNotificaciones(this.filtro() === 'no-leidas' ? 0 : this.pagina()?.number ?? 0);
    }, error: () => this.errorAccion.set('No se pudieron actualizar las notificaciones. Puedes volver a intentarlo.')});
  }

  cambiarPagina(delta: number): void {
    const current = this.pagina();
    if (!current || this.cargando() || this.procesando() !== null) return;
    const next = current.number + delta;
    if (next >= 0 && next < current.totalPages) this.cargarNotificaciones(next);
  }

  destino(notification: NotificacionResponse): string | null {
    const url = notification.urlDestino;
    // Notifications navigate only within RoomMatch; legacy external/malformed destinations stay inert.
    return typeof url === 'string' && /^\/(?!\/)[^\\\s\u0000-\u001f]*$/.test(url) ? url : null;
  }
  private navegar(notification: NotificacionResponse): void {
    const url = this.destino(notification); if (url) void this.router.navigateByUrl(url);
  }
  nombreTipo(type: string): string {
    return ({solicitud:'Solicitud',contacto:'Contacto',match:'Match',habitacion:'Habitación',lead:'Interés',reporte:'Reporte',sistema:'RoomMatch'} as Record<string,string>)[type] ?? 'Notificación';
  }
  icono(type: string): IconName {
    return ({solicitud:'people',contacto:'people',match:'heart',habitacion:'house',lead:'key',reporte:'search'} as Record<string,IconName>)[type] ?? 'check-lg';
  }
}
