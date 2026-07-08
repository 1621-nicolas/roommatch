import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  RouterLink
} from '@angular/router';

import {
  LeadHabitacionService
} from '../../../core/services/lead-habitacion.service';

import {
  LeadHabitacionResponse
} from '../../../core/models/lead-habitacion-response';


@Component({
  selector: 'app-interesados',

  imports: [
    FormsModule,
    RouterLink
  ],

  templateUrl: './interesados.html',

  styleUrl: './interesados.css'
})
export class Interesados implements OnInit {


  /*
   * =========================================================
   * LEADS
   * =========================================================
   */

  leads:
    LeadHabitacionResponse[] = [];


  /*
   * =========================================================
   * FILTRO
   * =========================================================
   */

  estadoSeleccionado = 'todos';


  /*
   * =========================================================
   * ESTADO DE LA PÁGINA
   * =========================================================
   */

  cargando = true;


  idLeadProcesando:
    number | null = null;


  /*
   * =========================================================
   * MENSAJES
   * =========================================================
   */

  mensajeError = '';

  mensajeExito = '';


  /*
   * =========================================================
   * CONFIRMACIÓN
   * =========================================================
   */

  leadConfirmacion:
    LeadHabitacionResponse | null = null;


  estadoConfirmacion:
    string | null = null;


  /*
   * =========================================================
   * CONSTRUCTOR
   * =========================================================
   */

  constructor(
    private leadHabitacionService:
      LeadHabitacionService
  ) {}


  /*
   * =========================================================
   * INICIALIZACIÓN
   * =========================================================
   */

  ngOnInit(): void {

    this.cargarInteresados();
  }


  /*
   * =========================================================
   * CARGAR INTERESADOS
   * =========================================================
   */

  cargarInteresados(): void {

    this.cargando = true;

    this.mensajeError = '';


    this.leadHabitacionService
      .listarLeadsPropietario(
        null,
        0,
        1000
      )
      .subscribe({

        next: response => {

          this.cargando = false;


          if (
            response.status !== 'success' ||
            !response.data
          ) {

            this.leads = [];


            this.mensajeError =

              response.message ||

              'No se pudieron cargar los interesados';


            return;
          }


          this.leads =

            Array.isArray(
              response.data.content
            )

              ? response.data.content

              : [];


          this.ordenarPorFecha();


          console.log(

            '[RoomMatch interesados] Leads:',

            this.leads

          );
        },


        error: error => {

          this.cargando = false;

          this.leads = [];


          this.mensajeError =

            error.error?.message ||

            'No se pudieron cargar los interesados';


          console.error(

            '[RoomMatch interesados] Error:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * FILTRAR INTERESADOS
   * =========================================================
   */

  obtenerLeadsFiltrados():
    LeadHabitacionResponse[] {

    if (
      this.estadoSeleccionado === 'todos'
    ) {

      return this.leads;
    }


    return this.leads.filter(

      lead =>

        this.normalizarEstado(
          lead.estado
        ) === this.estadoSeleccionado

    );
  }


  /*
   * =========================================================
   * CAMBIAR FILTRO
   * =========================================================
   */

  cambiarFiltro(
    estado: string
  ): void {

    this.estadoSeleccionado =
      estado;
  }


  /*
   * =========================================================
   * SOLICITAR CAMBIO DE ESTADO
   * =========================================================
   */

  solicitarCambioEstado(
    lead: LeadHabitacionResponse,
    estado: string
  ): void {

    this.limpiarMensajes();


    /*
     * Contactado se actualiza directamente.
     */

    if (
      estado === 'contactado'
    ) {

      this.actualizarEstado(
        lead,
        estado
      );


      return;
    }


    /*
     * Cerrado y rechazado
     * solicitan confirmación.
     */

    this.leadConfirmacion =
      lead;


    this.estadoConfirmacion =
      estado;
  }


  /*
   * =========================================================
   * CANCELAR CONFIRMACIÓN
   * =========================================================
   */

  cancelarConfirmacion(): void {

    this.leadConfirmacion = null;

    this.estadoConfirmacion = null;
  }


  /*
   * =========================================================
   * CONFIRMAR CAMBIO
   * =========================================================
   */

  confirmarCambioEstado(): void {

    if (
      !this.leadConfirmacion ||
      !this.estadoConfirmacion
    ) {

      return;
    }


    const lead =
      this.leadConfirmacion;


    const estado =
      this.estadoConfirmacion;


    this.cancelarConfirmacion();


    this.actualizarEstado(
      lead,
      estado
    );
  }


  /*
   * =========================================================
   * ACTUALIZAR ESTADO
   * =========================================================
   */

  private actualizarEstado(
    lead: LeadHabitacionResponse,
    estado: string
  ): void {

    this.limpiarMensajes();


    this.idLeadProcesando =
      lead.idLead;


    this.leadHabitacionService
      .actualizarEstado(
        lead.idLead,
        estado
      )
      .subscribe({

        next: response => {

          this.idLeadProcesando = null;


          if (
            response.status !== 'success'
          ) {

            this.mensajeError =

              response.message ||

              'No se pudo actualizar el interesado';


            return;
          }


          /*
           * Actualizamos la tarjeta directamente
           * sin recargar toda la página.
           */

          this.leads =

            this.leads.map(

              leadActual =>

                leadActual.idLead ===
                lead.idLead

                  ? {
                      ...leadActual,

                      estado:
                        response.data?.estado ??
                        estado
                    }

                  : leadActual

            );


          this.mensajeExito =

            this.obtenerMensajeActualizacion(
              estado
            );
        },


        error: error => {

          this.idLeadProcesando = null;


          this.mensajeError =

            error.error?.message ||

            'No se pudo actualizar el interesado';


          console.error(

            '[RoomMatch interesados] Error estado:',

            error

          );
        }

      });
  }


  /*
   * =========================================================
   * CONTAR POR ESTADO
   * =========================================================
   */

  contarPorEstado(
    estado: string
  ): number {

    return this.leads.filter(

      lead =>

        this.normalizarEstado(
          lead.estado
        ) === estado

    ).length;
  }


  /*
   * =========================================================
   * OBTENER INICIALES
   * =========================================================
   */

  obtenerIniciales(
    nombre: string
  ): string {

    if (
      !nombre ||
      !nombre.trim()
    ) {

      return 'RM';
    }


    const partes =

      nombre
        .trim()
        .split(/\s+/);


    if (
      partes.length === 1
    ) {

      return partes[0]
        .substring(
          0,
          2
        )
        .toUpperCase();
    }


    return (

      partes[0][0] +

      partes[
        partes.length - 1
      ][0]

    ).toUpperCase();
  }


  /*
   * =========================================================
   * FORMATEAR PRECIO
   * =========================================================
   */

  formatearPrecio(
    precio: number
  ): string {

    return new Intl
      .NumberFormat(
        'es-PE',
        {
          minimumFractionDigits: 0,
          maximumFractionDigits: 2
        }
      )
      .format(
        precio
      );
  }


  /*
   * =========================================================
   * FORMATEAR FECHA RELATIVA
   * =========================================================
   */

  formatearFechaRelativa(
    fecha: string
  ): string {

    if (!fecha) {

      return '';
    }


    const fechaLead =
      new Date(fecha);


    if (
      Number.isNaN(
        fechaLead.getTime()
      )
    ) {

      return '';
    }


    const ahora =
      new Date();


    const diferencia =

      ahora.getTime() -

      fechaLead.getTime();


    const minutos =

      Math.floor(
        diferencia / 60000
      );


    if (
      minutos < 1
    ) {

      return 'Ahora';
    }


    if (
      minutos < 60
    ) {

      return `Hace ${minutos} min`;
    }


    const horas =

      Math.floor(
        minutos / 60
      );


    if (
      horas < 24
    ) {

      return horas === 1

        ? 'Hace 1 hora'

        : `Hace ${horas} horas`;
    }


    const dias =

      Math.floor(
        horas / 24
      );


    if (
      dias < 7
    ) {

      return dias === 1

        ? 'Hace 1 día'

        : `Hace ${dias} días`;
    }


    return fechaLead
      .toLocaleDateString(
        'es-PE',
        {
          day: '2-digit',
          month: 'short',
          year: 'numeric'
        }
      );
  }


  /*
   * =========================================================
   * ETIQUETA DEL ESTADO
   * =========================================================
   */

  obtenerEtiquetaEstado(
    estado: string
  ): string {

    switch (
      this.normalizarEstado(
        estado
      )
    ) {

      case 'pendiente':

        return 'Pendiente';


      case 'contactado':

        return 'Contactado';


      case 'cerrado':

        return 'Cerrado';


      case 'rechazado':

        return 'Rechazado';


      default:

        return estado;
    }
  }


  /*
   * =========================================================
   * TEXTO DE CONFIRMACIÓN
   * =========================================================
   */

  obtenerTituloConfirmacion(): string {

    if (
      this.estadoConfirmacion === 'cerrado'
    ) {

      return '¿Cerrar interesado?';
    }


    return '¿Rechazar interesado?';
  }


  obtenerTextoConfirmacion(): string {

    if (
      this.estadoConfirmacion === 'cerrado'
    ) {

      return 'El interesado será marcado como cerrado. Utiliza este estado cuando el proceso ya haya finalizado.';
    }


    return 'El interesado será marcado como rechazado y dejará de aparecer entre tus pendientes.';
  }


  obtenerTextoBotonConfirmacion(): string {

    if (
      this.estadoConfirmacion === 'cerrado'
    ) {

      return 'Sí, cerrar';
    }


    return 'Sí, rechazar';
  }


  /*
   * =========================================================
   * MENSAJE DE ACTUALIZACIÓN
   * =========================================================
   */

  private obtenerMensajeActualizacion(
    estado: string
  ): string {

    switch (estado) {

      case 'contactado':

        return 'Interesado marcado como contactado';


      case 'cerrado':

        return 'Interesado cerrado correctamente';


      case 'rechazado':

        return 'Interesado rechazado correctamente';


      default:

        return 'Estado actualizado correctamente';
    }
  }


  /*
   * =========================================================
   * NORMALIZAR ESTADO
   * =========================================================
   */

  private normalizarEstado(
    estado: string
  ): string {

    return (
      estado ??
      ''
    )
      .trim()
      .toLowerCase();
  }


  /*
   * =========================================================
   * ORDENAR POR FECHA
   * =========================================================
   */

  private ordenarPorFecha(): void {

    this.leads = [

      ...this.leads

    ].sort(

      (
        leadA,
        leadB
      ) =>

        new Date(
          leadB.fechaLead
        ).getTime()

        -

        new Date(
          leadA.fechaLead
        ).getTime()

    );
  }


  /*
   * =========================================================
   * LIMPIAR MENSAJES
   * =========================================================
   */

  private limpiarMensajes(): void {

    this.mensajeError = '';

    this.mensajeExito = '';
  }
}