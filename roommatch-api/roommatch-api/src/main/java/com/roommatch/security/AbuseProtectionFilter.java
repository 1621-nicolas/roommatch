package com.roommatch.security;

import com.roommatch.exception.RateLimitException;
import com.roommatch.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;

@Component
public class AbuseProtectionFilter extends OncePerRequestFilter {
    private final RequestLimiter limiter;
    private final SecurityErrorWriter errors;
    @org.springframework.beans.factory.annotation.Value("${app.limits.login-per-ip:30}") private int loginLimit = 30;
    @org.springframework.beans.factory.annotation.Value("${app.limits.register-per-ip:5}") private int registerLimit = 5;
    @org.springframework.beans.factory.annotation.Value("${app.limits.requests-per-user:20}") private int requestLimit = 20;
    @org.springframework.beans.factory.annotation.Value("${app.limits.reports-per-user:10}") private int reportLimit = 10;
    public AbuseProtectionFilter(RequestLimiter limiter, SecurityErrorWriter errors) {
        this.limiter = limiter; this.errors = errors;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.isBlank()) path = request.getRequestURI();
        try {
            if (!java.util.Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod())) {
                // Use the socket peer only; arbitrary X-Forwarded-For must never grant fresh quotas.
                if (path.equals("/api/auth/login")) limiter.check("login-ip:" + request.getRemoteAddr(), loginLimit, Duration.ofMinutes(15));
                if (path.equals("/api/auth/register")) limiter.check("register-ip:" + request.getRemoteAddr(), registerLimit, Duration.ofHours(1));
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Usuario user) {
                    String actor = user.getIdUsuario().toString();
                    if (path.startsWith("/api/matches")) limiter.check("matches:" + actor, 2, Duration.ofMinutes(1));
                    else if ("POST".equals(request.getMethod()) && path.matches("/api/solicitudes/\\d+"))
                        limiter.check("solicitudes:" + actor, requestLimit, Duration.ofDays(1));
                    else if (path.matches("/api/reportes/(usuarios|habitaciones)/\\d+"))
                        limiter.check("reportes:" + actor, reportLimit, Duration.ofDays(1));
                    else limiter.check("writes:" + actor, 60, Duration.ofMinutes(1));
                }
            }
        } catch (RateLimitException ex) {
            response.setHeader("Retry-After", Long.toString(ex.getRetryAfter()));
            errors.write(response, 429, ex.getMessage());
            return;
        }
        chain.doFilter(request, response);
    }

    @jakarta.annotation.PostConstruct void validateLimits() {
        if (loginLimit < 1 || registerLimit < 1 || requestLimit < 1 || reportLimit < 1)
            throw new IllegalStateException("Los límites de abuso deben ser positivos");
    }
}
