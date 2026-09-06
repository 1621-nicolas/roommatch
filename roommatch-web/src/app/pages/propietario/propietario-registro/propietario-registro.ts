import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { PropietarioContextService } from '../../../core/services/propietario-context.service';
import { PropietarioService } from '../../../core/services/propietario.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { PropietarioRequest } from '../../../core/models/propietario-request';

@Component({
  selector: 'app-propietario-registro',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './propietario-registro.html',
  styleUrl: './propietario-registro.css'
})
export class PropietarioRegistro {

  formulario: PropietarioRequest = {
    tipoPropietario: 'persona',
    nombreComercial: null,
    ruc: null,
    descripcion: null
  };

  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    private propietarioService: PropietarioService,
    private propietarioContext: PropietarioContextService,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private router: Router
  ) {}

  seleccionarTipo(tipo: 'persona' | 'empresa'): void {
    this.formulario.tipoPropietario = tipo;
    this.mensajeError = '';
    this.mensajeExito = '';

    if (tipo === 'persona') {
      this.formulario.ruc = null;
    }
  }

  guardar(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.formulario.nombreComercial?.trim()) {
      this.mensajeError = 'Ingresa un nombre para tu perfil de propietario';
      return;
    }

    if (this.formulario.tipoPropietario === 'empresa') {
      const ruc = this.formulario.ruc?.trim() ?? '';

      if (ruc.length !== 11 || !/^\d+$/.test(ruc)) {
        this.mensajeError = 'El RUC debe contener 11 números';
        return;
      }
    }

    const request: PropietarioRequest = {
      tipoPropietario: this.formulario.tipoPropietario,
      nombreComercial: this.formulario.nombreComercial.trim(),
      ruc:
        this.formulario.tipoPropietario === 'empresa'
          ? this.formulario.ruc?.trim() ?? null
          : null,
      descripcion: this.formulario.descripcion?.trim() || null
    };

    this.guardando = true;

    this.propietarioService
      .convertirmeEnPropietario(request)
      .pipe(
        switchMap(response => {
          if (response.status !== 'success' || !response.data) {
            throw new Error(
              response.message || 'No se pudo crear tu perfil de propietario'
            );
          }

          this.propietarioContext.establecerPropietario(response.data);
          return this.usuarioService.obtenerMiUsuario();
        })
      )
      .subscribe({
        next: responseUsuario => {
          this.guardando = false;

          if (responseUsuario.status !== 'success' || !responseUsuario.data) {
            this.mensajeError =
              responseUsuario.message ||
              'El perfil fue creado, pero no se pudo actualizar tu sesión';
            return;
          }

          // El backend cambió el rol a PROPIETARIO. Actualizamos localStorage
          // antes de navegar para que propietarioGuard no redirija al registro.
          this.authService.actualizarUsuarioLocal(responseUsuario.data);
          this.mensajeExito = 'Tu perfil de propietario fue creado correctamente';

          setTimeout(() => {
            this.router.navigate(['/propietario']);
          }, 500);
        },
        error: error => {
          this.guardando = false;
          this.mensajeError =
            error.error?.message ||
            error.message ||
            'No se pudo crear tu perfil de propietario';
        }
      });
  }

  obtenerLongitudDescripcion(): number {
    return this.formulario.descripcion?.length ?? 0;
  }
}
