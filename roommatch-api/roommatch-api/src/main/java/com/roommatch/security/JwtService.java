package com.roommatch.security;

import com.roommatch.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;

/** Role and account state come from the database, never from token claims. */
@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    private final String issuer;
    private final String audience;
    private final Clock clock;
    private final JwtParser parser;

    public JwtService(@Value("${jwt.secret:}") String secret,
                      @Value("${jwt.expiration:7200000}") long expiration,
                      @Value("${jwt.issuer:roommatch-production}") String issuer,
                      @Value("${jwt.audience:roommatch-web}") String audience,
                      Environment environment, Clock clock) {
        boolean local = environment.acceptsProfiles(Profiles.of("development", "test"));
        if (local && environment.acceptsProfiles(Profiles.of("production"))) {
            throw new IllegalStateException("production no puede combinarse con development/test");
        }
        if (expiration < 60_000 || expiration > 86_400_000 || issuer.isBlank() || audience.isBlank()) {
            throw new IllegalStateException("JWT requiere issuer/audience y TTL entre 1 minuto y 24 horas");
        }
        if (local && secret.isBlank()) {
            // Ephemeral development key: never logged, persisted, or used in production.
            key = Jwts.SIG.HS256.key().build();
        } else {
            String normalized = secret.toLowerCase(Locale.ROOT);
            if (secret.getBytes(StandardCharsets.UTF_8).length < 32 || (!local &&
                    (normalized.contains("roommatch") || normalized.contains("example") ||
                     normalized.contains("changeme") || normalized.contains("development") ||
                     secret.codePoints().distinct().count() < 16))) {
                throw new IllegalStateException("JWT_SECRET debe ser una clave aleatoria externa de al menos 32 bytes; no se admiten ejemplos");
            }
            key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        }
        this.expiration = expiration;
        this.issuer = issuer;
        this.audience = audience;
        this.clock = clock;
        parser = Jwts.parser().verifyWith(key)
                .requireIssuer(issuer).requireAudience(audience)
                .clock(() -> Date.from(clock.instant())).build();
    }

    public String generarToken(Usuario usuario) {
        if (usuario.getIdUsuario() == null) throw new IllegalArgumentException("Usuario sin identidad persistida");
        return Jwts.builder().subject(usuario.getIdUsuario().toString())
                .issuer(issuer).audience().add(audience).and()
                .issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plusMillis(expiration)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Integer obtenerIdUsuario(String token) {
        var signed = parser.parseSignedClaims(token);
        // JJWT 0.12.6 cannot clear its algorithm registry. Check the verified header
        // before exposing any claims; every accepted token must be HS256.
        if (!"HS256".equals(signed.getHeader().getAlgorithm())) throw new JwtException("Algoritmo no permitido");
        Claims claims = signed.getPayload();
        Date now = Date.from(clock.instant());
        if (claims.getExpiration() == null || !claims.getExpiration().after(now) ||
                claims.getIssuedAt() == null || claims.getIssuedAt().after(now) ||
                Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant())
                        .compareTo(Duration.ofHours(24)) > 0) {
            throw new JwtException("Token fuera de su período de validez");
        }
        try {
            int id = Integer.parseInt(claims.getSubject());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new JwtException("Identidad de token inválida");
        }
    }
}
