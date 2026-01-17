# Feature Specification: Subscription Service

**Feature Branch**: `007-subscription-service`
**Created**: 2026-01-17
**Status**: Draft
**Input**: User description: "The contact functionality needs to be refactored into a modular and
reusable package/library. This will allow its reuse across multiple applications (as
services/containers) without duplication. The waitlist functionality currently located at
@server/engine/src/main/kotlin/com/cvix/waitlist/ must be modularized and generalized into a
reusable service called subscription-service. This generalization should allow it to handle various
use cases such as capturing emails for waitlists, newsletters, and other metadata-driven user
registrations."

## Clarifications

### Session 2026-01-17

- Q: What should the default deduplication key be? → A: Option B — dedupe by normalized email +
  source. This means the same normalized email may have multiple entries if submitted from different
  sources; integrators can override this behavior per FR-009.

- Q: What should the confirmation token TTL be? → A: Default 48 hours; configurable by the
  integrator. Tokens older than the configured TTL are invalid and must be rejected by the
  confirmation API.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Submit email capture (Priority: P1)

As an end user on any consumer-facing surface (landing page, modal, marketing form), I want to
submit my email (and optional metadata) so I can join a waitlist or subscribe to a newsletter.

**Why this priority**: This is the core, user-visible value: capturing interest and contact
information. Without this the feature is useless.

**Independent Test**: Submit an email via the public capture interface (or call the library API).
Verify response indicates success and the entry is persisted and retrievable.

**Acceptance Scenarios**:

1. **Given** a valid email and optional metadata, **When** the user submits the form, **Then** the
   system acknowledges the submission and creates a unique capture entry.
2. **Given** an invalid email format, **When** the user submits the form, **Then** the system
   rejects the submission with a clear validation error.
3. **Given** a repeat submission for the same email and context (e.g., source, metadata, tags—see
   FR-009 for configurable rules), **When** the user submits again, **Then** the system deduplicates
   and returns an idempotent success (no duplicate persisted) with `200 OK`.

---

### User Story 2 - Capture metadata-driven registrations (Priority: P2)

As a product owner, I want to attach free-form metadata (source, campaign, tags, arbitrary
key/value) to each captured email so I can segment and route signups for different use cases (
waitlist, newsletter, beta-invite lists).

**Why this priority**: Enables reuse across multiple applications and campaigns without creating
bespoke waitlist code for each case.

**Independent Test**: Submit captures with different metadata payloads and verify each persisted
entry includes that metadata and that entries can be filtered by metadata value.

**Acceptance Scenarios**:

1. **Given** a valid email and metadata {source: "landing-page", campaign: "spring"}, **When**
   submitted, **Then** the capture entry persists with the metadata attached.
2. **Given** malformed metadata (e.g., field exceeds documented size), **When** submitted, **Then**
   the system returns a clear validation error describing limits.
   **Limits**:
   - **Non-PII (Default)**: Total 10 KB, max 100 fields.
   - **PII-Enabled**: Total 1 MB (strict access controls apply).
   - If limits are exceeded, reject with `400 Bad Request`.

---

### User Story 3 - Downstream notification and integration (Priority: P3)

As an integrator, I want the service to notify downstream systems (via events, callbacks or
pluggable handlers) when a new capture is created so that other systems (email sender, CRM,
analytics) can react.

**Why this priority**: Makes the module useful across apps — they can plug their own integrations
without changing core logic.

**Independent Test**: Register a test handler/subscriber; submit a capture; verify the handler
receives a notification containing the capture payload.

**Acceptance Scenarios**:

1. **Given** a registered handler, **When** a new capture is created, **Then** the handler receives
   a notification containing the capture entry details.
2. **Given** a handler fails temporarily, **When** a notification is attempted, **Then** the system
   uses a configurable retry policy and dead-letter behavior. Example configuration keys and
   defaults: `capture.retry.maxAttempts = 3`, `capture.retry.backoff = exponential+jitter` starting
   at 200ms, `capture.retry.async = true` (notifications retried asynchronously),
   `capture.retry.deadLetter.enabled = true`. Retryable failures: transient network errors and 5xx
   responses; terminal failures: 4xx client errors. Retries must be observable (metrics) and
   dead-letter entries must be visible for operator inspection.

---

### Edge Cases

- What happens when the same email is submitted concurrently from multiple clients (race
  conditions)? The system must ensure a single canonical entry is created (idempotency constraints).
- How are extremely large metadata blobs handled? The service must validate and reject payloads that
  exceed agreed limits.
- How to handle transient downstream failures when notifying integrators? The system should surface
  failures and allow retry (configurable).
- How are privacy/legal requests handled (delete/export)? This spec assumes existing org policies
  will be followed; see Assumptions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept email capture submissions through a reusable interface that
  supports at minimum: email, optional name, source, tags, and an extensible metadata map.
- **FR-002**: The system MUST validate the email format and reject malformed addresses with a clear
  error message. Email validation MUST include a deterministic normalization step performed prior to
  validation and deduplication: trim surrounding whitespace, apply Unicode Normalization Form C (
  NFC), and perform case-folding where appropriate (domain must be case-folded; local-part case
  handling follows RFC-specific rules. Deterministic policy: by default the system MUST preserve
  the submitted local-part exactly and NOT case-fold local-parts (Option A). The system MUST only
  case-fold the domain part. Option B (provider-specific local-part case-folding) is allowed only as
  an opt-in behavior and requires explicit integrator acknowledgement of risks; if enabled it must
  be documented in deployment configuration. Normalization (trim + NFC + domain case-folding and
  any configured local-part policy) MUST be applied before uniqueness/deduplication checks. Note:
  RFC 5321 treats local-parts as case-sensitive.
- **FR-003**: The system MUST persist capture entries in a durable store and allow retrieval by
  identifier or by email for auditing and downstream processing.
- **FR-004**: The system MUST enforce idempotency: repeated submissions of the same email within the
  same context must not create duplicate logical entries.

  Implementation guidance: The service SHOULD enforce uniqueness at the durable store level using a
  database constraint (for example, a UNIQUE(email_normalized, source[, context_id]) index). Write
  operations must be performed as an atomic upsert (INSERT ... ON CONFLICT / MERGE / equivalent)
  inside a transaction to let the database enforce uniqueness rather than relying on in-memory
  checks. For heavy contention, implement retry-on-conflict with exponential backoff or use explicit
  row-level locking. Response semantics: the request that creates the record MUST return
  `201 Created`; subsequent idempotent duplicate submissions MUST return `200 OK` with the existing
  resource representation. Configurable `409 Conflict` is NOT supported to simplify client logic.

  Default behavior: unless configured otherwise by the integrator (see FR-009), the service defaults
  to deduplication by the pair `(email_normalized, source)`. This means the same normalized email
  may exist multiple times when submitted from different sources; integrations that require global
  uniqueness should explicitly configure a different dedupe key.
- **FR-005**: The system MUST allow attaching arbitrary metadata (key/value) to captures.
  Implementers
  MUST enforce documented metadata size limits and schema constraints (see "User Story 2: metadata
  validation constraints"). Example defaults: total metadata map max 10 KB, individual metadata
  field max 1 KB, maximum 100 metadata fields per capture. These limits are product-configurable;
  tests MUST reflect configured values when different from the defaults.
- **FR-006**: The system MUST provide a pluggable notification mechanism so consuming applications
  can subscribe to new-capture events (e.g., callbacks, event hooks, or message publish) to trigger
  downstream workflows.

  Implementation note (FR-006): Notifications **MUST** be asynchronous by default and the service
  **MUST** implement a Transactional Outbox pattern to atomically persist captures and enqueue
  notification events. Delivery guarantees should be documented: at-least-once delivery with
  idempotent consumer expectations is recommended; exactly-once may be provided only if underlying
  infrastructure supports it. Ordering is not guaranteed globally unless explicitly requested. The
  default retry and dead-letter behavior described in retry configuration applies to notification
  delivery. Synchronous webhooks are allowed as an alternative but must be opt-in and include
  caveats about latency and coupling.

  Downstream consumer requirements for idempotency: consumers MUST deduplicate notifications using a
  stable identifier (for example, `captureId` or a unique `eventId`) provided in the notification
  envelope. Example consumer strategies include:
    - Persistent deduplication cache (e.g., Redis with TTL) keyed by `eventId` or `captureId`.
    - Database uniqueness constraints on processed `eventId` or `captureId` when writing downstream
      records.
    - Versioned/state-machine-based processing where each event carries a version or sequence to
      allow idempotent transitions.

  Implementations SHOULD include an `eventId` in every notification envelope and document its
  generation scheme. These deduplication approaches satisfy the at-least-once delivery model
  expected when using a Transactional Outbox; consumers MUST be tested for idempotent processing in
  integration tests.

  Notification encoding and safety: notification payloads MUST escape metadata values according to
  the notification format (for example, JSON string escaping, XML entity encoding, HTML entity
  encoding where applicable) and MUST NOT forward raw unescaped input. FR-013 (input validation and
  sanitization) applies to notification payloads; implementers MUST include encoding validation in
  tests and documentation.
- **FR-007**: The system MUST expose explicit hooks and APIs to enable confirmation workflows (
  double opt-in) for integrators that require them. At minimum the service MUST:
    - Offer an issuance API for confirmation tokens and a way to associate a confirmation token with
      a `Subscription` (for example, `issueConfirmationToken(subscriptionId)`).
    - Expose a confirmation API endpoint for integrators to confirm tokens (for example,
      `POST /subscriptions/{id}/confirm?token=...`) and to retrieve confirmation state (for example,
      `GET /subscriptions/{id}` includes `status`, `confirmation_token`, `confirmation_timestamp`).
    - Emit events/hooks for `confirmation_token.issued` and `subscription.confirmed` so integrators can
      implement email delivery and follow-up flows.
      The service itself does NOT automatically send confirmation emails by default; it provides the
      plumbing for integrators to do so. This makes responsibilities explicit and testable.

  **Unconfirmed Lifecycle**:
  - `PENDING`: Initial state.
  - `CONFIRMED`: After valid token confirmation.
  - `EXPIRED`: If not confirmed within TTL. Expired subscriptions are archived/deleted based on retention policy.

  Token expiration: Confirmation tokens MUST expire after a configurable TTL. Default TTL is
  `48 hours` unless the integrator overrides it in deployment configuration. The `POST
    /subscriptions/{id}/confirm` endpoint MUST validate token age and reject expired tokens with a
  clear machine-parseable error (example: `{"code":"TOKEN_EXPIRED","message":"..."}`).
  Implementations SHOULD store `confirmation_expires_at` on the `Subscription` for audit
  and validation purposes and provide a background job or DB constraint to clean/expire old
  tokens as appropriate.
- **FR-008**: The system MUST return clear, machine-parseable error information for validation
  failures, duplicate attempts, and system errors.
  **Error Schema**:
  ```json
  {
    "code": "ERROR_CODE",
    "message": "Human readable description",
    "details": [{"field": "email", "issue": "Invalid format"}]
  }
  ```
- **FR-009**: The system MUST support configurable deduplication rules (e.g., dedupe by email, by
  email+source) set by the integrator.

  **Default**: Dedupe by normalized email + source (`email_normalized, source`). Metadata is NOT part of the key.
  **Merge Semantics**: If a duplicate is detected, the existing record is returned (`200 OK`). Metadata is merged (new keys added, existing keys updated).
- **FR-010**: The system MUST expose a way for integrators to query entries filtered by metadata and
  source to support segmentation.
- **FR-011**: The system MUST enforce configurable rate limiting and throttling policies (per IP,
  API key, or account). Acceptance: limits configurable and tests validate limit enforcement.
  Normative client behavior: when limits are exceeded, the service will reject excess submissions
  and
  return `429 Too Many Requests` with a `Retry-After` header indicating when the client MAY retry.
  Rejected submissions are not enqueued by the service for delayed processing; clients MUST retry if
  they wish to resubmit. Implementers must document server-side limits (burst window, sustained
  rate), any per-tenant overrides, and monitoring/alerting for rate-limit saturation.
- **FR-012**: The system MUST require authentication and role-based authorization for write and
  query APIs. Acceptance: unauthorized requests return `401/403` and authorized roles can perform
  documented operations.
- **FR-013**: The system MUST validate and sanitize all inputs (metadata, tags, names). Acceptance:
  inputs violating schema or containing injection payloads (SQL, NoSQL, XSS, Command, LDAP, XML/XXE, Path Traversal) are rejected with `400` and not
  persisted; safe-encoding is applied when forwarding to downstream systems.
- **FR-014**: The system MUST produce immutable audit logs for create/read/update/delete and
  administrative operations containing who/what/when/why metadata and integration-friendly format.

- **FR-015**: Bulk import: The system SHOULD provide a bulk import API (e.g.,
  `POST /subscriptions/bulk/import`) with acceptance criteria: supported formats (CSV/JSONL),
  chunking/pagination, idempotency/duplicate detection, authentication, and per-batch reporting of
  successes/failures. Rate/size limits and size-per-request constraints must be documented.
- **FR-016**: Bulk export: The system **MUST** provide a bulk export API (e.g.,
  `GET /subscriptions/export?filter=...`) that returns a machine-readable payload (CSV/JSONL) and
  supports pagination, authentication, and audit logging.
- **FR-017**: Bulk deletion for compliance: The system **MUST** provide a bulk deletion API (e.g.,
  `POST /subscriptions/bulk/delete`) with safeguards (confirmation, authorization, dry-run mode, audit
  logging), and document soft-delete vs hard-delete semantics.

### Non-functional Requirements (high level)

- **NFR-001**: The capture operation should complete quickly from the caller's perspective. (See
  measurable success criteria.)
- **NFR-002**: The system must include observability signals (create/failure counters, timings,
  retry and DLQ metrics) to allow monitoring of capture volume and failure rates and to power
  alerts.
- **NFR-003**: Security & Privacy (concrete):
    - (a) Encryption: TLS 1.2+ / TLS 1.3 for transport and AES-256 (or equivalent) for data at rest.
      Keys must be managed via KMS/secret manager.
    - (b) PII minimization: fields classified as PII must be optional unless required; configurable
      redaction/obfuscation rules must be available for persisted and exported payloads.
    - (c) Retention enforcement: configurable retention TTLs, automated deletion/archival jobs, and
      policy-driven purge workflows. **Constraint**: Configured `retention-days` MUST NOT exceed `compliance.maxRetentionDays` (validated on startup).
    - (d) Access control: RBAC defaults to least-privilege and offers integration points for
      SSO/IDP.
    - (e) Audit logging: immutable logs recording `who/what/when/why` for compliance and incident
      response with configurable retention.
- **NFR-004**: The system must be configurable by consuming applications (dedupe rules, confirmation
  workflow, metadata limits). NFR-004 must remain compatible with NFR-003 (
  dedupe/confirmation/metadata handling must honor retention and PII rules).

  Configuration validation and invariants: the configuration loader MUST perform schema-level
  validation and enforce critical invariants before accepting a deployment or runtime reload. The
  loader MUST check and reject invalid configurations with clear error codes and messages. Examples
  of enforced invariants and required behavior:
    - `custom dedupe keys` MUST NOT exclude PII fields from data-deletion scope; config validation
      must fail if a dedupe key would prevent compliant deletion semantics.
    - `retention TTL` configurations MUST NOT exceed the maximum legal retention period defined by
      the organization; attempts to set longer retention MUST be rejected by the loader.
    - `metadata size` configuration MUST enforce an upper bound for PII-containing records (example
      default: 1 MB); setting limits that allow larger PII storage MUST fail validation unless an
      explicit compliance waiver is present.

  Failure behavior: invalid configurations MUST be rejected and the service must refuse to start or
  reload the configuration. Error responses from the loader SHOULD be machine-parseable (example:
  `{"code":"CONFIG_INVALID","field":"metadata.maxSize","message":"exceeds-legal-limit"}`)
  and CI pipelines MUST include acceptance tests validating these checks (for example: set
  `metadata.maxSize` to >1MB for PII records → expect configuration validation error). These
  checks guarantee NFR-004 remains compatible with NFR-003 in all deployments.

- **NFR-005 (Availability)**: Target 99.9% uptime SLA for the capture API (measurable monthly).
- **NFR-006 (Scalability)**: The service should handle a steady-state throughput of X captures/sec
  and bursts of Y captures/sec for short windows (team to fill X/Y based on expected traffic);
  support horizontal scaling with stateless frontends.
- **NFR-007 (Data Durability)**: Persistent store must provide replication and durability
  guarantees (>= 99.99% durability / four nines) and backups with daily
  snapshots and 30-day retention.
- **NFR-008 (Security Posture)**: Must pass org security reviews, use encryption-in-transit/at-rest,
  RBAC, and periodic audits/pen-tests.
- **NFR-009 (Disaster Recovery)**: RTO <= 1 hour, RPO <= 1 hour for core data; documented
  backup/restore runbooks.

### Key Entities *(include if feature involves data)*

- **Subscription**: Represents a captured contact. Key attributes: id, email (normalized),
  name (optional), source, tags (list), metadata (key/value map), createdAt, status (e.g.,
  pending/confirmed), optional `confirmation_token` and `confirmation_timestamp`,
  `confirmation_expires_at` (optional), `do_not_contact` flag, and retention metadata
  (expiry/soft-delete state).
  **Audit Fields**: `createdBy`, `modifiedBy`, `modifiedAt`, `deletedBy`, `deletedAt`, `isDeleted`, `deletionReason`.
- **Source**: Logical origin of capture (landing page, marketing campaign, API consumer).
- **CaptureNotification**: Payload sent to downstream handlers when a new capture is created.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% of valid capture submissions complete and return success to the caller in under 3
  seconds under normal load. This success indicates durable persistence to the primary transactional
  store; downstream indexing/search pipelines may complete asynchronously (see Consistency &
  Persistence) and can take up to 10 seconds before the entry is queryable via indexed queries.

- **SC-002**: Duplicate submissions are reduced so that duplicate persistent entries are <1% for
  repeated user submissions (measured during integration testing).

- **SC-003**: 100% of valid submissions are retrievable via query APIs for audit within 10 seconds
  of creation (factoring asynchronous indexing). If immediate read-after-write is required,
  integrators should query the primary store or wait for an indexing-complete event.
- **SC-004**: Integrators can register at least one downstream handler and receive notifications for
  new captures in 95% of cases (excluding transient failures).
  **Note**: "Transient failures" include network timeouts, 503s, and connection resets. 4xx errors are permanent. SLA is measured after all retries.
- **SC-005**: Metadata attached to captures can be used to filter entries in test queries (basic
  filtering correctness validated by integration tests).
- **SC-006**: The feature is usable as a reusable module by at least two different internal apps
  without code duplication (verified by code reuse review).

## Assumptions

- Default data retention: captured entries are retained for 24 months unless legal/compliance
  requires otherwise. (If different retention is required, this is configurable per-deployment.)
- Privacy, consent, and emailing rules (e.g., whether to send marketing emails) are governed by
  consuming applications and organizational policy. The service provides hooks but does not by
  itself send marketing emails unless an integrator wires that behavior.
- The service will run in the existing deployment model used by the organization (library module or
  containerized service); operational details handled by infra teams.
- Email normalization (case-folding, trimming) is performed by the service before deduplication.

### Consistency & Persistence

- The `under 3 seconds` acknowledgement target in SC-001 refers to successful durable persistence of
  the capture into the primary transactional store (i.e., the write is considered durable). However,
  the system may perform additional asynchronous indexing and enrichment steps (search/index
  pipelines) which can take up to 10 seconds before the capture is queryable via full-text or
  metadata-filtered query APIs. Implementers must document this async pipeline and surface an
  indexing-complete event if read-after-write visibility is required.

Implications: idempotency must rely on durable primary-store constraints, not on index visibility.
Error handling should distinguish between durable-write failures and downstream indexing failures.
Observers should expect eventual queryability within 10 seconds and rely on events/hooks for
stronger guarantees.

## Privacy & Legal API Endpoints (GDPR/CCPA)

The service MUST provide explicit endpoints to support privacy and legal requests. Example endpoints
and behaviors:

- `DELETE /subscriptions/{id}`: delete a capture by id. Requires authentication/authorization. Must
  support soft-delete vs hard-delete semantics (configurable). Success: `204 No Content`.
  Deletion MUST immediately invalidate any pending confirmation tokens associated with the
  `Subscription` (soft- or hard-delete). Any subsequent attempts to confirm using those tokens
  MUST return `404 Not Found` or `410 Gone` and no confirmation action should be performed. Token
  invalidation is a required part of the deletion workflow and MUST be recorded in the audit log
  alongside the deletion event (for example, an audit entry `subscription.deleted` plus
  `token.invalidated`).
- `DELETE /subscriptions?email={email}`: delete all captures for an email. Requires elevated
  authorization and confirmation/dry-run support. Returns `202 Accepted` for async bulk deletes with
  job id; job completion/audit logged.
- `GET /subscriptions/export?email={email}`: export all data for an email. Authenticated request returns
  downloadable machine-readable payload (JSONL/CSV) with all associated metadata, events, and audit
  trails. Success: `200 OK` with `Content-Disposition` attachment; export actions MUST be audited.
- `POST /subscriptions/{id}/do-not-contact` or `PATCH /subscriptions/{id}` with `{ do_not_contact: true }`:
  set `do_not_contact` flag for a capture. Requires authorization and audit logging. Setting this
  flag should surface to downstream handlers/events.

Each endpoint must require appropriate authentication/authorization, return consistent error codes (
`401/403/404`), and be auditable. Bulk operations should document soft-delete vs hard-delete
semantics and retention consequences.

## Out of Scope

- Sending marketing emails or managing unsubscribe lists beyond providing hooks/events for
  integrators. (Integration with email providers is out of scope for the core module.)
- UI components for forms — the module provides back-end interfaces only.
- Detailed deployment/topology decisions (cloud provider, multi-region) are left to infrastructure
  teams.

## Acceptance Tests (high level)

- End-to-end: Submit valid email + metadata → expect 201/OK → query by email → entry present with
  metadata.
- Validation: Submit malformed email → expect 400 with validation error and no persisted entry.
- Idempotency: Submit same email twice in quick succession → expect second submission to return
  idempotent success and no duplicate entry.
- Notification: Register test handler → submit capture → handler receives payload containing
  expected fields.
- **Unconfirmed Lifecycle**: Submit capture → Status PENDING → Wait TTL → Confirm fails (TOKEN_EXPIRED).
- **Configuration Validation**: Start app with retention > max legal limit → Startup Fails.
- **Do Not Contact**: Set do_not_contact → Resubmit → Expect Rejection/Flagging.

### Security & Privacy Acceptance Tests

- Rate limiting: Setup a test client and exceed configured rate limits → expect
  `429 Too Many Requests` and no new entries persisted after limits are hit; metrics and audit logs
  updated.
- Authentication failure: Call protected endpoints without credentials or with invalid credentials →
  expect `401`/`403` and no persisted entry.
- Data deletion: Perform `DELETE /subscriptions/{id}` → expect `204 No Content`, entry marked/removed per
  retention policy, and audit log contains deletion event.
- Data export: Perform `GET /subscriptions/export?email=` → expect `200 OK` with machine-readable payload
  containing all associated data and audit entry for export.
- Malicious input: Submit a payload containing XSS or SQL-injection-like payloads in metadata →
  expect `400 Bad Request` (or sanitized storage) and no stored unsafe content. Downstream
  notifications MUST carry safely-encoded metadata values (for example, JSON-escaped strings for
  JSON envelopes, XML entity-encoded values for XML envelopes). Tests MUST assert that notification
  handlers receive escaped metadata rather than raw unescaped input.

## Implementation Notes (for planning only)

- Keep implementation-agnostic: spec focuses on behavior and outcomes. Implementation approach (
  library vs container) is a planning decision and is not specified here.

**Spec status**: Draft — clarifications requested before finalizing.

Double opt-in (email confirmation) decision: The core service exposes confirmation plumbing (token
issuance APIs, confirmation endpoints, and confirmation-related events/hooks) so integrators can
implement double opt-in workflows. The core service does NOT automatically send confirmation emails;
it provides the tokens/events and stores confirmation state (`confirmation_token`,
`confirmation_timestamp`, `status`) so integrators can implement confirmation flows while preserving
auditability and compliance.

Email sending responsibility: Email sending is exclusively the responsibility of downstream
integrators. The core service will NOT send confirmation or welcome emails. Instead it exposes
events/webhooks so integrators can implement customized email sending (templates, providers, retry
logic).


---

SUCCESS (spec drafted, clarifications pending)
