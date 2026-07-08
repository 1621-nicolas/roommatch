import { UsuarioResponse } from './usuario-response';

export interface LoginResponse {
  token: string;
  tipoToken?: string;
  tokenType?: string;
  usuario: UsuarioResponse;
}