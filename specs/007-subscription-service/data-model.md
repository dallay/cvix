# Data Model: Subscription Service

## Entity: `Subscription`

Represents a captured contact/email.

| Field                     | Type                  | Required | Description             | Constraints                          |
|---------------------------|-----------------------|----------|-------------------------|--------------------------------------|
| `id`                      | `UUID`                | Yes      | Unique ID               | Primary Key                          |
| `email`                   | `String`              | Yes      | Validated Email         | Normalized (NFC, trimmed)            |
| `source`                  | `String`              | Yes      | Capture Source          | Normalized (lowercase), Max 64 chars |
| `source_raw`              | `String`              | Yes      | Original Source         | Max 255 chars                        |
| `status`                  | `SubscriptionStatus`  | Yes      | `PENDING`, `CONFIRMED`, `EXPIRED`  | Default: `PENDING`                   |
| `metadata`                | `JSONB`               | No       | Arbitrary KV pairs      | Max 10KB total                       |
| `tags`                    | `Array<String>`       | No       | Classification tags     | Max 20 tags                          |
| `ip_hash`                 | `String`              | No       | SHA-256 of IP           | Hex string (64 chars)                |
| `confirmation_token`      | `String`              | No       | Token for double-opt-in | Max 128 chars, Indexed               |
| `confirmation_expires_at` | `Instant`             | No       | Token expiration        |                                      |
| `created_at`              | `Instant`             | Yes      | Creation time           |                                      |
| `updated_at`              | `Instant`             | Yes      | Update time             |                                      |
| `do_not_contact`          | `Boolean`             | Yes      | User opt-out            | Default: `false`                     |

### Database Schema (PostgreSQL)

```sql
CREATE TYPE subscription_status AS ENUM ('PENDING', 'CONFIRMED', 'EXPIRED');

CREATE TABLE subscriptions
(
    id                      UUID PRIMARY KEY,
    email                   VARCHAR(254)             NOT NULL,
    source                  VARCHAR(64)              NOT NULL,
    source_raw              VARCHAR(255)             NOT NULL,
    status                  subscription_status      NOT NULL DEFAULT 'PENDING',
    metadata                JSONB,
    tags                    TEXT[],
    ip_hash                 CHAR(64),
    confirmation_token      VARCHAR(128),
    confirmation_expires_at TIMESTAMP WITH TIME ZONE,
    do_not_contact          BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Uniqueness: Email + Source (FR-004, FR-009 default)
    CONSTRAINT uq_email_source UNIQUE (email, source),
    -- Constraints
    CONSTRAINT chk_metadata_size CHECK (octet_length(metadata::text) <= 10240),
    CONSTRAINT chk_tags_size CHECK (cardinality(tags) <= 20)
);

CREATE INDEX idx_subscription_email ON subscriptions (email);
CREATE INDEX idx_subscription_token ON subscriptions (confirmation_token) WHERE confirmation_token IS NOT NULL;

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

## Entity: `OutboxEvent` (Reliability)

Implements Transactional Outbox pattern for downstream notifications.

| Field          | Type      | Description                                 |
|----------------|-----------|---------------------------------------------|
| `id`           | `UUID`    | Unique Event ID                             |
| `aggregate_id` | `UUID`    | Reference to Subscription ID                |
| `type`         | `String`  | e.g. `SUBSCRIPTION_CREATED`, `SUBSCRIPTION_CONFIRMED` |
| `payload`      | `JSONB`   | Full event data                             |
| `created_at`   | `Instant` |                                             |
| `processed_at` | `Instant` | When published (null if pending)            |

```sql
CREATE TABLE subscription_outbox_events
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID                     NOT NULL,
    type         VARCHAR(64)              NOT NULL,
    payload      JSONB                    NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_outbox_aggregate FOREIGN KEY (aggregate_id) REFERENCES subscriptions(id) ON DELETE RESTRICT
);

-- Index for pending events
CREATE INDEX idx_outbox_processed ON subscription_outbox_events (created_at) WHERE processed_at IS NULL;

-- Index for cleanup optimization
CREATE INDEX idx_outbox_cleanup ON subscription_outbox_events (processed_at) WHERE processed_at IS NOT NULL;
```

### Retention & Cleanup

**Strategy**:
- **Retention Period**: Processed events (`processed_at IS NOT NULL`) are retained for **30 days**.
- **Cleanup Mechanism**: Automated nightly job (Cron) performing batched deletion.
- **Job Logic**:
  Repeatedly execute until no rows remain:
  ```sql
  DELETE FROM subscription_outbox_events
  WHERE id IN (
      SELECT id FROM subscription_outbox_events
      WHERE processed_at < NOW() - INTERVAL '30 days'
      LIMIT 1000
  );
  ```

  Pause between iterations (e.g., 100–500ms) to reduce lock contention and allow concurrent writes. For high-volume systems prefer time-based partitioning and DROP PARTITION for retention or use tools like `pg_repack` to avoid table bloat. Tune batch size and pause based on observed contention/throughput.

  **Rationale for RESTRICT**: `ON DELETE RESTRICT` is used on `fk_outbox_aggregate` to ensure we don't delete a subscription while unprocessed outbox events exist. This prevents ghost events and maintains data integrity during high load.

