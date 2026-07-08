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

            @Value(
                    "${app.cors.allowed-origins:http://localhost:4200}"
            )
            String allowedOriginsProperty

    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;


        this.allowedOriginsProperty =
                allowedOriginsProperty;
    }


    /*
     * =========================================================
     * PASSWORD ENCODER
     * =========================================================
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    /*
     * =========================================================
     * SECURITY FILTER CHAIN
     * =========================================================
     */

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 * =================================================
                 * CSRF
                 * =================================================
                 */

                .csrf(
                        csrf ->
                                csrf.disable()
                )


                /*
                 * =================================================
                 * CORS
                 * =================================================
                 */

                .cors(
                        Customizer.withDefaults()
                )


                /*
                 * =================================================
                 * SESSION STATELESS
                 * =================================================
                 */

                .sessionManagement(

                        session ->

                                session.sessionCreationPolicy(

                                        SessionCreationPolicy.STATELESS

                                )

                )


                /*
                 * =================================================
                 * AUTORIZACIÓN
                 * =================================================
                 */

                .authorizeHttpRequests(

                        auth -> auth


                                /*
                                 * =================================
                                 * PREFLIGHT CORS
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * ERROR
                                 * =================================
                                 */

                                .requestMatchers(
                                        "/error"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * AUTENTICACIÓN
                                 * =================================
                                 */

                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * TEST
                                 * =================================
                                 */

                                .requestMatchers(
                                        "/api/test/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * SWAGGER
                                 * =================================
                                 */

                                .requestMatchers(
                                        "/v3/api-docs"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/v3/api-docs/**"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/swagger-ui.html"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/swagger-ui/**"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/swagger-resources/**"
                                )
                                .permitAll()


                                .requestMatchers(
                                        "/webjars/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * RUTAS PRIVADAS DE HABITACIONES
                                 *
                                 * IMPORTANTE:
                                 * DEBEN IR ANTES DE /**
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/habitaciones/mis"
                                )
                                .authenticated()


                                /*
                                 * =================================
                                 * RUTAS PRIVADAS DE
                                 * PUBLICACIONES ROOMIE
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/publicaciones-roomie/mis"
                                )
                                .authenticated()


                                /*
                                 * =================================
                                 * PLANES PÚBLICOS
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/planes"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * HABITACIONES PÚBLICAS
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/habitaciones/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * PUBLICACIONES ROOMIE PÚBLICAS
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/publicaciones-roomie/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * IMÁGENES DE HABITACIONES PÚBLICAS
                                 * =================================
                                 */

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/imagenes-habitacion/habitacion/**"
                                )
                                .permitAll()


                                /*
                                 * =================================
                                 * RESTO PROTEGIDO
                                 * =================================
                                 */

                                .anyRequest()
                                .authenticated()

                )


                /*
                 * =================================================
                 * JWT FILTER
                 * =================================================
                 */

                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class

                );


        return http.build();
    }


    /*
     * =========================================================
     * CORS
     * =========================================================
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        List<String> allowedOrigins =

                Arrays
                        .stream(

                                allowedOriginsProperty
                                        .split(",")

                        )

                        .map(
                                String::trim
                        )

                        .filter(
                                origin ->

                                        !origin.isBlank()
                        )

                        .toList();


        CorsConfiguration configuration =
                new CorsConfiguration();


        configuration.setAllowedOrigins(
                allowedOrigins
        );


        configuration.setAllowedMethods(

                List.of(

                        "GET",

                        "POST",

                        "PUT",

                        "DELETE",

                        "OPTIONS"

                )

        );


        configuration.setAllowedHeaders(

                List.of(

                        "Authorization",

                        "Content-Type",

                        "Accept",

                        "Origin",

                        "X-Requested-With"

                )

        );


        configuration.setAllowCredentials(
                true
        );


        configuration.setMaxAge(
                3600L
        );


        UrlBasedCorsConfigurationSource source =

                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(

                "/**",

                configuration

        );


        return source;
    }
}