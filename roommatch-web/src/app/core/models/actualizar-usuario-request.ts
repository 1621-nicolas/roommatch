export interface ActualizarUsuarioRequest {
  nombres: string;
  apellidos: string;
  edad: number | null;
  ocupacion: string | null;
  universidad: string | null;
  foto: string | null;
}