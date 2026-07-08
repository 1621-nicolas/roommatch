package com.roommatch.service;

import com.roommatch.dto.DashboardAdminResponse;
import com.roommatch.model.Usuario;
import com.roommatch.repository.DashboardAdminRepository;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardAdminService {

    private final DashboardAdminRepository dashboardRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardAdminService(
            DashboardAdminRepository dashboardRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.dashboardRepository = dashboardRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public DashboardAdminResponse obtenerDashboard(Integer idAdmin) {

        validarAdmin(idAdmin);

        DashboardAdminResponse response = new DashboardAdminResponse();

        response.setTotalUsuarios(dashboardRepository.totalUsuarios());
        response.setUsuariosActivos(dashboardRepository.usuariosPorEstado("activo"));
        response.setUsuariosSuspendidos(dashboardRepository.usuariosPorEstado("suspendido"));

        response.setTotalPropietarios(dashboardRepository.totalPropietarios());
        response.setTotalPerfilesConvivencia(dashboardRepository.totalPerfilesConvivencia());

        response.setTotalHabitaciones(dashboardRepository.totalHabitaciones());
        response.setHabitacionesActivas(dashboardRepository.habitacionesPorEstado("activa"));
        response.setHabitacionesPausadas(dashboardRepository.habitacionesPorEstado("pausada"));

        response.setTotalPublicacionesRoomie(dashboardRepository.totalPublicacionesRoomie());
        response.setPublicacionesActivas(dashboardRepository.publicacionesRoomiePorEstado("activa"));

        response.setTotalMatches(dashboardRepository.totalMatches());
        response.setSolicitudesPendientes(dashboardRepository.solicitudesPorEstado("pendiente"));
        response.setLeadsPendientes(dashboardRepository.leadsPorEstado("pendiente"));

        response.setReportesUsuariosPendientes(dashboardRepository.reportesUsuariosPorEstado("pendiente"));
        response.setReportesHabitacionesPendientes(dashboardRepository.reportesHabitacionesPorEstado("pendiente"));

        response.setNotificacionesNoLeidas(dashboardRepository.notificacionesNoLeidas());

        return response;
    }

    private void validarAdmin(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == null ||
                !usuario.getRol().getNombreRol().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("No tienes permisos de administrador");
        }
    }
}