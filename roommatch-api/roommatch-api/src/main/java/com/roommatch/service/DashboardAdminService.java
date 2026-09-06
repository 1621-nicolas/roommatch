package com.roommatch.service;

import com.roommatch.dto.DashboardAdminResponse;
import com.roommatch.model.Usuario;
import com.roommatch.repository.DashboardAdminRepository;
import com.roommatch.repository.UsuarioRepository;
import com.roommatch.util.ApiConstants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public DashboardAdminResponse obtenerDashboard(Integer idAdmin) {
        validarAdmin(idAdmin);

        DashboardAdminResponse response = new DashboardAdminResponse();
        response.setTotalUsuarios(dashboardRepository.totalUsuarios());
        response.setUsuariosActivos(
                dashboardRepository.usuariosPorEstado(ApiConstants.ESTADO_ACTIVO)
        );
        response.setUsuariosSuspendidos(
                dashboardRepository.usuariosPorEstado(ApiConstants.ESTADO_SUSPENDIDO)
        );
        response.setTotalPropietarios(dashboardRepository.totalPropietarios());
        response.setTotalPerfilesConvivencia(dashboardRepository.totalPerfilesConvivencia());
        response.setTotalHabitaciones(dashboardRepository.totalHabitaciones());
        response.setHabitacionesActivas(
                dashboardRepository.habitacionesPorEstado(ApiConstants.ESTADO_ACTIVA)
        );
        response.setHabitacionesPausadas(
                dashboardRepository.habitacionesPorEstado(ApiConstants.ESTADO_PAUSADA)
        );
        response.setTotalPublicacionesRoomie(dashboardRepository.totalPublicacionesRoomie());
        response.setPublicacionesActivas(
                dashboardRepository.publicacionesRoomiePorEstado(ApiConstants.ESTADO_ACTIVA)
        );
        response.setTotalMatches(dashboardRepository.totalMatches());
        response.setSolicitudesPendientes(
                dashboardRepository.solicitudesPorEstado(ApiConstants.ESTADO_PENDIENTE)
        );
        response.setLeadsPendientes(
                dashboardRepository.leadsPorEstado(ApiConstants.ESTADO_PENDIENTE)
        );
        response.setReportesUsuariosPendientes(
                dashboardRepository.reportesUsuariosPorEstado(ApiConstants.ESTADO_PENDIENTE)
        );
        response.setReportesHabitacionesPendientes(
                dashboardRepository.reportesHabitacionesPorEstado(ApiConstants.ESTADO_PENDIENTE)
        );
        response.setNotificacionesNoLeidas(dashboardRepository.notificacionesNoLeidas());

        return response;
    }

    private void validarAdmin(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new AccessDeniedException("No tienes permisos de administrador");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new AccessDeniedException(
                        "No tienes permisos de administrador"
                ));

        if (
                usuario.getRol() == null ||
                !ApiConstants.ROL_ADMIN.equalsIgnoreCase(usuario.getRol().getNombreRol())
        ) {
            throw new AccessDeniedException("No tienes permisos de administrador");
        }
    }
}
