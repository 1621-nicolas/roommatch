export type ReportKind = 'usuarios' | 'habitaciones';
export type ReportState = 'pendiente' | 'revisado' | 'rechazado' | 'sancionado';
export type ModerationAction = 'revisado' | 'rechazado' | 'sancionado' | 'restaurado';

export interface AdminReport {
  idReporte?: number;
  idReporteHabitacion?: number;
  idUsuarioReportante: number;
  nombreReportante: string;
  idUsuarioReportado?: number;
  nombreReportado?: string;
  idHabitacion?: number;
  tituloHabitacion?: string;
  propietario?: string | null;
  motivo: string;
  descripcion: string | null;
  estado: ReportState;
  estadoObjetivo: string;
  habitacionBloqueada?: boolean;
  fechaReporte: string;
  fechaRevision: string | null;
}
export interface ModerationEvent {
  idEvento: number;
  idAdmin: number;
  administrador: string;
  accion: ModerationAction;
  motivo: string;
  fecha: string;
}
export interface AdminDashboard {
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosSuspendidos: number;
  totalPropietarios: number;
  totalPerfilesConvivencia: number;
  totalHabitaciones: number;
  habitacionesActivas: number;
  habitacionesPausadas: number;
  totalPublicacionesRoomie: number;
  publicacionesActivas: number;
  totalMatches: number;
  solicitudesPendientes: number;
  leadsPendientes: number;
  reportesUsuariosPendientes: number;
  reportesHabitacionesPendientes: number;
  notificacionesNoLeidas: number;
}
