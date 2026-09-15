import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription, finalize } from 'rxjs';
import { ContactoService } from '../../core/services/contacto.service';
import { ContactoDesbloqueadoView } from '../../core/models/contacto-desbloqueado-view';
import { ContactoUsuarioResponse } from '../../core/models/contacto-usuario-response';
import { Pagination } from '../../shared/components/pagination/pagination';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { socialProfileUrl } from '../../core/validation/contact-links';

interface ContactLink { label: string; value: string; href: string | null; external: boolean; action: string; }
interface ContactView extends ContactoDesbloqueadoView { links: ContactLink[]; }

@Component({
  selector: 'app-contactos',
  imports: [RouterLink, Pagination, EmptyState],
  templateUrl: './contactos.html',
  styleUrl: './contactos.css'
})
export class Contactos implements OnInit, OnDestroy {
  contactos: ContactView[] = [];
  cargando = false;
  mensajeError = '';
  page = 0;
  totalPages = 0;
  totalElements = 0;
  private request?: Subscription;

  constructor(private contactoService: ContactoService) {}
  ngOnInit(): void { this.cargarContactos(); }
  ngOnDestroy(): void { this.request?.unsubscribe(); }

  cargarContactos(page = this.page): void {
    this.request?.unsubscribe();
    this.cargando = true;
    this.mensajeError = '';
    this.contactos = [];
    this.page = page;
    this.request = this.contactoService.listarDesbloqueados(page, 20)
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: result => {
          this.contactos = result.content.map(row => ({...row, links: this.contactLinks(row.contacto)}));
          this.page = result.number;
          this.totalPages = result.totalPages;
          this.totalElements = result.totalElements;
        },
        error: error => {
          this.totalPages = 0;
          this.totalElements = 0;
          this.mensajeError = error.error?.message || error.message || 'No se pudieron cargar tus contactos. Intenta de nuevo.';
        }
      });
  }

  obtenerIniciales(nombre: string): string {
    return nombre.trim().split(/\s+/).slice(0, 2).map(part => part.charAt(0)).join('').toUpperCase() || 'RM';
  }

  formatearFecha(fecha: string | null): string {
    if (!fecha || Number.isNaN(new Date(fecha).getTime())) return 'Fecha no disponible';
    return new Date(fecha).toLocaleDateString('es-PE', {day: 'numeric', month: 'long', year: 'numeric'});
  }

  private contactLinks(contact: ContactoUsuarioResponse | null): ContactLink[] {
    if (!contact) return [];
    const links: ContactLink[] = [];
    const phone = (value: string) => /^\+?[0-9 ()\-.]{7,20}$/.test(value) && value.replace(/\D/g, '').length >= 7;
    if (contact.telefono) links.push({label: 'Teléfono', value: contact.telefono,
      href: phone(contact.telefono) ? 'tel:' + contact.telefono.replace(/[^+0-9]/g, '') : null, external: false, action: 'Llamar'});
    if (contact.whatsapp) links.push({label: 'WhatsApp', value: contact.whatsapp,
      href: phone(contact.whatsapp) ? 'https://wa.me/' + contact.whatsapp.replace(/\D/g, '') : null, external: true, action: 'Abrir WhatsApp'});
    if (contact.instagram) links.push({label: 'Instagram', value: contact.instagram,
      href: socialProfileUrl(contact.instagram, 'instagram'), external: true, action: 'Ver Instagram'});
    if (contact.facebook) links.push({label: 'Facebook', value: contact.facebook,
      href: socialProfileUrl(contact.facebook, 'facebook'), external: true, action: 'Ver Facebook'});
    if (contact.emailContacto) links.push({label: 'Email de contacto', value: contact.emailContacto,
      href: /^[^\s@?&#]+@[^\s@?&#]+\.[^\s@?&#]+$/.test(contact.emailContacto) ? 'mailto:' + encodeURIComponent(contact.emailContacto) : null,
      external: false, action: 'Escribir email'});
    return links;
  }
}
