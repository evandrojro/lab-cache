package com.example.redisguard.service;

import com.example.redisguard.api.dto.IdempotencyRequest;
import com.example.redisguard.api.dto.IdempotencyResponse;
import com.example.redisguard.api.exception.ProcessingException;
import com.example.redisguard.audit.IdempotencyAudit;
import com.example.redisguard.audit.IdempotencyAuditRepository;
import com.example.redisguard.audit.IdempotencyStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class IdempotencyService {

    private static final String IDEM_PREFIX = "idem:";
    private static final String LOCK_PREFIX = "lock:idem:";
    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);
    private static final Duration DONE_TTL = Duration.ofHours(24);
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redisTemplate,
                              IdempotencyAuditRepository auditRepository,
                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public IdempotencyResponse execute(String idemKey, IdempotencyRequest request) {
        String cacheKey = IDEM_PREFIX + idemKey;
        String lockKey = LOCK_PREFIX + idemKey;
        long start = System.currentTimeMillis();

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return buildResponseFromCache(idemKey, cached, false, start);
        }

        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL);
        if (Boolean.FALSE.equals(acquired)) {
            return waitForProcessing(idemKey, cacheKey, start);
        }

        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return buildResponseFromCache(idemKey, cached, false, start);
            }
            redisTemplate.opsForValue().set(cacheKey, "PROCESSING", PROCESSING_TTL);
            IdempotencyAudit audit = new IdempotencyAudit(UUID.randomUUID(), idemKey, IdempotencyStatus.PROCESSING, Instant.now());
            auditRepository.save(audit);
            try {
                String result = simulateWork(request);
                Instant finished = Instant.now();
                long took = finished.toEpochMilli() - audit.getStartedAt().toEpochMilli();

                audit.setFinishedAt(finished);
                audit.setDurationMs(took);
                audit.setStatus(IdempotencyStatus.DONE);
                audit.setResultHash(sha256(result));
                auditRepository.save(audit);

                cacheResult(cacheKey, result);
                return new IdempotencyResponse(idemKey, true, result, System.currentTimeMillis() - start);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                markFailure(audit, "Interrupted");
                redisTemplate.delete(cacheKey);
                throw new ProcessingException("Interrupted while processing idempotent request");
            } catch (Exception e) {
                markFailure(audit, e.getMessage());
                redisTemplate.delete(cacheKey);
                throw new ProcessingException("Failed processing idempotent request");
            }
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    private IdempotencyResponse waitForProcessing(String idemKey, String cacheKey, long start) {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                return buildResponseFromCache(idemKey, value, false, start);
            }
        }
        throw new ProcessingException("Still processing");
    }

    private IdempotencyResponse buildResponseFromCache(String idemKey, String cached, boolean processed, long start) {
        if ("PROCESSING".equals(cached)) {
            throw new ProcessingException("Still processing");
        }
        if (cached.startsWith("DONE:")) {
            String base64 = cached.substring("DONE:".length());
            try {
                byte[] decoded = Base64.getDecoder().decode(base64);
                Map<String, String> payload = objectMapper.readValue(decoded, Map.class);
                String result = payload.getOrDefault("result", "");
                return new IdempotencyResponse(idemKey, processed, result, System.currentTimeMillis() - start);
            } catch (IllegalArgumentException | JsonProcessingException e) {
                throw new ProcessingException("Corrupted idempotency payload");
            }
        }
        throw new ProcessingException("Unknown idempotency state");
    }

    private void cacheResult(String cacheKey, String result) throws JsonProcessingException {
        Map<String, String> payload = Map.of("result", result);
        String json = objectMapper.writeValueAsString(payload);
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        redisTemplate.opsForValue().set(cacheKey, "DONE:" + encoded, DONE_TTL);
    }

    private String simulateWork(IdempotencyRequest request) throws InterruptedException {
        long sleep = Math.max(0, request.getSimulateMs());
        Thread.sleep(sleep);
        String payload = request.getPayload() == null ? "" : request.getPayload();
        return "processed:" + payload;
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            String current = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        } catch (DataAccessException ignored) {
            // best effort
        }
    }

    protected void markFailure(IdempotencyAudit audit, String error) {
        audit.setFinishedAt(Instant.now());
        audit.setStatus(IdempotencyStatus.FAILED);
        audit.setErrorMessage(error);
        auditRepository.save(audit);
    }

    private String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
