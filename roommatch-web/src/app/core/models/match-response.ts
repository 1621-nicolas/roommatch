export interface MatchResponse {
  idMatch: number | null;
  cobertura: number;
  versionAlgoritmo: string;
  hayImporteComun: boolean | null;
  criterios: {id: string; nombre: string; peso: number; puntuacion: number | null}[];
  idUsuarioDestino: number;

  nombres: string;
  apellidos: string;

  edad: number | null;
  ocupacion: string | null;
  universidad: string | null;
  foto: string | null;

  porcentaje: number;

  coincidencias: string;
  diferencias: string;

  fechaCalculo: string;
}