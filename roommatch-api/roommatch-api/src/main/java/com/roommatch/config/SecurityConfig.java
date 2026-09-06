package com.roommatch.config;

import com.roommatch.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOriginsProperty;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String allowedOriginsProperty
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOriginsProperty = allowedOriginsProperty;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error", "/api/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Administración: defensa adicional además de las validaciones de servicio.
                        .requestMatchers("/api/admin/**", "/api/reportes/admin/**").hasRole("ADMIN")

                        // Estas rutas exactas deben evaluarse antes de los comodines públicos.
                        .requestMatchers(HttpMethod.GET, "/api/habitaciones/mis").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/publicaciones-roomie/mis").authenticated()

                        // Catálogo y contenido público.
                        .requestMatchers(HttpMethod.GET, "/api/planes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/habitaciones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/publicaciones-roomie/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/imagenes-habitacion/habitacion/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(origin -> origin.trim())
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();

        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "app.cors.allowed-origins debe contener al menos un origen permitido"
            );
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
        );
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
