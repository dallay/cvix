# API Requirements Quality Checklist

Purpose: Unit tests for the written API requirements for the Subscription Service.
Created: 2026-01-17
Focus: API Requirements, acceptance criteria, errors, idempotency, dedupe and query semantics.

## Requirement Completeness
- [ ] CHK001 - Are all public capture endpoints and their minimal request/response fields explicitly listed? [Completeness, Spec §FR-001]
- [ ] CHK002 - Is the confirmation/token issuance API surface documented (endpoints, payloads, TTLs)? [Completeness, Spec §FR-007]
- [ ] CHK003 - Are bulk import/export endpoints and supported formats documented with pagination and error reporting? [Completeness, Spec §FR-015, FR-016]

## Requirement Clarity
- [ ] CHK004 - Is the deduplication response semantics (201 vs 200) precisely defined for each endpoint? [Clarity, Spec §FR-004]
- [ ] CHK005 - Are the error schema fields (`code`, `message`, `details`) and canonical error codes enumerated? [Clarity, Spec §FR-008]
- [ ] CHK006 - Is the public capture endpoint authentication model and allowed unauthenticated mode clearly specified (including required rate-limiting)? [Clarity, Spec §FR-012, FR-011]

## Requirement Consistency
- [ ] CHK007 - Do idempotency and deduplication rules in FR-004 and configurable rules in FR-009 align without contradictions? [Consistency, Spec §FR-004, FR-009]
- [ ] CHK008 - Are field names and types consistent between capture, confirmation, export, and notification payloads? [Consistency, Spec §Key Entities]

## Acceptance Criteria Quality
- [ ] CHK009 - Are measurable success criteria tied to API behaviors (latency targets, indexing windows, duplicate thresholds) referenced for each relevant API? [Acceptance Criteria, Spec §SC-001, SC-001b, SC-003]
- [ ] CHK010 - Are clear machine-parseable error codes defined for validation, dedupe, rate-limit, and token expiry scenarios? [Acceptance Criteria, Spec §FR-008, FR-011]

## Scenario Coverage
- [ ] CHK011 - Are alternate flows covered (invalid email, malformed metadata, duplicate submissions, concurrent submissions)? [Coverage, Spec §User Stories & Edge Cases]
- [ ] CHK012 - Are bulk operation failure semantics, partial successes, and per-record reporting specified? [Coverage, Spec §FR-015]

## Edge Case Coverage
- [ ] CHK013 - Are behavior and acceptance specified for read-after-write visibility vs indexing delays? [Edge Case, Spec §SC-001 note]
- [ ] CHK014 - Is the expected behavior when `do_not_contact` is present on resubmission precisely specified? [Edge Case, Spec §Privacy Endpoints]

## Dependencies & Assumptions
- [ ] CHK015 - Is any required dependency (auth provider, rate-limiter, DB uniqueness constraints) documented and traced to plan/tasks? [Dependencies, Spec §NFR-004]


---
Generated from: `spec.md` (Subscription Service). Traceability: spec references included per item.
