export interface NotificacionResponse {

  idNotificacion: number;

  idUsuario: number;

  titulo: string;

  mensaje: string;

  tipo: string;

  leido: boolean;

  urlDestino: string | null;

  fechaCreacion: string;

}