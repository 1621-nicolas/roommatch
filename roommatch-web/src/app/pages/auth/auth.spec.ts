import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { Login } from './login/login';
import { Register } from './register/register';
import { AuthService } from '../../core/services/auth.service';

const registration = {nombres: ' Ana ', apellidos: ' Prueba ', edad: 25, email: ' ANA@EXAMPLE.TEST ', password: 'Una frase larga de prueba 739!'};
describe('Authentication forms', () => {
  function setup() {
    const auth = {login: vi.fn(), registrar: vi.fn(), getRol: vi.fn().mockReturnValue('USUARIO')};
    TestBed.configureTestingModule({imports: [Login, Register], providers: [provideRouter([]), {provide: AuthService, useValue: auth}]});
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    return {auth, navigate};
  }
  it('disables duplicate login submissions and shows an asynchronous failure without navigation', () => {
    const {auth, navigate} = setup(); const pending = new Subject<any>(); auth.login.mockReturnValue(pending);
    const fixture = TestBed.createComponent(Login);
    fixture.componentInstance.loginData = {email: ' ANA@EXAMPLE.TEST ', password: 'existing-password'};
    fixture.detectChanges();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', {bubbles: true, cancelable: true}));
    fixture.componentInstance.iniciarSesion(); fixture.detectChanges();
    expect(auth.login).toHaveBeenCalledExactlyOnceWith({email: 'ana@example.test', password: 'existing-password'});
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
    pending.error({status: 401, error: {message: 'Credenciales inválidas'}}); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Credenciales inválidas');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false); expect(navigate).not.toHaveBeenCalled();
  });
  it.each([['ADMIN', '/admin/dashboard'], ['PROPIETARIO', '/propietario'], ['USUARIO', '/perfil']])('retains the destination for %s', (role, destination) => {
    const {auth, navigate} = setup(); auth.login.mockReturnValue(of({status: 'success'})); auth.getRol.mockReturnValue(role);
    const fixture = TestBed.createComponent(Login); fixture.componentInstance.loginData = {email: 'ana@example.test', password: 'existing-password'};
    fixture.componentInstance.iniciarSesion(); expect(navigate).toHaveBeenCalledWith([destination]);
  });
  it('validates age and preserves the exact password while trimming registration fields', () => {
    const {auth, navigate} = setup(); auth.registrar.mockReturnValue(of({status: 'success'}));
    const fixture = TestBed.createComponent(Register); fixture.componentInstance.registroData = {...registration, edad: 121};
    fixture.componentInstance.crearCuenta(); expect(auth.registrar).not.toHaveBeenCalled();
    fixture.componentInstance.registroData.edad = 25; fixture.componentInstance.crearCuenta();
    expect(auth.registrar).toHaveBeenCalledWith({...registration, nombres: 'Ana', apellidos: 'Prueba', email: 'ana@example.test'});
    expect(navigate).toHaveBeenCalledWith(['/login'], {state: {registroExitoso: true}});
  });
  it('cancels pending registration when leaving and prevents duplicate POSTs', () => {
    const {auth, navigate} = setup(); const pending = new Subject<any>(); auth.registrar.mockReturnValue(pending);
    const fixture = TestBed.createComponent(Register); fixture.componentInstance.registroData = {...registration};
    fixture.componentInstance.crearCuenta(); fixture.componentInstance.crearCuenta(); expect(auth.registrar).toHaveBeenCalledTimes(1);
    fixture.destroy(); expect(pending.observed).toBe(false); expect(navigate).not.toHaveBeenCalled();
  });
  it('renders recoverable registration errors without losing the draft', () => {
    const {auth} = setup(); auth.registrar.mockReturnValue(throwError(() => ({error: {data: {email: 'Correo no disponible'}}})));
    const fixture = TestBed.createComponent(Register); fixture.componentInstance.registroData = {...registration};
    fixture.componentInstance.crearCuenta(); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Correo no disponible');
    expect(fixture.componentInstance.registroData.nombres).toBe(' Ana ');
    expect(fixture.nativeElement.textContent).not.toMatch(/Camila|Diego|95%|1,250/);
    expect(fixture.nativeElement.querySelector('a[href="/habitaciones/1"]')).toBeNull();
  });
});
