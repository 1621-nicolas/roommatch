import { HttpInterceptorFn } from '@angular/common/http';

import { API_BASE_URL } from '../config/api.config';

const TOKEN_KEY = 'roommatch_token';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem(TOKEN_KEY);
  const esApiRoomMatch = request.url.startsWith(`${API_BASE_URL}/`);
  const esEndpointAuth = request.url.startsWith(`${API_BASE_URL}/auth/`);

  if (!token || !esApiRoomMatch || esEndpointAuth) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
