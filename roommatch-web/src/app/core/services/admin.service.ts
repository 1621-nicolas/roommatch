import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';
import { AdminDashboard, AdminReport, ModerationAction, ModerationEvent, ReportKind } from '../models/admin';
import { requirePage } from '../validation/api-page';

@Injectable({providedIn: 'root'})
export class AdminService {
  private readonly http = inject(HttpClient);

  dashboard() {
    return this.http.get<ApiResponse<AdminDashboard>>(`${API_BASE_URL}/admin/dashboard`).pipe(map(response => {
      if (response?.status !== 'success' || !response.data) throw new Error('No se pudo obtener el resumen administrativo.');
      const keys: (keyof AdminDashboard)[] = ['totalUsuarios','usuariosActivos','usuariosSuspendidos','totalPropietarios','totalPerfilesConvivencia',
        'totalHabitaciones','habitacionesActivas','habitacionesPausadas','totalPublicacionesRoomie','publicacionesActivas','totalMatches',
        'solicitudesPendientes','leadsPendientes','reportesUsuariosPendientes','reportesHabitacionesPendientes','notificacionesNoLeidas'];
      if (keys.some(key => !Number.isSafeInteger(response.data[key]) || response.data[key] < 0)) throw new Error('El resumen recibido tiene un formato inesperado.');
      return response.data;
    }));
  }

  reportes(kind: ReportKind, state: string, page = 0, size = 10) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (state) params = params.set('estado', state);
    return this.http.get<ApiResponse<PageResponse<AdminReport>>>(`${API_BASE_URL}/reportes/admin/${kind}`, {params}).pipe(map(response => {
      const page = requirePage<AdminReport>(response);
      if (page.content.some(row => !row || (!Number.isSafeInteger(kind === 'usuarios' ? row.idReporte : row.idReporteHabitacion) || (kind === 'usuarios' ? row.idReporte! : row.idReporteHabitacion!) < 1)))
        throw new Error('El servidor devolvió reportes sin identificadores válidos.');
      return page;
    }));
  }

  historial(kind: ReportKind, id: number, page = 0) {
    const params = new HttpParams().set('page', page).set('size', 10);
    return this.http.get<ApiResponse<PageResponse<ModerationEvent>>>(`${API_BASE_URL}/reportes/admin/${kind}/${id}/historial`, {params})
      .pipe(map(requirePage<ModerationEvent>));
  }

  decidir(kind: ReportKind, id: number, action: ModerationAction, motivo: string) {
    const endpoint = action === 'sancionado' ? 'sancionar' : action === 'restaurado' ? 'restaurar' : 'revisar';
    const params = endpoint === 'revisar' ? new HttpParams().set('estado', action) : new HttpParams();
    return this.http.put<ApiResponse<AdminReport>>(`${API_BASE_URL}/reportes/admin/${kind}/${id}/${endpoint}`, {motivo}, {params}).pipe(map(response => {
      if (response?.status !== 'success' || !response.data) throw new Error(response?.message || 'No se pudo guardar la decisión.');
      return response.data;
    }));
  }
}
