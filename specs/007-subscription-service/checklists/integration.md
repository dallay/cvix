# Integration & Notifications Checklist

Purpose: Validate the written requirements about notification contracts, outbox, retry, and DLQ behavior.
Created: 2026-01-17
Focus: Transactional Outbox, event schema, at-least-once delivery, retry/backoff, DLQ, consumer dedupe.

## Requirement Completeness
- [ ] CHK001 - Is the notification contract (envelope fields, `captureId`/`eventId`, metadata escaping rules) explicitly specified? [Completeness, Spec §FR-006]
- [ ] CHK002 - Is the asynchronous delivery model and the requirement to decouple delivery from capture ack clearly stated? [Completeness, Spec §Appendix: Transactional Outbox]

## Requirement Clarity
- [ ] CHK003 - Are retry policies, backoff defaults, maximum attempts, and DLQ behavior documented and configurable? [Clarity, Spec §User Story 3]
- [ ] CHK004 - Is the consumer deduplication guidance (use `eventId`/`captureId`) and example strategies documented? [Clarity, Spec §FR-006]

## Acceptance Criteria Quality
- [ ] CHK005 - Are measurable delivery success targets (e.g., 95% delivery after retries) mapped to observability metrics and retry semantics? [Acceptance Criteria, Spec §SC-004]
- [ ] CHK006 - Are observability requirements defined for notification attempts, failures, retry counts, and DLQ sizes? [Acceptance Criteria, Spec §NFR-002]

## Scenario & Edge Case Coverage
- [ ] CHK007 - Are behaviors defined for transient consumer failures, non-retryable 4xx responses, and long-term DLQ inspection/remediation? [Coverage, Spec §User Story 3]
- [ ] CHK008 - Is the expected ordering guarantee (if any) for notifications specified or intentionally left unordered? [Edge Case, Spec §Appendix: Transactional Outbox]

## Traceability & Integration Tests
- [ ] CHK009 - Are integration test expectations defined (e.g., a test handler that receives payload after commit) and referenced in tasks or plan? [Traceability, Spec §User Stories & tasks.md]


---
Generated from: `spec.md`. Traceability: spec references included per item.
