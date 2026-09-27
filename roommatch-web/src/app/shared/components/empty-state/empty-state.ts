import { Component, Input } from '@angular/core';
@Component({
  selector: 'app-empty-state',
  templateUrl: './empty-state.html',
  styles: [`
    :host {display:block}
    section {padding:3rem 1.5rem;text-align:center;background:var(--rm-surface);border:1px dashed var(--rm-border-input);border-radius:12px}
    h2 {font-size:1.25rem;line-height:1.3;margin:0 0 .65rem;color:var(--rm-text)}
    p {max-width:55ch;margin:0 auto 1rem;color:var(--rm-text-secondary);line-height:1.65}
  `]
})
export class EmptyState {
  @Input() title = 'No hay resultados';
  @Input() message = '';
}
