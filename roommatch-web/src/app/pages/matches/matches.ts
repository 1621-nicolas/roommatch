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
  MatchService
} from '../../core/services/match.service';

import {
  FavoritoService
} from '../../core/services/favorito.service';

import {
  SolicitudService
} from '../../core/services/solicitud.service';

import {
  MatchResponse
} from '../../core/models/match-response';


@Component({
  selector: 'app-matches',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './matches.html'
})
export class Matches implements OnInit {

  matches: MatchResponse[] = [];

  porcentajeMinimo: number | null = null;

  page = 0;
  size = 10;

  totalPages = 0;
  totalElements = 0;

  cargando = true;
  calculando = false;

  mensajeError = '';
  mensajeExito = '';

  usuarioSolicitud: MatchResponse | null = null;

  mensajeSolicitud = '';

  enviandoSolicitud = false;


  constructor(
    private matchService: MatchService,
    private favoritoService: FavoritoService,
    private solicitudService: SolicitudService
  ) {}


  ngOnInit(): void {
    this.cargarMatches();
  }


  cargarMatches(): void {

    this.cargando = true;

    this.mensajeError = '';

    this.matchService
      .listarMatches(
        this.porcentajeMinimo,
        this.page,
        this.size
      )
      .subscribe({

        next: response => {

          this.cargando = false;

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.matches =
              response.data.content;

            this.totalPages =
              response.data.totalPages;

            this.totalElements =
              response.data.totalElements;

            return;
          }

          this.mensajeError =
            response.message ||
            'No se pudieron obtener los matches';
        },

        error: error => {

          this.cargando = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudieron cargar los matches';
        }

      });
  }


  calcularCompatibilidad(): void {

    this.calculando = true;

    this.mensajeError = '';
    this.mensajeExito = '';

    this.matchService
      .calcularMatches()
      .subscribe({

        next: response => {

          this.calculando = false;

          if (response.status !== 'success') {

            this.mensajeError =
              response.message ||
              'No se pudo calcular la compatibilidad';

            return;
          }

          this.mensajeExito =
            'Compatibilidad calculada correctamente';

          this.page = 0;

          this.cargarMatches();
        },

        error: error => {

          this.calculando = false;

          this.mensajeError =
            error.error?.message ||
            'No se pudo calcular la compatibilidad';
        }

      });
  }


  aplicarFiltro(): void {

    this.page = 0;

    this.mensajeExito = '';

    this.cargarMatches();
  }


  limpiarFiltro(): void {

    this.porcentajeMinimo = null;

    this.page = 0;

    this.cargarMatches();
  }


  agregarFavorito(
    match: MatchResponse
  ): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    this.favoritoService
      .agregarFavorito(
        match.idUsuarioDestino
      )
      .subscribe({

        next: response => {

          if (response.status !== 'success') {

            this.mensajeError =
              response.message ||
              'No se pudo agregar a favoritos';

            return;
          }

          this.mensajeExito =
            `${match.nombres} fue agregado a tus favoritos`;
        },

        error: error => {

          this.mensajeError =
            error.error?.message ||
            'No se pudo agregar a favoritos';
        }

      });
  }


  abrirSolicitud(
    match: MatchResponse
  ): void {

    this.usuarioSolicitud = match;

    this.mensajeSolicitud =
      `Hola ${match.nombres}, tenemos una buena compatibilidad en RoomMatch y me gustaría conversar contigo.`;

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

    this.mensajeError = '';
    this.mensajeExito = '';

    const match =
      this.usuarioSolicitud;

    this.solicitudService
      .enviarSolicitud(
        match.idUsuarioDestino,
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
            `Solicitud enviada correctamente a ${match.nombres}`;

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


  paginaAnterior(): void {

    if (this.page <= 0) {
      return;
    }

    this.page--;

    this.cargarMatches();
  }


  paginaSiguiente(): void {

    if (
      this.page >=
      this.totalPages - 1
    ) {
      return;
    }

    this.page++;

    this.cargarMatches();
  }


  obtenerNombreCompleto(
    match: MatchResponse
  ): string {

    return `${match.nombres} ${match.apellidos}`;
  }


  obtenerIniciales(
    match: MatchResponse
  ): string {

    const nombre =
      match.nombres?.charAt(0) ?? '';

    const apellido =
      match.apellidos?.charAt(0) ?? '';

    return (
      nombre + apellido
    ).toUpperCase();
  }


  obtenerNivelCompatibilidad(
    porcentaje: number
  ): string {

    if (porcentaje >= 90) {
      return 'Excelente match';
    }

    if (porcentaje >= 80) {
      return 'Alta compatibilidad';
    }

    if (porcentaje >= 70) {
      return 'Buen match';
    }

    if (porcentaje >= 50) {
      return 'Compatibilidad media';
    }

    return 'Compatibilidad baja';
  }


  obtenerClaseCompatibilidad(
    porcentaje: number
  ): string {

    if (porcentaje >= 90) {
      return 'match-excellent';
    }

    if (porcentaje >= 80) {
      return 'match-high';
    }

    if (porcentaje >= 70) {
      return 'match-good';
    }

    if (porcentaje >= 50) {
      return 'match-medium';
    }

    return 'match-low';
  }


  separarCriterios(
    texto: string
  ): string[] {

    if (!texto) {
      return [];
    }

    return texto
      .split(',')
      .map(
        criterio =>
          criterio.trim()
      )
      .filter(
        criterio =>
          criterio.length > 0
      );
  }
}