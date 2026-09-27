package com.roommatch.matching;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Diagnostic CPU workload, not a latency gate or an end-to-end SQL benchmark. */
class CompatibilityPerformanceIT {
    @Test
    void recordsCostAtProjectScalesWithoutRetainingEveryResult() {
        var calculator = new CompatibilityCalculator();
        var a = new CompatibilityProfile("Lima", new BigDecimal("500"), new BigDecimal("900"), LocalDate.of(2026, 10, 1),
                4, 3, 3, "mañana", "moderadas", "no", "no", "no", "divididos", "tranquila");
        var b = new CompatibilityProfile("LIMA", new BigDecimal("700"), new BigDecimal("1000"), LocalDate.of(2026, 11, 1),
                3, 2, 4, "variable", "bajas", "si", "no", "si", "proporcional", "mixta");
        for (int i = 0; i < 10_000; i++) calculator.calculate(a, b);
        for (int n : new int[]{100, 1_000, 10_000, 100_000}) {
            long start = System.nanoTime();
            BigDecimal checksum = BigDecimal.ZERO;
            for (int i = 0; i < n; i++) checksum = checksum.add(calculator.calculate(a, b).porcentaje());
            System.out.printf("MATCHING_CPU profiles=%d elapsed_ms=%.2f checksum=%s%n", n, (System.nanoTime() - start) / 1e6, checksum);
            assertThat(checksum).isPositive().isLessThanOrEqualTo(BigDecimal.valueOf(n * 100L));
        }
    }
}
