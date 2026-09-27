import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiResponse } from '../models/api-response';

export type GalleryKind = 'habitacion' | 'publicacion';
export interface ListingPhoto {idImagen: number; urlImagen: string; orden: number; principal: boolean;}

@Injectable({providedIn: 'root'})
export class ListingGalleryService {
  private readonly http = inject(HttpClient);
  list(kind: GalleryKind, id: number) {
    if (!Number.isSafeInteger(id) || id < 1) throw new Error('El anuncio no tiene un identificador válido.');
    const path = kind === 'habitacion' ? `/imagenes-habitacion/habitacion/${id}` : `/publicaciones-roomie/${id}/imagenes`;
    return this.http.get<ApiResponse<ListingPhoto[]>>(`${API_BASE_URL}${path}`).pipe(map(response => {
      if (response.status !== 'success' || !Array.isArray(response.data) || response.data.some(photo =>
        !photo || !Number.isSafeInteger(photo.idImagen) || photo.idImagen < 1 || typeof photo.urlImagen !== 'string' ||
        !Number.isSafeInteger(photo.orden) || photo.orden < 1 || typeof photo.principal !== 'boolean')) {
        throw new Error('No se pudo interpretar la galería. Intenta cargarla nuevamente.');
      }
      return [...response.data].sort((a, b) => a.orden - b.orden || a.idImagen - b.idImagen);
    }));
  }
}
