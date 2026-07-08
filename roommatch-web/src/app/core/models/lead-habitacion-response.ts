export interface LeadHabitacionResponse {

  idLead: number;

  idHabitacion: number;

  tituloHabitacion: string;

  distrito: string;

  precio: number;

  idUsuarioInteresado: number;

  nombreInteresado: string;

  emailInteresado: string;

  idPropietario: number;

  nombrePropietario: string;

  mensaje: string | null;

  estado: string;

  fechaLead: string;
}