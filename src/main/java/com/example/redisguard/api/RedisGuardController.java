package com.example.redisguard.api;

import com.example.redisguard.api.dto.IdempotencyRequest;
import com.example.redisguard.api.dto.IdempotencyResponse;
import com.example.redisguard.api.dto.RateLimitResponse;
import com.example.redisguard.service.IdempotencyService;
import com.example.redisguard.service.RateLimitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisGuardController {

    private final IdempotencyService idempotencyService;
    private final RateLimitService rateLimitService;

    public RedisGuardController(IdempotencyService idempotencyService, RateLimitService rateLimitService) {
        this.idempotencyService = idempotencyService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping(path = "/idem/execute", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public IdempotencyResponse execute(@RequestHeader("Idempotency-Key") String idemKey,
                                       @Valid @RequestBody IdempotencyRequest request) {
        return idempotencyService.execute(idemKey, request);
    }

    @GetMapping(path = "/rl/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public RateLimitResponse check(
            @RequestParam("userId") String userId,
            @RequestParam("route") String route,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "windowSeconds", defaultValue = "10") int windowSeconds
    ) {
        return rateLimitService.check(userId, route, limit, windowSeconds);
    }
}
