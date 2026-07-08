package com.roommatch.service;

import com.roommatch.dto.ReporteHabitacionResponse;
import com.roommatch.dto.ReporteRequest;
import com.roommatch.dto.ReporteUsuarioResponse;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class ReporteService {

    private final ReporteUsuarioRepository reporteUsuarioRepository;
    private final ReporteHabitacionRepository reporteHabitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabitacionRepository habitacionRepository;
    private final NotificacionRepository notificacionRepository;

    public ReporteService(
            ReporteUsuarioRepository reporteUsuarioRepository,
            ReporteHabitacionRepository reporteHabitacionRepository,
            UsuarioRepository usuarioRepository,
            HabitacionRepository habitacionRepository,
            NotificacionRepository notificacionRepository
    ) {
        this.reporteUsuarioRepository = reporteUsuarioRepository;
        this.reporteHabitacionRepository = reporteHabitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.habitacionRepository = habitacionRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public ReporteUsuarioResponse reportarUsuario(
            Integer idUsuarioReportante,
            Integer idUsuarioReportado,
            ReporteRequest request
    ) {
        if (idUsuarioReportante.equals(idUsuarioReportado)) {
            throw new IllegalArgumentException("No puedes reportarte a ti mismo");
        }

        Usuario reportante = usuarioRepository.findById(idUsuarioReportante)
                .orElseThrow(() -> new IllegalArgumentException("Usuario reportante no encontrado"));

        Usuario reportado = usuarioRepository.findById(idUsuarioReportado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario reportado no encontrado"));

        ReporteUsuario reporte = new ReporteUsuario();
        reporte.setUsuarioReportante(reportante);
        reporte.setUsuarioReportado(reportado);
        reporte.setMotivo(request.getMotivo());
        reporte.setDescripcion(request.getDescripcion());
        reporte.setEstado("pendiente");

        ReporteUsuario reporteGuardado = reporteUsuarioRepository.save(reporte);

        return ReporteUsuarioResponse.fromEntity(reporteGuardado);
    }

    @Transactional
    public ReporteHabitacionResponse reportarHabitacion(
            Integer idUsuarioReportante,
            Integer idHabitacion,
            ReporteRequest request
    ) {
        Usuario reportante = usuarioRepository.findById(idUsuarioReportante)
                .orElseThrow(() -> new IllegalArgumentException("Usuario reportante no encontrado"));

        Habitacion habitacion = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        Integer idUsuarioPropietario = habitacion.getPropietario().getUsuario().getIdUsuario();

        if (idUsuarioPropietario.equals(idUsuarioReportante)) {
            throw new IllegalArgumentException("No puedes reportar tu propia habitación");
        }

        ReporteHabitacion reporte = new ReporteHabitacion();
        reporte.setUsuarioReportante(reportante);
        reporte.setHabitacion(habitacion);
        reporte.setMotivo(request.getMotivo());
        reporte.setDescripcion(request.getDescripcion());
        reporte.setEstado("pendiente");

        ReporteHabitacion reporteGuardado = reporteHabitacionRepository.save(reporte);

        return ReporteHabitacionResponse.fromEntity(reporteGuardado);
    }

    public Page<ReporteUsuarioResponse> listarReportesUsuarios(
            Integer idAdmin,
            String estado,
            Pageable pageable
    ) {
        validarAdmin(idAdmin);

        return reporteUsuarioRepository
                .listarReportes(estado, pageable)
                .map(ReporteUsuarioResponse::fromEntity);
    }

    public Page<ReporteHabitacionResponse> listarReportesHabitaciones(
            Integer idAdmin,
            String estado,
            Pageable pageable
    ) {
        validarAdmin(idAdmin);

        return reporteHabitacionRepository
                .listarReportes(estado, pageable)
                .map(ReporteHabitacionResponse::fromEntity);
    }

    @Transactional
    public ReporteUsuarioResponse revisarReporteUsuario(
            Integer idAdmin,
            Integer idReporte,
            String nuevoEstado
    ) {
        validarAdmin(idAdmin);
        validarEstadoRevision(nuevoEstado);

        ReporteUsuario reporte = reporteUsuarioRepository.findById(idReporte)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        reporte.setEstado(nuevoEstado.toLowerCase());
        reporte.setFechaRevision(LocalDateTime.now());

        ReporteUsuario actualizado = reporteUsuarioRepository.save(reporte);

        return ReporteUsuarioResponse.fromEntity(actualizado);
    }

    @Transactional
    public ReporteHabitacionResponse revisarReporteHabitacion(
            Integer idAdmin,
            Integer idReporte,
            String nuevoEstado
    ) {
        validarAdmin(idAdmin);
        validarEstadoRevision(nuevoEstado);

        ReporteHabitacion reporte = reporteHabitacionRepository.findById(idReporte)
                .orElseThrow(() -> new IllegalArgumentException("Reporte de habitación no encontrado"));

        reporte.setEstado(nuevoEstado.toLowerCase());
        reporte.setFechaRevision(LocalDateTime.now());

        ReporteHabitacion actualizado = reporteHabitacionRepository.save(reporte);

        return ReporteHabitacionResponse.fromEntity(actualizado);
    }

    @Transactional
    public ReporteUsuarioResponse sancionarUsuario(
            Integer idAdmin,
            Integer idReporte
    ) {
        validarAdmin(idAdmin);

        ReporteUsuario reporte = reporteUsuarioRepository.findById(idReporte)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        Usuario usuarioReportado = reporte.getUsuarioReportado();

        reporte.setEstado("sancionado");
        reporte.setFechaRevision(LocalDateTime.now());
        reporteUsuarioRepository.save(reporte);

        usuarioReportado.setEstado("suspendido");
        usuarioRepository.save(usuarioReportado);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuarioReportado);
        notificacion.setTitulo("Cuenta suspendida");
        notificacion.setMensaje("Tu cuenta fue suspendida por incumplir las normas de RoomMatch.");
        notificacion.setTipo("reporte");
        notificacion.setUrlDestino("/soporte");

        notificacionRepository.save(notificacion);

        return ReporteUsuarioResponse.fromEntity(reporte);
    }

    @Transactional
    public ReporteHabitacionResponse sancionarHabitacion(
            Integer idAdmin,
            Integer idReporte
    ) {
        validarAdmin(idAdmin);

        ReporteHabitacion reporte = reporteHabitacionRepository.findById(idReporte)
                .orElseThrow(() -> new IllegalArgumentException("Reporte de habitación no encontrado"));

        Habitacion habitacion = reporte.getHabitacion();

        reporte.setEstado("sancionado");
        reporte.setFechaRevision(LocalDateTime.now());
        reporteHabitacionRepository.save(reporte);

        habitacion.setEstado("pausada");
        habitacionRepository.save(habitacion);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(habitacion.getPropietario().getUsuario());
        notificacion.setTitulo("Habitación pausada");
        notificacion.setMensaje(
                "Tu habitación \"" + habitacion.getTitulo() +
                "\" fue pausada por revisión administrativa."
        );
        notificacion.setTipo("reporte");
        notificacion.setUrlDestino("/propietario/habitaciones");

        notificacionRepository.save(notificacion);

        return ReporteHabitacionResponse.fromEntity(reporte);
    }

    private void validarAdmin(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == null ||
                !usuario.getRol().getNombreRol().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("No tienes permisos de administrador");
        }
    }

    private void validarEstadoRevision(String estado) {
        Set<String> estadosPermitidos = Set.of("revisado", "rechazado");

        if (estado == null || !estadosPermitidos.contains(estado.toLowerCase())) {
            throw new IllegalArgumentException("Estado no válido. Usa revisado o rechazado");
        }
    }
}