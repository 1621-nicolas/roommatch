import {
  Component
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';
import {
  PropietarioContextService
} from '../../../core/services/propietario-context.service';
import {
  Router,
  RouterLink
} from '@angular/router';

import {
  PropietarioService
} from '../../../core/services/propietario.service';

import {
  PropietarioRequest
} from '../../../core/models/propietario-request';


@Component({
  selector: 'app-propietario-registro',

  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],

  templateUrl:
    './propietario-registro.html',

  styleUrl:
    './propietario-registro.css'
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
    private propietarioService:
      PropietarioService,

    private propietarioContext:
    PropietarioContextService,

  private router:
    Router
  ) {}


  seleccionarTipo(
    tipo: 'persona' | 'empresa'
  ): void {

    this.formulario.tipoPropietario =
      tipo;

    this.mensajeError = '';
    this.mensajeExito = '';


    if (
      tipo === 'persona'
    ) {

      this.formulario.ruc = null;
    }
  }


  guardar(): void {

    this.mensajeError = '';
    this.mensajeExito = '';


    if (
      !this.formulario.nombreComercial ||
      this.formulario.nombreComercial
        .trim() === ''
    ) {

      this.mensajeError =
        'Ingresa un nombre para tu perfil de propietario';

      return;
    }


    if (
      this.formulario.tipoPropietario ===
      'empresa'
    ) {

      const ruc =
        this.formulario.ruc?.trim() ?? '';


      if (
        ruc.length !== 11 ||
        !/^\d+$/.test(ruc)
      ) {

        this.mensajeError =
          'El RUC debe contener 11 números';

        return;
      }
    }


    const request:
      PropietarioRequest = {

      tipoPropietario:
        this.formulario.tipoPropietario,

      nombreComercial:
        this.formulario
          .nombreComercial
          ?.trim() ?? null,

      ruc:
        this.formulario
          .tipoPropietario === 'empresa'
          ? this.formulario.ruc?.trim() ?? null
          : null,

      descripcion:
        this.formulario
          .descripcion
          ?.trim() || null
    };


    this.guardando = true;


    this.propietarioService
      .convertirmeEnPropietario(
        request
      )
      .subscribe({

        next: response => {

  this.guardando = false;


  if (
    response.status !== 'success' ||
    !response.data
  ) {

    this.mensajeError =
      response.message ||
      'No se pudo crear tu perfil de propietario';

    return;
  }


  this.propietarioContext
    .establecerPropietario(
      response.data
    );


  this.mensajeExito =
    'Tu perfil de propietario fue creado correctamente';


  setTimeout(
    () => {

      this.router.navigate([
        '/propietario'
      ]);

    },
    700
  );
},


        error: error => {

          this.guardando = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudo crear tu perfil de propietario';
        }

      });
  }


  obtenerLongitudDescripcion(): number {

    return this.formulario
      .descripcion
      ?.length ?? 0;
  }
}