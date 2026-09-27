package com.roommatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatch.model.Rol;
import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtService jwt = mock(JwtService.class);
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users,
            new SecurityErrorWriter(new ObjectMapper()));
    private final FilterChain chain = mock(FilterChain.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void invalidTokenDoesNotExecuteDownstreamTwiceOnFailure() throws Exception {
        request.addHeader("Authorization", "Bearer bad");
        when(jwt.obtenerIdUsuario("bad")).thenThrow(new JwtException("invalid"));
        doThrow(new ServletException("downstream")).when(chain).doFilter(request, response);
        assertThatThrownBy(() -> filter.doFilter(request, response, chain)).isInstanceOf(ServletException.class);
        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(users);
    }

    @Test void suspendedUserIsNotAuthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer valid");
        when(jwt.obtenerIdUsuario("valid")).thenReturn(1);
        var user = new Usuario(); user.setEstado("suspendido");
        when(users.findById(1)).thenReturn(Optional.of(user));
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test void authorityUsesCurrentDatabaseRole() throws Exception {
        request.addHeader("Authorization", "Bearer valid");
        when(jwt.obtenerIdUsuario("valid")).thenReturn(1);
        var role = new Rol(); role.setNombreRol("ADMIN");
        var user = new Usuario(); user.setEstado("activo"); user.setRol(role);
        when(users.findById(1)).thenReturn(Optional.of(user));
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test void databaseFailureIs503InsteadOfAnonymousAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer valid");
        when(jwt.obtenerIdUsuario("valid")).thenReturn(1);
        when(users.findById(1)).thenThrow(new DataAccessResourceFailureException("offline"));
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentType()).startsWith("application/json");
        verifyNoInteractions(chain);
    }
}
