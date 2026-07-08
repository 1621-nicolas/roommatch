package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.SolicitudContactoRequest;
import com.roommatch.dto.SolicitudContactoResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.SolicitudContactoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudContactoController {

    private final SolicitudContactoService solicitudService;

    public SolicitudContactoController(SolicitudContactoService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/{idUsuarioReceptor}")
    public ResponseEntity<ApiResponse<SolicitudContactoResponse>> enviarSolicitud(
            Authentication authentication,
            @PathVariable Integer idUsuarioReceptor,
            @Valid @RequestBody SolicitudContactoRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            SolicitudContactoResponse response = solicitudService.enviarSolicitud(
                    usuario.getIdUsuario(),
                    idUsuarioReceptor,
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Solicitud enviada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/recibidas")
    public ResponseEntity<ApiResponse<List<SolicitudContactoResponse>>> listarRecibidas(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        List<SolicitudContactoResponse> solicitudes = solicitudService.listarRecibidas(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(solicitudes, "Solicitudes recibidas obtenidas correctamente")
        );
    }

    @GetMapping("/enviadas")
    public ResponseEntity<ApiResponse<List<SolicitudContactoResponse>>> listarEnviadas(
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        List<SolicitudContactoResponse> solicitudes = solicitudService.listarEnviadas(
                usuario.getIdUsuario()
        );

        return ResponseEntity.ok(
                ApiResponse.success(solicitudes, "Solicitudes enviadas obtenidas correctamente")
        );
    }

    @PutMapping("/{idSolicitud}/aceptar")
    public ResponseEntity<ApiResponse<SolicitudContactoResponse>> aceptarSolicitud(
            Authentication authentication,
            @PathVariable Integer idSolicitud
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            SolicitudContactoResponse response = solicitudService.aceptarSolicitud(
                    usuario.getIdUsuario(),
                    idSolicitud
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Solicitud aceptada y contacto desbloqueado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/{idSolicitud}/rechazar")
    public ResponseEntity<ApiResponse<SolicitudContactoResponse>> rechazarSolicitud(
            Authentication authentication,
            @PathVariable Integer idSolicitud
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            SolicitudContactoResponse response = solicitudService.rechazarSolicitud(
                    usuario.getIdUsuario(),
                    idSolicitud
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Solicitud rechazada correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
}