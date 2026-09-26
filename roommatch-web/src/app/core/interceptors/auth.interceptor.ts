import { inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { activeToken, clearSession } from '../auth/session-storage';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const document = inject(DOCUMENT);
  const router = inject(Router);
  const base = new URL(API_BASE_URL, document.baseURI);
  let target: URL;
  try { target = new URL(request.url, document.baseURI); }
  catch { return next(request); }
  const isApi = target.origin === base.origin && (target.pathname === base.pathname || target.pathname.startsWith(`${base.pathname}/`));
  const isLogin = target.pathname === `${base.pathname}/auth/login` || target.pathname === `${base.pathname}/auth/register`;
  if (!isApi || isLogin) return next(request);
  const token = activeToken();
  const authenticated = token ? request.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : request;
  return next(authenticated).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse && error.status === 401 && token && activeToken() === token) {
      // A delayed 401 from an older session must not clear a newly established session.
      clearSession();
      void router.navigate(['/login'], {queryParams: {reason: 'session-expired'}});
    }
    return throwError(() => error);
  }));
};
