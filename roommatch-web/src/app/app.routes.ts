import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { propietarioGuard } from './core/guards/propietario.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/home/home').then(module => module.Home) },
  { path: 'login', loadComponent: () => import('./pages/auth/login/login').then(module => module.Login) },
  { path: 'register', loadComponent: () => import('./pages/auth/register/register').then(module => module.Register) },

  { path: 'habitaciones', loadComponent: () => import('./pages/habitaciones/habitaciones').then(module => module.Habitaciones) },
  { path: 'habitaciones/:idHabitacion', loadComponent: () => import('./pages/habitacion-detalle/habitacion-detalle').then(module => module.HabitacionDetalle) },
  { path: 'publicaciones-roomie', loadComponent: () => import('./pages/publicaciones-roomie/publicaciones-list/publicaciones-list').then(module => module.PublicacionesList) },
  { path: 'publicaciones-roomie/:id', loadComponent: () => import('./pages/publicaciones-roomie/publicaciones-detail/publicaciones-detail').then(module => module.PublicacionesDetail) },

  { path: 'mi-cuenta', loadComponent: () => import('./pages/mi-cuenta/mi-cuenta').then(module => module.MiCuenta), canActivate: [authGuard] },
  { path: 'perfil', loadComponent: () => import('./pages/perfil/perfil').then(module => module.Perfil), canActivate: [authGuard] },
  { path: 'matches', loadComponent: () => import('./pages/matches/matches').then(module => module.Matches), canActivate: [authGuard] },
  { path: 'favoritos', loadComponent: () => import('./pages/favoritos/favoritos').then(module => module.Favoritos), canActivate: [authGuard] },
  { path: 'solicitudes', loadComponent: () => import('./pages/solicitudes/solicitudes').then(module => module.Solicitudes), canActivate: [authGuard] },
  { path: 'contactos', loadComponent: () => import('./pages/contactos/contactos').then(module => module.Contactos), canActivate: [authGuard] },
  { path: 'notificaciones', loadComponent: () => import('./pages/notificaciones/notificaciones').then(module => module.Notificaciones), canActivate: [authGuard] },
  { path: 'mis-publicaciones', loadComponent: () => import('./pages/publicaciones-roomie/mis-publicaciones/mis-publicaciones').then(module => module.MisPublicaciones), canActivate: [authGuard] },

  {
    path: 'propietario/registro',
    loadComponent: () => import('./pages/propietario/propietario-registro/propietario-registro').then(module => module.PropietarioRegistro),
    canActivate: [authGuard]
  },
  {
    path: 'propietario',
    loadComponent: () => import('./pages/propietario/propietario-panel/propietario-panel').then(module => module.PropietarioPanel),
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/planes',
    loadComponent: () => import('./pages/propietario/propietario-planes/propietario-planes').then(module => module.Planes),
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/habitaciones',
    loadComponent: () => import('./pages/propietario/mis-habitaciones/mis-habitaciones').then(module => module.MisHabitaciones),
    canActivate: [propietarioGuard]
  },
  {
    path: 'propietario/leads',
    loadComponent: () => import('./pages/propietario/leads/interesados').then(module => module.Interesados),
    canActivate: [propietarioGuard]
  },

  {
    path: 'admin/dashboard',
    loadComponent: () => import('./pages/admin/dashboard/dashboard').then(module => module.Dashboard),
    canActivate: [adminGuard]
  },
  {
    path: 'admin/reportes',
    loadComponent: () => import('./pages/admin/reportes/reportes').then(module => module.Reportes),
    canActivate: [adminGuard]
  },

  { path: '**', redirectTo: '' }
];
