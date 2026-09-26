import { activeToken, clearSession, sessionUser } from './session-storage';

const now = 1_800_000_000_000;
const token = (payload: unknown) => `eyJhbGciOiJIUzI1NiJ9.${btoa(JSON.stringify(payload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')}.signature`;
const user = {idUsuario: 1, nombres: 'Ana', apellidos: 'Pérez', email: 'ana@example.test', rol: {nombreRol: 'USUARIO'}};

describe('Session storage boundary', () => {
  beforeEach(() => clearSession());
  afterEach(() => clearSession());

  it('accepts an unexpired token and rejects the exact expiration boundary', () => {
    const jwt = token({sub: '1', exp: now / 1000 + 1});
    localStorage.setItem('roommatch_token', jwt);
    expect(activeToken(now)).toBe(jwt);
    expect(activeToken(now + 1000)).toBeNull();
  });

  it('clears both values for malformed tokens or missing numeric expiration', () => {
    for (const jwt of ['not-a-token', 'a.???.b', token({}), token({exp: '9999999999'}), token(null), token({exp: now / 1000 - 1})]) {
      localStorage.setItem('roommatch_token', jwt);
      localStorage.setItem('roommatch_usuario', JSON.stringify(user));
      expect(activeToken(now)).toBeNull();
      expect(localStorage.getItem('roommatch_usuario')).toBeNull();
    }
  });

  it('validates stored user shape rather than trusting JSON parsing', () => {
    for (const value of [[], null, 1, 'user', {...user, idUsuario: -1}, {...user, rol: []}, {...user, email: undefined}]) {
      localStorage.setItem('roommatch_usuario', JSON.stringify(value));
      expect(sessionUser()).toBeNull();
    }
    localStorage.setItem('roommatch_usuario', '{broken');
    expect(sessionUser()).toBeNull();
    for (const rol of ['USUARIO', {nombreRol: 'USUARIO'}]) {
      localStorage.setItem('roommatch_usuario', JSON.stringify({...user, rol}));
      expect(sessionUser()?.idUsuario).toBe(1);
    }
  });
});
