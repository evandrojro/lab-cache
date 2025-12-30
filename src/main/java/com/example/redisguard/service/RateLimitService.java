package com.example.redisguard.service;

import com.example.redisguard.api.dto.RateLimitResponse;
import com.example.redisguard.api.exception.RateLimitExceededException;
import com.example.redisguard.audit.RateLimitViolation;
import com.example.redisguard.audit.RateLimitViolationRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private static final String PREFIX = "rl:";

    private final StringRedisTemplate redisTemplate;
    private final RateLimitViolationRepository repository;

    public RateLimitService(StringRedisTemplate redisTemplate, RateLimitViolationRepository repository) {
        this.redisTemplate = redisTemplate;
        this.repository = repository;
    }

    public RateLimitResponse check(String userId, String route, int limit, int windowSeconds) {
        String key = PREFIX + userId + ":" + route;
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long reset = ttl < 0 ? windowSeconds : ttl;
        if (current != null && current > limit) {
            repository.save(new RateLimitViolation(
                    UUID.randomUUID(),
                    userId,
                    route,
                    limit,
                    windowSeconds,
                    Instant.now(),
                    current.intValue()
            ));
            throw new RateLimitExceededException(current, limit, reset);
        }
        return new RateLimitResponse(true, current == null ? 0 : current, limit, reset);
    }
}
