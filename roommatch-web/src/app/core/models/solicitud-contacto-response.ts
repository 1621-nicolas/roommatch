export interface SolicitudContactoResponse {
  idSolicitud: number;

  idUsuarioEmisor: number;
  nombreEmisor: string;

  idUsuarioReceptor: number;
  nombreReceptor: string;

  mensaje: string;

  estado: string;

  fechaSolicitud: string;
  fechaRespuesta: string | null;
}