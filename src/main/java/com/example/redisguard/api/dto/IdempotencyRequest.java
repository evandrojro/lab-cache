package com.example.redisguard.api.dto;

public class IdempotencyRequest {
    private String payload;
    private long simulateMs = 200;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public long getSimulateMs() {
        return simulateMs;
    }

    public void setSimulateMs(long simulateMs) {
        this.simulateMs = simulateMs;
    }
}
