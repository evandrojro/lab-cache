package com.example.redisguard.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyAuditRepository extends JpaRepository<IdempotencyAudit, UUID> {
    Optional<IdempotencyAudit> findFirstByIdemKeyAndStatusOrderByFinishedAtDesc(String idemKey, IdempotencyStatus status);
}
