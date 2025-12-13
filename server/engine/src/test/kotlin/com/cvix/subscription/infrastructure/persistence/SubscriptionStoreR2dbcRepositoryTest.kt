package com.cvix.subscription.infrastructure.persistence

import com.cvix.IntegrationTest
import com.cvix.config.InfrastructureTestContainers
import com.cvix.subscription.domain.Subscription
import com.cvix.subscription.domain.SubscriptionStatus
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for SubscriptionStoreR2dbcRepository.
 *
 * Tests R2DBC reactive database operations with real PostgreSQL via Testcontainers.
 * CoroutineCrudRepository handles transactions internally for single operations.
 *
 * @created 12/11/25
 */
@IntegrationTest
class SubscriptionStoreR2dbcRepositoryTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionStoreR2dbcRepository

    @Autowired
    private lateinit var userRepository: com.cvix.users.infrastructure.persistence.repository.UserR2dbcRepository

    private lateinit var testUserId: UUID

    @BeforeEach
    fun setUp() = runTest {
        testUserId = UUID.randomUUID()
        // Create a test user to satisfy foreign key constraint
        userRepository.insertIgnoreConflict(testUserId, "test-$testUserId@example.com", "Test User")
    }

    @AfterEach
    fun tearDown() = runTest {
        subscriptionRepository.deleteAllByUserId(testUserId)
    }

    @Test
    fun `should save and retrieve a subscription`() = runTest {
        // Arrange
        val subscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.BASIC,
            validFrom = Instant.now(),
            validUntil = null,
        )

        // Act
        subscriptionRepository.save(subscription)
        val retrieved = subscriptionRepository.findById(subscription.id)

        // Assert
        retrieved.shouldNotBeNull()
        retrieved.id shouldBe subscription.id
        retrieved.userId shouldBe subscription.userId
        retrieved.tier shouldBe subscription.tier
        retrieved.status shouldBe subscription.status
    }

    @Test
    fun `should find active subscription by user ID`() = runTest {
        // Arrange
        val subscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.PROFESSIONAL,
            validFrom = Instant.now(),
            validUntil = null,
        )
        subscriptionRepository.save(subscription)

        // Act
        val active = subscriptionRepository.findActiveByUserId(testUserId)

        // Assert
        active.shouldNotBeNull()
        active.userId shouldBe testUserId
        active.tier shouldBe SubscriptionTier.PROFESSIONAL
        active.status shouldBe SubscriptionStatus.ACTIVE
    }

    @Test
    fun `should return null when no active subscription exists`() = runTest {
        // Arrange
        val expiredSubscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.BASIC,
            validFrom = Instant.now().minus(30, ChronoUnit.DAYS),
            validUntil = Instant.now().minus(1, ChronoUnit.DAYS),
        ).expire()
        subscriptionRepository.save(expiredSubscription)

        // Act
        val active = subscriptionRepository.findActiveByUserId(testUserId)

        // Assert
        active.shouldBeNull()
    }

    @Test
    fun `should find all subscriptions for a user`() = runTest {
        // Arrange
        val subscription1 = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.FREE,
            validFrom = Instant.now().minus(60, ChronoUnit.DAYS),
            validUntil = Instant.now().minus(30, ChronoUnit.DAYS),
        ).expire()

        val subscription2 = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.BASIC,
            validFrom = Instant.now(),
            validUntil = null,
        )

        subscriptionRepository.save(subscription1)
        subscriptionRepository.save(subscription2)

        // Act
        val all = subscriptionRepository.findAllByUserId(testUserId)

        // Assert
        all shouldHaveSize 2
        all.any { it.tier == SubscriptionTier.FREE && it.status == SubscriptionStatus.EXPIRED } shouldBe true
        all.any { it.tier == SubscriptionTier.BASIC && it.status == SubscriptionStatus.ACTIVE } shouldBe true
    }

    @Test
    fun `should update an existing subscription`() = runTest {
        // Arrange
        val subscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.BASIC,
            validFrom = Instant.now(),
        )
        subscriptionRepository.save(subscription)

        // Act
        val cancelled = subscription.cancel()
        subscriptionRepository.save(cancelled)
        val retrieved = subscriptionRepository.findById(subscription.id)

        // Assert
        retrieved.shouldNotBeNull()
        retrieved.status shouldBe SubscriptionStatus.CANCELLED
    }

    @Test
    fun `should delete a subscription`() = runTest {
        // Arrange
        val subscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.FREE,
        )
        subscriptionRepository.save(subscription)

        // Act
        subscriptionRepository.delete(subscription.id)
        val retrieved = subscriptionRepository.findById(subscription.id)

        // Assert
        retrieved.shouldBeNull()
    }

    @Test
    fun `should return empty list when user has no subscriptions`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()

        // Act
        val subscriptions = subscriptionRepository.findAllByUserId(userId)

        // Assert
        subscriptions.shouldBeEmpty()
    }

    @Test
    fun `should only return the most recent active subscription`() = runTest {
        // Arrange - Create an expired subscription first
        val oldSubscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.FREE,
            validFrom = Instant.now().minus(90, ChronoUnit.DAYS),
            validUntil = Instant.now().minus(30, ChronoUnit.DAYS),
        ).expire()
        subscriptionRepository.save(oldSubscription)

        // Create a new active subscription
        val newSubscription = Subscription.create(
            userId = testUserId,
            tier = SubscriptionTier.PROFESSIONAL,
            validFrom = Instant.now(),
        )
        subscriptionRepository.save(newSubscription)

        // Act
        val active = subscriptionRepository.findActiveByUserId(testUserId)

        // Assert
        active.shouldNotBeNull()
        active.tier shouldBe SubscriptionTier.PROFESSIONAL
        active.status shouldBe SubscriptionStatus.ACTIVE
    }
}
