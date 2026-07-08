export interface PublicacionRoomieRequest {

  tipoPublicacion: string;

  titulo: string;

  descripcion: string;

  distrito: string;

  presupuestoMin: number | null;

  presupuestoMax: number | null;

  tipoVinculacionVivienda: string | null;

  idHabitacion: number | null;

  viviendaExternaTitulo: string | null;

  viviendaExternaDireccion: string | null;

  viviendaExternaPrecio: number | null;

}