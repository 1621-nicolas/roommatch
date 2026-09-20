import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { Subscription, finalize } from 'rxjs';
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
  templateUrl: './register.html',
  styleUrl: '../auth-form.css'
})
export class Register implements OnDestroy {
  private request?: Subscription;
  private readonly changeDetector = inject(ChangeDetectorRef);

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

  ngOnDestroy(): void { this.request?.unsubscribe(); }

  crearCuenta(): void {
    if (this.cargando) return;
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

    if (!Number.isInteger(this.registroData.edad) || this.registroData.edad < 18 || this.registroData.edad > 120) {
      this.mensajeError = 'Ingresa una edad entre 18 y 120 años';
      return;
    }

    if (!this.emailValido(this.registroData.email.trim())) {
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

    this.request = this.authService.registrar(request).pipe(finalize(() => { this.cargando = false; this.changeDetector.markForCheck(); })).subscribe({
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
          const errores = typeof error.error.data === 'object' ? Object.values(error.error.data).filter((value): value is string => typeof value === 'string') : []; 
          this.mensajeError = errores.join('. ') || 'No se pudo crear la cuenta. Revisa los datos e inténtalo otra vez.';
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
