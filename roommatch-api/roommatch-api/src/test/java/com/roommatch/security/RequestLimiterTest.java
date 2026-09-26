package com.roommatch.security;

import com.roommatch.exception.RateLimitException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

class RequestLimiterTest {
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-09-13T00:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }
    @Test void quotaExpiresAndAccountsAreIndependent() {
        var clock = new MutableClock(); var limiter = new RequestLimiter(clock);
        limiter.check("a", 1, Duration.ofSeconds(30));
        assertThatThrownBy(() -> limiter.check("a", 1, Duration.ofSeconds(30)))
                .isInstanceOfSatisfying(RateLimitException.class, ex -> assertThat(ex.getRetryAfter()).isEqualTo(30));
        assertThatCode(() -> limiter.check("b", 1, Duration.ofSeconds(30))).doesNotThrowAnyException();
        clock.now = clock.now.plusSeconds(30);
        assertThatCode(() -> limiter.check("a", 1, Duration.ofSeconds(30))).doesNotThrowAnyException();
    }
    @Test void concurrentRequestsCannotExceedQuota() throws Exception {
        var limiter = new RequestLimiter(new MutableClock());
        var pool = Executors.newFixedThreadPool(8); var accepted = new AtomicInteger();
        try {
            var tasks = java.util.stream.IntStream.range(0, 100).<Callable<Void>>mapToObj(i -> () -> {
                try { limiter.check("same", 5, Duration.ofMinutes(1)); accepted.incrementAndGet(); }
                catch (RateLimitException expected) { }
                return null;
            }).toList();
            for (var future : pool.invokeAll(tasks)) future.get();
            assertThat(accepted.get()).isEqualTo(5);
        } finally { pool.shutdownNow(); }
    }
    @Test void capacityIsBoundedWithoutEvictingExistingBlocks() {
        var limiter = new RequestLimiter(new MutableClock());
        for (int i=0; i<10_000; i++) limiter.check("key"+i, 1, Duration.ofHours(1));
        assertThatThrownBy(() -> limiter.check("extra", 1, Duration.ofHours(1))).isInstanceOf(RateLimitException.class);
        assertThatThrownBy(() -> limiter.check("key0", 1, Duration.ofHours(1))).isInstanceOf(RateLimitException.class);
    }
}
