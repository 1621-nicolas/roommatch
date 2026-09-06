import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

export const propietarioGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.estaAutenticado()) {
    return router.createUrlTree(['/login']);
  }

  const rol = authService.getRol()?.toUpperCase();

  return rol === 'PROPIETARIO' || rol === 'ADMIN'
    ? true
    : router.createUrlTree(['/propietario/registro']);
};
