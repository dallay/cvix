package com.cvix.subscription.domain

import com.cvix.UnitTest
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for Subscription domain model.
 *
 * @created 12/11/25
 */
@UnitTest
class SubscriptionTest {

    @Test
    fun `should create a valid subscription with all fields`() {
        // Arrange
        val userId = UUID.randomUUID()
        val now = Instant.now()
        val future = now.plus(30, ChronoUnit.DAYS)

        // Act
        val subscription = Subscription.create(
            userId = userId,
            tier = SubscriptionTier.BASIC,
            validFrom = now,
            validUntil = future,
        )

        // Assert
        subscription.userId shouldBe userId
        subscription.tier shouldBe SubscriptionTier.BASIC
        subscription.status shouldBe SubscriptionStatus.ACTIVE
        subscription.validFrom shouldBe now
        subscription.validUntil shouldBe future
    }

    @Test
    fun `should create a free subscription`() {
        // Arrange
        val userId = UUID.randomUUID()

        // Act
        val subscription = Subscription.createFree(userId)

        // Assert
        subscription.userId shouldBe userId
        subscription.tier shouldBe SubscriptionTier.FREE
        subscription.status shouldBe SubscriptionStatus.ACTIVE
        subscription.validUntil shouldBe null
    }

    @Test
    fun `should validate an active subscription within validity period`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.PROFESSIONAL,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        )

        // Act & Assert
        subscription.isValid(now) shouldBe true
    }

    @Test
    fun `should invalidate subscription before validFrom date`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.plus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        )

        // Act & Assert
        subscription.isValid(now) shouldBe false
    }

    @Test
    fun `should invalidate subscription after validUntil date`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(30, ChronoUnit.DAYS),
            validUntil = now.minus(1, ChronoUnit.DAYS),
        )

        // Act & Assert
        subscription.isValid(now) shouldBe false
    }

    @Test
    fun `should validate perpetual subscription without end date`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.PROFESSIONAL,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = null,
        )

        // Act & Assert
        subscription.isValid(now) shouldBe true
    }

    @Test
    fun `should invalidate cancelled subscription`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        ).cancel()

        // Act & Assert
        subscription.isValid(now) shouldBe false
    }

    @Test
    fun `should invalidate expired subscription`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        ).expire()

        // Act & Assert
        subscription.isValid(now) shouldBe false
    }

    @Test
    fun `should cancel an active subscription`() {
        // Arrange
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
        )

        // Act
        val cancelled = subscription.cancel()

        // Assert
        cancelled.status shouldBe SubscriptionStatus.CANCELLED
        cancelled.id shouldBe subscription.id
        cancelled.userId shouldBe subscription.userId
        cancelled.tier shouldBe subscription.tier
    }

    @Test
    fun `should expire a subscription`() {
        // Arrange
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.PROFESSIONAL,
        )

        // Act
        val expired = subscription.expire()

        // Assert
        expired.status shouldBe SubscriptionStatus.EXPIRED
        expired.id shouldBe subscription.id
    }

    @Test
    fun `should detect expired subscription by status`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(30, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        ).expire()

        // Act & Assert
        subscription.isExpired(now) shouldBe true
    }

    @Test
    fun `should detect expired subscription by date`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(60, ChronoUnit.DAYS),
            validUntil = now.minus(1, ChronoUnit.DAYS),
        )

        // Act & Assert
        subscription.isExpired(now) shouldBe true
    }

    @Test
    fun `should not mark active subscription within period as expired`() {
        // Arrange
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.BASIC,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS),
        )

        // Act & Assert
        subscription.isExpired(now) shouldBe false
    }

    @Test
    fun `should demonstrate semantic distinction between cancelled status and validity period`() {
        // Arrange: A cancelled subscription with a future validity period
        val now = Instant.now()
        val subscription = Subscription.create(
            userId = UUID.randomUUID(),
            tier = SubscriptionTier.PROFESSIONAL,
            validFrom = now.minus(1, ChronoUnit.DAYS),
            validUntil = now.plus(30, ChronoUnit.DAYS), // Still has 30 days of paid access
        ).cancel()

        // Assert: The subscription is cancelled (status) so it's not valid (cannot be used)
        // but the validity period extends into the future (paid-for period)
        subscription.status shouldBe SubscriptionStatus.CANCELLED
        subscription.validUntil.let { it != null && now.isBefore(it) } shouldBe true
        subscription.isValid(now) shouldBe false // Not valid for use, even though paid-for
        subscription.status.isActive() shouldBe false // Cancelled is never active
    }
}
