import { afterNextRender, Component, DestroyRef, ElementRef, inject, Injector, OnInit, signal, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, Subscription } from 'rxjs';
import { AdminService } from '../../../core/services/admin.service';
import { AdminReport, ModerationAction, ModerationEvent, ReportKind } from '../../../core/models/admin';
import { PageResponse } from '../../../core/models/page-response';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { AdminNav } from '../admin-nav';

type Decision = {kind: ReportKind; report: AdminReport; action: ModerationAction};
@Component({
  selector: 'app-reportes',
  imports: [DatePipe, FormsModule, Pagination, EmptyState, AdminNav],
  templateUrl: './reportes.html',
  styleUrl: '../admin.css'
})
export class Reportes implements OnInit {
  private readonly api = inject(AdminService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroy = inject(DestroyRef);
  private readonly injector = inject(Injector);
  private listRequest?: Subscription;
  private historyRequest?: Subscription;
  @ViewChild('decisionDialog', {static: true}) private dialog!: ElementRef<HTMLDialogElement>;
  @ViewChild('details') private details?: ElementRef<HTMLElement>;
  readonly kind = signal<ReportKind>('usuarios');
  readonly state = signal('pendiente');
  readonly page = signal<PageResponse<AdminReport> | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly selected = signal<AdminReport | null>(null);
  readonly history = signal<PageResponse<ModerationEvent> | null>(null);
  readonly historyLoading = signal(false);
  readonly historyError = signal('');
  readonly decision = signal<Decision | null>(null);
  readonly saving = signal(false);
  readonly actionError = signal('');
  motive = '';

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.kind.set(params.get('tipo') === 'habitaciones' ? 'habitaciones' : 'usuarios');
    const state = params.get('estado');
    if (state && ['pendiente','revisado','rechazado','sancionado'].includes(state)) this.state.set(state);
    this.load();
  }

  changeKind(kind: ReportKind): void {
    if (this.saving() || kind === this.kind()) return;
    this.kind.set(kind); this.success.set(''); this.load();
  }

  load(page = 0): void {
    this.listRequest?.unsubscribe(); this.historyRequest?.unsubscribe();
    this.selected.set(null); this.history.set(null); this.error.set(''); this.loading.set(true);
    this.listRequest = this.api.reportes(this.kind(), this.state(), page).pipe(takeUntilDestroyed(this.destroy), finalize(() => this.loading.set(false))).subscribe({
      next: result => this.page.set(result),
      error: error => {this.page.set(null); this.error.set(this.message(error, 'No se pudieron cargar los reportes.'));}
    });
  }

  id(report: AdminReport, kind = this.kind()): number {
    const id = kind === 'usuarios' ? report.idReporte : report.idReporteHabitacion;
    if (!Number.isSafeInteger(id) || !id || id < 1) throw new Error('El reporte recibido no tiene un identificador válido.');
    return id;
  }
  target(report: AdminReport): string { return this.kind() === 'usuarios' ? report.nombreReportado || 'Usuario reportado' : report.tituloHabitacion || 'Habitación reportada'; }
  open(report: AdminReport): boolean { return report.estado === 'pendiente' || report.estado === 'revisado'; }
  canRestore(report: AdminReport): boolean {
    return report.estado === 'sancionado' && (this.kind() === 'usuarios' ? report.estadoObjetivo === 'suspendido' : !!report.habitacionBloqueada);
  }

  select(report: AdminReport): void {
    this.selected.set(report); this.loadHistory();
    afterNextRender(() => this.details?.nativeElement.focus(), {injector: this.injector});
  }

  loadHistory(page = 0): void {
    const report = this.selected(); if (!report) return;
    this.historyRequest?.unsubscribe(); this.historyLoading.set(true); this.historyError.set('');
    this.historyRequest = this.api.historial(this.kind(), this.id(report), page)
      .pipe(takeUntilDestroyed(this.destroy), finalize(() => this.historyLoading.set(false))).subscribe({
        next: history => this.history.set(history),
        error: error => {this.history.set(null); this.historyError.set(this.message(error, 'No se pudo consultar el historial.'));}
      });
  }

  prepare(action: ModerationAction): void {
    const report = this.selected(); if (!report || this.saving()) return;
    this.decision.set({kind: this.kind(), report, action}); this.motive = ''; this.actionError.set('');
    this.dialog.nativeElement.showModal();
  }

  cancel(event?: Event): void {
    event?.preventDefault();
    if (this.saving()) return;
    this.dialog.nativeElement.close(); this.decision.set(null); this.actionError.set('');
  }

  actionTitle(): string {
    const action = this.decision()?.action;
    if (action === 'sancionado') return this.kind() === 'usuarios' ? 'Suspender esta cuenta' : 'Bloquear esta habitación';
    if (action === 'restaurado') return this.kind() === 'usuarios' ? 'Reactivar esta cuenta' : 'Retirar el bloqueo';
    return action === 'rechazado' ? 'Rechazar este reporte' : 'Marcar reporte en revisión';
  }

  actionEffect(): string {
    const action = this.decision()?.action;
    if (action === 'sancionado') return this.kind() === 'usuarios'
      ? 'La persona perderá acceso a RoomMatch. El reporte y el motivo de tu decisión se conservarán.'
      : 'La habitación dejará de mostrarse públicamente. El propietario no podrá reactivarla mientras siga bloqueada.';
    if (action === 'restaurado') return this.kind() === 'usuarios'
      ? 'Se reactivará la cuenta completa, aunque tenga otros reportes históricos. Revisa sus antecedentes antes de confirmar.'
      : 'Se retirará el bloqueo de la habitación completa. No se publicará automáticamente: el propietario deberá reactivarla y cumplir el límite de su plan.';
    return action === 'rechazado' ? 'El caso quedará resuelto como rechazado. No se borrará ni volverá a pendiente.' : 'El caso seguirá abierto para investigar. Todavía no se aplicará una sanción.';
  }

  confirm(): void {
    const decision = this.decision(); if (!decision || this.saving()) return;
    if (!this.motive.trim() || this.motive.length > 500) {this.actionError.set('Escribe un motivo de hasta 500 caracteres.'); return;}
    this.saving.set(true); this.actionError.set('');
    this.api.decidir(decision.kind, this.id(decision.report, decision.kind), decision.action, this.motive.trim())
      .pipe(takeUntilDestroyed(this.destroy), finalize(() => this.saving.set(false))).subscribe({
        next: () => {
          this.dialog.nativeElement.close(); this.decision.set(null);
          this.success.set('Decisión guardada. Puedes consultarla en el historial del reporte.');
          this.load();
        },
        error: error => this.actionError.set(this.message(error, 'No se pudo guardar la decisión. Revisa el estado actual del reporte.'))
      });
  }

  private message(error: unknown, fallback: string): string {
    const response = error as {error?: {message?: string}; message?: string};
    return response?.error?.message || response?.message || fallback;
  }
}
