package com.roommatch.service;

import com.roommatch.dto.SolicitudContactoRequest;
import com.roommatch.dto.SolicitudContactoResponse;
import com.roommatch.model.ContactoRoomie;
import com.roommatch.model.Notificacion;
import com.roommatch.model.SolicitudContacto;
import com.roommatch.model.Usuario;
import com.roommatch.repository.ContactoRoomieRepository;
import com.roommatch.repository.NotificacionRepository;
import com.roommatch.repository.SolicitudContactoRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudContactoService {

    private final SolicitudContactoRepository solicitudRepository;
    private final ContactoRoomieRepository contactoRoomieRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public SolicitudContactoService(
            SolicitudContactoRepository solicitudRepository,
            ContactoRoomieRepository contactoRoomieRepository,
            UsuarioRepository usuarioRepository,
            NotificacionRepository notificacionRepository
    ) {
        this.solicitudRepository = solicitudRepository;
        this.contactoRoomieRepository = contactoRoomieRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public SolicitudContactoResponse enviarSolicitud(
            Integer idUsuarioEmisor,
            Integer idUsuarioReceptor,
            SolicitudContactoRequest request
    ) {
        if (idUsuarioEmisor.equals(idUsuarioReceptor)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        if (solicitudRepository.existsByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuario(
                idUsuarioEmisor,
                idUsuarioReceptor
        )) {
            throw new IllegalArgumentException("Ya enviaste una solicitud a este usuario");
        }

        if (solicitudRepository.existsByUsuarioEmisorIdUsuarioAndUsuarioReceptorIdUsuario(
                idUsuarioReceptor,
                idUsuarioEmisor
        )) {
            throw new IllegalArgumentException("Ya existe una solicitud entre ambos usuarios");
        }

        Usuario emisor = usuarioRepository.findById(idUsuarioEmisor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(idUsuarioReceptor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario receptor no encontrado"));

        if (!receptor.getEstado().equalsIgnoreCase("activo")) {
            throw new IllegalArgumentException("No puedes enviar solicitud a un usuario inactivo");
        }

        SolicitudContacto solicitud = new SolicitudContacto();
        solicitud.setUsuarioEmisor(emisor);
        solicitud.setUsuarioReceptor(receptor);
        solicitud.setMensaje(request.getMensaje());
        solicitud.setEstado("pendiente");

        SolicitudContacto solicitudGuardada = solicitudRepository.save(solicitud);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(receptor);
        notificacion.setTitulo("Nueva solicitud de contacto");
        notificacion.setMensaje(emisor.getNombres() + " quiere desbloquear contacto contigo.");
        notificacion.setTipo("solicitud");
        notificacion.setUrlDestino("/solicitudes");

        notificacionRepository.save(notificacion);

        return SolicitudContactoResponse.fromEntity(solicitudGuardada);
    }

    public List<SolicitudContactoResponse> listarRecibidas(Integer idUsuarioReceptor) {
        return solicitudRepository
                .findByUsuarioReceptorIdUsuarioOrderByFechaSolicitudDesc(idUsuarioReceptor)
                .stream()
                .map(SolicitudContactoResponse::fromEntity)
                .toList();
    }

    public List<SolicitudContactoResponse> listarEnviadas(Integer idUsuarioEmisor) {
        return solicitudRepository
                .findByUsuarioEmisorIdUsuarioOrderByFechaSolicitudDesc(idUsuarioEmisor)
                .stream()
                .map(SolicitudContactoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public SolicitudContactoResponse aceptarSolicitud(
            Integer idUsuarioReceptor,
            Integer idSolicitud
    ) {
        SolicitudContacto solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!solicitud.getUsuarioReceptor().getIdUsuario().equals(idUsuarioReceptor)) {
            throw new IllegalArgumentException("No tienes permiso para aceptar esta solicitud");
        }

        if (!solicitud.getEstado().equalsIgnoreCase("pendiente")) {
            throw new IllegalArgumentException("Solo se pueden aceptar solicitudes pendientes");
        }

        solicitud.setEstado("aceptada");
        solicitud.setFechaRespuesta(LocalDateTime.now());

        SolicitudContacto solicitudActualizada = solicitudRepository.save(solicitud);

        if (contactoRoomieRepository.existsBySolicitudIdSolicitud(idSolicitud)) {
            throw new IllegalArgumentException("El contacto ya fue desbloqueado anteriormente");
        }

        ContactoRoomie contactoRoomie = new ContactoRoomie();
        contactoRoomie.setSolicitud(solicitudActualizada);
        contactoRoomie.setUsuarioA(solicitud.getUsuarioEmisor());
        contactoRoomie.setUsuarioB(solicitud.getUsuarioReceptor());

        contactoRoomieRepository.save(contactoRoomie);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(solicitud.getUsuarioEmisor());
        notificacion.setTitulo("Solicitud aceptada");
        notificacion.setMensaje(
                solicitud.getUsuarioReceptor().getNombres() +
                " aceptó tu solicitud. Ahora pueden ver sus datos de contacto."
        );
        notificacion.setTipo("contacto");
        notificacion.setUrlDestino("/contactos");

        notificacionRepository.save(notificacion);

        return SolicitudContactoResponse.fromEntity(solicitudActualizada);
    }

    @Transactional
    public SolicitudContactoResponse rechazarSolicitud(
            Integer idUsuarioReceptor,
            Integer idSolicitud
    ) {
        SolicitudContacto solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!solicitud.getUsuarioReceptor().getIdUsuario().equals(idUsuarioReceptor)) {
            throw new IllegalArgumentException("No tienes permiso para rechazar esta solicitud");
        }

        if (!solicitud.getEstado().equalsIgnoreCase("pendiente")) {
            throw new IllegalArgumentException("Solo se pueden rechazar solicitudes pendientes");
        }

        solicitud.setEstado("rechazada");
        solicitud.setFechaRespuesta(LocalDateTime.now());

        SolicitudContacto solicitudActualizada = solicitudRepository.save(solicitud);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(solicitud.getUsuarioEmisor());
        notificacion.setTitulo("Solicitud rechazada");
        notificacion.setMensaje(
                solicitud.getUsuarioReceptor().getNombres() +
                " rechazó tu solicitud de contacto."
        );
        notificacion.setTipo("solicitud");
        notificacion.setUrlDestino("/solicitudes");

        notificacionRepository.save(notificacion);

        return SolicitudContactoResponse.fromEntity(solicitudActualizada);
    }
}