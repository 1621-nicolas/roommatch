import {
  Component,
  OnInit
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

import {
  forkJoin,
  of
} from 'rxjs';

import {
  catchError,
  finalize,
  map,
  switchMap
} from 'rxjs/operators';

import {
  AuthService
} from '../../core/services/auth.service';

import {
  SolicitudService
} from '../../core/services/solicitud.service';

import {
  ContactoService
} from '../../core/services/contacto.service';

import {
  ContactoUsuarioResponse
} from '../../core/models/contacto-usuario-response';

import {
  SolicitudContactoResponse
} from '../../core/models/solicitud-contacto-response';


interface ContactoDesbloqueadoView {

  idUsuario: number;

  nombreCompleto: string;

  fechaConexion: string | null;

  contacto: ContactoUsuarioResponse | null;

  contactoRegistrado: boolean;

}


@Component({
  selector: 'app-contactos',

  imports: [
    RouterLink
  ],

  templateUrl: './contactos.html'
})
export class Contactos implements OnInit {

  contactos: ContactoDesbloqueadoView[] = [];


  cargando = false;


  mensajeError = '';


  constructor(
    private authService: AuthService,
    private solicitudService: SolicitudService,
    private contactoService: ContactoService
  ) {}


  ngOnInit(): void {

    /*
     * Una sola carga al iniciar.
     */
    this.cargarContactos();

  }


  /*
   * =========================================================
   * CARGAR CONTACTOS DESBLOQUEADOS
   * =========================================================
   */
  cargarContactos(): void {

    /*
     * Evitamos cargas paralelas.
     */
    if (this.cargando) {

      return;

    }


    const usuarioActual =
      this.authService.getUsuario();


    if (!usuarioActual) {

      this.contactos = [];

      this.mensajeError =
        'No se pudo identificar al usuario autenticado';

      return;

    }


    this.cargando = true;

    this.mensajeError = '';

    this.contactos = [];


    /*
     * Consultamos recibidas y enviadas
     * una única vez.
     */
    forkJoin({

      recibidas:
        this.solicitudService
          .listarRecibidas(),

      enviadas:
        this.solicitudService
          .listarEnviadas()

    })
    .pipe(

      /*
       * Convertimos solicitudes aceptadas
       * en conexiones únicas.
       */
      map(response => {

        const recibidas =
          response.recibidas.data ?? [];

        const enviadas =
          response.enviadas.data ?? [];


        return this.obtenerConexionesAceptadas(

          recibidas,

          enviadas,

          usuarioActual.idUsuario

        );

      }),


      /*
       * Por cada conexión aceptada
       * consultamos el contacto desbloqueado.
       */
      switchMap(conexiones => {

        /*
         * Sin conexiones.
         */
        if (conexiones.length === 0) {

          return of(
            [] as ContactoDesbloqueadoView[]
          );

        }


        const peticiones =

          conexiones.map(
            conexion => {


              return this.contactoService
                .obtenerContactoDesbloqueado(
                  conexion.idUsuario
                )
                .pipe(

                  /*
                   * Contacto obtenido.
                   */
                  map(response => {


                    return {

                      ...conexion,

                      contacto:
                        response.data ?? null,

                      contactoRegistrado:
                        !!response.data

                    } as ContactoDesbloqueadoView;

                  }),


                  /*
                   * Una conexión puede existir aunque
                   * el otro usuario todavía no haya
                   * registrado contacto.
                   */
                  catchError(error => {


                    console.warn(

                      'Contacto no disponible para usuario:',

                      conexion.idUsuario,

                      error.error?.message

                    );


                    return of({

                      ...conexion,

                      contacto: null,

                      contactoRegistrado: false

                    } as ContactoDesbloqueadoView);

                  })

                );

            }

          );


        /*
         * Esperamos todas las consultas.
         */
        return forkJoin(
          peticiones
        );

      }),


      /*
       * Se ejecuta tanto en éxito como
       * cuando el flujo termina por error.
       */
      finalize(() => {

        this.cargando = false;

      })

    )
    .subscribe({

      next: contactos => {


        this.contactos =
          contactos;

      },


      error: error => {


        console.error(

          'Error cargando contactos:',

          error

        );


        this.contactos = [];


        this.mensajeError =

          error.error?.message ||

          'No se pudieron cargar tus conexiones';

      }

    });

  }


  /*
   * =========================================================
   * OBTENER CONEXIONES ACEPTADAS
   * =========================================================
   */
  private obtenerConexionesAceptadas(
    recibidas: SolicitudContactoResponse[],
    enviadas: SolicitudContactoResponse[],
    idUsuarioActual: number
  ): ContactoDesbloqueadoView[] {


    const conexiones =
      new Map<
        number,
        ContactoDesbloqueadoView
      >();


    /*
     * SOLICITUDES RECIBIDAS
     *
     * Otro usuario me envió una solicitud.
     */
    recibidas
      .filter(
        solicitud =>
          solicitud.estado
            ?.toLowerCase() ===
          'aceptada'
      )
      .forEach(
        solicitud => {


          if (
            solicitud.idUsuarioReceptor !==
            idUsuarioActual
          ) {

            return;

          }


          conexiones.set(

            solicitud.idUsuarioEmisor,

            {

              idUsuario:
                solicitud.idUsuarioEmisor,

              nombreCompleto:
                solicitud.nombreEmisor,

              fechaConexion:
                solicitud.fechaRespuesta ?? null,

              contacto: null,

              contactoRegistrado: false

            }

          );

        }
      );


    /*
     * SOLICITUDES ENVIADAS
     *
     * Yo envié una solicitud.
     */
    enviadas
      .filter(
        solicitud =>
          solicitud.estado
            ?.toLowerCase() ===
          'aceptada'
      )
      .forEach(
        solicitud => {


          if (
            solicitud.idUsuarioEmisor !==
            idUsuarioActual
          ) {

            return;

          }


          conexiones.set(

            solicitud.idUsuarioReceptor,

            {

              idUsuario:
                solicitud.idUsuarioReceptor,

              nombreCompleto:
                solicitud.nombreReceptor,

              fechaConexion:
                solicitud.fechaRespuesta ?? null,

              contacto: null,

              contactoRegistrado: false

            }

          );

        }
      );


    return Array.from(
      conexiones.values()
    );

  }


  /*
   * =========================================================
   * INICIALES
   * =========================================================
   */
  obtenerIniciales(
    nombreCompleto: string
  ): string {


    if (!nombreCompleto) {

      return 'RM';

    }


    const partes =

      nombreCompleto
        .trim()
        .split(/\s+/);


    const primera =

      partes[0]
        ?.charAt(0)

      ?? '';


    const segunda =

      partes.length > 1

        ? partes[1]
            ?.charAt(0) ?? ''

        : '';


    return (

      primera + segunda

    ).toUpperCase();

  }


  /*
   * =========================================================
   * FECHA
   * =========================================================
   */
  formatearFecha(
    fecha: string | null
  ): string {


    if (!fecha) {

      return 'Fecha no disponible';

    }


    return new Date(
      fecha
    )
    .toLocaleDateString(
      'es-PE',
      {

        day: '2-digit',

        month: 'long',

        year: 'numeric'

      }
    );

  }


  /*
   * =========================================================
   * DATOS VISIBLES
   * =========================================================
   */
  tieneDatosVisibles(
    contactoView: ContactoDesbloqueadoView
  ): boolean {


    const contacto =
      contactoView.contacto;


    if (!contacto) {

      return false;

    }


    return !!(

      contacto.telefono ||

      contacto.whatsapp ||

      contacto.instagram ||

      contacto.facebook ||

      contacto.emailContacto

    );

  }


  /*
   * =========================================================
   * WHATSAPP
   * =========================================================
   */
  obtenerWhatsappUrl(
    whatsapp: string
  ): string {


    const numero =

      whatsapp.replace(
        /\D/g,
        ''
      );


    return `https://wa.me/${numero}`;

  }


  /*
   * =========================================================
   * INSTAGRAM
   * =========================================================
   */
  obtenerInstagramUrl(
    instagram: string
  ): string {


    if (
      instagram.startsWith('http://') ||
      instagram.startsWith('https://')
    ) {

      return instagram;

    }


    const usuario =

      instagram.replace(
        '@',
        ''
      );


    return (

      `https://www.instagram.com/${usuario}`

    );

  }


  /*
   * =========================================================
   * FACEBOOK
   * =========================================================
   */
  obtenerFacebookUrl(
    facebook: string
  ): string {


    if (
      facebook.startsWith('http://') ||
      facebook.startsWith('https://')
    ) {

      return facebook;

    }


    return `https://${facebook}`;

  }

}