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
        int modes = 0;
        for (String mode : new String[]{"development", "test", "production"}) {
            if (environment.acceptsProfiles(Profiles.of(mode))) modes++;
        }
        if (modes > 1) throw new IllegalStateException("No combines perfiles development, test y production");
        if (!environment.acceptsProfiles(Profiles.of("development", "test"))) {
            String url = environment.getProperty("spring.datasource.url", "").toLowerCase(Locale.ROOT);
            var properties = java.util.regex.Pattern.compile("(?:^|;)\\s*(encrypt|trustservercertificate)\\s*=\\s*([^;]*)").matcher(url);
            java.util.Map<String, String> tls = new java.util.HashMap<>();
            while (properties.find()) {
                if (tls.putIfAbsent(properties.group(1), properties.group(2).trim()) != null)
                    throw new IllegalStateException("No repitas propiedades TLS en DB_URL");
            }
            if (!"true".equals(tls.get("encrypt")) || !"false".equals(tls.get("trustservercertificate"))) {
                throw new IllegalStateException("Producción requiere SQL Server con encrypt=true y trustServerCertificate=false");
            }
        }
        if (!"none".equalsIgnoreCase(environment.getProperty("server.forward-headers-strategy", "none"))) {
            throw new IllegalStateException("Usa forward-headers-strategy=none; configura TRUSTED_PROXY_IPS para conservar la IP del socket");
        }
    }

    @Bean
    public Clock applicationClock() { return Clock.systemUTC(); }
}
