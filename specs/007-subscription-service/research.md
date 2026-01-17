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
    - `sourceNormalized`: Enum for known/system sources.
    - `sourceRaw`: String for custom/arbitrary sources.
    - Logic: Parser checks allowlist; if match -> set Enum, else set OTHER and populate `sourceRaw`.
- **Metadata**: Retain `Map<String, Any>` with strict limits:
    - Max keys: 20
    - Max value size: 1 KB (String/JSON)
    - Max total size: 10 KB
    - Allowed types: String, Number, Boolean

### 2. Consistency & Idempotency (FR-004)

- **Problem**: Current `existsByEmail` + `save` is not atomic.
- **Solution**: Use `R2DBC`'s native SQL support to execute
  `INSERT ... ON CONFLICT DO UPDATE/NOTHING`.
- **Constraint**: Create a unique index on `(email_normalized, source)` in the database.

### 3. Reliability & Outbox (FR-006)

- **Problem**: Dual write risk.
- **Solution**: Adopt **Transactional Outbox**.
    - Create `outbox_events` table.
    - In `SubscriptionService` (renamed from `WaitlistJoiner`), save `Subscription` AND `OutboxEvent` in the
      same `@Transactional` block.
    - Use a separate scheduler (or CDC) to poll `outbox_events` and publish to `EventBus`.
    - *MVP Alternative*: If Outbox infrastructure is too heavy, use
      `@TransactionalEventListener(phase = AFTER_COMMIT)` for "Best Effort", but Spec "SHOULD"
      suggests robust pattern. Given "Senior Architect" persona, we will design the Outbox Table.

### 4. Module Structure

- **Module**: `shared/engagement` (New module)
- **Package**: `com.cvix.subscription`
- **Structure**:
    - `application`: `SubscriptionService`, `CreateSubscriptionCommand`.
    - `domain`: `Subscription`, `Events`.
    - `infrastructure`: `R2dbcRepository`, `WebController`.

### 5. Migration Strategy

- Since the Spec implies "refactor", we will implement the new structure side-by-side or in-place
  if "waitlist" is the *only* consumer.
- Given "reusable package/library" input, we will treat this as a **New Module** (`shared/engagement`)
  and deprecate `waitlist` package in `server/engine`.
- `WaitlistController` will be replaced by `SubscriptionController`.

## Errors, Validation & Observability

- **Validation**:
    - Email: Regex + DNS check (optional).
    - Rate Limits: 5 req/IP/hr, 3 req/email/hr.
    - Metadata: Enforce limits (size, count) via validator.
- **Errors**:
    - `400`: Validation failed (Code: `VALIDATION_ERROR`).
    - `429`: Rate limit (Code: `RATE_LIMIT_EXCEEDED`).
    - `409`: Conflict (Code: `DUPLICATE_SUBSCRIPTION` - only if configured).
- **Observability**:
    - Metrics: `subscription.created`, `subscription.duplicate`, `outbox.published`, `outbox.lag`.
    - Logs: Audit logs for all PII access (export/delete).

## Open Questions (Resolved)

- **Outbox**: No shared Outbox found. **Decision**: Define a local `outbox` table for this module or
  use `ApplicationEventPublisher` with transactional awareness as a simpler step 1, but plan for
  Outbox table if strict consistency required. *Decision*: Start with `ApplicationEventPublisher` (
  Spring) which binds to transaction commit for local events, then broadcast. This solves "publish
  only if committed", but doesn't solve "publish failed after commit". For NFR-007 (Data
  Durability) + SC-004 (Reliability), we'll specify the **Schema** for Outbox but might use a
  simpler implementation (Job polling table) for now.

## Plan Updates

- **Phase 1**: Define `Subscription` with `source` as String.
- **Phase 1**: Define API Contract (OpenAPI) for generic subscription.

