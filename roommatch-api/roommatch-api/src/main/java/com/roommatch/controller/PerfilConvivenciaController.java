package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.PerfilConvivenciaRequest;
import com.roommatch.dto.PerfilConvivenciaResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.PerfilConvivenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/perfil")
public class PerfilConvivenciaController {

    private final PerfilConvivenciaService perfilService;

    public PerfilConvivenciaController(PerfilConvivenciaService perfilService) {
        this.perfilService = perfilService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PerfilConvivenciaResponse>> crearPerfil(
            Authentication authentication,
            @Valid @RequestBody PerfilConvivenciaRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            PerfilConvivenciaResponse response = perfilService.crearPerfil(
                    usuario.getIdUsuario(),
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Perfil de convivencia registrado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PerfilConvivenciaResponse>> obtenerMiPerfil(
            Authentication authentication
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            PerfilConvivenciaResponse response = perfilService.obtenerMiPerfil(
                    usuario.getIdUsuario()
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Perfil obtenido correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<PerfilConvivenciaResponse>> actualizarMiPerfil(
            Authentication authentication,
            @Valid @RequestBody PerfilConvivenciaRequest request
    ) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();

            PerfilConvivenciaResponse response = perfilService.actualizarMiPerfil(
                    usuario.getIdUsuario(),
                    request
            );

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Perfil actualizado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
    @GetMapping("/buscar/distrito")
public ResponseEntity<ApiResponse<List<PerfilConvivenciaResponse>>> buscarPorDistrito(
        @RequestParam String distrito
) {
    List<PerfilConvivenciaResponse> perfiles =
            perfilService.buscarPerfilesPorDistrito(distrito);

    return ResponseEntity.ok(
            ApiResponse.success(perfiles, "Perfiles encontrados por distrito")
    );
}

@GetMapping("/compatibles-basico")
public ResponseEntity<ApiResponse<List<PerfilConvivenciaResponse>>> buscarCompatiblesBasico(
        Authentication authentication
) {
    Usuario usuario = (Usuario) authentication.getPrincipal();

    List<PerfilConvivenciaResponse> perfiles =
            perfilService.buscarCompatiblesBasico(usuario.getIdUsuario());

    return ResponseEntity.ok(
            ApiResponse.success(perfiles, "Perfiles compatibles encontrados con Named Query")
    );
}
}