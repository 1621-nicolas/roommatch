export function passwordError(value: string): string | null {
  if ([...value].length < 15) return 'Usa una frase de al menos 15 caracteres.';
  if (new TextEncoder().encode(value).length > 72) return 'La contraseña supera 72 bytes. Algunos símbolos y letras con tilde ocupan más de un byte.';
  if (new Set([...value]).size < 2 || ['passwordpassword', '123456789012345', '1234567890123456', 'qwertyuiopasdfgh'].includes(value.toLowerCase())) {
    return 'Esta contraseña es demasiado común. Elige una frase diferente.';
  }
  return null;
}
