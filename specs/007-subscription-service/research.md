# Phase 0: Research & Analysis

## Current Implementation Analysis (`com.cvix.waitlist`)

The existing `waitlist` module implements a basic sign-up flow but lacks the robustness and
genericity required by the new specification.

| Feature           | Current `Waitlist`                        | Required `Subscription`             | Gap                                                                           |
|-------------------|-------------------------------------------|-------------------------------------|-------------------------------------------------------------------------------|
| **Entity**        | `WaitlistEntry` (Specific)                | `Subscription` (Generic)            | Rename & Generalized fields                                                   |
| **Source**        | Enum (`WaitlistSource`)                   | String (Free-form)                  | Convert Enum to String, remove hardcoded values                               |
| **Deduplication** | Application-level check (`existsByEmail`) | Database-level (`ON CONFLICT`)      | **Critical**: Race condition in current impl. Need DB constraint.             |
| **Persistence**   | `WaitlistRepository.save()` (R2DBC)       | Atomic Upsert                       | Implement `INSERT ON CONFLICT`                                                |
| **Events**        | In-memory `EventBroadcaster` after save   | Transactional Outbox (Recommended)  | **Reliability**: Current is "Dual Write" (Event lost if crash after DB save). |
| **Confirmation**  | None                                      | Token generation & Confirmation API | New Feature                                                                   |
| **Privacy**       | IP Hashing                                | IP Hashing + GDPR endpoints         | Keep hashing, add Deletion/Export APIs                                        |

## Architecture Decisions

### 1. Domain Model Generalization

- **Rename**: `WaitlistEntry` -> `Subscription`.
- **Source Strategy**: Hybrid Approach.
    - `sourceNormalized`: Enum for known/system sources (e.g., `LANDING_PAGE`, `API`, `MARKETING`). Used for optimized querying and reporting.
    - `sourceRaw`: String for custom/arbitrary source identifiers.
    - **Logic**: Parser checks an internal allowlist; if match found, `sourceNormalized` is set to the Enum value and `sourceRaw` is kept as-is. If no match, `sourceNormalized` is set to `OTHER` and the full string is stored in `sourceRaw`.
- **Metadata**: Retain `Map<String, Object>` with strict validation:
    - **Max keys**: 20
    - **Max value size**: 1 KB (serialized UTF-8 size).
    - **Max total size**: 10 KB (sum of serialized values).
    - **Nesting**: Allowed up to depth 3, max 10 keys per nested object.
    - **Allowed types**: String, Number, Boolean, Nested Object (with constraints).

### 2. Consistency & Idempotency (FR-004)

- **Problem**: Current `existsByEmail` + `save` is not atomic.
- **Solution**: Use `R2DBC`'s native SQL support to execute
  `INSERT ... ON CONFLICT DO UPDATE/NOTHING`.
- **Constraint**: Create a unique index on `(email_normalized, source)` in the database.

### 3. Reliability & Outbox (FR-006)

- **Problem**: Dual write risk.
- **Solution**: Adopt **Transactional Outbox**.
    - Create `outbox_events` table.
    - In `SubscriptionService`, save `Subscription` AND `OutboxEvent` in the same `@Transactional` block.
    - Use a separate scheduler (or CDC) to poll `outbox_events` and publish to `EventBus`.

### 4. Module Structure

- **Module**: `shared/subscription` (New module)
- **Package**: `com.cvix.subscription`
- **Structure**:
    - `application`: `SubscriptionService`, `CreateSubscriptionCommand`.
    - `domain`: `Subscription`, `Events`.
    - `infrastructure`: `R2dbcRepository`, `WebController`.

### 5. Migration Strategy

- Implement `shared/subscription` in parallel.
- Create temporary adapter in `server/engine` to bridge `com.cvix.waitlist` to the new module.
- Phase out `waitlist` package after successful data migration.

## Security Considerations

- **IP Hashing**: Uses `HMAC-SHA256` with a rotating secret (pepper) stored in KMS. Hashed IPs are retained for 30 days for abuse detection, then purged or re-hashed with a new key.
- **Rate Limiting**: Implementation uses Redis-backed sliding-window algorithm.
    - **Limits**: 5 attempts/IP/hr, 3 attempts/email/hr.
    - **Protection**: Rejects requests with `429` before reaching persistence layer.
- **Token Generation**: Cryptographically secure tokens (CSPRNG) with minimum 32 bytes entropy. Base64URL encoded.
    - **Lifecycle**: Single-use (consumed on success), 48h TTL, salted-hash stored in DB.
- **Injection Prevention**:
    - JSON depth limits (max 3).
    - Strict schema validation for metadata keys (regex: `^[a-z0-9_]{1,32}$`).
    - Entity expansion protection enabled in JSON parser.

## Errors, Validation & Observability

- **Validation**:
    - Email: Apache Commons Validator. DNS MX record check enabled in PROD (`dnsTimeoutMs = 2000`). Fallback to regex-only if DNS times out.
    - Source: Strict validation for `sourceNormalized` (allowlist), relaxed for `sourceRaw`.
- **Errors**:
    - Standard `ErrorResponse` returned for all non-2xx codes.
    - Retry logic for Outbox: Exponential backoff (start 1s, max 60s, 10 retries). Permanent failures move to DLQ table.
- **Observability**:
    - Micrometer metrics: `subscription.count`, `subscription.latency`, `outbox.lag_seconds`.
    - Audit: All PII access (GET/DELETE) logged with requester ID and purpose.
