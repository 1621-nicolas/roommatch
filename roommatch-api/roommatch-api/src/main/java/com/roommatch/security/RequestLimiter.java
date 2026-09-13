package com.roommatch.security;

import com.roommatch.exception.RateLimitException;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Bounded single-instance limiter. Configure shared enforcement before running multiple replicas. */
@Component
public class RequestLimiter {
    private record Bucket(int count, long expiresAt) {}
    private final Map<String, Bucket> buckets = new HashMap<>();
    private final Clock clock;
    public RequestLimiter(Clock clock) { this.clock = clock; }

    public synchronized void check(String key, int limit, Duration window) {
        long now = clock.millis();
        if (limit < 1 || window.isNegative() || window.isZero()) throw new IllegalArgumentException("Límite inválido");
        Bucket old = buckets.get(key);
        if (old == null || old.expiresAt() <= now) {
            if (buckets.size() >= 10_000) {
                buckets.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
                if (buckets.size() >= 10_000 && old == null) throw new RateLimitException(60);
            }
            buckets.put(key, new Bucket(1, now + window.toMillis()));
        } else if (old.count() >= limit) {
            throw new RateLimitException((old.expiresAt() - now + 999) / 1000);
        } else {
            buckets.put(key, new Bucket(old.count() + 1, old.expiresAt()));
        }
    }

    public synchronized void reset(String key) { buckets.remove(key); }
}
