package com.cvix.subscription.infrastructure

import com.cvix.UnitTest
import com.cvix.subscription.domain.Subscription
import com.cvix.subscription.domain.SubscriptionRepository
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for DatabaseSubscriptionResolver.
 *
 * @created 12/11/25
 */
@UnitTest
internal class DatabaseSubscriptionResolverTest {

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var resolver: DatabaseSubscriptionResolver

    @BeforeEach
    fun setUp() {
        subscriptionRepository = mockk()
        resolver = DatabaseSubscriptionResolver(subscriptionRepository)
    }

    @Test
    fun `should resolve BASIC tier for user with active BASIC subscription`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()
        val subscription = Subscription.create(
            userId = userId,
            tier = SubscriptionTier.BASIC,
            validFrom = Instant.now(),
        )
        coEvery { subscriptionRepository.findActiveByUserId(userId) } returns subscription

        // Act
        val tier = resolver.resolve(userId.toString())

        // Assert
        tier shouldBe SubscriptionTier.BASIC
        coVerify(exactly = 1) { subscriptionRepository.findActiveByUserId(userId) }
    }

    @Test
    fun `should resolve PROFESSIONAL tier for user with active PROFESSIONAL subscription`() =
        runTest {
            // Arrange
            val userId = UUID.randomUUID()
            val subscription = Subscription.create(
                userId = userId,
                tier = SubscriptionTier.PROFESSIONAL,
                validFrom = Instant.now(),
            )
            coEvery { subscriptionRepository.findActiveByUserId(userId) } returns subscription

            // Act
            val tier = resolver.resolve(userId.toString())

            // Assert
            tier shouldBe SubscriptionTier.PROFESSIONAL
        }

    @Test
    fun `should resolve to FREE tier when no active subscription exists`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()
        coEvery { subscriptionRepository.findActiveByUserId(userId) } returns null

        // Act
        val tier = resolver.resolve(userId.toString())

        // Assert
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should resolve to FREE tier when user ID is blank`() = runTest {
        // Act
        val tier = resolver.resolve("")

        // Assert
        tier shouldBe SubscriptionTier.FREE
        coVerify(exactly = 0) { subscriptionRepository.findActiveByUserId(any()) }
    }

    @Test
    fun `should resolve to FREE tier when user ID is invalid`() = runTest {
        // Act
        val tier = resolver.resolve("invalid-uuid")

        // Assert
        tier shouldBe SubscriptionTier.FREE
        coVerify(exactly = 0) { subscriptionRepository.findActiveByUserId(any()) }
    }

    @Test
    fun `should resolve to FREE tier when repository throws exception`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()
        coEvery { subscriptionRepository.findActiveByUserId(userId) } throws RuntimeException("Database error")

        // Act
        val tier = resolver.resolve(userId.toString())

        // Assert
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should resolve FREE tier for user with FREE subscription`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()
        val subscription = Subscription.create(
            userId = userId,
            tier = SubscriptionTier.FREE,
            validFrom = Instant.now(),
        )
        coEvery { subscriptionRepository.findActiveByUserId(userId) } returns subscription

        // Act
        val tier = resolver.resolve(userId.toString())

        // Assert
        tier shouldBe SubscriptionTier.FREE
    }
}
