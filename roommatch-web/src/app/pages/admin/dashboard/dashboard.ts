import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AdminService } from '../../../core/services/admin.service';
import { AdminDashboard } from '../../../core/models/admin';
import { AdminNav } from '../admin-nav';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, DecimalPipe, RouterLink, AdminNav, EmptyState],
  templateUrl: './dashboard.html',
  styleUrl: '../admin.css'
})
export class Dashboard implements OnInit {
  private readonly api = inject(AdminService);
  private readonly destroy = inject(DestroyRef);
  readonly metrics = signal<AdminDashboard | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly updated = signal<Date | null>(null);
  ngOnInit(): void { this.load(); }
  load(): void {
    if (this.loading()) return;
    this.loading.set(true); this.error.set(''); this.metrics.set(null);
    this.api.dashboard().pipe(takeUntilDestroyed(this.destroy), finalize(() => this.loading.set(false))).subscribe({
      next: metrics => {this.metrics.set(metrics); this.updated.set(new Date());},
      error: error => this.error.set(error.error?.message || error.message || 'No se pudo cargar el resumen. Intenta nuevamente.')
    });
  }
}
