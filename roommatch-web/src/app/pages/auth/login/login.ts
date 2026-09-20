import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { Subscription, finalize } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/login-request';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: '../auth-form.css'
})
export class Login implements OnDestroy {
  private request?: Subscription;
  private readonly changeDetector = inject(ChangeDetectorRef);
  readonly registroExitoso: boolean;
  loginData: LoginRequest = {
    email: '',
    password: ''
  };

  cargando = false;
  mensajeError = '';
  readonly sesionVencida: boolean;

  constructor(
    private authService: AuthService,
    private router: Router,
    route: ActivatedRoute
  ) { this.sesionVencida = route.snapshot.queryParamMap.get('reason') === 'session-expired'; this.registroExitoso = router.getCurrentNavigation()?.extras.state?.['registroExitoso'] === true; }

  ngOnDestroy(): void { this.request?.unsubscribe(); }

  iniciarSesion(): void {
    if (this.cargando) return;
    this.mensajeError = '';

    if (!this.loginData.email || !this.loginData.password) {
      this.mensajeError = 'Ingresa tu correo y contraseña';
      return;
    }

    this.cargando = true;

    this.request = this.authService.login({...this.loginData, email: this.loginData.email.trim().toLowerCase()}).pipe(finalize(() => { this.cargando = false; this.changeDetector.markForCheck(); })).subscribe({
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