package com.roommatch.security;

import com.roommatch.repository.UsuarioRepository;
import com.roommatch.util.ApiConstants;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final SecurityErrorWriter errors;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
                                   SecurityErrorWriter errors) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            Integer id = null;
            try {
                String token = header.substring(7).trim();
                if (token.length() > 4096) throw new JwtException("Token demasiado largo");
                id = jwtService.obtenerIdUsuario(token);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
            if (id != null) {
                try {
                    var usuario = usuarioRepository.findById(id).orElse(null);
                    if (usuario != null && ApiConstants.ESTADO_ACTIVO.equalsIgnoreCase(usuario.getEstado())
                            && usuario.getRol() != null) {
                        var authentication = UsernamePasswordAuthenticationToken.authenticated(usuario, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombreRol())));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (DataAccessException ex) {
                    errors.write(response, 503, "El servicio de datos no está disponible temporalmente");
                    return;
                }
            }
        }
        // Exactly once; downstream exceptions must not be mistaken for JWT errors.
        chain.doFilter(request, response);
    }
}
