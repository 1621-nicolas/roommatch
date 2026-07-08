export interface FavoritoResponse {
  idFavorito: number;
  idUsuarioFavorito: number;

  nombres: string;
  apellidos: string;
  email: string;

  edad: number | null;
  ocupacion: string | null;
  universidad: string | null;
  foto: string | null;

  fechaFavorito: string;
}