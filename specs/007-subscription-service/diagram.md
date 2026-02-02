# Diagrams: Subscription Service

Here you go — a ready-to-paste mermaid diagram you can drop anywhere. No fuss: this covers the main capture, outbox, and confirmation flows. Paste it into docs/PRs/MD and you're done.

## Flowchart (high level)

```mermaid
flowchart TD
    Start([Start])

    User[User / Client]
    API[Public APIs]
    AdminUI[Admin UI]
    AdminAPI["Admin APIs (export, delete)"]

    Validate{Valid request?}
    Create[Create pending subscription]
    SendEmail[Send confirmation email]
    Confirm[Confirm subscription]
    Unsubscribe[Unsubscribe request]

    EndSuccess([Success])
    EndError([Error])

    Start --> User
    User --> API
    API --> Validate

    Validate -- No --> EndError
    Validate -- Yes --> Create --> SendEmail --> EndSuccess

    User -->|Confirm link| Confirm --> EndSuccess
    User -->|Unsubscribe| Unsubscribe --> EndSuccess

    AdminUI --> AdminAPI --> EndSuccess
```

## Sequence (confirmation and token issuance — critical detail)

```mermaid
sequenceDiagram
  participant Integrator
  participant API as SubscriptionController
  participant Service as SubscriptionService
  participant DB
  participant Outbox
  participant User

  Integrator->>API: POST /subscriptions (email + metadata)
  API->>Service: create(command)
  Service->>DB: BEGIN TRANSACTION
  DB->>DB: INSERT ... ON CONFLICT DO NOTHING/UPDATE
  Service->>DB: INSERT outbox_event
  DB->>Service: COMMIT
  Service-->>API: 201 Created / 200 OK
  Service->>Outbox: outbox_event persisted (for publisher)

  Note over Integrator,Service: Issuing confirmation token
  Integrator->>Service: issueConfirmationToken(subscriptionId)
  Service->>DB: store hashedToken + expiresAt (single-use)
  Service-->>Integrator: token issued (raw token returned to integrator)

  User->>Integrator: clicks link / submits token to API
  User->>API: POST /subscriptions/{id}/confirm { token }
  API->>Service: confirm(id, token)
  Service->>DB: retrieve hashedToken, timing-safe-compare, validate expiry
  alt token valid
    Service->>DB: mark subscription CONFIRMED, set confirmation_timestamp
    Service->>DB: insert outbox_event (subscription.confirmed)
    Service-->>API: 200 OK
  else token invalid/expired
    Service-->>API: 400 TOKEN_EXPIRED / INVALID_TOKEN
  end
```

## Quick notes (don't skip these)

- Idempotency: enforce UNIQUE(email_normalized, source) + INSERT ... ON CONFLICT. Do not use exists-before-insert due to race conditions.
- Atomicity: Subscription + Outbox must persist in the same transaction. Period.
- Tokens: generate with a CSPRNG, store only the hash with a salt, compare using a timing-safe compare, single-use token with a default TTL of 48h (configurable).
- OutboxPublisher: scheduler with exponential backoff + jitter, metrics and a DLQ.
- Metadata: validate size/depth before persisting (reject with 400). Defaults: total 10KB, max keys 100 (configurable).
- Security: rate-limiter (Redis sliding-window), IP hashing (HMAC-SHA256 with a pepper in KMS), RBAC on admin endpoints.
- Error schema: follow FR-008 — { code: UPPER_SNAKE_CASE, message, details? }.

---

If you'd like me to commit this or convert it to SVG/PNG and place it in the repo, tell me. Otherwise, it's ready.
