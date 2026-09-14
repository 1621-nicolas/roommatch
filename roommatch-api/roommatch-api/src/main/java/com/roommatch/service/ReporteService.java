package com.roommatch.service;

import com.roommatch.dto.*;
import com.roommatch.exception.*;
import com.roommatch.model.*;
import com.roommatch.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    private final ReporteUsuarioRepository userReports;
    private final ReporteHabitacionRepository roomReports;
    private final UsuarioRepository users;
    private final HabitacionRepository rooms;
    private final NotificacionRepository notifications;
    private final ModeracionEventoRepository events;
    private final AdminAuthorization admins;
    private final PlanPolicy visibility;
    private final Clock clock;

    public ReporteService(ReporteUsuarioRepository userReports, ReporteHabitacionRepository roomReports,
            UsuarioRepository users, HabitacionRepository rooms, NotificacionRepository notifications,
            ModeracionEventoRepository events, AdminAuthorization admins, PlanPolicy visibility, Clock clock) {
        this.userReports=userReports; this.roomReports=roomReports; this.users=users; this.rooms=rooms;
        this.notifications=notifications; this.events=events; this.admins=admins; this.visibility=visibility; this.clock=clock;
    }

    @Transactional
    public ReporteUsuarioResponse reportarUsuario(Integer reporter, Integer target, ReporteRequest request) {
        if (reporter.equals(target)) throw new IllegalArgumentException("No puedes reportarte a ti mismo");
        Usuario sender = activeReporter(reporter);
        Usuario reported = users.findById(target).filter(u -> "activo".equals(u.getEstado()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no disponible"));
        if (userReports.hasOpenReport(reporter, target)) throw new ConflictException("Ya tienes un reporte abierto sobre este usuario");
        ReporteUsuario row = new ReporteUsuario();
        row.setUsuarioReportante(sender); row.setUsuarioReportado(reported);
        row.setMotivo(reason(request.getMotivo(), 150)); row.setDescripcion(request.getDescripcion());
        return ReporteUsuarioResponse.fromEntity(userReports.saveAndFlush(row));
    }

    @Transactional
    public ReporteHabitacionResponse reportarHabitacion(Integer reporter, Integer target, ReporteRequest request) {
        Usuario sender = activeReporter(reporter);
        Habitacion room = rooms.findById(target).orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        if (room.getPropietario().getUsuario().getIdUsuario().equals(reporter)) throw new IllegalArgumentException("No puedes reportar tu propia habitación");
        if (!visibility.visible(room)) throw new ResourceNotFoundException("Habitación no disponible");
        if (roomReports.hasOpenReport(reporter, target)) throw new ConflictException("Ya tienes un reporte abierto sobre esta habitación");
        ReporteHabitacion row = new ReporteHabitacion();
        row.setUsuarioReportante(sender); row.setHabitacion(room);
        row.setMotivo(reason(request.getMotivo(), 150)); row.setDescripcion(request.getDescripcion());
        return ReporteHabitacionResponse.fromEntity(roomReports.saveAndFlush(row));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ReporteUsuarioResponse> listarReportesUsuarios(Integer admin, String state, Pageable page) {
        admins.require(admin); validatePage(page);
        return userReports.listarReportes(filterState(state), page).map(ReporteUsuarioResponse::fromEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ReporteHabitacionResponse> listarReportesHabitaciones(Integer admin, String state, Pageable page) {
        admins.require(admin); validatePage(page);
        return roomReports.listarReportes(filterState(state), page).map(ReporteHabitacionResponse::fromEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteUsuarioResponse revisarReporteUsuario(Integer adminId, Integer id, String requested, String motive) {
        Usuario admin = admins.require(adminId);
        String state = reviewState(requested), reason = reason(motive, 500);
        ReporteUsuario row = userReport(id);
        if (row.getEstado().equals(state)) return ReporteUsuarioResponse.fromEntity(row);
        requireOpen(row.getEstado());
        row.setEstado(state); row.setFechaRevision(now()); userReports.saveAndFlush(row);
        events.save(new ModeracionEvento(admin, row, null, state, reason, now()));
        return ReporteUsuarioResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteHabitacionResponse revisarReporteHabitacion(Integer adminId, Integer id, String requested, String motive) {
        Usuario admin = admins.require(adminId);
        String state = reviewState(requested), reason = reason(motive, 500);
        ReporteHabitacion row = roomReport(id);
        if (row.getEstado().equals(state)) return ReporteHabitacionResponse.fromEntity(row);
        requireOpen(row.getEstado());
        row.setEstado(state); row.setFechaRevision(now()); roomReports.saveAndFlush(row);
        events.save(new ModeracionEvento(admin, null, row, state, reason, now()));
        return ReporteHabitacionResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteUsuarioResponse sancionarUsuario(Integer adminId, Integer id, String motive) {
        Usuario admin = admins.require(adminId); String reason = reason(motive, 500);
        ReporteUsuario row = userReport(id);
        if ("sancionado".equals(row.getEstado())) return ReporteUsuarioResponse.fromEntity(row);
        requireOpen(row.getEstado());
        Usuario target = row.getUsuarioReportado();
        if (target.getIdUsuario().equals(adminId) || target.getRol() != null && "ADMIN".equals(target.getRol().getNombreRol()))
            throw new AccessDeniedException("Esta operación no puede suspender cuentas administrativas");
        row.setEstado("sancionado"); row.setFechaRevision(now()); userReports.saveAndFlush(row);
        target.setEstado("suspendido"); users.saveAndFlush(target);
        events.save(new ModeracionEvento(admin, row, null, "sancionado", reason, now()));
        notify(target, "Cuenta suspendida", "La administración suspendió tu cuenta tras revisar un reporte.", "/login");
        return ReporteUsuarioResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteHabitacionResponse sancionarHabitacion(Integer adminId, Integer id, String motive) {
        Usuario admin = admins.require(adminId); String reason = reason(motive, 500);
        ReporteHabitacion row = roomReport(id);
        if ("sancionado".equals(row.getEstado())) return ReporteHabitacionResponse.fromEntity(row);
        requireOpen(row.getEstado());
        Habitacion room = row.getHabitacion();
        row.setEstado("sancionado"); row.setFechaRevision(now()); roomReports.saveAndFlush(row);
        // An archived room remains archived; moderation must not resurrect its state.
        if (!"eliminada".equals(room.getEstado())) room.setEstado("pausada");
        room.setBloqueada(true); room.setDestacada(false); rooms.saveAndFlush(room);
        events.save(new ModeracionEvento(admin, null, row, "sancionado", reason, now()));
        notify(room.getPropietario().getUsuario(), "Habitación bloqueada", "Tu habitación fue bloqueada tras una revisión administrativa.", "/propietario");
        return ReporteHabitacionResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteUsuarioResponse restaurarUsuario(Integer adminId, Integer id, String motive) {
        Usuario admin = admins.require(adminId); String reason = reason(motive, 500);
        ReporteUsuario row = userReport(id); Usuario target = row.getUsuarioReportado();
        if (!"sancionado".equals(row.getEstado()) || !"suspendido".equals(target.getEstado()))
            throw new ConflictException("Este reporte no corresponde a una cuenta actualmente suspendida");
        target.setEstado("activo"); users.saveAndFlush(target);
        events.save(new ModeracionEvento(admin, row, null, "restaurado", reason, now()));
        notify(target, "Cuenta reactivada", "La administración reactivó tu cuenta. Ya puedes iniciar sesión.", "/login");
        return ReporteUsuarioResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReporteHabitacionResponse restaurarHabitacion(Integer adminId, Integer id, String motive) {
        Usuario admin = admins.require(adminId); String reason = reason(motive, 500);
        ReporteHabitacion row = roomReport(id); Habitacion room = row.getHabitacion();
        if (!"sancionado".equals(row.getEstado()) || !Boolean.TRUE.equals(room.getBloqueada()))
            throw new ConflictException("Este reporte no corresponde a una habitación actualmente bloqueada");
        room.setBloqueada(false); rooms.saveAndFlush(room);
        events.save(new ModeracionEvento(admin, null, row, "restaurado", reason, now()));
        notify(room.getPropietario().getUsuario(), "Bloqueo retirado", "La administración retiró el bloqueo. La reactivación sigue sujeta a tu plan.", "/propietario");
        return ReporteHabitacionResponse.fromEntity(row);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ModeracionEventoResponse> historialUsuario(Integer admin, Integer id, Pageable page) {
        admins.require(admin); validatePage(page);
        if (!userReports.existsById(id)) throw new ResourceNotFoundException("Reporte no encontrado");
        return events.findByReporteUsuarioIdReporteOrderByFechaDescIdEventoDesc(id, page).map(ModeracionEventoResponse::fromEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ModeracionEventoResponse> historialHabitacion(Integer admin, Integer id, Pageable page) {
        admins.require(admin); validatePage(page);
        if (!roomReports.existsById(id)) throw new ResourceNotFoundException("Reporte no encontrado");
        return events.findByReporteHabitacionIdReporteHabitacionOrderByFechaDescIdEventoDesc(id, page).map(ModeracionEventoResponse::fromEntity);
    }

    private Usuario activeReporter(Integer id) {
        return users.lockById(id).filter(u -> "activo".equals(u.getEstado())).orElseThrow(() -> new AccessDeniedException("La cuenta no está activa"));
    }
    private ReporteUsuario userReport(Integer id) { return userReports.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado")); }
    private ReporteHabitacion roomReport(Integer id) { return roomReports.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado")); }
    private void requireOpen(String state) {
        if (!Set.of("pendiente","revisado").contains(state)) throw new ConflictException("El reporte ya fue resuelto. Su historial no se modifica.");
    }
    private String reviewState(String state) {
        String value = filterState(state);
        if (value == null || !Set.of("revisado","rechazado").contains(value)) throw new IllegalArgumentException("Usa revisado o rechazado");
        return value;
    }
    private String filterState(String state) {
        if (state == null || state.isBlank()) return null;
        String value = state.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("pendiente","revisado","rechazado","sancionado").contains(value)) throw new IllegalArgumentException("Estado de reporte no válido");
        return value;
    }
    private void validatePage(Pageable page) {
        if (page.isUnpaged() || page.getPageSize()>100 || page.getPageNumber()>1000) throw new IllegalArgumentException("La página excede el límite permitido");
    }
    private String reason(String value, int max) {
        if (value == null || value.isBlank() || value.length()>max) throw new IllegalArgumentException("El motivo es obligatorio y no puede superar " + max + " caracteres");
        return value.trim();
    }
    private LocalDateTime now() { return LocalDateTime.now(clock); }
    private void notify(Usuario user, String title, String message, String path) {
        Notificacion notification = new Notificacion(); notification.setUsuario(user); notification.setTipo("reporte");
        notification.setTitulo(title); notification.setMensaje(message); notification.setUrlDestino(path); notifications.save(notification);
    }
}
