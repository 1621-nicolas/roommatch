package com.roommatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatch.model.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private static final String KEY = "Fixture_Only_0123456789_abcdefghijklmnopqrstuvwxyz_ABCD";
    private static final Instant NOW = Instant.parse("2026-09-13T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private JwtService service(String key, String profile, long ttl, Clock clock) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return new JwtService(key, ttl, "issuer-test", "roommatch-web", environment, clock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "short", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "RoomMatchLocalJWTSecret_2026_Development_Only_Change_In_Production_123456789"})
    void productionRejectsMissingWeakAndPublishedKeys(String key) {
        assertThatThrownBy(() -> service(key, "production", 60_000, CLOCK)).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 59_999, 86_400_001})
    void rejectsInvalidTtl(long ttl) {
        assertThatThrownBy(() -> service(KEY, "test", ttl, CLOCK)).isInstanceOf(IllegalStateException.class);
    }

    @Test void incompatibleProfilesCannotRelaxProduction() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("production", "development");
        assertThatThrownBy(() -> new JwtService("", 60_000, "issuer", "audience", environment, CLOCK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test void tokenHasStableIdentityAndNoProfilePii() throws Exception {
        var service = service(KEY, "production", 60_000, CLOCK);
        var user = new Usuario();
        user.setIdUsuario(42);
        user.setEmail("private@example.test");
        user.setNombres("Nombre privado");
        var token = service.generarToken(user);
        assertThat(service.obtenerIdUsuario(token)).isEqualTo(42);
        var claims = new ObjectMapper().readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertThat(claims.has("email")).isFalse();
        assertThat(claims.has("nombres")).isFalse();
        assertThat(claims.has("apellidos")).isFalse();
        assertThat(claims.has("rol")).isFalse();
        assertThat(claims.get("sub").asText()).isEqualTo("42");
        assertThat(claims.get("iss").asText()).isEqualTo("issuer-test");
    }

    @Test void expirationAtBoundaryIsRejected() {
        var user = new Usuario(); user.setIdUsuario(1);
        var token = service(KEY, "test", 60_000, CLOCK).generarToken(user);
        assertThatThrownBy(() -> service(KEY, "test", 60_000,
                Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC)).obtenerIdUsuario(token))
                .isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong-issuer", "wrong-audience", "no-exp", "future-iat", "no-sub", "wrong-key", "unsigned", "hs512"})
    void rejectsTokensOutsideContract(String variant) {
        var builder = Jwts.builder().issuer(variant.equals("wrong-issuer") ? "other" : "issuer-test")
                .audience().add(variant.equals("wrong-audience") ? "other" : "roommatch-web").and()
                .issuedAt(Date.from(variant.equals("future-iat") ? NOW.plusSeconds(10) : NOW));
        if (!variant.equals("no-sub")) builder.subject("1");
        if (!variant.equals("no-exp")) builder.expiration(Date.from(NOW.plusSeconds(60)));
        if (!variant.equals("unsigned")) {
            String key = variant.equals("wrong-key") ? KEY.replace("Fixture", "Another") : KEY;
            if (variant.equals("hs512")) builder.signWith(Jwts.SIG.HS512.key().build(), Jwts.SIG.HS512);
            else builder.signWith(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"), Jwts.SIG.HS256);
        }
        String token = builder.compact();
        assertThatThrownBy(() -> service(KEY, "test", 60_000, CLOCK).obtenerIdUsuario(token)).isInstanceOf(JwtException.class);
    }

    @Test void developmentKeysAreEphemeralAndNotShared() {
        var user = new Usuario(); user.setIdUsuario(1);
        var first = service("", "development", 60_000, CLOCK);
        var second = service("", "development", 60_000, CLOCK);
        assertThatThrownBy(() -> second.obtenerIdUsuario(first.generarToken(user))).isInstanceOf(JwtException.class);
    }
}
