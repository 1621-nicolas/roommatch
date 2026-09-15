import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html',
  styles: [`
    :host {display:block}
    nav {display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:1rem;margin-top:1.5rem}
    p {margin:0;color:var(--rm-text-secondary);font-size:.875rem}
    .pages {display:flex;gap:.35rem;flex-wrap:wrap}
    button {min-width:44px;min-height:44px;border:1px solid var(--rm-border-input);border-radius:8px;background:var(--rm-surface);color:var(--rm-text);padding:.55rem .85rem}
    button[aria-current=page] {border-color:var(--rm-primary);background:var(--rm-primary);color:white}
    button:hover:not(:disabled) {border-color:var(--rm-primary)}
    button:focus-visible {outline:3px solid var(--rm-primary);outline-offset:3px}
    button:disabled {cursor:not-allowed;opacity:.5}
  `]
})
export class Pagination {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() loading = false;
  @Output() pageChange = new EventEmitter<number>();
  get pages(): number[] {
    if (!Number.isSafeInteger(this.totalPages) || this.totalPages < 1) return [];
    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    return Array.from({length: Math.min(5, this.totalPages)}, (_, i) => start + i);
  }
  go(page: number): void {
    if (!this.loading && page >= 0 && page < this.totalPages && page !== this.page) this.pageChange.emit(page);
  }
}
