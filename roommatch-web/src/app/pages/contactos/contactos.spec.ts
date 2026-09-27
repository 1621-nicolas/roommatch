import { of, Subject, throwError } from 'rxjs';
import { Contactos } from './contactos';
import { ContactoService } from '../../core/services/contacto.service';
import { ContactoDesbloqueadoView } from '../../core/models/contacto-desbloqueado-view';
import { PageResponse } from '../../core/models/page-response';

const data = (content: ContactoDesbloqueadoView[]): PageResponse<ContactoDesbloqueadoView> => ({content,
  number: 0, size: 20, totalElements: content.length, totalPages: content.length ? 1 : 0,
  first: true, last: true, numberOfElements: content.length, empty: !content.length});

describe('Contactos loading and privacy states', () => {
  function component(listarDesbloqueados = vi.fn()) {
    return new Contactos({listarDesbloqueados} as unknown as ContactoService);
  }
  it('uses one paginated request and keeps accepted connections without stored data', () => {
    const list = vi.fn().mockReturnValue(of(data([{idUsuario: 2, nombreCompleto: 'Persona de prueba', fechaConexion: null, contacto: null}])));
    const page = component(list); page.ngOnInit();
    expect(list).toHaveBeenCalledExactlyOnceWith(0, 20);
    expect(page.contactos).toHaveLength(1); expect(page.contactos[0].contacto).toBeNull();
    expect(page.cargando).toBe(false); expect(page.mensajeError).toBe('');
  });
  it('distinguishes a server error from an empty contact book and allows retry', () => {
    const list = vi.fn().mockReturnValueOnce(throwError(() => ({status: 500, error: {message: 'Intenta de nuevo'}}))).mockReturnValueOnce(of(data([])));
    const page = component(list); page.cargarContactos();
    expect(page.mensajeError).toContain('Intenta'); expect(page.cargando).toBe(false);
    page.cargarContactos(); expect(page.mensajeError).toBe(''); expect(page.contactos).toEqual([]);
  });
  it('cancels superseded requests and the remaining request on destroy', () => {
    const old = new Subject<PageResponse<ContactoDesbloqueadoView>>(), current = new Subject<PageResponse<ContactoDesbloqueadoView>>();
    const list = vi.fn().mockReturnValueOnce(old).mockReturnValueOnce(current);
    const page = component(list); page.cargarContactos(); page.cargarContactos(1);
    expect(old.observed).toBe(false); expect(current.observed).toBe(true);
    expect(page.cargando).toBe(true); page.ngOnDestroy(); expect(current.observed).toBe(false);
  });
  it('keeps old untrusted social values as text without an external link', () => {
    const contact = {telefono: null, whatsapp: null, instagram: 'https://evil.test/person', facebook: null, emailContacto: null,
      mostrarTelefono: false, mostrarWhatsapp: false, mostrarInstagram: true, mostrarFacebook: false, mostrarEmail: false};
    const page = component(vi.fn().mockReturnValue(of(data([{idUsuario: 2, nombreCompleto: 'Persona', fechaConexion: null, contacto: contact}]))));
    page.cargarContactos(); expect(page.contactos[0].links[0].href).toBeNull();
    expect(page.formatearFecha('not a date')).toBe('Fecha no disponible');
  });
});
