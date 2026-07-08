package com.roommatch.service;

import com.roommatch.dto.LoginRequest;
import com.roommatch.dto.LoginResponse;
import com.roommatch.dto.RegistroRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.model.Rol;
import com.roommatch.model.Usuario;
import com.roommatch.repository.RolRepository;
import com.roommatch.repository.UsuarioRepository;
import com.roommatch.security.JwtService;
import com.roommatch.util.ApiConstants;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse registrarUsuario(RegistroRequest request) {

        String emailNormalizado = request.getEmail()
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta registrada con este correo electrónico"
            );
        }

        Rol rolUsuario = rolRepository
                .findByNombreRol(ApiConstants.ROL_USUARIO)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rol USUARIO no encontrado"
                        )
                );

        Usuario usuario = new Usuario();

        usuario.setNombres(
                request.getNombres().trim()
        );

        usuario.setApellidos(
                request.getApellidos().trim()
        );

        usuario.setEmail(
                emailNormalizado
        );

        usuario.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        usuario.setRol(
                rolUsuario
        );

        usuario.setEstado(
                ApiConstants.ESTADO_ACTIVO
        );

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        return UsuarioResponse.fromEntity(
                usuarioGuardado
        );
    }

    public LoginResponse login(LoginRequest request) {

        String emailNormalizado = request.getEmail()
                .trim()
                .toLowerCase();

        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El correo no está registrado"
                        )
                );

        if (!usuario.getEstado().equalsIgnoreCase(
                ApiConstants.ESTADO_ACTIVO
        )) {
            throw new IllegalArgumentException(
                    "El usuario no se encuentra activo"
            );
        }

        boolean passwordValida = passwordEncoder.matches(
                request.getPassword(),
                usuario.getPasswordHash()
        );

        if (!passwordValida) {
            throw new IllegalArgumentException(
                    "La contraseña es incorrecta"
            );
        }

        String token = jwtService.generarToken(
                usuario
        );

        UsuarioResponse usuarioResponse =
                UsuarioResponse.fromEntity(usuario);

        return new LoginResponse(
                token,
                "Bearer",
                usuarioResponse
        );
    }
}