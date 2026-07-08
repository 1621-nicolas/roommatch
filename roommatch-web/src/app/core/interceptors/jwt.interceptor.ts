import {
  HttpInterceptorFn
} from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (
  request,
  next
) => {

  const token =
    localStorage.getItem('token');


  if (!token) {

    return next(request);
  }


  const requestConToken =
    request.clone({

      setHeaders: {

        Authorization:
          `Bearer ${token}`

      }

    });


  return next(requestConToken);
};