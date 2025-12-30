package com.example.redisguard.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rate_limit_violation")
public class RateLimitViolation {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 60)
    private String userId;

    @Column(nullable = false, length = 120)
    private String route;

    @Column(name = "limit_value", nullable = false)
    private Integer limitValue;

    @Column(name = "window_seconds", nullable = false)
    private Integer windowSeconds;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant occurredAt;

    @Column(name = "current_count", nullable = false)
    private Integer currentCount;

    public RateLimitViolation() {
    }

    public RateLimitViolation(UUID id, String userId, String route, Integer limitValue, Integer windowSeconds, Instant occurredAt, Integer currentCount) {
        this.id = id;
        this.userId = userId;
        this.route = route;
        this.limitValue = limitValue;
        this.windowSeconds = windowSeconds;
        this.occurredAt = occurredAt;
        this.currentCount = currentCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    public Integer getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Integer windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }
}
