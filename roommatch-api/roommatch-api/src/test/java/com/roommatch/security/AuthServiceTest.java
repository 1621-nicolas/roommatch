package com.roommatch.security;

import com.roommatch.dto.LoginRequest;
import com.roommatch.model.Usuario;
import com.roommatch.repository.*;
import com.roommatch.service.AuthService;
import com.roommatch.validation.StrongPasswordValidator;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Clock;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test void missingInactiveAndWrongPasswordHaveSameError() {
        var users=mock(UsuarioRepository.class); var encoder=mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("dummy");
        when(encoder.matches(anyString(),anyString())).thenReturn(false);
        var service=new AuthService(users,mock(RolRepository.class),encoder,mock(JwtService.class),new RequestLimiter(Clock.systemUTC()));
        var request=new LoginRequest(); request.setEmail(" A@example.test "); request.setPassword("old123");
        when(users.findByEmail("a@example.test")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class)
                .hasMessage("El correo o la contraseña son incorrectos");
        verify(encoder).matches("old123","dummy");
        var user=new Usuario(); user.setPasswordHash("hash"); user.setEstado("suspendido");
        when(users.findByEmail("a@example.test")).thenReturn(Optional.of(user));
        when(encoder.matches("old123","hash")).thenReturn(true);
        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class)
                .hasMessage("El correo o la contraseña son incorrectos");
        user.setEstado("activo"); when(encoder.matches("old123","hash")).thenReturn(false);
        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class)
                .hasMessage("El correo o la contraseña son incorrectos");
    }
    @Test void passwordPolicySupportsPhrasesAndRejectsUtf8Overflow() {
        assertThat(StrongPasswordValidator.valid("mi primera casa tiene muchas ventanas")).isTrue();
        assertThat(StrongPasswordValidator.valid("short")).isFalse();
        assertThat(StrongPasswordValidator.valid("🔑".repeat(8))).isFalse();
        assertThat(StrongPasswordValidator.valid("abc"+"🔑".repeat(17))).isTrue();
        assertThat(StrongPasswordValidator.valid("abc"+"🔑".repeat(18))).isFalse();
        assertThat(StrongPasswordValidator.valid("123456789012345")).isFalse();
    }
}
