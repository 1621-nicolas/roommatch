package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.HabitacionRequest;
import com.roommatch.dto.HabitacionResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.HabitacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HabitacionResponse>> crearHabitacion(
            Authentication authentication,
            @Valid @RequestBody HabitacionRequest request
    ) {
        Usuario usuario = usuarioAutenticado(authentication);
        HabitacionResponse response = habitacionService.crearHabitacion(
                usuario.getIdUsuario(),
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Habitación publicada correctamente")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HabitacionResponse>>> listarHabitaciones(
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Boolean amoblado,
            @RequestParam(required = false) Boolean banoPrivado,
            @RequestParam(required = false) Boolean permiteMascotas,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(100) int size
    ) {
        if (precioMin != null && precioMax != null && precioMax.compareTo(precioMin) < 0) {
            throw new IllegalArgumentException(
                    "El precio máximo no puede ser menor que el precio mínimo"
            );
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<HabitacionResponse> habitaciones = habitacionService.listarHabitacionesPublicas(
                distrito,
                precioMin,
                precioMax,
                amoblado,
                banoPrivado,
                permiteMascotas,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(habitaciones, "Habitaciones obtenidas correctamente")
        );
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<Page<HabitacionResponse>>> listarMisHabitaciones(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(100) int size
    ) {
        Usuario usuario = usuarioAutenticado(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<HabitacionResponse> habitaciones = habitacionService.listarMisHabitaciones(
                usuario.getIdUsuario(),
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(habitaciones, "Mis habitaciones obtenidas correctamente")
        );
    }

    @GetMapping("/{idHabitacion}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> obtenerHabitacion(
            @PathVariable @Min(1) Integer idHabitacion
    ) {
        HabitacionResponse response = habitacionService.obtenerHabitacionPorId(idHabitacion);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Habitación obtenida correctamente")
        );
    }

    @PutMapping("/{idHabitacion}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> actualizarHabitacion(
            Authentication authentication,
            @PathVariable @Min(1) Integer idHabitacion,
            @Valid @RequestBody HabitacionRequest request
    ) {
        Usuario usuario = usuarioAutenticado(authentication);
        HabitacionResponse response = habitacionService.actualizarHabitacion(
                usuario.getIdUsuario(),
                idHabitacion,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Habitación actualizada correctamente")
        );
    }

    @PutMapping("/{idHabitacion}/pausar")
    public ResponseEntity<ApiResponse<HabitacionResponse>> pausarHabitacion(
            Authentication authentication,
            @PathVariable @Min(1) Integer idHabitacion
    ) {
        Usuario usuario = usuarioAutenticado(authentication);
        HabitacionResponse response = habitacionService.pausarHabitacion(
                usuario.getIdUsuario(),
                idHabitacion
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Habitación pausada correctamente")
        );
    }

    @PutMapping("/{idHabitacion}/activar")
    public ResponseEntity<ApiResponse<HabitacionResponse>> activarHabitacion(
            Authentication authentication,
            @PathVariable @Min(1) Integer idHabitacion
    ) {
        Usuario usuario = usuarioAutenticado(authentication);
        HabitacionResponse response = habitacionService.activarHabitacion(
                usuario.getIdUsuario(),
                idHabitacion
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Habitación activada correctamente")
        );
    }

    private Usuario usuarioAutenticado(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("No se pudo resolver el usuario autenticado");
        }
        return usuario;
    }
}
