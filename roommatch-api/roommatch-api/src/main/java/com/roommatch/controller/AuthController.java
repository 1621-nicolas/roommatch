package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.RegistroRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.roommatch.dto.LoginRequest;
import com.roommatch.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrar(
            @Valid @RequestBody RegistroRequest request
    ) {
        try {
            UsuarioResponse usuario = authService.registrarUsuario(request);

            return ResponseEntity.ok(
                    ApiResponse.success(usuario, "Usuario registrado correctamente")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(e.getMessage())
            );
        }
    }
   @PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
) {
    try {
        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Inicio de sesión correcto")
        );

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.fail(e.getMessage())
        );
    }
}
}