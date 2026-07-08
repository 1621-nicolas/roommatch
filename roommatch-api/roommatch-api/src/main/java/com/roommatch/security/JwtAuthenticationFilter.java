package com.roommatch.security;

import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        /*
         * Si no existe Authorization
         * o no utiliza Bearer,
         * continuamos la cadena.
         */
        if (
                authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {

            /*
             * Validamos firma y expiración
             * del token JWT.
             */
            if (!jwtService.validarToken(token)) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * El subject del JWT contiene
             * el correo del usuario.
             */
            String email =
                    jwtService.obtenerEmailDelToken(token);

            if (
                    email != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null
            ) {

                /*
                 * Recuperamos la entidad Usuario.
                 */
                Usuario usuario =
                        usuarioRepository
                                .findByEmail(
                                        email
                                                .trim()
                                                .toLowerCase()
                                )
                                .orElse(null);

                if (usuario != null) {

                    String nombreRol =
                            usuario
                                    .getRol()
                                    .getNombreRol();

                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority(
                                    "ROLE_" + nombreRol
                            );

                    /*
                     * IMPORTANTE:
                     *
                     * EL PRINCIPAL ES EL USUARIO.
                     *
                     * Por tanto:
                     *
                     * authentication.getPrincipal()
                     *
                     * devuelve un objeto Usuario.
                     */
                    UsernamePasswordAuthenticationToken authentication =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    usuario,
                                    null,
                                    List.of(authority)
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception exception) {

            System.err.println(
                    "ERROR JWT ROOMMATCH: "
                            + exception
                                    .getClass()
                                    .getName()
            );

            System.err.println(
                    "MENSAJE: "
                            + exception.getMessage()
            );

            exception.printStackTrace();

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}