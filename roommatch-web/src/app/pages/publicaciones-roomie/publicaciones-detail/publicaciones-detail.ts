import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import {
  PublicacionRoomieService
} from '../../../core/services/publicacion-roomie.service';

import {
  SolicitudService
} from '../../../core/services/solicitud.service';

import {
  PublicacionRoomieResponse
} from '../../../core/models/publicacion-roomie-response';


@Component({
  selector: 'app-publicaciones-detail',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './publicaciones-detail.html',
  styleUrl: './publicaciones-detail.css'
})
export class PublicacionesDetail implements OnInit {

  publicacion:
    PublicacionRoomieResponse | null = null;

  cargando = true;

  enviandoSolicitud = false;

  mensajeError = '';

  mensajeExito = '';

  mensajeSolicitud = '';


  constructor(
    private route: ActivatedRoute,
    private publicacionService: PublicacionRoomieService,
    private solicitudService: SolicitudService
  ) {}


  ngOnInit(): void {

    this.cargarPublicacion();

  }


  /*
   * =========================================================
   * CARGAR PUBLICACIÓN
   * =========================================================
   */

  cargarPublicacion(): void {

    this.cargando = true;

    this.mensajeError = '';


    const idPublicacion = Number(

      this.route.snapshot.paramMap.get('id')

    );


    if (
      !Number.isInteger(idPublicacion) ||
      idPublicacion <= 0
    ) {

      this.cargando = false;

      this.mensajeError =
        'El identificador de la publicación no es válido';

      return;

    }


    this.publicacionService
      .obtenerPorId(
        idPublicacion
      )
      .subscribe({

        next: response => {

          this.cargando = false;


          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo cargar la publicación';

            return;

          }


          this.publicacion =
            response.data;


          this.mensajeSolicitud =

            `Hola ${this.obtenerPrimerNombre(
              this.publicacion.nombreUsuario
            )}, vi tu publicación "${this.publicacion.titulo}" y creo que podríamos ser compatibles. Me gustaría conversar contigo.`;

        },


        error: error => {

          this.cargando = false;

          this.mensajeError =

            error.error?.message ||

            'No se pudo cargar la publicación Roomie';

        }

      });

  }


  /*
   * =========================================================
   * ENVIAR SOLICITUD
   * =========================================================
   */

  enviarSolicitud(): void {

    if (
      !this.publicacion ||
      this.publicacion.esMiPublicacion
    ) {

      return;

    }


    const mensaje =

      this.mensajeSolicitud.trim();


    if (
      mensaje.length < 5
    ) {

      this.mensajeError =

        'Escribe un mensaje antes de enviar la solicitud';

      return;

    }


    if (
      mensaje.length > 500
    ) {

      this.mensajeError =

        'El mensaje no puede superar los 500 caracteres';

      return;

    }


    this.enviandoSolicitud = true;

    this.mensajeError = '';

    this.mensajeExito = '';


    this.solicitudService
      .enviarSolicitud(

        this.publicacion.idUsuario,

        mensaje

      )
      .subscribe({

        next: response => {

          this.enviandoSolicitud = false;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo enviar la solicitud';

            return;

          }


          this.mensajeExito =

            `Solicitud enviada a ${this.publicacion?.nombreUsuario}. Podrás seguir su estado desde Solicitudes.`;

        },


        error: error => {

          this.enviandoSolicitud = false;


          this.mensajeError =

            error.error?.message ||

            'No se pudo enviar la solicitud de contacto';

        }

      });

  }


  /*
   * =========================================================
   * INICIALES
   * =========================================================
   */

  obtenerIniciales(
    nombreCompleto: string
  ): string {

    if (
      !nombreCompleto
    ) {

      return '';

    }


    const partes =

      nombreCompleto
        .trim()
        .split(/\s+/);


    const primera =

      partes[0]?.charAt(0) ?? '';


    const segunda =

      partes.length > 1

        ? partes[1]?.charAt(0) ?? ''

        : '';


    return (
      primera +
      segunda
    ).toUpperCase();

  }


  obtenerPrimerNombre(
    nombreCompleto: string
  ): string {

    if (
      !nombreCompleto
    ) {

      return '';

    }


    return (

      nombreCompleto
        .trim()
        .split(/\s+/)[0]

      ?? ''

    );

  }


  /*
   * =========================================================
   * TIPO PUBLICACIÓN
   * =========================================================
   */

  obtenerTextoTipo(
    tipo: string
  ): string {

    switch (
      tipo
    ) {

      case 'busco_roomie':

        return 'Busco roomie';


      case 'busco_cuarto':

        return 'Busco habitación';


      case 'busco_compartir':

        return 'Busco compartir vivienda';


      default:

        return tipo;

    }

  }


  /*
   * =========================================================
   * COINCIDENCIAS
   * =========================================================
   */

  obtenerCoincidencias(): string[] {

    if (
      !this.publicacion?.coincidencias
    ) {

      return [];

    }


    return this.publicacion
      .coincidencias
      .split(',')
      .map(
        coincidencia =>
          coincidencia.trim()
      )
      .filter(
        coincidencia =>
          coincidencia.length > 0
      );

  }


  /*
   * =========================================================
   * DIFERENCIAS
   * =========================================================
   */

  obtenerDiferencias(): string[] {

    if (
      !this.publicacion?.diferencias
    ) {

      return [];

    }


    return this.publicacion
      .diferencias
      .split(',')
      .map(
        diferencia =>
          diferencia.trim()
      )
      .filter(
        diferencia =>
          diferencia.length > 0
      );

  }


  /*
   * =========================================================
   * PRECIO
   * =========================================================
   */

  formatearPrecio(
    precio: number | null
  ): string {

    if (
      precio === null
    ) {

      return 'Sin definir';

    }


    return new Intl.NumberFormat(
      'es-PE',
      {
        style: 'currency',
        currency: 'PEN',
        maximumFractionDigits: 0
      }
    ).format(
      precio
    );

  }


  obtenerPresupuesto(): string {

    if (
      !this.publicacion
    ) {

      return '';

    }


    if (
      this.publicacion.presupuestoMin !== null &&
      this.publicacion.presupuestoMax !== null
    ) {

      return (

        this.formatearPrecio(
          this.publicacion.presupuestoMin
        )

        +

        ' - '

        +

        this.formatearPrecio(
          this.publicacion.presupuestoMax
        )

      );

    }


    if (
      this.publicacion.presupuestoMin !== null
    ) {

      return (

        'Desde '

        +

        this.formatearPrecio(
          this.publicacion.presupuestoMin
        )

      );

    }


    if (
      this.publicacion.presupuestoMax !== null
    ) {

      return (

        'Hasta '

        +

        this.formatearPrecio(
          this.publicacion.presupuestoMax
        )

      );

    }


    return 'Presupuesto por conversar';

  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */

  formatearFecha(
    fecha: string
  ): string {

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