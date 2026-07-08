export interface ContactoUsuarioRequest {
  telefono: string | null;
  whatsapp: string | null;
  instagram: string | null;
  facebook: string | null;
  emailContacto: string | null;

  mostrarTelefono: boolean;
  mostrarWhatsapp: boolean;
  mostrarInstagram: boolean;
  mostrarFacebook: boolean;
  mostrarEmail: boolean;
}