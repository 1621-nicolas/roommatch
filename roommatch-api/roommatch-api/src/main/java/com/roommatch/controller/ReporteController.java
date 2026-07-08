package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.ReporteHabitacionResponse;
import com.roommatch.dto.ReporteRequest;
import com.roommatch.dto.ReporteUsuarioResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping("/usuarios/{idUsuarioReportado}")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> reportarUsuario(
            Authentication authentication,
            @PathVariable Integer idUsuarioReportado,
            @Valid @RequestBody ReporteRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteUsuarioResponse response = reporteService.reportarUsuario(
                    usuario.getIdUsuario(),
                    idUsuarioReportado,
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Usuario reportado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PostMapping("/habitaciones/{idHabitacion}")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> reportarHabitacion(
            Authentication authentication,
            @PathVariable Integer idHabitacion,
            @Valid @RequestBody ReporteRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteHabitacionResponse response = reporteService.reportarHabitacion(
                    usuario.getIdUsuario(),
                    idHabitacion,
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación reportada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<ApiResponse<Page<ReporteUsuarioResponse>>> listarReportesUsuarios(
            Authentication authentication,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);

            Page<ReporteUsuarioResponse> reportes = reporteService.listarReportesUsuarios(
                    usuario.getIdUsuario(),
                    estado,
                    pageable
            );

            return ResponseEntity.ok(
                    ApiResponse.success(reportes, "Reportes de usuarios obtenidos correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/admin/habitaciones")
    public ResponseEntity<ApiResponse<Page<ReporteHabitacionResponse>>> listarReportesHabitaciones(
            Authentication authentication,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);

            Page<ReporteHabitacionResponse> reportes = reporteService.listarReportesHabitaciones(
                    usuario.getIdUsuario(),
                    estado,
                    pageable
            );

            return ResponseEntity.ok(
                    ApiResponse.success(reportes, "Reportes de habitaciones obtenidos correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/admin/usuarios/{idReporte}/revisar")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> revisarReporteUsuario(
            Authentication authentication,
            @PathVariable Integer idReporte,
            @RequestParam String estado
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteUsuarioResponse response = reporteService.revisarReporteUsuario(
                    usuario.getIdUsuario(),
                    idReporte,
                    estado
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Reporte de usuario actualizado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/admin/habitaciones/{idReporte}/revisar")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> revisarReporteHabitacion(
            Authentication authentication,
            @PathVariable Integer idReporte,
            @RequestParam String estado
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteHabitacionResponse response = reporteService.revisarReporteHabitacion(
                    usuario.getIdUsuario(),
                    idReporte,
                    estado
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Reporte de habitación actualizado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/admin/usuarios/{idReporte}/sancionar")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> sancionarUsuario(
            Authentication authentication,
            @PathVariable Integer idReporte
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteUsuarioResponse response = reporteService.sancionarUsuario(
                    usuario.getIdUsuario(),
                    idReporte
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Usuario sancionado y suspendido correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/admin/habitaciones/{idReporte}/sancionar")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> sancionarHabitacion(
            Authentication authentication,
            @PathVariable Integer idReporte
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            ReporteHabitacionResponse response = reporteService.sancionarHabitacion(
                    usuario.getIdUsuario(),
                    idReporte
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación sancionada y pausada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
}