package com.roommatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import java.time.Clock;
import java.util.Locale;

@Configuration
public class RuntimeConfig {
    public RuntimeConfig(Environment environment) {
        if (!environment.acceptsProfiles(Profiles.of("development", "test"))) {
            String url = environment.getProperty("spring.datasource.url", "").toLowerCase(Locale.ROOT);
            if (!url.matches(".*;encrypt=true(?:;.*)?$") ||
                    !url.matches(".*;trustservercertificate=false(?:;.*)?$")) {
                throw new IllegalStateException("Producción requiere SQL Server con encrypt=true y trustServerCertificate=false");
            }
        }
    }

    @Bean
    public Clock applicationClock() { return Clock.systemUTC(); }
}
