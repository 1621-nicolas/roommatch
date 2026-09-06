import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { RegistroRequest } from '../models/registro-request';
import { ApiResponse } from '../models/api-response';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { UsuarioResponse } from '../models/usuario-response';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'roommatch_token';
  private readonly USER_KEY = 'roommatch_usuario';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http
      .post<ApiResponse<LoginResponse>>(
        `${API_BASE_URL}/auth/login`,
        request
      )
      .pipe(
        tap(response => {
          if (response.status === 'success' && response.data) {
            this.guardarSesion(response.data);
          }
        })
      );
  }

  registrar(request: RegistroRequest): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.post<ApiResponse<UsuarioResponse>>(
      `${API_BASE_URL}/auth/register`,
      request
    );
  }

  guardarSesion(data: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, data.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(data.usuario));
  }

  actualizarUsuarioLocal(usuario: UsuarioResponse): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(usuario));
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUsuario(): UsuarioResponse | null {
    const usuarioStorage = localStorage.getItem(this.USER_KEY);

    if (!usuarioStorage) {
      return null;
    }

    try {
      return JSON.parse(usuarioStorage) as UsuarioResponse;
    } catch {
      this.cerrarSesion();
      return null;
    }
  }

  getNombreUsuario(): string {
    const usuario = this.getUsuario();
    return usuario ? `${usuario.nombres} ${usuario.apellidos}`.trim() : '';
  }

  getRol(): string | null {
    const usuario = this.getUsuario();

    if (!usuario || !usuario.rol) {
      return null;
    }

    if (typeof usuario.rol === 'string') {
      return usuario.rol;
    }

    return usuario.rol.nombreRol ?? null;
  }

  estaAutenticado(): boolean {
    return Boolean(this.getToken());
  }

  cerrarSesion(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }
}
