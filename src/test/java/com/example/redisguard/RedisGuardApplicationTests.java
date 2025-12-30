package com.example.redisguard;

import com.example.redisguard.api.dto.IdempotencyRequest;
import com.example.redisguard.api.dto.IdempotencyResponse;
import com.example.redisguard.api.dto.RateLimitResponse;
import com.example.redisguard.audit.IdempotencyAuditRepository;
import com.example.redisguard.audit.IdempotencyStatus;
import com.example.redisguard.audit.RateLimitViolationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false"
})
class RedisGuardApplicationTests {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("redis_guard_test")
            .withReuse(false);

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IdempotencyAuditRepository auditRepository;

    @Autowired
    private RateLimitViolationRepository violationRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> MYSQL.getJdbcUrl() + "&serverTimezone=UTC");
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.data.redis.host", () -> REDIS.getHost());
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Test
    void concurrencyIdempotencyTest() throws Exception {
        String key = "concurrent-key";
        IdempotencyRequest request = new IdempotencyRequest();
        request.setPayload("hello");
        request.setSimulateMs(200);

        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Callable<ResponseEntity<IdempotencyResponse>>> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tasks.add(() -> sendIdem(key, request));
        }
        List<Future<ResponseEntity<IdempotencyResponse>>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        List<ResponseEntity<IdempotencyResponse>> responses = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        long successCount = responses.stream().filter(r -> r.getStatusCode().is2xxSuccessful()).count();
        long conflictCount = responses.stream().filter(r -> r.getStatusCode() == HttpStatus.CONFLICT).count();
        long processedTrue = responses.stream()
                .filter(r -> r.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .filter(Objects::nonNull)
                .filter(IdempotencyResponse::isProcessed)
                .count();

        Assertions.assertEquals(1, processedTrue);
        Assertions.assertTrue(successCount >= 1);
        Assertions.assertTrue(conflictCount >= 0);

        long doneAudits = auditRepository.findAll().stream()
                .filter(a -> a.getStatus() == IdempotencyStatus.DONE)
                .count();
        Assertions.assertEquals(1, doneAudits);
    }

    @Test
    void rateLimitViolationTest() {
        String user = "user-1";
        String route = "/test";
        int limit = 3;
        int window = 5;

        RateLimitResponse lastResponse = null;
        HttpStatus lastStatus = null;
        for (int i = 0; i < 5; i++) {
            ResponseEntity<RateLimitResponse> response = restTemplate.getForEntity(url("/rl/check?userId=" + user + "&route=" + route + "&limit=" + limit + "&windowSeconds=" + window), RateLimitResponse.class);
            lastResponse = response.getBody();
            lastStatus = response.getStatusCode();
        }

        Assertions.assertEquals(HttpStatus.TOO_MANY_REQUESTS, lastStatus);
        Assertions.assertNotNull(lastResponse);
        Assertions.assertFalse(lastResponse.isAllowed());
        Assertions.assertTrue(violationRepository.count() >= 1);
    }

    private ResponseEntity<IdempotencyResponse> sendIdem(String key, IdempotencyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Idempotency-Key", key);
        HttpEntity<IdempotencyRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url("/idem/execute"), HttpMethod.POST, entity, IdempotencyResponse.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
