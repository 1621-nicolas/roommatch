export interface PublicacionRoomieResponse {

  /*
   * =========================================================
   * IDENTIFICACIÓN
   * =========================================================
   */

  idPublicacion: number;

  idUsuario: number;


  /*
   * =========================================================
   * USUARIO
   * =========================================================
   */

  nombreUsuario: string;

  edad: number | null;

  ocupacion: string | null;

  foto: string | null;


  /*
   * =========================================================
   * PUBLICACIÓN ROOMIE
   * =========================================================
   */

  tipoPublicacion: string;

  titulo: string;

  descripcion: string;

  distrito: string;

  presupuestoMin: number | null;

  presupuestoMax: number | null;

  estado: string;


  /*
   * =========================================================
   * VINCULACIÓN DE VIVIENDA
   * =========================================================
   */

  tipoVinculacionVivienda: string | null;


  /*
   * =========================================================
   * HABITACIÓN ROOMMATCH
   * =========================================================
   */

  idHabitacion: number | null;

  habitacionTitulo: string | null;

  habitacionDistrito: string | null;

  habitacionPrecio: number | null;

  nombrePropietarioHabitacion: string | null;


  /*
   * =========================================================
   * VIVIENDA EXTERNA
   * =========================================================
   */

  viviendaExternaTitulo: string | null;

  viviendaExternaDireccion: string | null;

  viviendaExternaPrecio: number | null;


  /*
   * =========================================================
   * COMPATIBILIDAD
   * =========================================================
   */

  porcentajeCompatibilidad: number | null;

  coincidencias: string | null;

  diferencias: string | null;


  /*
   * =========================================================
   * CONTROL DEL USUARIO
   * =========================================================
   */

  esMiPublicacion: boolean;


  /*
   * =========================================================
   * FECHAS
   * =========================================================
   */

  fechaPublicacion: string;

  fechaActualizacion: string;

}