import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { RegistroRequest } from '../../../core/models/registro-request';
import { passwordError } from '../../../core/validation/password-policy';

@Component({
  selector: 'app-register',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './register.html'
})
export class Register {

  registroData: RegistroRequest = {
    nombres: '',
    apellidos: '',
    edad: 18,
    email: '',
    password: ''
  };

  cargando = false;
  mensajeError = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  crearCuenta(): void {
    this.mensajeError = '';

    if (
      !this.registroData.nombres.trim() ||
      !this.registroData.apellidos.trim() ||
      !this.registroData.email.trim() ||
      !this.registroData.password
    ) {
      this.mensajeError = 'Completa todos los campos';
      return;
    }

    if (!Number.isInteger(this.registroData.edad) || this.registroData.edad < 18) {
      this.mensajeError = 'Debes tener al menos 18 años';
      return;
    }

    if (!this.emailValido(this.registroData.email)) {
      this.mensajeError = 'Ingresa un correo electrónico válido';
      return;
    }

    const passwordMessage = passwordError(this.registroData.password);
    if (passwordMessage) {
      this.mensajeError = passwordMessage;
      return;
    }

    this.cargando = true;

    const request: RegistroRequest = {
      nombres: this.registroData.nombres.trim(),
      apellidos: this.registroData.apellidos.trim(),
      edad: this.registroData.edad,
      email: this.registroData.email.trim().toLowerCase(),
      password: this.registroData.password
    };

    this.authService.registrar(request).subscribe({
      next: response => {
        this.cargando = false;

        if (response.status !== 'success') {
          this.mensajeError = response.message || 'No se pudo crear la cuenta';
          return;
        }

        this.router.navigate(
          ['/login'],
          {
            state: {
              registroExitoso: true
            }
          }
        );
      },

      error: error => {
        this.cargando = false;

        if (error.error?.data) {
          const errores = Object.values(error.error.data) as string[];
          this.mensajeError = errores.join('. ');
          return;
        }

        this.mensajeError = error.error?.message || 'No se pudo conectar con el servidor';
      }
    });
  }

  private emailValido(email: string): boolean {
    const expresion = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return expresion.test(email);
  }
}
