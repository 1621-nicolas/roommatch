import { ChangeDetectorRef, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, finalize, Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { PropietarioContextService } from '../../../core/services/propietario-context.service';
import { PropietarioResponse } from '../../../core/models/propietario-response';

@Component({selector: 'app-navbar', imports: [RouterLink, RouterLinkActive], templateUrl: './navbar.html', styleUrl: './navbar.css'})
export class Navbar implements OnInit, OnDestroy {
  propietario: PropietarioResponse | null = null;
  estadoPropietarioVerificado = false;
  verificandoPropietario = false;
  errorPropietario = false;
  menuUsuarioAbierto = false;
  menuMovilAbierto = false;
  private readonly subscriptions = new Subscription();
  private readonly element = inject(ElementRef<HTMLElement>);
  private readonly changeDetector = inject(ChangeDetectorRef);
  @ViewChild('accountTrigger') accountTrigger?: ElementRef<HTMLButtonElement>;
  @ViewChild('mobileTrigger') mobileTrigger?: ElementRef<HTMLButtonElement>;

  constructor(public authService: AuthService, private propietarioContext: PropietarioContextService, private router: Router) {}

  ngOnInit(): void {
    this.subscriptions.add(this.propietarioContext.propietario$.subscribe(value => { this.propietario = value; this.changeDetector.markForCheck(); }));
    this.subscriptions.add(this.propietarioContext.verificado$.subscribe(value => { this.estadoPropietarioVerificado = value; this.changeDetector.markForCheck(); }));
    this.sincronizarEstadoPropietario();
    this.subscriptions.add(this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.sincronizarEstadoPropietario(); this.cerrarMenus(); this.changeDetector.markForCheck();
    }));
  }
  ngOnDestroy(): void { this.subscriptions.unsubscribe(); }

  sincronizarEstadoPropietario(): void {
    if (!this.authService.getUsuario()) { this.propietarioContext.limpiar(); this.errorPropietario = false; return; }
    if (this.estadoPropietarioVerificado || this.verificandoPropietario) return;
    this.verificandoPropietario = true; this.errorPropietario = false;
    this.subscriptions.add(this.propietarioContext.verificarPropietario().pipe(finalize(() => {
      this.verificandoPropietario = false; this.changeDetector.markForCheck();
    })).subscribe({error: () => this.errorPropietario = true}));
  }
  esPropietario(): boolean { return this.propietario !== null; }
  toggleMenuUsuario(): void {
    this.menuUsuarioAbierto = !this.menuUsuarioAbierto; this.menuMovilAbierto = false;
    if (this.menuUsuarioAbierto) this.sincronizarEstadoPropietario();
  }
  toggleMenuMovil(): void { this.menuMovilAbierto = !this.menuMovilAbierto; this.menuUsuarioAbierto = false; }
  cerrarMenus(): void { this.menuUsuarioAbierto = false; this.menuMovilAbierto = false; }
  @HostListener('document:click', ['$event']) outsideClick(event: MouseEvent): void {
    if (event.target instanceof Node && !this.element.nativeElement.contains(event.target)) this.cerrarMenus();
  }
  @HostListener('keydown.escape', ['$event']) escape(event: Event): void {
    if (this.menuUsuarioAbierto) { this.menuUsuarioAbierto = false; this.accountTrigger?.nativeElement.focus(); event.preventDefault(); }
    else if (this.menuMovilAbierto) { this.menuMovilAbierto = false; this.mobileTrigger?.nativeElement.focus(); event.preventDefault(); }
  }
  closeAccountOnFocusExit(event: FocusEvent): void {
    if (event.relatedTarget instanceof Node && !(event.currentTarget as HTMLElement).contains(event.relatedTarget)) this.menuUsuarioAbierto = false;
  }
  obtenerIniciales(): string { const u = this.authService.getUsuario(); return u ? `${u.nombres?.charAt(0) ?? ''}${u.apellidos?.charAt(0) ?? ''}`.toUpperCase() : 'RM'; }
  obtenerNombreCompleto(): string { const u = this.authService.getUsuario(); return u ? [u.nombres, u.apellidos].filter(Boolean).join(' ') : 'RoomMatch'; }
  obtenerEmail(): string { return this.authService.getUsuario()?.email ?? ''; }
  cerrarSesion(): void { this.cerrarMenus(); this.propietarioContext.limpiar(); this.authService.cerrarSesion(); this.router.navigate(['/']); }
}
