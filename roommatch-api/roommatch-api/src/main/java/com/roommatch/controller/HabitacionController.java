package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.HabitacionRequest;
import com.roommatch.dto.HabitacionResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.HabitacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            HabitacionResponse response = habitacionService.crearHabitacion(
                    usuario.getIdUsuario(),
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación publicada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HabitacionResponse>>> listarHabitaciones(
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Boolean amoblado,
            @RequestParam(required = false) Boolean banoPrivado,
            @RequestParam(required = false) Boolean permiteMascotas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
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

    @GetMapping("/{idHabitacion}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> obtenerHabitacion(
            @PathVariable Integer idHabitacion
    ) {
        try {
            HabitacionResponse response = habitacionService.obtenerHabitacionPorId(idHabitacion);

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación obtenida correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<Page<HabitacionResponse>>> listarMisHabitaciones(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);

            Page<HabitacionResponse> habitaciones = habitacionService.listarMisHabitaciones(
                    usuario.getIdUsuario(),
                    pageable
            );

            return ResponseEntity.ok(
                    ApiResponse.success(habitaciones, "Mis habitaciones obtenidas correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/{idHabitacion}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> actualizarHabitacion(
            Authentication authentication,
            @PathVariable Integer idHabitacion,
            @Valid @RequestBody HabitacionRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            HabitacionResponse response = habitacionService.actualizarHabitacion(
                    usuario.getIdUsuario(),
                    idHabitacion,
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación actualizada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/{idHabitacion}/pausar")
    public ResponseEntity<ApiResponse<HabitacionResponse>> pausarHabitacion(
            Authentication authentication,
            @PathVariable Integer idHabitacion
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            HabitacionResponse response = habitacionService.pausarHabitacion(
                    usuario.getIdUsuario(),
                    idHabitacion
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación pausada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/{idHabitacion}/activar")
    public ResponseEntity<ApiResponse<HabitacionResponse>> activarHabitacion(
            Authentication authentication,
            @PathVariable Integer idHabitacion
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            HabitacionResponse response = habitacionService.activarHabitacion(
                    usuario.getIdUsuario(),
                    idHabitacion
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Habitación activada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
}