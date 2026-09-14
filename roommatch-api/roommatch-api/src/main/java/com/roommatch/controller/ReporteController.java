package com.roommatch.controller;

import com.roommatch.dto.*;
import com.roommatch.model.Usuario;
import com.roommatch.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService service;
    public ReporteController(ReporteService service) { this.service = service; }

    @PostMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> reportarUsuario(Authentication auth, @PathVariable Integer id, @Valid @RequestBody ReporteRequest request) {
        return ok(service.reportarUsuario(actor(auth), id, request), "Reporte enviado");
    }
    @PostMapping("/habitaciones/{id}")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> reportarHabitacion(Authentication auth, @PathVariable Integer id, @Valid @RequestBody ReporteRequest request) {
        return ok(service.reportarHabitacion(actor(auth), id, request), "Reporte enviado");
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<ApiResponse<Page<ReporteUsuarioResponse>>> listarUsuario(Authentication auth, @RequestParam(required=false) String estado,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ok(service.listarReportesUsuarios(actor(auth), estado, page(page,size)), "Reportes obtenidos");
    }
    @PutMapping("/admin/usuarios/{id}/revisar")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> revisarUsuario(Authentication auth, @PathVariable Integer id,
            @RequestParam String estado, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.revisarReporteUsuario(actor(auth), id, estado, request.motivo()), "Revisión registrada");
    }
    @PutMapping("/admin/usuarios/{id}/sancionar")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> sancionarUsuario(Authentication auth, @PathVariable Integer id, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.sancionarUsuario(actor(auth), id, request.motivo()), "Sanción registrada");
    }
    @PutMapping("/admin/usuarios/{id}/restaurar")
    public ResponseEntity<ApiResponse<ReporteUsuarioResponse>> restaurarUsuario(Authentication auth, @PathVariable Integer id, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.restaurarUsuario(actor(auth), id, request.motivo()), "Restauración registrada");
    }
    @GetMapping("/admin/usuarios/{id}/historial")
    public ResponseEntity<ApiResponse<Page<ModeracionEventoResponse>>> historialUsuario(Authentication auth, @PathVariable Integer id,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ok(service.historialUsuario(actor(auth), id, page(page,size)), "Historial obtenido");
    }

    @GetMapping("/admin/habitaciones")
    public ResponseEntity<ApiResponse<Page<ReporteHabitacionResponse>>> listarHabitacion(Authentication auth, @RequestParam(required=false) String estado,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ok(service.listarReportesHabitaciones(actor(auth), estado, page(page,size)), "Reportes obtenidos");
    }
    @PutMapping("/admin/habitaciones/{id}/revisar")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> revisarHabitacion(Authentication auth, @PathVariable Integer id,
            @RequestParam String estado, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.revisarReporteHabitacion(actor(auth), id, estado, request.motivo()), "Revisión registrada");
    }
    @PutMapping("/admin/habitaciones/{id}/sancionar")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> sancionarHabitacion(Authentication auth, @PathVariable Integer id, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.sancionarHabitacion(actor(auth), id, request.motivo()), "Sanción registrada");
    }
    @PutMapping("/admin/habitaciones/{id}/restaurar")
    public ResponseEntity<ApiResponse<ReporteHabitacionResponse>> restaurarHabitacion(Authentication auth, @PathVariable Integer id, @Valid @RequestBody DecisionModeracionRequest request) {
        return ok(service.restaurarHabitacion(actor(auth), id, request.motivo()), "Restauración registrada");
    }
    @GetMapping("/admin/habitaciones/{id}/historial")
    public ResponseEntity<ApiResponse<Page<ModeracionEventoResponse>>> historialHabitacion(Authentication auth, @PathVariable Integer id,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ok(service.historialHabitacion(actor(auth), id, page(page,size)), "Historial obtenido");
    }

    private Integer actor(Authentication auth) { return ((Usuario) auth.getPrincipal()).getIdUsuario(); }
    private PageRequest page(int page, int size) {
        if (page<0 || page>1000 || size<1 || size>100) throw new IllegalArgumentException("Usa page entre 0 y 1000 y size entre 1 y 100");
        return PageRequest.of(page,size);
    }
    private <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) { return ResponseEntity.ok(ApiResponse.success(data,message)); }
}
