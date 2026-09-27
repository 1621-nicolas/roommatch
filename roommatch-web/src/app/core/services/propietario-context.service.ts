import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject, defer, finalize, map, takeUntil } from 'rxjs';
import { PropietarioService } from './propietario.service';
import { PropietarioResponse } from '../models/propietario-response';

@Injectable({providedIn: 'root'})
export class PropietarioContextService {
  private readonly propietarioSubject = new BehaviorSubject<PropietarioResponse | null>(null);
  private readonly cargandoSubject = new BehaviorSubject(false);
  private readonly verificadoSubject = new BehaviorSubject(false);
  private readonly reset = new Subject<void>();
  private generation = 0;
  readonly propietario$ = this.propietarioSubject.asObservable();
  readonly cargando$ = this.cargandoSubject.asObservable();
  readonly verificado$ = this.verificadoSubject.asObservable();

  constructor(private propietarioService: PropietarioService) {}

  verificarPropietario(): Observable<boolean> {
    return defer(() => {
      const generation = this.generation;
      this.cargandoSubject.next(true);
      return this.propietarioService.obtenerMiPerfil().pipe(
        takeUntil(this.reset),
        map(response => {
          // The API deliberately returns success/null for a user without this capability.
          // An error response must not turn an existing owner into a new registration prompt.
          if (response?.status !== 'success' || response.data === undefined) throw new Error('No se pudo verificar el perfil de propietario.');
          this.propietarioSubject.next(response.data);
          this.verificadoSubject.next(true);
          return response.data !== null;
        }),
        finalize(() => { if (generation === this.generation) this.cargandoSubject.next(false); })
      );
    });
  }

  obtenerPropietarioActual(): PropietarioResponse | null { return this.propietarioSubject.value; }
  esPropietario(): boolean { return this.propietarioSubject.value !== null; }
  establecerPropietario(propietario: PropietarioResponse): void {
    this.generation++; this.reset.next(); this.cargandoSubject.next(false);
    this.propietarioSubject.next(propietario); this.verificadoSubject.next(true);
  }
  limpiar(): void {
    this.generation++; this.reset.next();
    this.propietarioSubject.next(null); this.cargandoSubject.next(false); this.verificadoSubject.next(false);
  }
}
