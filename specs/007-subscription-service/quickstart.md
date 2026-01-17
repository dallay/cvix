# Quickstart: Subscription Service

## Dependencies

Add the module to your `build.gradle.kts`:

```kotlin
implementation(project(":shared:engagement"))
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
    suspend fun signup(email: String) {
        // Validate input before calling service
        if (!email.contains("@")) {
            throw IllegalArgumentException("Invalid email format")
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
            // Handle duplicate - service is idempotent but throws if conflict mode is on
            log.info("Already subscribed: $email")
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        try {
            // Idempotency: Provider should handle duplicate requests based on subscriptionId
            emailProvider.sendWelcome(
                email = event.email,
                idempotencyKey = event.subscriptionId
            )
        } catch (e: Exception) {
            log.error("Failed to send welcome email for ${event.subscriptionId}", e)
            // Implement DLQ or retry logic here
        }
    }
}
```
