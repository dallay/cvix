package com.cvix.waitlist.application.create

import com.cvix.TestConstants
import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.waitlist.domain.WaitlistEntry
import com.cvix.waitlist.domain.WaitlistMetrics
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.domain.WaitlistSecurityConfig
import com.cvix.waitlist.domain.WaitlistSource
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class JoinWaitlistCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WaitlistEntryCreatedEvent>
    private lateinit var repository: WaitlistRepository
    private lateinit var metrics: WaitlistMetrics
    private lateinit var securityConfig: WaitlistSecurityConfig
    private lateinit var waitlistJoiner: WaitlistJoiner
    private lateinit var joinWaitlistCommandHandler: JoinWaitlistCommandHandler
    private val faker = Faker()

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk(relaxed = true, relaxUnitFun = true)
        repository = mockk(relaxed = true, relaxUnitFun = true)
        metrics = mockk(relaxed = true, relaxUnitFun = true)
        securityConfig = mockk(relaxed = true) {
            every { ipHmacSecret } returns TestConstants.TEST_HMAC_SECRET
        }

        val testHasher = com.cvix.common.domain.security.HmacHasher(TestConstants.TEST_HMAC_SECRET)
        waitlistJoiner =
            WaitlistJoiner(repository, eventPublisher, metrics, securityConfig, testHasher)
        joinWaitlistCommandHandler = JoinWaitlistCommandHandler(waitlistJoiner)
    }

    @Test
    fun `should join waitlist and publish event when handle is called`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val source = "landing-hero"
        val ipV4Address = faker.internet().ipV4Address()
        val metadata = mapOf("userAgent" to "Mozilla/5.0")
        val command = JoinWaitlistCommand(
            id = id,
            email = email,
            source = source,
            language = "en",
            ipAddress = ipV4Address,
            metadata = metadata,
        )

        // Mocks & Captors
        val entrySlot = slot<WaitlistEntry>()
        val eventSlot = slot<WaitlistEntryCreatedEvent>()
        coEvery { repository.save(capture(entrySlot)) } answers { entrySlot.captured }
        coEvery { eventPublisher.publish(capture(eventSlot)) } returns Unit

        // When
        joinWaitlistCommandHandler.handle(command)

        // Then: verify interactions occurred and the arguments passed
        coVerify(exactly = 1) { repository.save(any()) }
        coVerify(exactly = 1) { eventPublisher.publish(capture(eventSlot)) }
        // Check the saved entry fields
        val savedEntry = entrySlot.captured
        assertEquals(email, savedEntry.email.value)
        assertEquals(source, savedEntry.sourceRaw)
        // Check the published event fields
        val publishedEvent = eventSlot.captured
        assertEquals(savedEntry.id.id.toString(), publishedEvent.id)
        assertEquals(savedEntry.email.value, publishedEvent.email)
        assertEquals(savedEntry.sourceNormalized.value, publishedEvent.source)
        assertEquals(savedEntry.language.code, publishedEvent.language)
    }

    @Test
    fun `should normalize unknown source and handle successfully`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val unknownSource = "twitter-campaign"
        val ipV4Address = faker.internet().ipV4Address()
        val command = JoinWaitlistCommand(
            id = id,
            email = email,
            source = unknownSource,
            language = "en",
            ipAddress = ipV4Address,
            metadata = null,
        )

        // Mocks & Captors
        val entrySlot = slot<WaitlistEntry>()
        val eventSlot = slot<WaitlistEntryCreatedEvent>()
        coEvery { repository.save(capture(entrySlot)) } answers { entrySlot.captured }
        coEvery { eventPublisher.publish(capture(eventSlot)) } returns Unit

        // When
        joinWaitlistCommandHandler.handle(command)

        // Then - command should be processed successfully despite unknown source
        val savedEntry = entrySlot.captured
        assertEquals(unknownSource, savedEntry.sourceRaw)
        assertEquals(
            WaitlistSource.UNKNOWN,
            savedEntry.sourceNormalized,
        )
        // Verify published event wraps the saved entry
        val publishedEvent = eventSlot.captured
        assertEquals(savedEntry.id.id.toString(), publishedEvent.id)
        assertEquals(savedEntry.email.value, publishedEvent.email)
        assertEquals(savedEntry.sourceNormalized.value, publishedEvent.source)
    }
}
