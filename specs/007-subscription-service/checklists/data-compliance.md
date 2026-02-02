# Data & Compliance Checklist

Purpose: Validate requirements for import/export, retention, bulk deletion and compliance endpoints.
Created: 2026-01-17
Focus: Bulk import/export, retention policies, bulk deletion safeguards, auditability.

## Requirement Completeness
- [ ] CHK001 - Are supported bulk import/export formats and schema constraints documented (CSV, JSONL), including field limits and per-request size limits? [Completeness, Spec §FR-015, FR-016]
- [ ] CHK002 - Are bulk deletion safeguards (multi-layer approval, configurable delay, cancellation) and expected audit logs fully specified? [Completeness, Spec §FR-017]

## Requirement Clarity
- [ ] CHK003 - Is the expected response model for bulk operations (per-record success/failure report) defined? [Clarity, Spec §FR-015]
- [ ] CHK004 - Is the behavior for importing records that violate PII policies or retention constraints defined (reject vs quarantine)? [Clarity, Spec §NFR-003, FR-015]

## Acceptance Criteria Quality
- [ ] CHK005 - Are success and failure metrics for bulk operations (throughput, failure rate, retry attempts) specified and measurable? [Acceptance Criteria, Spec §NFR-006, SC-005]
- [ ] CHK006 - Are retention enforcement and purge runbook acceptance criteria defined and measurable (e.g., hard-delete after configured delay)? [Acceptance Criteria, Spec §NFR-003]

## Scenario & Edge Case Coverage
- [ ] CHK007 - Are export and deletion operations audited with per-request traceability for compliance reviews? [Coverage, Spec §Privacy & Legal API Endpoints]
- [ ] CHK008 - Are rollback or compensation expectations for partially-applied bulk deletes documented? [Edge Case, Spec §FR-017]

## Dependencies & Assumptions
- [ ] CHK009 - Are operational assumptions (backup retention, restore runbooks, limits) documented and validated against compliance.maxRetentionDays? [Dependencies, Spec §NFR-009]


---
Generated from: `spec.md`. Traceability: spec references included per item.
