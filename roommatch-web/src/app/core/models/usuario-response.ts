export interface UsuarioResponse {
  idUsuario: number;

  nombres: string;
  apellidos: string;

  email: string;

  edad: number | null;
  ocupacion: string | null;
  universidad: string | null;
  foto: string | null;

  estado: string;

  rol:
    | string
    | {
        idRol?: number;
        nombreRol?: string;
      };
}