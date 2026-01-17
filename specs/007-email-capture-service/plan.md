# Implementation Plan: Email Capture Service

**Branch**: `007-email-capture-service` | **Date**: 2026-01-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-email-capture-service/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

The Email Capture Service will modularize and generalize the existing waitlist functionality into a reusable service. This service will handle various use cases such as capturing emails for waitlists, newsletters, and metadata-driven user registrations. It will be designed to be easily pluggable into any Spring Boot application.

## Technical Context

**Language/Version**: Kotlin 2.2
**Primary Dependencies**: Spring Boot 3.5, R2DBC, PostgreSQL
**Storage**: PostgreSQL
**Testing**: JUnit 5, Kotest
**Target Platform**: JVM (Java 17+)
**Project Type**: Modular library
**Performance Goals**: Handle 1000 requests/second with <200ms p95 latency
**Constraints**: Configurable deduplication and token TTL, modular design
**Scale/Scope**: Support 10k concurrent users

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Code Quality First**: Ensure adherence to Kotlin conventions, immutability, and static analysis tools.
- **Testing Standards**: Unit tests for all core logic, integration tests for database interactions.
- **Governance**: Follow commit conventions and pre-commit hooks.

## Project Structure

### Documentation (this feature)

```text
/specs/007-email-capture-service/
  spec.md
  plan.md
  research.md
  data-model.md
  contracts/
  quickstart.md
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure: feature modules, UI flows, platform tests]
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation                  | Why Needed         | Simpler Alternative Rejected Because |
| -------------------------- | ------------------ | ------------------------------------ |
| [e.g., 4th project]        | [current need]     | [why 3 projects insufficient]        |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]  |
