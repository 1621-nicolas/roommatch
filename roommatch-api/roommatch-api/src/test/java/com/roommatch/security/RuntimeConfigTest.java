package com.roommatch.security;

import com.roommatch.config.RuntimeConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class RuntimeConfigTest {
    @ParameterizedTest
    @ValueSource(strings = {"", "jdbc:sqlserver://db;encrypt=false;trustServerCertificate=true",
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
}
