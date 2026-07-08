export interface PropietarioResponse {

  idPropietario: number;

  idUsuario: number;

  nombreUsuario: string;

  email: string;

  tipoPropietario: string;

  nombreComercial: string | null;

  ruc: string | null;

  descripcion: string | null;

  verificado: boolean;

  estado: string;

  fechaRegistro: string;


  /*
   * =========================================================
   * PLAN ACTUAL
   * =========================================================
   */

  planActual: string | null;

  idPlan: number | null;

  nombrePlan: string | null;

  precioMensual: number | null;

  limiteHabitaciones: number | null;

  permiteDestacar: boolean | null;

  permiteEstadisticas: boolean | null;

  estadoSuscripcion: string | null;

  fechaInicioPlan: string | null;

  fechaFinPlan: string | null;
}