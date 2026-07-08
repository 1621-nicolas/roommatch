import { Routes } from '@angular/router';

import { Home } from './pages/home/home';

import { Login } from './pages/auth/login/login';

import { Register } from './pages/auth/register/register';

import { MiCuenta } from './pages/mi-cuenta/mi-cuenta';

import { Perfil } from './pages/perfil/perfil';

import { Matches } from './pages/matches/matches';

import { Favoritos } from './pages/favoritos/favoritos';

import { Solicitudes } from './pages/solicitudes/solicitudes';

import { Contactos } from './pages/contactos/contactos';

import { Notificaciones } from './pages/notificaciones/notificaciones';

import { Habitaciones } from './pages/habitaciones/habitaciones';
import {
  HabitacionDetalle
} from './pages/habitacion-detalle/habitacion-detalle';

import { PropietarioPanel } from './pages/propietario/propietario-panel/propietario-panel';

import {Planes } from './pages/propietario/propietario-planes/propietario-planes';

import { MisHabitaciones } from './pages/propietario/mis-habitaciones/mis-habitaciones';

import { Interesados } from './pages/propietario/leads/interesados';
import {
  PropietarioRegistro
} from './pages/propietario/propietario-registro/propietario-registro';

import { PublicacionesList } from './pages/publicaciones-roomie/publicaciones-list/publicaciones-list';

import { PublicacionesDetail } from './pages/publicaciones-roomie/publicaciones-detail/publicaciones-detail';

import { MisPublicaciones } from './pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones';


import { Dashboard } from './pages/admin/dashboard/dashboard';

import { Reportes } from './pages/admin/reportes/reportes';


export const routes: Routes = [

  /*
   * =========================================================
   * PÁGINAS PÚBLICAS
   * =========================================================
   */

  {
    path: '',
    component: Home
  },

  {
    path: 'login',
    component: Login
  },

  {
    path: 'register',
    component: Register
  },


  /*
   * =========================================================
   * USUARIO
   * =========================================================
   */

  {
    path: 'mi-cuenta',
    component: MiCuenta
  },

  {
    path: 'perfil',
    component: Perfil
  },

  {
    path: 'matches',
    component: Matches
  },

  {
    path: 'favoritos',
    component: Favoritos
  },

  {
    path: 'solicitudes',
    component: Solicitudes
  },

  {
    path: 'contactos',
    component: Contactos
  },

  {
    path: 'notificaciones',
    component: Notificaciones
  },


  /*
   * =========================================================
   * HABITACIONES
   * =========================================================
   */

  {
    path: 'habitaciones',
    component: Habitaciones
  },
  {
  path: 'habitaciones/:idHabitacion',
  component: HabitacionDetalle
},


  /*
   * La ruta de detalle se agregará cuando
   * creemos HabitacionDetalle.
   *
   * NO agregar todavía:
   *
   * habitaciones/:idHabitacion
   */


  /*
   * =========================================================
   * PROPIETARIO
   * =========================================================
   */
  {
  path: 'propietario/registro',
  component: PropietarioRegistro
},

  {
    path: 'propietario',
    component: PropietarioPanel
  },

  {
    path: 'propietario/planes',
    component: Planes
  },

  {
    path: 'propietario/habitaciones',
    component: MisHabitaciones
  },

  {
    path: 'propietario/leads',
    component: Interesados
  },


  /*
   * =========================================================
   * PUBLICACIONES ROOMIE
   * =========================================================
   */

  {
    path: 'publicaciones-roomie',
    component: PublicacionesList
  },

  {
    path: 'publicaciones-roomie/:id',
    component: PublicacionesDetail
  },

  {
    path: 'mis-publicaciones',
    component: MisPublicaciones
  },


  /*
   * =========================================================
   * ADMINISTRACIÓN
   * =========================================================
   */

  {
    path: 'admin/dashboard',
    component: Dashboard
  },

  {
    path: 'admin/reportes',
    component: Reportes
  },


  /*
   * =========================================================
   * RUTA DESCONOCIDA
   * =========================================================
   */

  {
    path: '**',
    redirectTo: ''
  }

];