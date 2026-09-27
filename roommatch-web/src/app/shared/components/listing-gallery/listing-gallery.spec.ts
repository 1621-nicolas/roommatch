import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { ListingGallery } from './listing-gallery';
import { ListingGalleryService, ListingPhoto } from '../../../core/services/listing-gallery.service';

const photos: ListingPhoto[] = [
  {idImagen: 1, urlImagen: 'https://images.example/1.jpg', orden: 1, principal: false},
  {idImagen: 2, urlImagen: 'https://images.example/2.jpg', orden: 2, principal: true}
];
describe('Public listing gallery', () => {
  function setup() {
    const list = vi.fn().mockReturnValue(of(photos));
    TestBed.configureTestingModule({imports: [ListingGallery], providers: [{provide: ListingGalleryService, useValue: {list}}]});
    const fixture = TestBed.createComponent(ListingGallery);
    fixture.componentRef.setInput('kind', 'habitacion'); fixture.componentRef.setInput('parentId', 4); fixture.componentRef.setInput('title', 'Habitación junto al parque');
    return {fixture, list};
  }
  it('shows loading, chooses the primary, then switches photos through labelled controls', () => {
    const {fixture, list} = setup(); const pending = new Subject<ListingPhoto[]>(); list.mockReturnValue(pending);
    fixture.detectChanges(); expect(fixture.nativeElement.textContent).toContain('Cargando fotos');
    pending.next(photos); pending.complete(); fixture.detectChanges();
    let image = fixture.nativeElement.querySelector('img') as HTMLImageElement;
    expect(image.src).toBe(photos[1].urlImagen); expect(image.alt).toContain('foto 2');
    expect(image.getAttribute('referrerpolicy')).toBe('no-referrer');
    (fixture.nativeElement.querySelector('.photo-controls button') as HTMLButtonElement).click(); fixture.detectChanges();
    image = fixture.nativeElement.querySelector('img'); expect(image.src).toBe(photos[0].urlImagen);
    expect(fixture.nativeElement.querySelector('[aria-pressed=true]').textContent).toContain('Foto 1');
  });
  it('keeps a network failure distinct from no photos and exposes a working retry', () => {
    const {fixture, list} = setup(); list.mockReturnValueOnce(throwError(() => ({status: 503}))).mockReturnValue(of([]));
    fixture.detectChanges(); expect(fixture.nativeElement.querySelector('[role=alert]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('todavía no tiene fotos');
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click(); fixture.detectChanges();
    expect(list).toHaveBeenCalledTimes(2); expect(fixture.nativeElement.textContent).toContain('todavía no tiene fotos');
  });
  it('cancels a former parent request and does not show its late pictures', () => {
    const {fixture, list} = setup(); const first = new Subject<ListingPhoto[]>(); const second = new Subject<ListingPhoto[]>();
    list.mockReturnValueOnce(first).mockReturnValue(second); fixture.detectChanges();
    fixture.componentRef.setInput('parentId', 8); fixture.detectChanges();
    expect(first.observed).toBe(false); first.next(photos);
    expect(fixture.componentInstance.photos()).toEqual([]);
    expect(list).toHaveBeenLastCalledWith('habitacion', 8);
    fixture.destroy(); expect(second.observed).toBe(false);
  });
});
