package com.roommatch.controller;

import com.roommatch.dto.ApiResponse;
import com.roommatch.dto.LoginRequest;
import com.roommatch.dto.LoginResponse;
import com.roommatch.dto.RegistroRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        UsuarioResponse usuario = authService.registrarUsuario(request);

        return ResponseEntity.ok(
                ApiResponse.success(usuario, "Usuario registrado correctamente")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Inicio de sesión correcto")
        );
    }
}
