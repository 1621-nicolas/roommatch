import { ContactoUsuarioResponse } from './contacto-usuario-response';

export interface ContactoDesbloqueadoView {

  idUsuario: number;

  nombreCompleto: string;

  contacto: ContactoUsuarioResponse | null;
    
  fechaConexion: string | null;
}