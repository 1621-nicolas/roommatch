import { TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

@Component({ template: '<h1>Destino de prueba</h1>' })
class TestPage {}

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([{ path: 'destino', component: TestPage }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  it('renders the RoomMatch navigation for a visitor', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('nav')?.textContent).toContain('RoomMatch');
    expect(element.querySelector('a[href="/login"]')).not.toBeNull();
    expect(element.querySelector('a[href="/admin/dashboard"]')).toBeNull();
  });

  it('renders navigation destinations inside the main landmark', async () => {
    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl('/destino');
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('main h1')?.textContent).toBe('Destino de prueba');
  });
});
