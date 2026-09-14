import { UsuarioResponse } from '../models/usuario-response';

const TOKEN_KEY = 'roommatch_token';
const USER_KEY = 'roommatch_usuario';

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

/** Expiration is a UI check only. The API verifies signature, issuer, audience and account status. */
export function activeToken(now = Date.now()): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (token.length > 4096 || parts.length !== 3 || parts.some(part => !/^[A-Za-z0-9_-]+$/.test(part))) throw new Error('Malformed token');
    const payload = JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')), char => char.charCodeAt(0))));
    if (!payload || typeof payload.exp !== 'number' || !Number.isFinite(payload.exp) || payload.exp * 1000 <= now) throw new Error('Expired token');
    return token;
  } catch {
    clearSession();
    return null;
  }
}

export function sessionUser(): UsuarioResponse | null {
  const stored = localStorage.getItem(USER_KEY);
  if (!stored) return null;
  try {
    const value: unknown = JSON.parse(stored);
    if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('Invalid session');
    const user = value as Record<string, unknown>;
    if (!Number.isInteger(user['idUsuario']) || Number(user['idUsuario']) < 1 || typeof user['nombres'] !== 'string'
        || typeof user['apellidos'] !== 'string' || typeof user['email'] !== 'string') throw new Error('Invalid user');
    const role = user['rol'];
    if (typeof role !== 'string' && (!role || typeof role !== 'object' || Array.isArray(role)
        || typeof (role as Record<string, unknown>)['nombreRol'] !== 'string')) throw new Error('Invalid role');
    return value as UsuarioResponse;
  } catch {
    clearSession();
    return null;
  }
}
