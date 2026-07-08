import {
  HttpInterceptorFn
} from '@angular/common/http';


export const authInterceptor: HttpInterceptorFn = (
  request,
  next
) => {

  const token =
    localStorage.getItem(
      'roommatch_token'
    );


  const esApiRoomMatch =
    request.url.startsWith(
      'http://localhost:8081/api/'
    );


  const esEndpointAuth =
    request.url.includes(
      '/api/auth/'
    );


  /*
   * No modificamos:
   *
   * - Peticiones sin sesión
   * - Peticiones externas
   * - Login
   * - Registro
   */
  if (
    !token ||
    !esApiRoomMatch ||
    esEndpointAuth
  ) {

    return next(request);
  }


  const requestAutenticada =
    request.clone({

      setHeaders: {

        Authorization:
          `Bearer ${token}`

      }

    });


  return next(
    requestAutenticada
  );
};