package com.example.redisguard.api.exception;

public class RateLimitExceededException extends RuntimeException {
    private final long current;
    private final int limit;
    private final long resetInSeconds;

    public RateLimitExceededException(long current, int limit, long resetInSeconds) {
        super("Rate limit exceeded");
        this.current = current;
        this.limit = limit;
        this.resetInSeconds = resetInSeconds;
    }

    public long getCurrent() {
        return current;
    }

    public int getLimit() {
        return limit;
    }

    public long getResetInSeconds() {
        return resetInSeconds;
    }
}
