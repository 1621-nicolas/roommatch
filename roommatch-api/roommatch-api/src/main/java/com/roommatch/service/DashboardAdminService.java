package com.roommatch.service;

import com.roommatch.dto.DashboardAdminResponse;
import com.roommatch.repository.DashboardAdminRepository;
import com.roommatch.util.ApiConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardAdminService {

    private final DashboardAdminRepository dashboardRepository;
    private final AdminAuthorization admins;

    public DashboardAdminService(
            DashboardAdminRepository dashboardRepository,
            AdminAuthorization admins
    ) {
        this.dashboardRepository = dashboardRepository;
        this.admins = admins;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public DashboardAdminResponse obtenerDashboard(Integer idAdmin) {
        admins.require(idAdmin);

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

}
