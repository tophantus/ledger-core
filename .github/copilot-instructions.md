# LedgerCore — GitHub Copilot Instructions

## 1. Project Overview

LedgerCore is a banking-core backend built with Java and Spring Boot.

Primary architectural principles:
- CQRS where it provides clear value
- Hexagonal Architecture / Ports and Adapters
- Clean Architecture
- Modular monolith
- Explicit domain boundaries
- Strong separation between application, domain, persistence, and infrastructure concerns

Primary stack:
- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Liquibase
- Redis where explicitly required
- RabbitMQ for asynchronous messaging
- JUnit + Mockito for testing

---

## 2. Architecture

Use the following dependency direction:

Inbound Adapter
    ↓
Inbound Use Case
    ↓
Application Handler / Service
    ↓
Outbound Port
    ↓
Outbound Adapter
    ↓
Infrastructure

Rules:
- Inbound adapters translate external requests into application commands/queries.
- Inbound use cases define application capabilities.
- Handlers orchestrate use cases and coordinate domain/application operations.
- Outbound ports define what the application needs from external systems.
- Outbound adapters implement outbound ports.
- Infrastructure details must not leak into application or domain code.
- Business logic must not be placed in controllers, repositories, HTTP clients, message adapters, or other infrastructure adapters.
- Do not introduce abstractions merely for the sake of abstraction.
- Preserve existing architectural boundaries when modifying code.

Never make application/domain code directly depend on:
- RestClient
- RabbitTemplate
- Redis/Redisson clients
- JavaMailSender
- HTTP-specific classes
- broker-specific classes
- infrastructure-specific repository implementations

Use ports when crossing a meaningful architectural boundary.

---

## 3. Module Organization

Organize code by business domain/module rather than by global technical layer.

Typical modules include:
- auth
- user
- otp
- account
- transaction
- ledger
- webhook
- outbox
- notification
- common

Each module should own its domain concepts and persistence model.

Avoid cross-module access to another module's repositories. Prefer a public use case or explicit port when crossing module boundaries.

Do not move code between modules unless the change is required by the requested task.

---

## 4. CQRS Rules

CQRS is not mandatory for every operation.

### Commands

Commands:
- change state
- execute business operations
- enforce business rules
- normally run inside a transaction

Examples:
- create account
- activate account
- transfer money
- deposit money
- withdraw money
- post journal entry
- process webhook delivery

### Queries

Queries:
- are read-only
- must not mutate domain state
- should use query-specific DTOs/projections when useful

Do not add CQRS layers mechanically when they provide no architectural value.

---

## 5. Application Layer

Handlers should orchestrate application behavior.

A handler should generally:
1. validate application-level input
2. load required domain state through appropriate ports/repositories
3. enforce or invoke business rules
4. perform the state transition
5. persist changes
6. create required domain events/outbox records
7. return an application response

Do not put large business workflows into controllers.

Do not put business decisions into persistence adapters.

Keep handlers focused and readable. Extract a domain/application service only when the logic is reusable or complex enough to justify it.

---

## 6. Domain and Banking Rules

LedgerCore is a banking core. Treat monetary correctness and accounting consistency as high-priority invariants.

### Money

- Always use `BigDecimal` for monetary values.
- Never use `double` or `float` for money.
- Validate amount positivity and scale according to the existing project rules.
- Currency must be explicitly represented where relevant.
- Do not silently convert currencies.

### Transaction

A `MoneyTransaction` represents a business transaction.

Typical lifecycle may include:
- PENDING
- processing/state transition defined by the current domain
- COMPLETED
- FAILED
- other statuses already established by the project

Do not invent new transaction statuses without checking the existing domain model and requirements.

Transaction identity and idempotency must be preserved. A business reference that is defined as unique must not be duplicated.

### Journal / Ledger

Keep the conceptual distinction clear:

MoneyTransaction
    ↓
JournalEntry
    ↓
JournalEntryLine
    ↓
Ledger Account

A journal entry represents an accounting event.
Journal entry lines represent debit/credit postings to ledger accounts.

Do not confuse:
- business transaction
- journal entry
- journal entry line
- ledger account
- account balance

Accounting entries must preserve double-entry invariants where the current domain requires them.

Do not create arbitrary ledger account codes inside handlers. Use the existing ledger account configuration/domain service/generator mechanisms.

---

## 7. Account and Balance Rules

Account balances are monetary state and must be updated consistently with the corresponding accounting operation.

Where optimistic locking is already used:
- preserve `@Version`
- handle concurrent update failures through the project's business error mechanism
- do not replace optimistic locking with ad-hoc synchronization

Do not update an account balance without understanding whether the operation also requires:
- a journal entry
- transaction state transition
- outbox/domain event
- reconciliation impact

When modifying balance-related code, inspect the complete existing flow before changing it.

---

## 8. Business Date

LedgerCore distinguishes system timestamps from banking business dates.

Use:
- `Instant` for technical timestamps such as `createdAt`, `updatedAt`, and processing timestamps.
- `businessDate` for the banking/business day to which a transaction or accounting record belongs.

Do not derive business date by simply truncating `Instant` unless the existing business-date service explicitly defines that behavior.

Business Date may affect:
- MoneyTransaction
- JournalEntry
- JournalEntryLine / Ledger Entry
- account/ledger closing
- reconciliation
- daily processing

When implementing business-date functionality:
- inspect the existing business-day/closing model first
- determine whether a transaction belongs to the open business date
- preserve the rule that closed business dates are not modified by normal posting flows
- ensure reconciliation uses the intended business date rather than `createdAt`
- do not silently move records between business dates

If the requested change affects closing/opening of a business day, treat the transition as a consistency boundary and inspect concurrent transaction behavior before implementation.

---

## 9. Reconciliation

Reconciliation must compare the correct records for the same business scope/date.

Do not assume that database creation time defines the reconciliation date.

When changing reconciliation:
1. identify the source of truth
2. identify the business date
3. identify the transaction/journal/ledger population included
4. calculate expected values
5. compare against actual persisted values
6. record or expose discrepancies according to the existing design

Do not modify historical closed business-day data as part of normal current-day processing unless explicitly required.

---

## 10. Persistence

Persistence stack:
- PostgreSQL
- Spring Data JPA
- Hibernate
- Liquibase

Conventions:
- UUID identifiers
- `Instant` timestamps
- `BigDecimal` monetary fields
- JPA entities belong to their owning module
- use `ddl-auto=validate` rather than automatic schema generation
- preserve existing indexes, unique constraints, foreign keys, and locking behavior

Spring Data repositories should be used directly when they already provide the required behavior. Do not create wrapper repositories without a real architectural reason.

---

## 11. Liquibase

Every database schema change requires a new Liquibase migration.

Rules:
- Never modify an already executed/shared migration.
- Add a new changeset for new columns, constraints, indexes, tables, or data migrations.
- Keep migration naming consistent with the existing project.
- When adding a non-null column to an existing table, consider existing rows and deployment order.
- Ensure JPA mappings and Liquibase schema remain consistent.

When changing an entity, always check whether a Liquibase migration is also required.

---

## 12. Transactions

Database transaction boundaries belong at the application/use-case level unless the existing design has a stronger explicit reason otherwise.

Business state changes and the corresponding Outbox record must be persisted in the same database transaction when the Outbox Pattern is used.

Do not publish a RabbitMQ message directly from inside a business transaction and assume the two operations are atomic.

For asynchronous delivery:
- transaction commits database state + outbox record
- publisher reads the outbox
- message is published
- publisher confirmation determines successful publication
- failed publication leaves the outbox record available for retry

---

## 13. Outbox

The Outbox Pattern is used for reliable asynchronous event publication.

Rules:
- Domain/application code creates the outbox record as part of the same DB transaction as the business change.
- RabbitMQ is infrastructure.
- RabbitMQ-specific details belong in adapters/infrastructure.
- Publisher confirms should be used where the existing implementation requires reliable publication.
- Do not mark an outbox record as published before successful broker confirmation.
- Failed messages must remain retryable.

Do not place broker configuration or routing logic in domain/application handlers.

---

## 14. Webhook

Webhook delivery is an asynchronous integration concern.

Typical concepts:
- WebhookEndpoint
- WebhookSubscription
- WebhookDelivery

Delivery state may include:
- PENDING
- PROCESSING
- RETRYING
- DELIVERED
- FAILED

Preserve idempotency and delivery state transitions.

Retry behavior should be controlled by application-level retry policy, not hidden inside the HTTP adapter.

HTTP adapters should handle HTTP communication only.

The adapter must not decide business retry policy or business state transitions.

---

## 15. Authentication and Security

For JWT:
- validate token signature and expiration
- extract the established claims
- populate Spring Security context
- do not perform unnecessary database lookups for every request

Refresh tokens should remain opaque random values where that is the established design, with only secure representations/hashes stored when applicable.

Do not weaken existing authentication or authorization behavior while implementing unrelated features.

---

## 16. Error Handling

Use the project's established error model:

`BusinessException`
`ErrorCode`
`GlobalExceptionHandler`

Business failures should use meaningful domain/application error codes.

Do not throw arbitrary `RuntimeException` for expected business errors.

Before introducing a new error code:
- search for an existing equivalent
- reuse it when appropriate
- add a new code only when the business case is genuinely distinct

---

## 17. DTOs and Mapping

Prefer Java records for immutable request/response DTOs where appropriate.

Keep API DTOs separate from persistence entities.

Do not expose JPA entities directly from controllers unless the existing architecture explicitly permits it.

Mapping belongs at the appropriate application/adapter boundary.

Do not put business rules into DTO mappers.

---

## 18. Adapters

Adapters translate between external technology and application/domain concepts.

Inbound adapters may include:
- REST controllers
- message consumers
- schedulers
- webhook test endpoints

Outbound adapters may include:
- JPA persistence
- RabbitMQ
- Redis
- HTTP clients
- email providers

Adapters must remain thin.

Do not put domain workflows in adapters.

---

## 19. Schedulers

Schedulers are triggers, not business workflows.

A scheduler should:
1. trigger the appropriate use case
2. pass required context
3. let application/domain code perform the actual business operation

Do not place reconciliation, retry, closing, or settlement business logic directly inside a scheduled method.

---

## 20. Testing

When changing code:

1. Inspect existing implementation and tests first.
2. Make the smallest necessary change.
3. Run relevant unit tests.
4. Run integration/context tests when the change affects Spring configuration, persistence, messaging, or infrastructure.
5. Fix compilation errors.
6. Fix tests broken by the intended change.
7. Review the final diff.

Use:
- JUnit
- Mockito
- Spring Boot Test where integration behavior is required

Tests should verify business behavior rather than implementation details.

When concurrency or idempotency is relevant, include tests for those invariants where practical.

---

## 21. Required Workflow for Copilot Agent

Before editing code:

1. Inspect the repository structure.
2. Find the relevant module.
3. Trace the current application flow end-to-end.
4. Inspect related entities, repositories, ports, adapters, migrations, and tests.
5. Identify architectural boundaries.
6. Propose the smallest coherent change.

For complex tasks, especially banking/accounting changes:
- produce a plan first
- identify affected modules
- identify schema changes
- identify transaction boundaries
- identify concurrency/idempotency implications
- identify tests
- wait for approval if the user explicitly requests planning before implementation

After implementation:
1. Run relevant tests.
2. Fix compilation failures.
3. Fix failures caused by the implementation.
4. Review changed files.
5. Check for accidental architectural violations.
6. Check Liquibase migrations when persistence changed.
7. Explain the final changes clearly.

---

## 22. Change Discipline

Always prefer the smallest change that correctly solves the requested problem.

Do not:
- rewrite unrelated modules
- rename unrelated classes
- reformat the whole project
- introduce new frameworks without a requirement
- replace working architecture with a different architecture
- add unnecessary interfaces/services/factories
- change public APIs without need
- modify executed Liquibase migrations
- bypass existing domain rules

Before deleting or replacing an abstraction, verify all usages first.

---

## 23. Existing Project Conventions Have Priority

When these instructions conflict with an already-established project convention, inspect the actual codebase first.

Do not blindly impose a generic architecture pattern.

The goal is to evolve LedgerCore consistently, not to redesign it during every task.

When uncertain:
- inspect existing implementations
- find analogous flows
- follow the closest established pattern
- explain the trade-off before making a large architectural change

---

## 24. Final Verification Checklist

Before considering a task complete, verify:

- [ ] Correct module was changed.
- [ ] CQRS boundary is preserved where applicable.
- [ ] Hexagonal dependency direction is preserved.
- [ ] No business logic was moved into adapters.
- [ ] No infrastructure dependency leaked into application/domain code.
- [ ] Money uses `BigDecimal`.
- [ ] Timestamps use `Instant` where appropriate.
- [ ] Business Date is not confused with technical timestamps.
- [ ] Transaction/idempotency rules are preserved.
- [ ] Account balance invariants are preserved.
- [ ] Journal/ledger invariants are preserved.
- [ ] Required Liquibase migration exists.
- [ ] Transaction boundaries remain correct.
- [ ] Outbox consistency is preserved.
- [ ] Relevant tests pass.
- [ ] No unrelated files were changed.
- [ ] Final diff was reviewed.
