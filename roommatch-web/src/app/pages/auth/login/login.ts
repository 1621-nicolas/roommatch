import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/login-request';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html'
})
export class Login {
  loginData: LoginRequest = {
    email: '',
    password: ''
  };

  cargando = false;
  mensajeError = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  iniciarSesion(): void {
    this.mensajeError = '';

    if (!this.loginData.email || !this.loginData.password) {
      this.mensajeError = 'Ingresa tu correo y contraseña';
      return;
    }

    this.cargando = true;

    this.authService.login(this.loginData).subscribe({
      next: response => {
        this.cargando = false;

        if (response.status !== 'success') {
          this.mensajeError = response.message || 'No se pudo iniciar sesión';
          return;
        }

        const rol = this.authService.getRol();

        if (rol === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
          return;
        }

        if (rol === 'PROPIETARIO') {
          this.router.navigate(['/propietario']);
          return;
        }

        this.router.navigate(['/perfil']);
      },
      error: error => {
        this.cargando = false;
        this.mensajeError = error.error?.message || 'No se pudo conectar con el servidor';
      }
    });
  }
}