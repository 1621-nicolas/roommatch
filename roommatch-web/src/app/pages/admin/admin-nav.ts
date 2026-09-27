import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
@Component({
  selector: 'app-admin-nav',
  imports: [RouterLink, RouterLinkActive],
  template: `<nav aria-label="Administración">
    <a routerLink="/admin/dashboard" routerLinkActive="current" ariaCurrentWhenActive="page">Resumen</a>
    <a routerLink="/admin/reportes" routerLinkActive="current" ariaCurrentWhenActive="page">Reportes</a>
  </nav>`,
  styles: [`
    nav {display:flex;gap:1.5rem;border-bottom:1px solid var(--rm-border);margin-bottom:2rem}
    a {display:block;padding:1rem .25rem;color:var(--rm-text-secondary);border-bottom:2px solid transparent;font-weight:600}
    a.current {color:var(--rm-primary-dark);border-color:var(--rm-primary)}
    a:focus-visible {outline:3px solid var(--rm-primary);outline-offset:3px}
  `]
})
export class AdminNav {}
