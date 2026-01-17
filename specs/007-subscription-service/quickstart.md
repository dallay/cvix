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
        ip-hmac-secret: "change-me-in-production"
    deduplication:
        strategy: "EMAIL_SOURCE" # or "EMAIL_ONLY"
    cleanup:
        retention-days: 730 # 2 years
```

## Usage

### 1. Capture an Email

```kotlin
@Service
class MyService(private val subscriptionService: SubscriptionService) {
    suspend fun signup(email: String) {
        subscriptionService.create(
            CreateSubscriptionCommand(
                email = email,
                source = "landing-page",
                metadata = mapOf("campaign" to "winter-2026")
            )
        )
    }
}
```

### 2. Listen for Events

```kotlin
@Component
class WelcomeEmailSender {
    @EventListener
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        // Send email via provider
        emailProvider.sendWelcome(event.email)
    }
}
```
