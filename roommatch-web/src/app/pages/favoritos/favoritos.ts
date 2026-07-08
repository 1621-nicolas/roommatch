import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  RouterLink
} from '@angular/router';

import {
  FavoritoService
} from '../../core/services/favorito.service';

import {
  SolicitudService
} from '../../core/services/solicitud.service';

import {
  FavoritoResponse
} from '../../core/models/favorito-response';

@Component({
  selector: 'app-favoritos',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './favoritos.html'
})
export class Favoritos implements OnInit {

  favoritos: FavoritoResponse[] = [];

  cargando = true;

  mensajeError = '';
  mensajeExito = '';

  favoritoEliminar: FavoritoResponse | null = null;

  usuarioSolicitud: FavoritoResponse | null = null;

  mensajeSolicitud = '';

  eliminando = false;
  enviandoSolicitud = false;

  constructor(
    private favoritoService: FavoritoService,
    private solicitudService: SolicitudService
  ) {}

  ngOnInit(): void {
    this.cargarFavoritos();
  }

  cargarFavoritos(): void {

    this.cargando = true;
    this.mensajeError = '';

    this.favoritoService
      .listarFavoritos()
      .subscribe({

        next: response => {

          this.cargando = false;

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.favoritos = response.data;

            return;
          }

          this.mensajeError =
            response.message ||
            'No se pudieron obtener los favoritos';
        },

        error: error => {

          this.cargando = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudieron cargar tus favoritos';
        }

      });
  }

  abrirEliminar(
    favorito: FavoritoResponse
  ): void {

    this.favoritoEliminar = favorito;

    this.mensajeError = '';
    this.mensajeExito = '';
  }

  cerrarEliminar(): void {

    this.favoritoEliminar = null;

    this.eliminando = false;
  }

  confirmarEliminar(): void {

    if (!this.favoritoEliminar) {
      return;
    }

    this.eliminando = true;

    const favorito = this.favoritoEliminar;

    this.favoritoService
      .eliminarFavorito(
        favorito.idUsuarioFavorito
      )
      .subscribe({

        next: response => {

          this.eliminando = false;

          if (response.status !== 'success') {

            this.mensajeError =
              response.message ||
              'No se pudo quitar el favorito';

            return;
          }

          this.favoritos =
            this.favoritos.filter(
              item =>
                item.idFavorito !==
                favorito.idFavorito
            );

          this.mensajeExito =
            `${favorito.nombres} fue eliminado de tus favoritos`;

          this.cerrarEliminar();
        },

        error: error => {

          this.eliminando = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudo quitar el favorito';
        }

      });
  }

  abrirSolicitud(
    favorito: FavoritoResponse
  ): void {

    this.usuarioSolicitud = favorito;

    this.mensajeSolicitud =
      `Hola ${favorito.nombres}, te guardé en mis favoritos porque creo que podríamos tener una buena convivencia. Me gustaría conversar contigo.`;

    this.mensajeError = '';
    this.mensajeExito = '';
  }

  cerrarSolicitud(): void {

    this.usuarioSolicitud = null;

    this.mensajeSolicitud = '';

    this.enviandoSolicitud = false;
  }

  enviarSolicitud(): void {

    if (!this.usuarioSolicitud) {
      return;
    }

    if (!this.mensajeSolicitud.trim()) {

      this.mensajeError =
        'Escribe un mensaje para enviar la solicitud';

      return;
    }

    this.enviandoSolicitud = true;

    const usuario = this.usuarioSolicitud;

    this.solicitudService
      .enviarSolicitud(
        usuario.idUsuarioFavorito,
        {
          mensaje:
            this.mensajeSolicitud.trim()
        }
      )
      .subscribe({

        next: response => {

          this.enviandoSolicitud = false;

          if (response.status !== 'success') {

            this.mensajeError =
              response.message ||
              'No se pudo enviar la solicitud';

            return;
          }

          this.mensajeExito =
            `Solicitud enviada correctamente a ${usuario.nombres}`;

          this.cerrarSolicitud();
        },

        error: error => {

          this.enviandoSolicitud = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudo enviar la solicitud';
        }

      });
  }

  obtenerNombreCompleto(
    favorito: FavoritoResponse
  ): string {

    return `${favorito.nombres} ${favorito.apellidos}`;
  }

  obtenerIniciales(
    favorito: FavoritoResponse
  ): string {

    const nombre =
      favorito.nombres?.charAt(0) ?? '';

    const apellido =
      favorito.apellidos?.charAt(0) ?? '';

    return (
      nombre + apellido
    ).toUpperCase();
  }

  formatearFecha(
    fecha: string
  ): string {

    if (!fecha) {
      return '';
    }

    return new Date(
      fecha
    ).toLocaleDateString(
      'es-PE',
      {
        day: '2-digit',
        month: 'long',
        year: 'numeric'
      }
    );
  }
}