import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ListingGalleryService } from './listing-gallery.service';

describe('Listing gallery API contracts', () => {
  beforeEach(() => TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]}));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('uses the publication image endpoint and sorts existing photos without mutating the response', () => {
    let result: number[] = [];
    TestBed.inject(ListingGalleryService).list('publicacion', 7).subscribe(rows => result = rows.map(row => row.idImagen));
    TestBed.inject(HttpTestingController).expectOne('/api/publicaciones-roomie/7/imagenes').flush({status: 'success', data: [
      {idImagen: 2, urlImagen: 'https://images.example/2.jpg', orden: 2, principal: true},
      {idImagen: 1, urlImagen: 'https://images.example/1.jpg', orden: 1, principal: false}
    ]});
    expect(result).toEqual([1, 2]);
  });
  it('reports a malformed array rather than an empty successful gallery', () => {
    const error = vi.fn(), next = vi.fn();
    TestBed.inject(ListingGalleryService).list('habitacion', 9).subscribe({next, error});
    TestBed.inject(HttpTestingController).expectOne('/api/imagenes-habitacion/habitacion/9').flush({status: 'success', data: {value: []}});
    expect(next).not.toHaveBeenCalled(); expect(error).toHaveBeenCalledOnce();
  });
});
