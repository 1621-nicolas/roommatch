package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.LeadHabitacionRequest;
import com.roommatch.dto.LeadHabitacionResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.LeadHabitacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadHabitacionController {

    private final LeadHabitacionService leadService;

    public LeadHabitacionController(LeadHabitacionService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/habitacion/{idHabitacion}")
    public ResponseEntity<ApiResponse<LeadHabitacionResponse>> crearLead(
            Authentication authentication,
            @PathVariable Integer idHabitacion,
            @Valid @RequestBody LeadHabitacionRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            LeadHabitacionResponse response = leadService.crearLead(
                    usuario.getIdUsuario(),
                    idHabitacion,
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Interés enviado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<List<LeadHabitacionResponse>>> listarMisIntereses(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        List<LeadHabitacionResponse> intereses = leadService.listarMisIntereses(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(intereses, "Mis intereses obtenidos correctamente")
        );
    }

    @GetMapping("/propietario")
    public ResponseEntity<ApiResponse<Page<LeadHabitacionResponse>>> listarLeadsPropietario(
            Authentication authentication,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            Pageable pageable = PageRequest.of(page, size);

            Page<LeadHabitacionResponse> leads = leadService.listarLeadsPropietario(
                    usuario.getIdUsuario(),
                    estado,
                    pageable
            );

            return ResponseEntity.ok(
                    ApiResponse.success(leads, "Leads del propietario obtenidos correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/{idLead}/estado")
    public ResponseEntity<ApiResponse<LeadHabitacionResponse>> cambiarEstadoLead(
            Authentication authentication,
            @PathVariable Integer idLead,
            @RequestParam String estado
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            LeadHabitacionResponse response = leadService.cambiarEstadoLead(
                    usuario.getIdUsuario(),
                    idLead,
                    estado
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Estado del lead actualizado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
}