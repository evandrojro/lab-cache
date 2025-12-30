CREATE TABLE idempotency_audit (
    id BINARY(16) NOT NULL,
    idem_key VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP(3) NOT NULL,
    finished_at TIMESTAMP(3),
    duration_ms BIGINT,
    result_hash VARCHAR(64),
    error_message VARCHAR(500),
    PRIMARY KEY (id),
    INDEX idx_idempotency_key (idem_key)
);

CREATE TABLE rate_limit_violation (
    id BINARY(16) NOT NULL,
    user_id VARCHAR(60) NOT NULL,
    route VARCHAR(120) NOT NULL,
    limit_value INT NOT NULL,
    window_seconds INT NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    current_count INT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_rl_user_route (user_id, route)
);
