# Security & Privacy Checklist

Purpose: Validate the written security and privacy requirements for the Subscription Service.
Created: 2026-01-17
Focus: PII handling, retention, audit, authz, encryption, privacy endpoints.

## Requirement Completeness
- [ ] CHK001 - Are PII classification rules and the `pii_enabled` flag behavior documented and scoped? [Completeness, Spec §User Story 2, NFR-003]
- [ ] CHK002 - Are retention defaults, override rules, and maximum allowable retention (`compliance.maxRetentionDays`) specified and linked to configuration validation? [Completeness, Spec §Assumptions, NFR-003]
- [ ] CHK003 - Are privacy/legal endpoints (export/delete) and their authorization/approval requirements explicitly described? [Completeness, Spec §Privacy & Legal API Endpoints]

## Requirement Clarity
- [ ] CHK004 - Is the difference between soft-delete and hard-delete clearly defined with timings (30 days/90 days) and audit expectations? [Clarity, Spec §Key Entities, Privacy Endpoints]
- [ ] CHK005 - Are token TTL behaviors and expiry error schemas explicitly defined (e.g., `TOKEN_EXPIRED`)? [Clarity, Spec §FR-007]

## Requirement Consistency
- [ ] CHK006 - Are PII handling and export rules consistent with deduplication, retention and backup policies? [Consistency, Spec §NFR-003, FR-009]
- [ ] CHK007 - Do authentication/authorization requirements for administrative APIs match the role descriptions and RBAC model? [Consistency, Spec §FR-012, NFR-003]

## Acceptance Criteria Quality
- [ ] CHK008 - Are measurable security acceptance criteria defined (e.g., audit log immutability, encryption standards TLS≥1.2, AES-256-GCM where required)? [Acceptance Criteria, Spec §NFR-003, Security & Privacy]
- [ ] CHK009 - Are rate-limit behaviors and monitoring alerts specified for abuse detection and enforcement? [Acceptance Criteria, Spec §FR-011, NFR-002]

## Scenario & Edge Case Coverage
- [ ] CHK010 - Are requirements for handling legal erasure requests covering bulk deletes, confirmations, and audit trail verification? [Coverage, Spec §Privacy & Legal API Endpoints, FR-017]
- [ ] CHK011 - Are expectations for sanitization vs rejection for malicious metadata inputs defined for each consumer/export path? [Edge Case, Spec §FR-013]

## Traceability & Validation
- [ ] CHK012 - Is configuration validation for security-critical invariants required at startup and documented with failure modes? [Traceability, Spec §NFR-004]


---
Generated from: `spec.md`. Traceability: spec references included per item.
