import { Component, inject, Input, OnChanges, OnDestroy, signal } from '@angular/core';
import { finalize, Subscription } from 'rxjs';
import { GalleryKind, ListingGalleryService, ListingPhoto } from '../../../core/services/listing-gallery.service';
import { ListingImage } from '../listing-image/listing-image';

@Component({
  selector: 'app-listing-gallery',
  imports: [ListingImage],
  template: `
    <section aria-label="Fotos del anuncio" [attr.aria-busy]="loading()">
      @if (loading()) { <p role="status">Cargando fotos…</p> }
      @else if (error()) {
        <div role="alert"><p>{{ error() }}</p><button type="button" (click)="load()">Reintentar fotos</button></div>
      } @else if (selected(); as photo) {
        <app-listing-image [src]="photo.urlImagen" [description]="title + ' · foto ' + (selectedIndex() + 1)" [contain]="true" />
        <div class="photo-controls" role="group" aria-label="Seleccionar foto">
          @for (item of photos(); track item.idImagen; let index = $index) {
            <button type="button" [attr.aria-pressed]="item.idImagen === photo.idImagen" (click)="selectedIndex.set(index)">Foto {{ index + 1 }}</button>
          }
        </div>
        <p class="photo-count" role="status">Foto {{ selectedIndex() + 1 }} de {{ photos().length }}</p>
      } @else { <p>Este anuncio todavía no tiene fotos.</p> }
    </section>
  `,
  styles: [`
    :host {display:block;margin-bottom:1.5rem;min-width:0}
    section {min-width:0}
    p {color:#475569;line-height:1.6}
    app-listing-image {border-radius:12px}
    .photo-controls {display:flex;flex-wrap:wrap;gap:.5rem;margin-top:1rem}
    button {border:1px solid #cbd5e1;border-radius:8px;padding:.65rem .9rem;background:white;color:#3730a3;min-height:44px;font:inherit;cursor:pointer}
    button[aria-pressed=true] {background:#4f46e5;color:white;border-color:#4f46e5}
    button:hover {border-color:#3730a3}
    button:focus-visible {outline:3px solid #4f46e5;outline-offset:3px}
    .photo-count {margin:.5rem 0 0;font-size:.9rem;font-variant-numeric:tabular-nums}
    [role=alert] {padding:1rem;border:1px solid #e3a0a0;border-radius:12px;background:#fff5f5}
  `]
})
export class ListingGallery implements OnChanges, OnDestroy {
  private readonly api = inject(ListingGalleryService);
  private request?: Subscription;
  @Input({required: true}) kind!: GalleryKind;
  @Input({required: true}) parentId!: number;
  @Input() title = 'Anuncio';
  readonly photos = signal<ListingPhoto[]>([]);
  readonly selectedIndex = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');
  selected(): ListingPhoto | null { return this.photos()[this.selectedIndex()] ?? null; }
  ngOnChanges(): void { this.load(); }
  ngOnDestroy(): void { this.request?.unsubscribe(); }
  load(): void {
    this.request?.unsubscribe(); this.photos.set([]); this.selectedIndex.set(0); this.error.set('');
    if (!Number.isSafeInteger(this.parentId) || this.parentId < 1) {this.error.set('No se encontró el anuncio.'); return;}
    this.loading.set(true);
    this.request = this.api.list(this.kind, this.parentId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: photos => {this.photos.set(photos); this.selectedIndex.set(Math.max(0, photos.findIndex(photo => photo.principal)));},
      error: () => this.error.set('No se pudieron cargar las fotos. El resto del anuncio sigue disponible.')
    });
  }
}
