package com.roommatch.exception;

public class RateLimitException extends RuntimeException {
    private final long retryAfter;
    public RateLimitException(long retryAfter) {
        super("Has realizado varios intentos. Espera antes de volver a intentarlo");
        this.retryAfter = Math.max(1, retryAfter);
    }
    public long getRetryAfter() { return retryAfter; }
}
