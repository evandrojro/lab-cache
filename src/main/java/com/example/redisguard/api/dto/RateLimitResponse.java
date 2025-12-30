package com.example.redisguard.api.dto;

public class RateLimitResponse {
    private boolean allowed;
    private long current;
    private int limit;
    private long resetInSeconds;

    public RateLimitResponse() {
    }

    public RateLimitResponse(boolean allowed, long current, int limit, long resetInSeconds) {
        this.allowed = allowed;
        this.current = current;
        this.limit = limit;
        this.resetInSeconds = resetInSeconds;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getResetInSeconds() {
        return resetInSeconds;
    }

    public void setResetInSeconds(long resetInSeconds) {
        this.resetInSeconds = resetInSeconds;
    }
}
