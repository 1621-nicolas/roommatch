import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PerfilService } from '../../core/services/perfil.service';
import { PerfilConvivenciaRequest } from '../../core/models/perfil-convivencia-request';

@Component({
  selector: 'app-perfil',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './perfil.html'
})
export class Perfil implements OnInit {

  perfilData: PerfilConvivenciaRequest = {
    presupuestoMin: null,
    presupuestoMax: null,
    distritoPreferido: '',
    fechaMudanza: '',

    limpieza: null,
    ruido: null,
    sociabilidad: null,

    horario: '',
    visitas: '',
    mascotas: '',
    fumar: '',
    alcohol: '',
    gastos: '',
    convivencia: '',

    descripcionPersonal: ''
  };

  perfilExiste = false;

  cargando = true;
  guardando = false;

  mensajeError = '';
  mensajeExito = '';

  pasoActual = 1;

  constructor(
    private perfilService: PerfilService
  ) {}

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {

    this.cargando = true;
    this.mensajeError = '';

    this.perfilService.obtenerMiPerfil().subscribe({

      next: response => {

        this.cargando = false;

        if (
          response.status === 'success' &&
          response.data
        ) {

          this.perfilExiste = true;

          this.perfilData = {
            presupuestoMin:
              response.data.presupuestoMin,

            presupuestoMax:
              response.data.presupuestoMax,

            distritoPreferido:
              response.data.distritoPreferido ?? '',

            fechaMudanza:
              response.data.fechaMudanza ?? '',

            limpieza:
              response.data.limpieza,

            ruido:
              response.data.ruido,

            sociabilidad:
              response.data.sociabilidad,

            horario:
              response.data.horario ?? '',

            visitas:
              response.data.visitas ?? '',

            mascotas:
              response.data.mascotas ?? '',

            fumar:
              response.data.fumar ?? '',

            alcohol:
              response.data.alcohol ?? '',

            gastos:
              response.data.gastos ?? '',

            convivencia:
              response.data.convivencia ?? '',

            descripcionPersonal:
              response.data.descripcionPersonal ?? ''
          };
        }
      },

      error: error => {

        this.cargando = false;

        /*
         * El backend responde error cuando el usuario
         * todavía no tiene perfil.
         *
         * No lo trataremos como un error visual grave.
         */
        const mensaje =
          error.error?.message ?? '';

        if (
          mensaje
            .toLowerCase()
            .includes('perfil')
        ) {

          this.perfilExiste = false;
          return;
        }

        this.mensajeError =
          mensaje ||
          'No se pudo consultar el perfil';
      }

    });
  }

  siguientePaso(): void {

    this.mensajeError = '';

    if (!this.validarPasoActual()) {
      return;
    }

    if (this.pasoActual < 4) {
      this.pasoActual++;
    }
  }

  pasoAnterior(): void {

    this.mensajeError = '';

    if (this.pasoActual > 1) {
      this.pasoActual--;
    }
  }

  irAlPaso(paso: number): void {

    if (paso < 1 || paso > 4) {
      return;
    }

    this.pasoActual = paso;
    this.mensajeError = '';
  }

  guardarPerfil(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.validarFormularioCompleto()) {
      return;
    }

    this.guardando = true;

    const request: PerfilConvivenciaRequest = {

  presupuestoMin:
    this.perfilData.presupuestoMin,

  presupuestoMax:
    this.perfilData.presupuestoMax,

  distritoPreferido:
    this.perfilData.distritoPreferido.trim(),

  fechaMudanza:
    this.perfilData.fechaMudanza,

  limpieza:
    this.perfilData.limpieza,

  ruido:
    this.perfilData.ruido,

  sociabilidad:
    this.perfilData.sociabilidad,

  horario:
    this.perfilData.horario,

  visitas:
    this.perfilData.visitas,

  mascotas:
    this.perfilData.mascotas,

  fumar:
    this.perfilData.fumar,

  alcohol:
    this.perfilData.alcohol,

  gastos:
    this.perfilData.gastos,

  convivencia:
    this.perfilData.convivencia,

  descripcionPersonal:
    (
      this.perfilData.descripcionPersonal ?? ''
    ).trim() || null

};
    const operacion = this.perfilExiste
      ? this.perfilService.actualizarPerfil(request)
      : this.perfilService.crearPerfil(request);

    operacion.subscribe({

      next: response => {

        this.guardando = false;

        if (response.status !== 'success') {

          this.mensajeError =
            response.message ||
            'No se pudo guardar el perfil';

          return;
        }

        this.perfilExiste = true;

        this.mensajeExito =
          'Tu perfil de convivencia se guardó correctamente';
      },

      error: error => {

        this.guardando = false;

        if (error.error?.data) {

          const errores = Object.values(
            error.error.data
          ) as string[];

          this.mensajeError =
            errores.join('. ');

          return;
        }

        this.mensajeError =
          error.error?.message ||
          'No se pudo guardar el perfil';
      }

    });
  }

  porcentajeProgreso(): number {
    return this.pasoActual * 25;
  }

  private validarPasoActual(): boolean {

    if (this.pasoActual === 1) {

      if (
        this.perfilData.presupuestoMin === null ||
        this.perfilData.presupuestoMax === null ||
        !this.perfilData.distritoPreferido ||
        !this.perfilData.fechaMudanza
      ) {

        this.mensajeError =
          'Completa los datos de presupuesto, distrito y fecha de mudanza';

        return false;
      }

      if (
        this.perfilData.presupuestoMax <
        this.perfilData.presupuestoMin
      ) {

        this.mensajeError =
          'El presupuesto máximo no puede ser menor al presupuesto mínimo';

        return false;
      }
    }

    if (this.pasoActual === 2) {

      if (
        this.perfilData.limpieza === null ||
        this.perfilData.ruido === null ||
        this.perfilData.sociabilidad === null ||
        !this.perfilData.horario
      ) {

        this.mensajeError =
          'Completa tus hábitos de convivencia';

        return false;
      }
    }

    if (this.pasoActual === 3) {

      if (
        !this.perfilData.visitas ||
        !this.perfilData.mascotas ||
        !this.perfilData.fumar ||
        !this.perfilData.alcohol ||
        !this.perfilData.gastos ||
        !this.perfilData.convivencia
      ) {

        this.mensajeError =
          'Completa tus preferencias de convivencia';

        return false;
      }
    }

    return true;
  }

  private validarFormularioCompleto(): boolean {

    for (
      let paso = 1;
      paso <= 3;
      paso++
    ) {

      this.pasoActual = paso;

      if (!this.validarPasoActual()) {
        return false;
      }
    }

    if (!((this.perfilData.descripcionPersonal ?? '').trim())) {
      this.mensajeError = 'Escribe una descripción sobre ti';
      return false;
    }

    this.pasoActual = 4;
    return true;
  }
}