# Subscription System Migration

## Overview

This migration transitions the subscription system from API key prefix-based resolution to database-persisted subscriptions, providing better scalability and flexibility.

## What Changed

### Before (API Key-Based)
- Subscription tiers were determined by API key prefixes (`PX001-*` = Professional, `BX001-*` = Basic)
- No persistence of subscription data
- Limited to API key context only

### After (Database-Based)
- Subscriptions are stored in PostgreSQL with full lifecycle tracking
- Support for subscription validity periods (`valid_from`, `valid_until`)
- Status management (ACTIVE, CANCELLED, EXPIRED)
- User-centric subscription management
- Foundation for payment provider integration (Stripe, etc.)

## Architecture

Following hexagonal architecture principles:

### Domain Layer (`domain/`)
- **`Subscription.kt`**: Aggregate root with business logic
- **`SubscriptionId.kt`**: Value object for subscription identity
- **`SubscriptionStatus.kt`**: Enum for subscription states
- **`SubscriptionTier.kt`**: Enum for tier definitions (unchanged)
- **`SubscriptionRepository.kt`**: Port interface for data access
- **`SubscriptionResolver.kt`**: Port interface for tier resolution (unchanged)
- **`SubscriptionException.kt`**: Domain-specific exceptions

### Application Layer
- No application services yet (CQRS commands/handlers can be added later)
- Current usage is read-only via `SubscriptionResolver`

### Infrastructure Layer (`infrastructure/`)

#### Persistence (`infrastructure/persistence/`)
- **`SubscriptionEntity.kt`**: R2DBC entity
- **`SubscriptionR2dbcRepository.kt`**: Spring Data repository interface
- **`SubscriptionStoreR2dbcRepository.kt`**: Repository implementation
- **`SubscriptionMapper.kt`**: Entity ↔ Domain mappers

#### Resolvers
- **`DatabaseSubscriptionResolver.kt`**: Primary resolver (queries database)
- **`ApiKeySubscriptionResolver.kt`**: Legacy resolver (fallback option)

#### Configuration (`infrastructure/config/`)
- **`SubscriptionResolverConfig.kt`**: Manages active resolver selection

## Database Schema

### Table: `subscriptions`

```sql
CREATE TABLE subscriptions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  tier subscription_tier NOT NULL DEFAULT 'FREE',
  status subscription_status NOT NULL DEFAULT 'ACTIVE',
  valid_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  valid_until TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TYPE subscription_tier AS ENUM ('FREE', 'BASIC', 'PROFESSIONAL');
CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED');

-- Ensure only one active subscription per user
CREATE UNIQUE INDEX idx_subscriptions_user_active_unique
ON subscriptions (user_id) WHERE status = 'ACTIVE';
```

### Indexes
- `idx_subscriptions_user_id`: Fast user lookups
- `idx_subscriptions_status`: Status filtering
- `idx_subscriptions_user_status`: Combined user + status queries
- `idx_subscriptions_user_active_unique`: Enforces business rule (one active subscription per user)

## Configuration

### Application Properties

```yaml
# Default: Use database-based resolver
subscription:
  resolver:
    type: database  # Options: database, apikey
```

To use the legacy API key resolver:

```yaml
subscription:
  resolver:
    type: apikey
```

## Usage

### For Existing Code

No changes required! The `SubscriptionResolver` interface remains unchanged.

```kotlin
class SomeService(
    private val subscriptionResolver: SubscriptionResolver
) {
    suspend fun doSomething(userId: UserId) {
        val tier = subscriptionResolver.resolve(userId.toString())
        // tier is SubscriptionTier (FREE, BASIC, or PROFESSIONAL)
    }
}
```

### Creating Subscriptions

```kotlin
// Create a FREE tier subscription (default for new users)
val freeSubscription = Subscription.createFree(userId)
subscriptionRepository.save(freeSubscription)

// Create a paid subscription with validity period
val paidSubscription = Subscription.create(
    userId = userId,
    tier = SubscriptionTier.PROFESSIONAL,
    validFrom = Instant.now(),
    validUntil = Instant.now().plus(30, ChronoUnit.DAYS)
)
subscriptionRepository.save(paidSubscription)
```

### Querying Subscriptions

```kotlin
// Get active subscription for a user
val active = subscriptionRepository.findActiveByUserId(userId)

// Get all subscriptions for a user (history)
val all = subscriptionRepository.findAllByUserId(userId)

// Find by ID
val subscription = subscriptionRepository.findById(subscriptionId)
```

### Managing Subscription Lifecycle

```kotlin
// Cancel a subscription
val cancelled = subscription.cancel()
subscriptionRepository.save(cancelled)

// Expire a subscription
val expired = subscription.expire()
subscriptionRepository.save(expired)

// Check validity
if (subscription.isValid()) {
    // Subscription is active and within validity period
}
```

## Migration Path

### Development/Test Environments

The migration includes a changeset that automatically creates FREE tier subscriptions for all existing users:

```sql
INSERT INTO subscriptions (id, user_id, tier, status, valid_from, created_at)
SELECT
  gen_random_uuid(),
  id,
  'FREE',
  'ACTIVE',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM users
WHERE NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = users.id);
```

This runs automatically in `dev` and `test` contexts.

### Production

For production deployment:

1. **Apply Liquibase migrations**: Run `./gradlew update` to create the `subscriptions` table
2. **Create initial subscriptions**: Run a data migration to create FREE tier subscriptions for existing users
3. **Deploy application**: The new `DatabaseSubscriptionResolver` will take effect
4. **Monitor**: Check logs for successful tier resolution

### Rollback Strategy

If issues arise, you can switch back to the API key resolver without code changes:

```yaml
subscription:
  resolver:
    type: apikey
```

## Testing

### Unit Tests
- **`SubscriptionTest.kt`**: Domain model business logic
- **`DatabaseSubscriptionResolverTest.kt`**: Resolver with mocked repository
- **`ApiKeySubscriptionResolverTest.kt`**: Legacy resolver (existing)

### Integration Tests
- **`SubscriptionStoreR2dbcRepositoryTest.kt`**: Repository with real database (Testcontainers)

Run tests:
```bash
./gradlew test --tests "*subscription*"
```

## Future Enhancements

### Application Layer (CQRS)
Commands for subscription management:
- `CreateSubscriptionCommand` / `CreateSubscriptionCommandHandler`
- `UpgradeSubscriptionCommand` / `UpgradeSubscriptionCommandHandler`
- `CancelSubscriptionCommand` / `CancelSubscriptionCommandHandler`

### Payment Integration
- Stripe webhook handlers for subscription lifecycle events
- Automatic subscription creation/updates on payment events
- Handling of subscription renewals and expirations

### Background Jobs
- Scheduled job to expire subscriptions past their `valid_until` date
- Notification system for expiring subscriptions

### API Endpoints
- REST controllers for subscription management
- User dashboard for viewing subscription history

## Files Created

### Domain
- `subscription/domain/Subscription.kt`
- `subscription/domain/SubscriptionId.kt`
- `subscription/domain/SubscriptionStatus.kt`
- `subscription/domain/SubscriptionRepository.kt`
- `subscription/domain/SubscriptionException.kt`

### Infrastructure
- `subscription/infrastructure/DatabaseSubscriptionResolver.kt`
- `subscription/infrastructure/config/SubscriptionResolverConfig.kt`
- `subscription/infrastructure/persistence/SubscriptionStoreR2dbcRepository.kt`
- `subscription/infrastructure/persistence/entity/SubscriptionEntity.kt`
- `subscription/infrastructure/persistence/repository/SubscriptionR2dbcRepository.kt`
- `subscription/infrastructure/persistence/mapper/SubscriptionMapper.kt`

### Database
- `db/changelog/migrations/05-subscription/005-subscriptions.yaml`

### Tests
- `subscription/domain/SubscriptionTest.kt`
- `subscription/infrastructure/DatabaseSubscriptionResolverTest.kt`
- `subscription/infrastructure/persistence/SubscriptionStoreR2dbcRepositoryTest.kt`

## Support

For questions or issues, refer to:
- Domain documentation in source files
- Test files for usage examples
- Architecture guidelines in `.agents/skills/spring-boot/SKILL.md`

