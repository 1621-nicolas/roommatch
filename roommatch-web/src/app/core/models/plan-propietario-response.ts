export interface PlanPropietarioResponse {

  idPlan: number;

  nombrePlan: string;

  descripcion: string | null;

  precioMensual: number;

  limiteHabitaciones: number;

  permiteDestacar: boolean;

  permiteEstadisticas: boolean;

  estado: string;
}