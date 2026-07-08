export interface PerfilConvivenciaResponse {

  /*
   * =========================================================
   * IDENTIFICACIÓN
   * =========================================================
   */

  idPerfil: number;

  idUsuario: number;


  /*
   * =========================================================
   * UBICACIÓN Y PRESUPUESTO
   * =========================================================
   */

  presupuestoMin: number;

  presupuestoMax: number;

  distritoPreferido: string;

  fechaMudanza: string | null;


  /*
   * =========================================================
   * HÁBITOS
   * =========================================================
   */

  limpieza: number;

  ruido: number;

  sociabilidad: number;


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


  /*
   * =========================================================
   * ESTADO DEL PERFIL
   * =========================================================
   */

  perfilCompleto: boolean;

  fechaActualizacion: string | null;

}