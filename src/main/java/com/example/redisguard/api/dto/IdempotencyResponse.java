package com.example.redisguard.api.dto;

public class IdempotencyResponse {
    private String idempotencyKey;
    private boolean processed;
    private String result;
    private long tookMs;

    public IdempotencyResponse() {
    }

    public IdempotencyResponse(String idempotencyKey, boolean processed, String result, long tookMs) {
        this.idempotencyKey = idempotencyKey;
        this.processed = processed;
        this.result = result;
        this.tookMs = tookMs;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getTookMs() {
        return tookMs;
    }

    public void setTookMs(long tookMs) {
        this.tookMs = tookMs;
    }
}
