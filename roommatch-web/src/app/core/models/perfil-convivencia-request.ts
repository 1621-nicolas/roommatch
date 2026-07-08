export interface PerfilConvivenciaRequest {

  /*
   * =========================================================
   * UBICACIÓN Y PRESUPUESTO
   * =========================================================
   */

  presupuestoMin: number | null;

  presupuestoMax: number | null;

  distritoPreferido: string;

  fechaMudanza: string | null;


  /*
   * =========================================================
   * HÁBITOS
   * =========================================================
   */

  limpieza: number | null;

  ruido: number | null;

  sociabilidad: number | null;


  /*
   * =========================================================
   * PREFERENCIAS DE CONVIVENCIA
   * =========================================================
   */

  horario: string;

  visitas: string;

  mascotas: string;

  fumar: string;

  alcohol: string;

  gastos: string;

  convivencia: string;


  /*
   * =========================================================
   * INFORMACIÓN PERSONAL
   * =========================================================
   */

  descripcionPersonal: string | null;

}