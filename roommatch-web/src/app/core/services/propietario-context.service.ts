import {
  Injectable
} from '@angular/core';

import {
  BehaviorSubject,
  Observable,
  catchError,
  map,
  of,
  tap
} from 'rxjs';

import {
  PropietarioService
} from './propietario.service';

import {
  PropietarioResponse
} from '../models/propietario-response';


@Injectable({
  providedIn: 'root'
})
export class PropietarioContextService {

  private readonly propietarioSubject =
    new BehaviorSubject<
      PropietarioResponse | null
    >(null);

  private readonly cargandoSubject =
    new BehaviorSubject<boolean>(false);

  private readonly verificadoSubject =
    new BehaviorSubject<boolean>(false);


  propietario$ =
    this.propietarioSubject.asObservable();

  cargando$ =
    this.cargandoSubject.asObservable();

  verificado$ =
    this.verificadoSubject.asObservable();


  constructor(
    private propietarioService:
      PropietarioService
  ) {}


  verificarPropietario():
    Observable<boolean> {

    this.cargandoSubject.next(true);

    return this.propietarioService
      .obtenerMiPerfil()
      .pipe(

        tap(response => {

          if (
            response.status === 'success' &&
            response.data
          ) {

            this.propietarioSubject.next(
              response.data
            );

          } else {

            this.propietarioSubject.next(
              null
            );
          }

          this.verificadoSubject.next(true);

          this.cargandoSubject.next(false);

        }),

        map(response => {

          return (
            response.status === 'success' &&
            response.data !== null
          );
        }),

        catchError(() => {

          this.propietarioSubject.next(null);

          this.verificadoSubject.next(true);

          this.cargandoSubject.next(false);

          return of(false);
        })

      );
  }


  obtenerPropietarioActual():
    PropietarioResponse | null {

    return this.propietarioSubject.value;
  }


  esPropietario(): boolean {

    return (
      this.propietarioSubject.value !== null
    );
  }


  establecerPropietario(
    propietario: PropietarioResponse
  ): void {

    this.propietarioSubject.next(
      propietario
    );

    this.verificadoSubject.next(true);
  }


  limpiar(): void {

    this.propietarioSubject.next(null);

    this.cargandoSubject.next(false);

    this.verificadoSubject.next(false);
  }
}