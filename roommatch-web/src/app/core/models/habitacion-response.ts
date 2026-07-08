export interface HabitacionResponse {

  idHabitacion: number;

  idPropietario: number;

  nombrePropietario: string;

  propietarioVerificado: boolean;

  titulo: string;

  descripcion: string;

  distrito: string;

  direccionReferencial: string | null;

  precio: number;

  areaM2: number | null;

  amoblado: boolean;

  banoPrivado: boolean;

  internetIncluido: boolean;

  aguaIncluida: boolean;

  luzIncluida: boolean;

  permiteMascotas: boolean;

  disponibleDesde: string | null;

  destacada: boolean;

  estado: string;

  fechaPublicacion: string;

  fechaActualizacion: string;

}