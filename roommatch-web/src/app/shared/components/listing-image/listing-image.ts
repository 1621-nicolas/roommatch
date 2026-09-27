import { Component, Input, OnChanges, signal } from '@angular/core';
import { imageUrl } from '../../../core/validation/image-url';

@Component({
  selector: 'app-listing-image',
  template: `
    @if (safeSrc() && !failed()) {
      <img [src]="safeSrc()" [alt]="description" loading="lazy" decoding="async"
        referrerpolicy="no-referrer" (error)="failed.set(true)" [class.contain]="contain">
    } @else {
      <p class="image-unavailable">{{ failed() ? 'No se pudo cargar esta foto' : 'Sin foto disponible' }}</p>
    }
  `,
  styles: [`
    :host {display:block;aspect-ratio:16/10;overflow:hidden;background:#eef1f6;min-width:0}
    img {display:block;width:100%;height:100%;object-fit:cover}
    img.contain {object-fit:contain}
    .image-unavailable {display:flex;align-items:center;justify-content:center;height:100%;margin:0;padding:1rem;color:#475569;text-align:center}
  `]
})
export class ListingImage implements OnChanges {
  @Input() src: string | null | undefined;
  @Input() description = 'Foto del anuncio';
  @Input() contain = false;
  readonly safeSrc = signal<string | null>(null);
  readonly failed = signal(false);
  ngOnChanges(): void { this.safeSrc.set(imageUrl(this.src)); this.failed.set(false); }
}
