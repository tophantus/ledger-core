# LedgerCore Copilot Instructions

## Architecture

LedgerCore uses:
- Java 21
- Spring Boot
- CQRS
- Hexagonal Architecture
- Clean Architecture
- Modular Monolith

Dependency direction:

```text
Inbound Adapter
    ↓
Inbound Use Case
    ↓
Application Handler
    ↓
Outbound Port
    ↓
Outbound Adapter
    ↓
Infrastructure
```

Rules:
- Keep command and query responsibilities separated.
- Handlers orchestrate application logic; do not put business logic in adapters.
- Respect module boundaries and dependency direction.
- Do not introduce unnecessary abstractions.
- Do not modify unrelated modules.

## Persistence

- PostgreSQL
- Spring Data JPA / Hibernate
- Liquibase
- UUID for identifiers
- `BigDecimal` for money
- `Instant` for technical timestamps
- `LocalDate` for business dates
- `ddl-auto=validate`
- Every schema change requires a new Liquibase migration.
- Never modify an already executed migration.

## Transactions

- Keep business state changes within the appropriate application transaction boundary.
- Preserve existing locking and idempotency mechanisms.
- Business logic must not depend directly on infrastructure implementations.

## Error Handling

Use:

```text
BusinessException
ErrorCode
GlobalExceptionHandler
```

Do not use arbitrary exceptions for business errors.

## Code Style

- Prefer constructor injection.
- Use records for suitable DTOs.
- Use Lombok where appropriate.
- Keep classes focused and cohesive.
- Follow existing project conventions before introducing new patterns.

## Testing

After making changes:
1. Compile the affected code.
2. Run relevant tests.
3. Fix failures caused by the changes.
4. Review the final diff.
5. Do not change unrelated code.
