package com.cvix.subscription.infrastructure

import com.cvix.UnitTest
import com.cvix.subscription.domain.ResolverContext
import com.cvix.subscription.domain.Subscription
import com.cvix.subscription.domain.SubscriptionRepository
import com.cvix.subscription.domain.SubscriptionTier
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for DatabaseSubscriptionResolver.
 *
 * @since 1.0.0
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
        val context = ResolverContext.UserId(userId)

        // Act
        val tier = resolver.resolve(context)

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
            val context = ResolverContext.UserId(userId)

            // Act
            val tier = resolver.resolve(context)

            // Assert
            tier shouldBe SubscriptionTier.PROFESSIONAL
        }

    @Test
    fun `should resolve to FREE tier when no active subscription exists`() = runTest {
        // Arrange
        val userId = UUID.randomUUID()
        coEvery { subscriptionRepository.findActiveByUserId(userId) } returns null
        val context = ResolverContext.UserId(userId)

        // Act
        val tier = resolver.resolve(context)

        // Assert
        tier shouldBe SubscriptionTier.FREE
    }

    @Test
    fun `should resolve to FREE tier when ApiKey context is provided`() = runTest {
        // Given
        val context = ResolverContext.ApiKey("PX001-KEY123")

        // Act
        val tier = resolver.resolve(context)

        // Assert
        tier shouldBe SubscriptionTier.FREE
        coVerify(exactly = 0) { subscriptionRepository.findActiveByUserId(any()) }
    }
}
