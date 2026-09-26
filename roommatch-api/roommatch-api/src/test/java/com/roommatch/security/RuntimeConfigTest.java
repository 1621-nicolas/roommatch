package com.roommatch.security;

import com.roommatch.config.RuntimeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class RuntimeConfigTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "jdbc:sqlserver://db;encrypt=false;trustServerCertificate=true",
            "jdbc:sqlserver://db;encrypt=true;trustServerCertificate=true"})
    void productionRequiresEncryptionAndCertificateValidation(String url) {
        var env = new MockEnvironment().withProperty("spring.datasource.url", url);
        env.setActiveProfiles("production");
        assertThatThrownBy(() -> new RuntimeConfig(env)).isInstanceOf(IllegalStateException.class);
    }
    @Test void localCompatibilityAndSecureProduction() {
        var env = new MockEnvironment(); env.setActiveProfiles("development");
        assertThatCode(() -> new RuntimeConfig(env)).doesNotThrowAnyException();
        env.setActiveProfiles("production");
        env.setProperty("spring.datasource.url", "jdbc:sqlserver://db;encrypt=true;trustServerCertificate=false");
        assertThatCode(() -> new RuntimeConfig(env)).doesNotThrowAnyException();
    }
    private MockEnvironment environment(String... profiles) {
        var env = new MockEnvironment(); env.setActiveProfiles(profiles);
        env.setProperty("spring.datasource.url", "jdbc:sqlserver://db:1433;databaseName=roommatch;encrypt=true;trustServerCertificate=false");
        return env;
    }
    @Test void secureProductionAndExplicitLocalModesRemainUsable() {
        assertThatCode(() -> new RuntimeConfig(environment("production"))).doesNotThrowAnyException();
        var dev = environment("development"); dev.setProperty("spring.datasource.url", "jdbc:sqlserver://localhost;encrypt=false;trustServerCertificate=true");
        assertThatCode(() -> new RuntimeConfig(dev)).doesNotThrowAnyException();
        assertThatCode(() -> new RuntimeConfig(environment("test"))).doesNotThrowAnyException();
    }
    @Test void productionCannotOptOutByCombiningProfiles() {
        assertThatThrownBy(() -> new RuntimeConfig(environment("production", "development"))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new RuntimeConfig(environment("production", "test"))).isInstanceOf(IllegalStateException.class);
    }
    @Test void duplicateOrMissingTlsFlagsCannotPassTheProductionGate() {
        for (String suffix : new String[]{";encrypt=false", "; encrypt =false", ";trustServerCertificate=true", ";encrypt=true"}) {
            var env = environment("production"); env.setProperty("spring.datasource.url", env.getProperty("spring.datasource.url") + suffix);
            assertThatThrownBy(() -> new RuntimeConfig(env)).isInstanceOf(IllegalStateException.class);
        }
        var missing = environment("production"); missing.setProperty("spring.datasource.url", "jdbc:sqlserver://db;encrypt=true");
        assertThatThrownBy(() -> new RuntimeConfig(missing)).isInstanceOf(IllegalStateException.class);
    }
    @Test void untrustedForwardedHeadersCannotReplaceTheSocketPeer() {
        var env = environment("test"); env.setProperty("server.forward-headers-strategy", "framework");
        assertThatThrownBy(() -> new RuntimeConfig(env)).isInstanceOf(IllegalStateException.class);
    }
}
