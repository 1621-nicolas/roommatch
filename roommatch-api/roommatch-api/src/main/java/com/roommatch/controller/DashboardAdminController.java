package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.DashboardAdminResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.DashboardAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class DashboardAdminController {

    private final DashboardAdminService dashboardService;

    public DashboardAdminController(DashboardAdminService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> obtenerDashboard(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        DashboardAdminResponse response = dashboardService.obtenerDashboard(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Dashboard administrativo obtenido correctamente")
        );
    }
}
