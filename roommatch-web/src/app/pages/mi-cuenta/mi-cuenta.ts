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
  forkJoin,
  of,
  throwError
} from 'rxjs';

import {
  catchError,
  finalize
} from 'rxjs/operators';

import {
  UsuarioService
} from '../../core/services/usuario.service';

import {
  PerfilService
} from '../../core/services/perfil.service';

import {
  ContactoService
} from '../../core/services/contacto.service';

import {
  AuthService
} from '../../core/services/auth.service';

import {
  ActualizarUsuarioRequest
} from '../../core/models/actualizar-usuario-request';

import {
  ContactoUsuarioRequest
} from '../../core/models/contacto-usuario-request';


@Component({
  selector: 'app-mi-cuenta',

  imports: [
    FormsModule,
    RouterLink
  ],

  templateUrl: './mi-cuenta.html'
})
export class MiCuenta implements OnInit {

  pestanaActiva:
    'datos' |
    'perfil' |
    'contacto' = 'datos';


  /*
   * DATOS PERSONALES
   */
  usuarioData: ActualizarUsuarioRequest = {

    nombres: '',

    apellidos: '',

    edad: null,

    ocupacion: null,

    universidad: null,

    foto: null

  };


  /*
   * PERFIL PÚBLICO
   */
  descripcionPersonal = '';

  perfilExiste = false;


  /*
   * CONTACTO Y PRIVACIDAD
   */
  contactoData: ContactoUsuarioRequest = {

    telefono: null,

    whatsapp: null,

    instagram: null,

    facebook: null,

    emailContacto: null,

    mostrarTelefono: false,

    mostrarWhatsapp: false,

    mostrarInstagram: false,

    mostrarFacebook: false,

    mostrarEmail: false

  };


  contactoExiste = false;


  /*
   * ESTADOS DE LA INTERFAZ
   */
  cargando = true;

  guardandoUsuario = false;

  guardandoPerfil = false;

  guardandoContacto = false;


  mensajeError = '';

  mensajeExito = '';


  constructor(
    private usuarioService: UsuarioService,
    private perfilService: PerfilService,
    private contactoService: ContactoService,
    private authService: AuthService
  ) {}


  ngOnInit(): void {

    this.cargarCuenta();

  }


  /*
   * =========================================================
   * CARGAR MI CUENTA
   * =========================================================
   */
  cargarCuenta(): void {

    if (this.cargando === false) {

      this.cargando = true;

    }


    this.mensajeError = '';

    this.mensajeExito = '';


    forkJoin({

      /*
       * Usuario obligatorio.
       */
      usuario:
        this.usuarioService
          .obtenerMiUsuario(),


      /*
       * El perfil puede no existir todavía.
       */
      perfil:
        this.perfilService
          .obtenerMiPerfil()
          .pipe(

            catchError(error => {

              if (error.status === 404) {

                return of(null);

              }


              return throwError(
                () => error
              );

            })

          ),


      /*
       * Los datos de contacto también
       * pueden no existir todavía.
       */
      contacto:
        this.contactoService
          .obtenerMiContacto()
          .pipe(

            catchError(error => {

              if (error.status === 404) {

                return of(null);

              }


              return throwError(
                () => error
              );

            })

          )

    })
    .pipe(

      /*
       * Siempre terminamos el estado
       * de carga.
       */
      finalize(() => {

        this.cargando = false;

      })

    )
    .subscribe({

      next: response => {


        /*
         * =====================================================
         * DATOS DEL USUARIO
         * =====================================================
         */
        if (
          response.usuario.status === 'success' &&
          response.usuario.data
        ) {

          const usuario =
            response.usuario.data;


          this.usuarioData = {

            nombres:
              usuario.nombres ?? '',

            apellidos:
              usuario.apellidos ?? '',

            edad:
              usuario.edad ?? null,

            ocupacion:
              usuario.ocupacion ?? null,

            universidad:
              usuario.universidad ?? null,

            foto:
              usuario.foto ?? null

          };


          /*
           * Actualizamos también la información
           * almacenada en localStorage.
           *
           * Esto mantiene actualizado el navbar.
           */
          this.authService
            .actualizarUsuarioLocal(
              usuario
            );

        }


        /*
         * =====================================================
         * PERFIL DE CONVIVENCIA
         * =====================================================
         */
        if (
          response.perfil &&
          response.perfil.status === 'success' &&
          response.perfil.data
        ) {

          this.perfilExiste = true;


          this.descripcionPersonal =

            response
              .perfil
              .data
              .descripcionPersonal

            ?? '';

        } else {

          this.perfilExiste = false;

          this.descripcionPersonal = '';

        }


        /*
         * =====================================================
         * DATOS DE CONTACTO
         * =====================================================
         */
        if (
          response.contacto &&
          response.contacto.status === 'success' &&
          response.contacto.data
        ) {

          const contacto =
            response.contacto.data;


          this.contactoExiste = true;


          this.contactoData = {

            telefono:
              contacto.telefono ?? null,

            whatsapp:
              contacto.whatsapp ?? null,

            instagram:
              contacto.instagram ?? null,

            facebook:
              contacto.facebook ?? null,

            emailContacto:
              contacto.emailContacto ?? null,

            mostrarTelefono:
              contacto.mostrarTelefono ?? false,

            mostrarWhatsapp:
              contacto.mostrarWhatsapp ?? false,

            mostrarInstagram:
              contacto.mostrarInstagram ?? false,

            mostrarFacebook:
              contacto.mostrarFacebook ?? false,

            mostrarEmail:
              contacto.mostrarEmail ?? false

          };

        } else {


          /*
           * Usuario nuevo.
           *
           * Todavía no tiene registro
           * en contacto_usuario.
           */
          this.contactoExiste = false;


          this.contactoData = {

            telefono: null,

            whatsapp: null,

            instagram: null,

            facebook: null,

            emailContacto: null,

            mostrarTelefono: false,

            mostrarWhatsapp: false,

            mostrarInstagram: false,

            mostrarFacebook: false,

            mostrarEmail: false

          };

        }

      },


      error: error => {

        console.error(
          'Error cargando Mi cuenta:',
          error
        );


        this.mensajeError =
          this.obtenerMensajeError(

            error,

            'No se pudo cargar la información de tu cuenta'

          );

      }

    });

  }


  /*
   * =========================================================
   * CAMBIAR PESTAÑA
   * =========================================================
   */
  cambiarPestana(
    pestana:
      'datos' |
      'perfil' |
      'contacto'
  ): void {

    this.pestanaActiva =
      pestana;


    this.mensajeError = '';

    this.mensajeExito = '';

  }


  /*
   * =========================================================
   * GUARDAR DATOS PERSONALES
   * =========================================================
   */
  guardarDatosPersonales(): void {

    this.mensajeError = '';

    this.mensajeExito = '';


    if (
      !this.usuarioData.nombres.trim() ||
      !this.usuarioData.apellidos.trim()
    ) {

      this.mensajeError =
        'Los nombres y apellidos son obligatorios';

      return;

    }


    if (
      this.usuarioData.edad !== null &&
      (
        this.usuarioData.edad < 18 ||
        this.usuarioData.edad > 100
      )
    ) {

      this.mensajeError =
        'La edad debe estar entre 18 y 100 años';

      return;

    }


    this.guardandoUsuario = true;


    const request: ActualizarUsuarioRequest = {

      nombres:
        this.usuarioData
          .nombres
          .trim(),

      apellidos:
        this.usuarioData
          .apellidos
          .trim(),

      edad:
        this.usuarioData.edad,

      ocupacion:
        this.normalizarTexto(
          this.usuarioData.ocupacion
        ),

      universidad:
        this.normalizarTexto(
          this.usuarioData.universidad
        ),

      foto:
        this.normalizarTexto(
          this.usuarioData.foto
        )

    };


    this.usuarioService
      .actualizarMiUsuario(request)
      .pipe(

        finalize(() => {

          this.guardandoUsuario = false;

        })

      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =
              response.message ||
              'No se pudo actualizar la información';

            return;

          }


          /*
           * Actualizamos datos visibles
           * del usuario.
           */
          this.usuarioData = {

            nombres:
              response.data.nombres ?? '',

            apellidos:
              response.data.apellidos ?? '',

            edad:
              response.data.edad ?? null,

            ocupacion:
              response.data.ocupacion ?? null,

            universidad:
              response.data.universidad ?? null,

            foto:
              response.data.foto ?? null

          };


          /*
           * Actualizamos navbar.
           */
          this.authService
            .actualizarUsuarioLocal(
              response.data
            );


          this.mensajeExito =
            'Tu información personal fue actualizada correctamente';

        },


        error: error => {

          this.mensajeError =
            this.obtenerMensajeError(

              error,

              'No se pudo actualizar la información personal'

            );

        }

      });

  }


  /*
   * =========================================================
   * GUARDAR DESCRIPCIÓN
   * =========================================================
   */
  guardarDescripcion(): void {

    this.mensajeError = '';

    this.mensajeExito = '';


    if (!this.perfilExiste) {

      this.mensajeError =
        'Primero debes completar tu perfil de convivencia';

      return;

    }


    if (
      !this.descripcionPersonal.trim()
    ) {

      this.mensajeError =
        'Escribe una descripción sobre ti';

      return;

    }


    this.guardandoPerfil = true;


    this.perfilService
      .obtenerMiPerfil()
      .subscribe({

        next: response => {

          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.guardandoPerfil = false;


            this.mensajeError =
              'No se pudo consultar tu perfil';

            return;

          }


          const perfil =
            response.data;


          this.perfilService
            .actualizarPerfil({

              presupuestoMin:
                perfil.presupuestoMin,

              presupuestoMax:
                perfil.presupuestoMax,

              distritoPreferido:
                perfil.distritoPreferido,

              fechaMudanza:
                perfil.fechaMudanza,

              limpieza:
                perfil.limpieza,

              ruido:
                perfil.ruido,

              sociabilidad:
                perfil.sociabilidad,

              horario:
                perfil.horario,

              visitas:
                perfil.visitas,

              mascotas:
                perfil.mascotas,

              fumar:
                perfil.fumar,

              alcohol:
                perfil.alcohol,

              gastos:
                perfil.gastos,

              convivencia:
                perfil.convivencia,

              descripcionPersonal:
                this.descripcionPersonal.trim()

            })
            .pipe(

              finalize(() => {

                this.guardandoPerfil = false;

              })

            )
            .subscribe({

              next: updateResponse => {

                if (
                  updateResponse.status !==
                  'success'
                ) {

                  this.mensajeError =
                    updateResponse.message ||
                    'No se pudo actualizar la descripción';

                  return;

                }


                this.descripcionPersonal =
                  updateResponse
                    .data
                    ?.descripcionPersonal

                  ?? this.descripcionPersonal;


                this.mensajeExito =
                  'Tu descripción pública fue actualizada correctamente';

              },


              error: error => {

                this.mensajeError =
                  this.obtenerMensajeError(

                    error,

                    'No se pudo actualizar la descripción'

                  );

              }

            });

        },


        error: error => {

          this.guardandoPerfil = false;


          this.mensajeError =
            this.obtenerMensajeError(

              error,

              'No se pudo consultar tu perfil'

            );

        }

      });

  }


  /*
   * =========================================================
   * GUARDAR CONTACTO
   * =========================================================
   */
  guardarContacto(): void {

    this.mensajeError = '';

    this.mensajeExito = '';


    this.guardandoContacto = true;


    const request: ContactoUsuarioRequest = {

      telefono:
        this.normalizarTexto(
          this.contactoData.telefono
        ),

      whatsapp:
        this.normalizarTexto(
          this.contactoData.whatsapp
        ),

      instagram:
        this.normalizarTexto(
          this.contactoData.instagram
        ),

      facebook:
        this.normalizarTexto(
          this.contactoData.facebook
        ),

      emailContacto:
        this.normalizarTexto(
          this.contactoData.emailContacto
        ),

      mostrarTelefono:
        this.contactoData.mostrarTelefono,

      mostrarWhatsapp:
        this.contactoData.mostrarWhatsapp,

      mostrarInstagram:
        this.contactoData.mostrarInstagram,

      mostrarFacebook:
        this.contactoData.mostrarFacebook,

      mostrarEmail:
        this.contactoData.mostrarEmail

    };


    const operacion =

      this.contactoExiste

        ? this.contactoService
            .actualizarMiContacto(
              request
            )

        : this.contactoService
            .crearMiContacto(
              request
            );


    operacion
      .pipe(

        finalize(() => {

          this.guardandoContacto = false;

        })

      )
      .subscribe({

        next: response => {

          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.mensajeError =
              response.message ||
              'No se pudieron guardar los datos de contacto';

            return;

          }


          this.contactoExiste = true;


          const contacto =
            response.data;


          /*
           * Actualizamos formulario con
           * la respuesta real de la API.
           */
          this.contactoData = {

            telefono:
              contacto.telefono ?? null,

            whatsapp:
              contacto.whatsapp ?? null,

            instagram:
              contacto.instagram ?? null,

            facebook:
              contacto.facebook ?? null,

            emailContacto:
              contacto.emailContacto ?? null,

            mostrarTelefono:
              contacto.mostrarTelefono ?? false,

            mostrarWhatsapp:
              contacto.mostrarWhatsapp ?? false,

            mostrarInstagram:
              contacto.mostrarInstagram ?? false,

            mostrarFacebook:
              contacto.mostrarFacebook ?? false,

            mostrarEmail:
              contacto.mostrarEmail ?? false

          };


          this.mensajeExito =
            'Tus datos de contacto y privacidad fueron actualizados';

        },


        error: error => {

          this.mensajeError =
            this.obtenerMensajeError(

              error,

              'No se pudieron guardar los datos de contacto'

            );

        }

      });

  }


  /*
   * =========================================================
   * INICIALES
   * =========================================================
   */
  obtenerIniciales(): string {

    const nombre =
      this.usuarioData
        .nombres
        ?.charAt(0)

      ?? '';


    const apellido =
      this.usuarioData
        .apellidos
        ?.charAt(0)

      ?? '';


    return (
      nombre + apellido
    ).toUpperCase();

  }


  /*
   * =========================================================
   * NOMBRE COMPLETO
   * =========================================================
   */
  obtenerNombreCompleto(): string {

    return (

      `${this.usuarioData.nombres} ${this.usuarioData.apellidos}`

    ).trim();

  }


  /*
   * =========================================================
   * NORMALIZAR TEXTO
   * =========================================================
   */
  private normalizarTexto(
    valor: string | null
  ): string | null {

    if (
      valor === null ||
      !valor.trim()
    ) {

      return null;

    }


    return valor.trim();

  }


  /*
   * =========================================================
   * OBTENER MENSAJE DE ERROR
   * =========================================================
   */
  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    if (error.error?.data) {

      const errores =
        Object.values(
          error.error.data
        ) as string[];


      return errores.join('. ');

    }


    return (

      error.error?.message ||
      mensajeDefecto

    );

  }

}