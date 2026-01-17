# Implementation Plan: Subscription Service

**Branch**: `007-subscription-service` | **Date**: 2026-01-17 | **Spec**: [specs/007-subscription-service/spec.md](spec.md)
**Input**: Feature specification from `/specs/007-subscription-service/spec.md`

## Summary

Refactor and generalize the existing `waitlist` functionality into a reusable
`subscription` module. The new service will support generic email capture (waitlists,
newsletters, etc.) with configurable metadata, deduplication strategies, and downstream
notifications, following Hexagonal Architecture.

## Technical Context

**Language/Version**: Kotlin 2.2
**Primary Dependencies**: Spring Boot 3.5 (WebFlux), R2DBC, PostgreSQL
**Storage**: PostgreSQL (Reactive)
**Testing**:
- **JUnit 5**: Test runner and lifecycle manager.
- **Kotest**: Assertions and property-based testing.
- **Testcontainers**: Integration testing with real dependencies (PostgreSQL).
- **MockK**: Mocking for unit tests.
**Target Platform**: JVM / Linux Container
**Project Type**: Backend Service (Spring Boot Module)
**Performance Goals**: <200ms p95 response time, 1000 captures/sec burst
- **Baseline**: Current waitlist p95 is ~350ms with 100 req/sec burst.
- **Strategy**: Validated via k6 load tests in staging environment (steady state and spike tests). Metrics collected via Micrometer/Prometheus.
**Constraints**: Non-blocking I/O (WebFlux), Hexagonal Architecture, Strict Validation
**Scale/Scope**: Reusable module for multiple apps

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles

- [ ] **Code Quality**: Follows Kotlin conventions, Detekt passes, no `!!`.
- [ ] **Testing**: Pyramid (Unit, Integration, E2E), `should ... when ...` naming.
- [ ] **UX**: Consistent error messages, i18n support.
- [ ] **Performance**: Non-blocking (no `.block()`), optimized DB queries.

### Quality Gates

- [ ] **Pre-Commit**: Linting passes.
- [ ] **Pre-Push**: Tests pass, Detekt passes.
- [ ] **Merge**: CI passes, Code Review.

## Project Structure

### Documentation (this feature)

```text
specs/007-subscription-service/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

**Module Rationale**: `shared/engagement` was chosen over `shared/subscription` to group related engagement features (future: comments, reactions) and avoid narrow "subscription-only" scoping.

**Migration Strategy**:
1. Implement new `SubscriptionService` in `shared/engagement`.
2. Introduce a temporary adapter in `server/engine` that delegates `WaitlistService` calls to `SubscriptionService`.
3. Migrate database data from `waitlist` tables to `subscriptions`.
4. Deprecate and remove `com.cvix.waitlist` package after grace period.

```text
shared/engagement/src/main/kotlin/com/cvix/subscription/
├── application/         # Use Cases / Application Services
│   └── port/            # Port Interfaces (Incoming/Outgoing)
├── domain/              # Domain Entities, Interfaces
│   └── port/            # Domain Ports (Repository interfaces)
└── infrastructure/      # Adapters (Web, Persistence, Messaging)
    ├── web/             # Controllers (Implements Incoming Ports)
    ├── persistence/     # R2DBC Repositories (Implements Outgoing Ports)
    └── config/          # Spring Configuration
```

**Structure Decision**: Create new module `shared/engagement` and refactor `com.cvix.waitlist` -> `com.cvix.subscription` there.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| N/A       |            |                                      |
