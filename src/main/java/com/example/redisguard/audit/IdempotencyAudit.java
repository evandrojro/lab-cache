package com.example.redisguard.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_audit")
public class IdempotencyAudit {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "idem_key", nullable = false, length = 120)
    private String idemKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "started_at", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant startedAt;

    @Column(name = "finished_at", columnDefinition = "TIMESTAMP(3)")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "result_hash", length = 64)
    private String resultHash;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public IdempotencyAudit() {
    }

    public IdempotencyAudit(UUID id, String idemKey, IdempotencyStatus status, Instant startedAt) {
        this.id = id;
        this.idemKey = idemKey;
        this.status = status;
        this.startedAt = startedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getResultHash() {
        return resultHash;
    }

    public void setResultHash(String resultHash) {
        this.resultHash = resultHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
