# Feature Specification: Email Capture Service

**Feature Branch**: `007-email-capture-service`
**Created**: 2026-01-17
**Status**: Draft
**Input**: User description: "The contact functionality needs to be refactored into a modular and reusable package/library. This will allow its reuse across multiple applications (as services/containers) without duplication. The waitlist functionality currently located at @server/engine/src/main/kotlin/com/cvix/waitlist/ must be modularized and generalized into a reusable service called email-capture-service. This generalization should allow it to handle various use cases such as capturing emails for waitlists, newsletters, and other metadata-driven user registrations."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Submit email capture (Priority: P1)

As an end user on any consumer-facing surface (landing page, modal, marketing form), I want to submit my email (and optional metadata) so I can join a waitlist or subscribe to a newsletter.

**Why this priority**: This is the core, user-visible value: capturing interest and contact information. Without this the feature is useless.

**Independent Test**: Submit an email via the public capture interface (or call the library API). Verify response indicates success and the entry is persisted and retrievable.

**Acceptance Scenarios**:

1. **Given** a valid email and optional metadata, **When** the user submits the form, **Then** the system acknowledges the submission and creates a unique capture entry.
2. **Given** an invalid email format, **When** the user submits the form, **Then** the system rejects the submission with a clear validation error.
3. **Given** a repeat submission for the same email+context, **When** the user submits again, **Then** the system deduplicates and returns an idempotent success (no duplicate persisted).

---

### User Story 2 - Capture metadata-driven registrations (Priority: P2)

As a product owner, I want to attach free-form metadata (source, campaign, tags, arbitrary key/value) to each captured email so I can segment and route signups for different use cases (waitlist, newsletter, beta-invite lists).

**Why this priority**: Enables reuse across multiple applications and campaigns without creating bespoke waitlist code for each case.

**Independent Test**: Submit captures with different metadata payloads and verify each persisted entry includes that metadata and that entries can be filtered by metadata value.

**Acceptance Scenarios**:

1. **Given** a valid email and metadata {source: "landing-page", campaign: "spring"}, **When** submitted, **Then** the capture entry persists with the metadata attached.
2. **Given** malformed metadata (e.g., field exceeds documented size), **When** submitted, **Then** the system returns a clear validation error describing limits.

---

### User Story 3 - Downstream notification and integration (Priority: P3)

As an integrator, I want the service to notify downstream systems (via events, callbacks or pluggable handlers) when a new capture is created so that other systems (email sender, CRM, analytics) can react.

**Why this priority**: Makes the module useful across apps — they can plug their own integrations without changing core logic.

**Independent Test**: Register a test handler/subscriber; submit a capture; verify the handler receives a notification containing the capture payload.

**Acceptance Scenarios**:

1. **Given** a registered handler, **When** a new capture is created, **Then** the handler receives a notification containing the capture entry details.
2. **Given** a handler fails temporarily, **When** a notification is attempted, **Then** the system retries or records the failure for later inspection (behavior should be configurable).

---

### Edge Cases

- What happens when the same email is submitted concurrently from multiple clients (race conditions)? The system must ensure a single canonical entry is created (idempotency constraints).
- How are extremely large metadata blobs handled? The service must validate and reject payloads that exceed agreed limits.
- How to handle transient downstream failures when notifying integrators? The system should surface failures and allow retry (configurable).
- How are privacy/legal requests handled (delete/export)? This spec assumes existing org policies will be followed; see Assumptions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept email capture submissions through a reusable interface that supports at minimum: email, optional name, source, tags, and an extensible metadata map.
- **FR-002**: The system MUST validate the email format and reject malformed addresses with a clear error message.
- **FR-003**: The system MUST persist capture entries in a durable store and allow retrieval by identifier or by email for auditing and downstream processing.
- **FR-004**: The system MUST enforce idempotency: repeated submissions of the same email within the same context must not create duplicate logical entries.
- **FR-005**: The system MUST allow attaching arbitrary metadata (key/value) to captures within documented size limits and schema constraints.
- **FR-006**: The system MUST provide a pluggable notification mechanism so consuming applications can subscribe to new-capture events (e.g., callbacks, event hooks, or message publish) to trigger downstream workflows.
- **FR-007**: The system SHOULD support optional confirmation workflows (double opt-in) when required by a consuming application or by legal/regulatory constraints. [NEEDS CLARIFICATION: Should double opt-in be enforced by default, provided as an opt-in option, or left to consuming applications?]
- **FR-008**: The system MUST return clear, machine-parseable error information for validation failures, duplicate attempts, and system errors.
- **FR-009**: The system MUST support configurable deduplication rules (e.g., dedupe by email, by email+source) set by the integrator.
- **FR-010**: The system MUST expose a way for integrators to query entries filtered by metadata and source to support segmentation.

### Non-functional Requirements (high level)

- **NFR-001**: The capture operation should complete quickly from the caller's perspective. (See measurable success criteria.)
- **NFR-002**: The system must include observability signals (basic create/failure counters, timestamps) to allow monitoring of capture volume and failure rates.
- **NFR-003**: The system must respect privacy/regulatory constraints; personal data handling should comply with organizational policies (e.g., deletion/export workflows).
- **NFR-004**: The system must be configurable by consuming applications (dedupe rules, confirmation workflow, metadata limits).

### Key Entities *(include if feature involves data)*

- **EmailCaptureEntry**: Represents a captured contact. Key attributes: id, email (normalized), name (optional), source, tags (list), metadata (key/value map), createdAt, status (e.g., pending/confirmed), optional confirmation token and confirmation timestamp.
- **Source**: Logical origin of capture (landing page, marketing campaign, API consumer).
- **CaptureNotification**: Payload sent to downstream handlers when a new capture is created.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% of valid capture submissions complete and return success to the caller in under 3 seconds under normal load.
- **SC-002**: Duplicate submissions are reduced so that duplicate persistent entries are <1% for repeated user submissions (measured during integration testing).
- **SC-003**: 100% of valid submissions are retrievable via query APIs for audit within 10 seconds of creation.
- **SC-004**: Integrators can register at least one downstream handler and receive notifications for new captures in 95% of cases (excluding transient failures).
- **SC-005**: Metadata attached to captures can be used to filter entries in test queries (basic filtering correctness validated by integration tests).
- **SC-006**: The feature is usable as a reusable module by at least two different internal apps without code duplication (verified by code reuse review).

## Assumptions

- Default data retention: captured entries are retained for 24 months unless legal/compliance requires otherwise. (If different retention is required, this is configurable per-deployment.)
- Privacy, consent, and emailing rules (e.g., whether to send marketing emails) are governed by consuming applications and organizational policy. The service provides hooks but does not by itself send marketing emails unless an integrator wires that behavior.
- The service will run in the existing deployment model used by the organization (library module or containerized service); operational details handled by infra teams.
- Email normalization (case-folding, trimming) is performed by the service before deduplication.

## Out of Scope

- Sending marketing emails or managing unsubscribe lists beyond providing hooks/events for integrators. (Integration with email providers is out of scope for the core module.)
- UI components for forms — the module provides back-end interfaces only.
- Detailed deployment/topology decisions (cloud provider, multi-region) are left to infrastructure teams.

## Acceptance Tests (high level)

- End-to-end: Submit valid email + metadata → expect 201/OK → query by email → entry present with metadata.
- Validation: Submit malformed email → expect 400 with validation error and no persisted entry.
- Idempotency: Submit same email twice in quick succession → expect second submission to return idempotent success and no duplicate entry.
- Notification: Register test handler → submit capture → handler receives payload containing expected fields.

## Implementation Notes (for planning only)

- Keep implementation-agnostic: spec focuses on behavior and outcomes. Implementation approach (library vs container) is a planning decision and is not specified here.


**Spec status**: Draft — clarifications requested before finalizing.


Double opt-in (email confirmation) decision: Left to integrators. The core service captures and persists entries and exposes events/hooks; it does NOT enforce or manage double opt-in confirmation workflows. Integrators who require confirmation will implement their own confirmation flows using the service events or query APIs. (This choice minimizes coupling and keeps the core capture service focused on eventing and persistence.)

Email sending responsibility: Email sending is exclusively the responsibility of downstream integrators. The core service will NOT send confirmation or welcome emails. Instead it exposes events/webhooks so integrators can implement customized email sending (templates, providers, retry logic).


---

SUCCESS (spec drafted, clarifications pending)
