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
import { HabitacionDetalle } from './pages/habitacion-detalle/habitacion-detalle';
import { PropietarioPanel } from './pages/propietario/propietario-panel/propietario-panel';
import { Planes } from './pages/propietario/propietario-planes/propietario-planes';
import { MisHabitaciones } from './pages/propietario/mis-habitaciones/mis-habitaciones';
import { Interesados } from './pages/propietario/leads/interesados';
import { PropietarioRegistro } from './pages/propietario/propietario-registro/propietario-registro';
import { PublicacionesList } from './pages/publicaciones-roomie/publicaciones-list/publicaciones-list';
import { PublicacionesDetail } from './pages/publicaciones-roomie/publicaciones-detail/publicaciones-detail';
import { MisPublicaciones } from './pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones';
import { Dashboard } from './pages/admin/dashboard/dashboard';
import { Reportes } from './pages/admin/reportes/reportes';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { propietarioGuard } from './core/guards/propietario.guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },

  { path: 'habitaciones', component: Habitaciones },
  { path: 'habitaciones/:idHabitacion', component: HabitacionDetalle },
  { path: 'publicaciones-roomie', component: PublicacionesList },
  { path: 'publicaciones-roomie/:id', component: PublicacionesDetail },

  { path: 'mi-cuenta', component: MiCuenta, canActivate: [authGuard] },
  { path: 'perfil', component: Perfil, canActivate: [authGuard] },
  { path: 'matches', component: Matches, canActivate: [authGuard] },
  { path: 'favoritos', component: Favoritos, canActivate: [authGuard] },
  { path: 'solicitudes', component: Solicitudes, canActivate: [authGuard] },
  { path: 'contactos', component: Contactos, canActivate: [authGuard] },
  { path: 'notificaciones', component: Notificaciones, canActivate: [authGuard] },
  { path: 'mis-publicaciones', component: MisPublicaciones, canActivate: [authGuard] },

  {
    path: 'propietario/registro',
    component: PropietarioRegistro,
    canActivate: [authGuard]
  },
  {
    path: 'propietario',
    component: PropietarioPanel,
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/planes',
    component: Planes,
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/habitaciones',
    component: MisHabitaciones,
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/leads',
    component: Interesados,
    canActivate: [propietarioGuard]
  },

  {
    path: 'admin/dashboard',
    component: Dashboard,
    canActivate: [adminGuard]
  },
  {
    path: 'admin/reportes',
    component: Reportes,
    canActivate: [adminGuard]
  },

  { path: '**', redirectTo: '' }
];
