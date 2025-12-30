package com.example.redisguard.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RateLimitViolationRepository extends JpaRepository<RateLimitViolation, UUID> {
}
