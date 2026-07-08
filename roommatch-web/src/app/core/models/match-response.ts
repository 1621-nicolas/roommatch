export interface MatchResponse {
  idMatch: number;
  idUsuarioDestino: number;

  nombres: string;
  apellidos: string;
  email: string;

  edad: number | null;
  ocupacion: string | null;
  universidad: string | null;
  foto: string | null;

  porcentaje: number;

  coincidencias: string;
  diferencias: string;

  fechaCalculo: string;
}