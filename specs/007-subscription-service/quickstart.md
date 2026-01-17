# Quickstart: Subscription Service

## Dependencies

Add the module to your `build.gradle.kts`:

```kotlin
implementation(project(":shared:subscription"))
implementation("commons-validator:commons-validator:1.8.0")
```

## Configuration

Configure `application.yml`:

```yaml
subscription:
    security:
        # Use env var in production: export SUBSCRIPTION_HMAC_SECRET="<random-base64>"
        ip-hmac-secret: ${SUBSCRIPTION_HMAC_SECRET:dev-secret-do-not-use-in-prod}
    deduplication:
        strategy: "EMAIL_SOURCE" # or "EMAIL_ONLY"
    cleanup:
        retention-days: 730 # 2 years
```

### Privacy & Compliance

**`retention-days`**: This setting controls the maximum lifespan of subscription data.
- **Lawful Basis**: Ensure you have documented the lawful basis (e.g., Consent, Legitimate Interest) for retaining data for this period.
- **Right to Erasure**: The service implements deletion endpoints that override this retention policy.
- **Jurisdiction**: Verify that 730 days complies with local regulations in your operating regions.

## Usage

### 1. Capture an Email

```kotlin
@Service
class MyService(private val subscriptionService: SubscriptionService) {
    private val emailValidator = EmailValidator.getInstance()

    suspend fun signup(email: String) {
        // Proper email validation using Apache Commons
        if (!emailValidator.isValid(email)) {
            throw IllegalArgumentException("Invalid email format: $email")
        }

        try {
            subscriptionService.create(
                CreateSubscriptionCommand(
                    email = email,
                    source = "landing-page",
                    metadata = mapOf("campaign" to "winter-2026")
                )
            )
        } catch (e: DuplicateSubscriptionException) {
            log.info("Already subscribed: $email")
        } catch (e: RateLimitException) {
            log.warn("Rate limit hit for $email")
            throw e
        } catch (e: ValidationException) {
            log.warn("Validation failed for $email: ${e.message}")
            throw e
        } catch (e: Exception) {
            log.error("Failed to capture subscription", e)
            throw DomainException("Subscription failed", e)
        }
    }
}
```

### 2. Listen for Events

```kotlin
@Component
class WelcomeEmailSender(
    private val emailProvider: EmailProvider
) {
    // Run after commit to ensure data is persisted. Async to avoid blocking.
    @Async
    @Retryable(
        value = [EmailException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        try {
            // Idempotency: Provider uses subscriptionId to avoid duplicate sends
            emailProvider.sendWelcome(
                email = event.email,
                idempotencyKey = event.subscriptionId
            )
        } catch (e: Exception) {
            log.error("Failed to send welcome email for ${event.subscriptionId}. Moving to DLQ.", e)
            deadLetterQueue.publish(event, error = e.message)
            throw e // Rethrow to trigger @Retryable if applicable
        }
    }
}
```
