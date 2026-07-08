export interface PropietarioRequest {

  tipoPropietario:
    'persona' | 'empresa';

  nombreComercial:
    string | null;

  ruc:
    string | null;

  descripcion:
    string | null;
}