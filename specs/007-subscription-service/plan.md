# Implementation Plan: Subscription Service

**Branch**: `007-subscription-service` | **Date**: 2026-01-17 | **Spec
**: [specs/007-subscription-service/spec.md](spec.md)
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
**Testing**: JUnit 5, Kotest, Testcontainers, MockK
**Target Platform**: JVM / Linux Container
**Project Type**: Backend Service (Spring Boot Module)
**Performance Goals**: <200ms p95 response time, 1000 captures/sec burst
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

```text
shared/engagement/src/main/kotlin/com/cvix/subscription/
├── application/         # Use Cases / Application Services
├── domain/              # Domain Entities, Interfaces (Ports)
└── infrastructure/      # Adapters (Web, Persistence, Messaging)
    ├── web/             # Controllers
    ├── persistence/     # R2DBC Repositories
    └── config/          # Spring Configuration
```

**Structure Decision**: Create new module `shared/engagement` and refactor `com.cvix.waitlist` -> `com.cvix.subscription` there.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| N/A       |            |                                      |
