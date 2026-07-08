import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import {
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive
} from '@angular/router';

import {
  filter,
  Subscription
} from 'rxjs';

import {
  AuthService
} from '../../../core/services/auth.service';

import {
  PropietarioContextService
} from '../../../core/services/propietario-context.service';

import {
  PropietarioResponse
} from '../../../core/models/propietario-response';


@Component({
  selector: 'app-navbar',

  imports: [
    RouterLink,
    RouterLinkActive
  ],

  templateUrl: './navbar.html',
})
export class Navbar
  implements OnInit, OnDestroy {

  propietario:
    PropietarioResponse | null = null;

  estadoPropietarioVerificado = false;

  verificandoPropietario = false;

  menuUsuarioAbierto = false;


  private readonly subscriptions =
    new Subscription();


  constructor(
    public authService:
      AuthService,

    private propietarioContext:
      PropietarioContextService,

    private router:
      Router
  ) {}


  ngOnInit(): void {

    /*
     * =====================================================
     * ESCUCHAR PERFIL DE PROPIETARIO
     * =====================================================
     */

    this.subscriptions.add(

      this.propietarioContext
        .propietario$
        .subscribe(
          propietario => {

            this.propietario =
              propietario;
          }
        )

    );


    /*
     * =====================================================
     * ESCUCHAR SI YA SE VERIFICÓ EL ESTADO
     * =====================================================
     */

    this.subscriptions.add(

      this.propietarioContext
        .verificado$
        .subscribe(
          verificado => {

            this.estadoPropietarioVerificado =
              verificado;
          }
        )

    );


    /*
     * =====================================================
     * VERIFICAR SESIÓN ACTUAL
     * =====================================================
     */

    this.sincronizarEstadoPropietario();


    /*
     * =====================================================
     * DETECTAR LOGIN O CAMBIO DE RUTA
     * =====================================================
     */

    this.subscriptions.add(

      this.router.events
        .pipe(

          filter(
            evento =>
              evento instanceof NavigationEnd
          )

        )
        .subscribe(() => {

          this.sincronizarEstadoPropietario();

          this.cerrarMenuUsuario();

        })

    );
  }


  ngOnDestroy(): void {

    this.subscriptions.unsubscribe();
  }


  /*
   * =====================================================
   * ESTADO PROPIETARIO
   * =====================================================
   */

  private sincronizarEstadoPropietario():
    void {

    const usuario =
      this.authService.getUsuario();


    /*
     * NO HAY SESIÓN
     */

    if (!usuario) {

      if (
        this.propietario !== null ||
        this.estadoPropietarioVerificado
      ) {

        this.propietarioContext
          .limpiar();
      }

      return;
    }


    /*
     * YA SE VERIFICÓ
     */

    if (
      this.estadoPropietarioVerificado
    ) {

      return;
    }


    /*
     * EVITAR PETICIONES DUPLICADAS
     */

    if (
      this.verificandoPropietario
    ) {

      return;
    }


    this.verificandoPropietario = true;


    this.propietarioContext
      .verificarPropietario()
      .subscribe({

        next: () => {

          this.verificandoPropietario =
            false;
        },

        error: () => {

          this.verificandoPropietario =
            false;
        }

      });
  }


  esPropietario(): boolean {

    return this.propietario !== null;
  }


  /*
   * =====================================================
   * MENÚ USUARIO
   * =====================================================
   */

  toggleMenuUsuario(): void {

    this.menuUsuarioAbierto =
      !this.menuUsuarioAbierto;


    /*
     * VERIFICAR CUANDO EL USUARIO
     * ABRE EL MENÚ
     */

    if (
      this.menuUsuarioAbierto
    ) {

      this.sincronizarEstadoPropietario();
    }
  }


  cerrarMenuUsuario(): void {

    this.menuUsuarioAbierto = false;
  }


  /*
   * =====================================================
   * DATOS DEL USUARIO
   * =====================================================
   */

  obtenerIniciales(): string {

    const usuario =
      this.authService.getUsuario();


    if (!usuario) {

      return 'RM';
    }


    const nombre =
      usuario.nombres
        ?.charAt(0) ?? '';


    const apellido =
      usuario.apellidos
        ?.charAt(0) ?? '';


    return (
      nombre + apellido
    ).toUpperCase();
  }


  obtenerNombreCompleto(): string {

    const usuario =
      this.authService.getUsuario();


    if (!usuario) {

      return 'RoomMatch';
    }


    return [
      usuario.nombres,
      usuario.apellidos
    ]
      .filter(
        valor =>
          valor &&
          valor.trim() !== ''
      )
      .join(' ');
  }


  obtenerEmail(): string {

    const usuario =
      this.authService.getUsuario();


    return usuario?.email ?? '';
  }


  /*
   * =====================================================
   * CERRAR SESIÓN
   * =====================================================
   */

  cerrarSesion(): void {

    this.cerrarMenuUsuario();


    /*
     * LIMPIAR ESTADO PROPIETARIO
     */

    this.propietarioContext
      .limpiar();


    /*
     * CERRAR SESIÓN
     */

    this.authService
      .cerrarSesion();


    /*
     * VOLVER AL INICIO
     */

    this.router.navigate([
      '/'
    ]);
  }
}