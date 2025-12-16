package com.cvix.waitlist.infrastructure.persistence

import com.cvix.IntegrationTest
import com.cvix.waitlist.infrastructure.persistence.entity.WaitlistEntryEntity
import com.cvix.waitlist.infrastructure.persistence.repository.WaitlistR2dbcRepository
import java.time.Instant
import java.util.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@IntegrationTest
internal class WaitlistStoreR2dbcRepositoryTest {
    @Autowired
    private lateinit var waitlistR2dbcRepository: WaitlistR2dbcRepository

    @BeforeEach
    fun setUp() = runTest { waitlistR2dbcRepository.deleteAll() }

    @AfterEach
    fun tearDown() = runTest { waitlistR2dbcRepository.deleteAll() }

    @Test
    fun `should save and find waitlist entry by email`() = runTest {
        // Arrange
        val entry = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            ipHash = "hashed-ip",
            sourceRaw = "landing-page",
            sourceNormalized = "landing-page",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )

        // Act
        waitlistR2dbcRepository.save(entry)
        val foundEntry = waitlistR2dbcRepository.findByEmail("test@example.com")

        // Assert
        assertNotNull(foundEntry)
        assertEquals(entry.email, foundEntry!!.email)
        assertEquals(entry.ipHash, foundEntry.ipHash)
    }

    @Test
    fun `should check if email exists`() = runTest {
        // Arrange
        val entry = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "exists@example.com",
            ipHash = "hashed-ip",
            sourceRaw = "landing-page",
            sourceNormalized = "landing-page",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )
        waitlistR2dbcRepository.save(entry)

        // Act
        val exists = waitlistR2dbcRepository.existsByEmail("exists@example.com")
        val notExists = waitlistR2dbcRepository.existsByEmail("not-exists@example.com")

        // Assert
        assertTrue(exists)
        assertFalse(notExists)
    }

    @Test
    fun `should find entries by source`() = runTest {
        // Arrange
        val entry1 = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "user1@example.com",
            ipHash = "ip1",
            sourceRaw = "twitter",
            sourceNormalized = "twitter",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )
        val entry2 = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "user2@example.com",
            ipHash = "ip2",
            sourceRaw = "linkedin",
            sourceNormalized = "linkedin",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )
        waitlistR2dbcRepository.save(entry1)
        waitlistR2dbcRepository.save(entry2)

        // Act
        val twitterEntries = waitlistR2dbcRepository.findBySourceRaw("twitter").toList()

        // Assert
        assertEquals(1, twitterEntries.size)
        assertEquals("user1@example.com", twitterEntries[0].email)
    }

    @Test
    fun `should count entries by source`() = runTest {
        // Arrange
        val entry1 = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "user1@example.com",
            ipHash = "ip1",
            sourceRaw = "twitter",
            sourceNormalized = "twitter",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )
        val entry2 = WaitlistEntryEntity(
            id = UUID.randomUUID(),
            email = "user2@example.com",
            ipHash = "ip2",
            sourceRaw = "twitter",
            sourceNormalized = "twitter",
            language = "en",
            metadata = io.r2dbc.postgresql.codec.Json.of("{}"),
            createdAt = Instant.now(),
        )
        waitlistR2dbcRepository.save(entry1)
        waitlistR2dbcRepository.save(entry2)

        // Act
        val count = waitlistR2dbcRepository.countBySourceRaw("twitter")

        // Assert
        assertEquals(2, count)
    }
}
