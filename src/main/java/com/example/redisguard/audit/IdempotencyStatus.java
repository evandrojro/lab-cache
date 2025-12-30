package com.example.redisguard.audit;

public enum IdempotencyStatus {
    PROCESSING,
    DONE,
    FAILED
}
