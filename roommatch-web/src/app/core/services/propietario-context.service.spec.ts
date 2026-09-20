import { of, Subject, throwError } from 'rxjs';
import { PropietarioContextService } from './propietario-context.service';
import { PropietarioService } from './propietario.service';
import { PropietarioResponse } from '../models/propietario-response';
const owner = {idPropietario: 1, idUsuario: 2} as PropietarioResponse;
describe('Owner context session boundaries', () => {
  function setup() {
    const obtenerMiPerfil = vi.fn();
    return {obtenerMiPerfil, context: new PropietarioContextService({obtenerMiPerfil} as unknown as PropietarioService)};
  }
  it('recognizes only successful null as absence and surfaces server failures', () => {
    const {context, obtenerMiPerfil} = setup(); let verified = false, loading = true;
    context.verificado$.subscribe(value => verified = value); context.cargando$.subscribe(value => loading = value);
    obtenerMiPerfil.mockReturnValueOnce(throwError(() => ({status: 503}))).mockReturnValueOnce(of({status: 'success', data: null}));
    const error = vi.fn(); context.verificarPropietario().subscribe({error});
    expect(error).toHaveBeenCalled(); expect(verified).toBe(false); expect(loading).toBe(false);
    const result = vi.fn(); context.verificarPropietario().subscribe(result);
    expect(result).toHaveBeenCalledWith(false); expect(verified).toBe(true);
  });
  it('does not accept an unsuccessful envelope as a new non-owner', () => {
    const {context, obtenerMiPerfil} = setup(); context.establecerPropietario(owner);
    obtenerMiPerfil.mockReturnValue(of({status: 'error', data: null}));
    const error = vi.fn(); context.verificarPropietario().subscribe({error});
    expect(error).toHaveBeenCalled(); expect(context.obtenerPropietarioActual()).toBe(owner);
  });
  it('cancels an old-session response when logout clears the context', () => {
    const {context, obtenerMiPerfil} = setup(); const pending = new Subject<any>();
    obtenerMiPerfil.mockReturnValue(pending); context.verificarPropietario().subscribe();
    context.limpiar(); expect(pending.observed).toBe(false);
    pending.next({status: 'success', data: owner}); expect(context.esPropietario()).toBe(false);
  });
  it('does not overwrite a newly created owner with an older pending response', () => {
    const {context, obtenerMiPerfil} = setup(); const pending = new Subject<any>();
    obtenerMiPerfil.mockReturnValue(pending); context.verificarPropietario().subscribe();
    context.establecerPropietario(owner); pending.next({status: 'success', data: null});
    expect(pending.observed).toBe(false); expect(context.obtenerPropietarioActual()).toBe(owner);
  });
});
