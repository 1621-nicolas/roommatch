import { TestBed } from '@angular/core/testing';
import { ListingImage } from './listing-image';

describe('Listing image fallback', () => {
  it('rejects unsafe sources and handles a failed external image without reloading it forever', () => {
    TestBed.configureTestingModule({imports: [ListingImage]});
    const fixture = TestBed.createComponent(ListingImage);
    fixture.componentRef.setInput('src', 'data:image/svg+xml,<svg/>'); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
    fixture.componentRef.setInput('src', 'https://images.example/missing.jpg'); fixture.detectChanges();
    fixture.nativeElement.querySelector('img').dispatchEvent(new Event('error')); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No se pudo cargar esta foto');
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
    fixture.componentRef.setInput('src', 'https://images.example/next.jpg'); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img').src).toBe('https://images.example/next.jpg');
  });
});
