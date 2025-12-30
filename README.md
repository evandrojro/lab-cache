# Redis Guard

Exemplo simples de "Redis Guard" com idempotência e rate limit usando Spring Boot 3, MySQL e Redis.

## O que vem pronto
- API REST com dois endpoints:
  - `POST /idem/execute` usa Redis para idempotência (locks + cache) e audita no MySQL.
  - `GET /rl/check` aplica rate limit de janela fixa em Redis e registra violações no MySQL.
- Flyway cria as tabelas `idempotency_audit` e `rate_limit_violation`.
- docker-compose sobe MySQL 8.0 e Redis 7.2 já prontos para o perfil `local`.
- Testes com Testcontainers garantindo concorrência na idempotência e registro de violações de rate limit.

## Subindo os serviços de apoio
```bash
docker-compose up -d
```

## Rodando a aplicação (perfil `local`, Java 17)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Exemplos rápidos de uso
### Idempotência
```bash
curl -X POST http://localhost:8080/idem/execute \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: chave-123" \
  -d '{"payload":"teste","simulateMs":300}'
```

### Rate limit
```bash
curl "http://localhost:8080/rl/check?userId=user-1&route=/demo&limit=3&windowSeconds=10"
```

## Testes automatizados
```bash
mvn test
```

## Estrutura rápida
- `src/main/java/com/example/redisguard/api` – controllers e handler global.
- `src/main/java/com/example/redisguard/service` – regras de idempotência e rate limit usando Redis.
- `src/main/java/com/example/redisguard/audit` – entidades e repositórios JPA (auditoria no MySQL).
- `src/main/resources/db/migration` – scripts Flyway.

Tudo roda em UTC e `spring.jpa.open-in-view` já está desabilitado.
