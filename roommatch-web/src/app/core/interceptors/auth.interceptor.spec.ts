import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { clearSession } from '../auth/session-storage';

const token = (sub = '1') => `eyJhbGciOiJIUzI1NiJ9.${btoa(JSON.stringify({sub, exp: Math.floor(Date.now() / 1000) + 3600})).replace(/=/g, '')}.signature`;

describe('Bearer interceptor isolation', () => {
  let client: HttpClient;
  let http: HttpTestingController;
  const navigate = vi.fn().mockResolvedValue(true);
  beforeEach(() => {
    clearSession();
    navigate.mockClear();
    TestBed.configureTestingModule({providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), {provide: Router, useValue: {navigate}}]});
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {http.verify(); clearSession();});

  it('sends the bearer token only to the configured API origin and path', () => {
    const jwt = token();
    localStorage.setItem('roommatch_token', jwt);
    for (const [url, authorized] of [['/api/habitaciones', true], ['/api', true], ['/api-other', false], ['https://example.test/api/usuarios', false], ['/api/auth/login', false], ['/api/auth/register', false]] as const) {
      client.get(url).subscribe();
      const request = http.expectOne(url);
      expect(request.request.headers.get('Authorization')).toBe(authorized ? `Bearer ${jwt}` : null);
      request.flush({});
    }
  });

  it('clears a rejected current session and returns the API error to the caller', () => {
    localStorage.setItem('roommatch_token', token());
    client.get('/api/usuarios/me').subscribe({error: error => expect(error.status).toBe(401)});
    http.expectOne('/api/usuarios/me').flush({message: 'No autenticado'}, {status: 401, statusText: 'Unauthorized'});
    expect(localStorage.getItem('roommatch_token')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], {queryParams: {reason: 'session-expired'}});
  });

  it('preserves a newer login when a previous request receives a delayed 401', () => {
    localStorage.setItem('roommatch_token', token('1'));
    client.get('/api/usuarios/me').subscribe({error: () => undefined});
    const newerToken = token('2');
    localStorage.setItem('roommatch_token', newerToken);
    http.expectOne('/api/usuarios/me').flush({}, {status: 401, statusText: 'Unauthorized'});
    expect(localStorage.getItem('roommatch_token')).toBe(newerToken);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('keeps a valid session on a forbidden resource and never sends an expired token', () => {
    const jwt = token();
    localStorage.setItem('roommatch_token', jwt);
    client.get('/api/admin/dashboard').subscribe({error: error => expect(error.status).toBe(403)});
    http.expectOne('/api/admin/dashboard').flush({}, {status: 403, statusText: 'Forbidden'});
    expect(localStorage.getItem('roommatch_token')).toBe(jwt);
    localStorage.setItem('roommatch_token', 'invalid');
    client.get('/api/habitaciones').subscribe();
    const request = http.expectOne('/api/habitaciones');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
    expect(navigate).not.toHaveBeenCalled();
  });
});
