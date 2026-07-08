export interface MiPlanResponse {

  idSuscripcion: number;

  idPropietario: number;

  idPlan: number;

  nombrePlan: string;

  descripcionPlan: string | null;

  precioMensual: number;

  limiteHabitaciones: number;

  permiteDestacar: boolean;

  permiteEstadisticas: boolean;

  estadoSuscripcion: string;

  fechaInicio: string;

  fechaFin: string | null;
}