package com.roommatch.service;

import com.roommatch.dto.SolicitudContactoRequest;
import com.roommatch.dto.SolicitudContactoResponse;
import com.roommatch.exception.ConflictException;
import com.roommatch.exception.ResourceNotFoundException;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SolicitudContactoService {
    private final SolicitudContactoRepository solicitudes;
    private final ContactoRoomieRepository contactos;
    private final UsuarioRepository usuarios;
    private final NotificacionRepository notificaciones;
    private final Clock clock;
    private final Duration esperaRechazo;
    private final Duration esperaCancelacion;

    public SolicitudContactoService(SolicitudContactoRepository solicitudes, ContactoRoomieRepository contactos,
            UsuarioRepository usuarios, NotificacionRepository notificaciones, Clock clock,
            @Value("${app.solicitudes.espera-rechazo:30d}") Duration esperaRechazo,
            @Value("${app.solicitudes.espera-cancelacion:1d}") Duration esperaCancelacion) {
        this.solicitudes = solicitudes;
        this.contactos = contactos;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.clock = clock;
        if (esperaRechazo.isNegative() || esperaCancelacion.isNegative()) {
            throw new IllegalArgumentException("Los plazos de solicitudes no pueden ser negativos");
        }
        this.esperaRechazo = esperaRechazo;
        this.esperaCancelacion = esperaCancelacion;
    }

    @Transactional
    public SolicitudContactoResponse enviarSolicitud(Integer emisorId, Integer receptorId, SolicitudContactoRequest request) {
        if (emisorId.equals(receptorId)) throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        var pareja = bloquearPareja(emisorId, receptorId);
        Usuario emisor = pareja.get(0), receptor = pareja.get(1);
        exigirActivos(emisor, receptor);
        if (contactos.existeContactoDesbloqueado(emisorId, receptorId)) {
            throw new ConflictException("El contacto ya está desbloqueado");
        }
        if (solicitudes.existePendiente(emisorId, receptorId)) {
            throw new ConflictException("Ya existe una solicitud pendiente entre ambos usuarios");
        }
        solicitudes.findFirstByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuarioOrderByFechaSolicitudDescIdSolicitudDesc(emisorId, receptorId)
                .ifPresent(this::comprobarReenvio);
        SolicitudContacto solicitud = new SolicitudContacto();
        solicitud.setUsuarioEmisor(emisor);
        solicitud.setUsuarioReceptor(receptor);
        solicitud.setMensaje(request.getMensaje());
        solicitud.setFechaSolicitud(LocalDateTime.now(clock));
        solicitudes.saveAndFlush(solicitud);
        notificar(receptor, "Nueva solicitud de contacto", emisor.getNombres() + " quiere desbloquear contacto contigo.", "solicitud");
        return SolicitudContactoResponse.fromEntity(solicitud);
    }

    public List<SolicitudContactoResponse> listarRecibidas(Integer usuario) {
        return solicitudes.findByUsuarioReceptorIdUsuarioOrderByFechaSolicitudDesc(usuario).stream()
                .map(SolicitudContactoResponse::fromEntity).toList();
    }

    public List<SolicitudContactoResponse> listarEnviadas(Integer usuario) {
        return solicitudes.findByUsuarioEmisorIdUsuarioOrderByFechaSolicitudDesc(usuario).stream()
                .map(SolicitudContactoResponse::fromEntity).toList();
    }

    @Transactional
    public SolicitudContactoResponse aceptarSolicitud(Integer usuario, Integer id) {
        SolicitudContacto solicitud = paraTransicion(usuario, id, false);
        exigirActivos(solicitud.getUsuarioEmisor(), solicitud.getUsuarioReceptor());
        if (contactos.existeContactoDesbloqueado(solicitud.getUsuarioEmisor().getIdUsuario(), usuario)) {
            throw new ConflictException("El contacto ya está desbloqueado");
        }
        cambiarEstado(solicitud, "aceptada");
        ContactoRoomie contacto = new ContactoRoomie();
        contacto.setSolicitud(solicitud);
        contacto.setUsuarioA(solicitud.getUsuarioEmisor());
        contacto.setUsuarioB(solicitud.getUsuarioReceptor());
        contactos.saveAndFlush(contacto);
        notificar(solicitud.getUsuarioEmisor(), "Solicitud aceptada",
                solicitud.getUsuarioReceptor().getNombres() + " aceptó tu solicitud. Ya pueden ver los canales que cada persona permite compartir.", "contacto");
        return SolicitudContactoResponse.fromEntity(solicitud);
    }

    @Transactional
    public SolicitudContactoResponse rechazarSolicitud(Integer usuario, Integer id) {
        SolicitudContacto solicitud = paraTransicion(usuario, id, false);
        cambiarEstado(solicitud, "rechazada");
        notificar(solicitud.getUsuarioEmisor(), "Solicitud rechazada",
                solicitud.getUsuarioReceptor().getNombres() + " rechazó tu solicitud de contacto.", "solicitud");
        return SolicitudContactoResponse.fromEntity(solicitud);
    }

    @Transactional
    public SolicitudContactoResponse cancelarSolicitud(Integer usuario, Integer id) {
        SolicitudContacto solicitud = paraTransicion(usuario, id, true);
        cambiarEstado(solicitud, "cancelada");
        notificar(solicitud.getUsuarioReceptor(), "Solicitud cancelada", "La persona que envió la solicitud la canceló.", "solicitud");
        return SolicitudContactoResponse.fromEntity(solicitud);
    }

    private SolicitudContacto paraTransicion(Integer usuario, Integer id, boolean esEmisor) {
        // Read scalar IDs first: no stale managed request before acquiring the pair locks.
        var partes = solicitudes.partes(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        Integer autorizado = esEmisor ? partes.getEmisor() : partes.getReceptor();
        if (!usuario.equals(autorizado)) throw new AccessDeniedException("No tienes permiso para modificar esta solicitud");
        bloquearPareja(partes.getEmisor(), partes.getReceptor());
        SolicitudContacto solicitud = solicitudes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        if (!"pendiente".equals(solicitud.getEstado())) throw new ConflictException("Solo puedes modificar una solicitud pendiente");
        return solicitud;
    }

    private List<Usuario> bloquearPareja(Integer a, Integer b) {
        // Every mutation takes these two locks in the same order, including reciprocal sends.
        Usuario menor = usuarios.lockById(Math.min(a, b)).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Usuario mayor = usuarios.lockById(Math.max(a, b)).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return a < b ? List.of(menor, mayor) : List.of(mayor, menor);
    }

    private void comprobarReenvio(SolicitudContacto anterior) {
        if ("aceptada".equals(anterior.getEstado())) throw new ConflictException("La solicitud anterior ya fue aceptada");
        Duration espera = "rechazada".equals(anterior.getEstado()) ? esperaRechazo : esperaCancelacion;
        LocalDateTime cierre = anterior.getFechaRespuesta() == null ? anterior.getFechaSolicitud() : anterior.getFechaRespuesta();
        if (cierre != null && LocalDateTime.now(clock).isBefore(cierre.plus(espera))) {
            throw new ConflictException("Podrás volver a enviar una solicitud a esta persona desde " + cierre.plus(espera));
        }
    }

    private void exigirActivos(Usuario emisor, Usuario receptor) {
        if (!"activo".equals(emisor.getEstado())) throw new AccessDeniedException("Tu cuenta no está activa");
        if (!"activo".equals(receptor.getEstado())) throw new ConflictException("Esta persona no está disponible para contacto");
    }

    private void cambiarEstado(SolicitudContacto solicitud, String estado) {
        solicitud.setEstado(estado);
        solicitud.setFechaRespuesta(LocalDateTime.now(clock));
        solicitudes.saveAndFlush(solicitud);
    }

    private void notificar(Usuario usuario, String titulo, String mensaje, String tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setUrlDestino("contacto".equals(tipo) ? "/contactos" : "/solicitudes");
        notificaciones.save(notificacion);
    }
}
