package com.roommatch.service;

import com.roommatch.exception.ConflictException;

import com.roommatch.dto.LoginRequest;
import com.roommatch.dto.LoginResponse;
import com.roommatch.dto.RegistroRequest;
import com.roommatch.dto.UsuarioResponse;
import com.roommatch.model.Rol;
import com.roommatch.model.Usuario;
import com.roommatch.repository.RolRepository;
import com.roommatch.repository.UsuarioRepository;
import com.roommatch.security.JwtService;
import com.roommatch.security.RequestLimiter;
import com.roommatch.validation.StrongPasswordValidator;
import org.springframework.security.authentication.BadCredentialsException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
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
    private final RequestLimiter limiter;
    private final String dummyPasswordHash;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RequestLimiter limiter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiter = limiter;
        this.dummyPasswordHash = passwordEncoder.encode("Dummy password, never a user credential");
    }

    @Transactional
    public UsuarioResponse registrarUsuario(RegistroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!StrongPasswordValidator.valid(request.getPassword())) {
            throw new IllegalArgumentException("Usa al menos 15 caracteres y hasta 72 bytes UTF-8 para la contraseña");
        }

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflictException(
                    "Ya existe una cuenta registrada con este correo electrónico"
            );
        }

        Rol rolUsuario = rolRepository
                .findByNombreRol(ApiConstants.ROL_USUARIO)
                .orElseThrow(() -> new IllegalStateException(
                        "Configuración incompleta: no existe el rol USUARIO"
                ));

        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setEdad(request.getEdad());
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolUsuario);
        usuario.setEstado(ApiConstants.ESTADO_ACTIVO);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(usuarioGuardado);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String accountKey = accountKey(emailNormalizado);
        limiter.check(accountKey, 10, Duration.ofMinutes(15));

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);
        String hash = usuario == null ? dummyPasswordHash : usuario.getPasswordHash();
        String password = request.getPassword();
        boolean supported = password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
        boolean matches = passwordEncoder.matches(supported ? password : "Invalid oversized password", hash);
        if (!supported || !matches || usuario == null || !ApiConstants.ESTADO_ACTIVO.equalsIgnoreCase(usuario.getEstado())) {
            throw new BadCredentialsException("El correo o la contraseña son incorrectos");
        }
        limiter.reset(accountKey);

        String token = jwtService.generarToken(usuario);
        UsuarioResponse usuarioResponse = UsuarioResponse.fromEntity(usuario);

        return new LoginResponse(token, "Bearer", usuarioResponse);
    }

    private String accountKey(String email) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(email.getBytes(StandardCharsets.UTF_8));
            return "account:" + java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
