package com.roommatch.controller;

import com.roommatch.dto.ActualizarUsuarioRequest;
import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.model.Usuario;
import com.roommatch.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioService usuarioService
    ) {
        this.usuarioService =
                usuarioService;
    }

    @GetMapping("/me")
    public ApiResponse<UsuarioResponse> obtenerMiUsuario(
            Authentication authentication
    ) {

        Usuario usuario =
                obtenerUsuarioAutenticado(
                        authentication
                );

        UsuarioResponse response =
                usuarioService.obtenerMiUsuario(
                        usuario.getIdUsuario()
                );

        return ApiResponse.success(
                response,
                "Usuario obtenido correctamente"
        );
    }

    @PutMapping("/me")
    public ApiResponse<UsuarioResponse> actualizarMiUsuario(
            Authentication authentication,
            @Valid
            @RequestBody
            ActualizarUsuarioRequest request
    ) {

        Usuario usuario =
                obtenerUsuarioAutenticado(
                        authentication
                );

        UsuarioResponse response =
                usuarioService.actualizarMiUsuario(
                        usuario.getIdUsuario(),
                        request
                );

        return ApiResponse.success(
                response,
                "Información personal actualizada correctamente"
        );
    }

    private Usuario obtenerUsuarioAutenticado(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            throw new IllegalArgumentException(
                    "Usuario no autenticado"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {

            return usuario;
        }

        throw new IllegalArgumentException(
                "No se pudo identificar al usuario autenticado"
        );
    }
}