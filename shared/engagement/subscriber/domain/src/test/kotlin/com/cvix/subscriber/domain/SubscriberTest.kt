package com.cvix.subscriber.domain

import com.cvix.UnitTest
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.security.Hasher
import com.cvix.common.infrastructure.security.HmacHasher
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriberTest {
    private val ipRaw = "96.251.32.95"
    private val secret = "bede9afc9f2bfdccdbddfd742eeff0d6b8ad6ea9"
    private val hasher = HmacHasher(secret)

    private val workspaceId = UUID.randomUUID()

    @Test
    fun `create returns subscriber with valid email source and ipHash`() {
        val id = UUID.randomUUID()
        val attributes = Attributes(tags = listOf("tag1"), metadata = mapOf("k" to "v"))
        val subscriber = Subscriber.create(
            id,
            "user@example.com",
            "web",
            Language.ENGLISH,
            ipRaw,
            hasher,
            attributes,
            workspaceId,
        )

        assertNotNull(subscriber.id)
        assertEquals(id, subscriber.id.value)
        assertEquals("user@example.com", subscriber.email.value)
        assertEquals("web", subscriber.source.source)
        assertEquals("web", subscriber.sourceRaw)
        assertNotNull(subscriber.ipHash)
        assertEquals(64, subscriber.ipHash?.value?.length)
        assertEquals(hasher.hash(ipRaw), subscriber.ipHash?.value)
        assertNotNull(subscriber.createdAt)
        assertEquals(workspaceId, subscriber.workspaceId.value)

        // Additional assertions for default/explicit fields
        assertEquals(Language.ENGLISH, subscriber.language)
        assertEquals(SubscriberStatus.PENDING, subscriber.status)
        assertNotNull(subscriber.attributes)
        assertEquals(attributes, subscriber.attributes)
        assertNull(subscriber.confirmationToken)
        assertNull(subscriber.confirmationExpiresAt)
        assertEquals(false, subscriber.doNotContact)
        assertEquals("system", subscriber.createdBy)
        assertNull(subscriber.updatedAt)
        assertNull(subscriber.updatedBy)
    }

    @Test
    fun `create throws when email is invalid`() {
        val id = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) {
            Subscriber.create(
                id,
                "not-an-email",
                "web",
                Language.ENGLISH,
                ipRaw,
                hasher,
                workspaceId = workspaceId,
            )
        }
    }

    @Test
    fun `create throws when source is blank`() {
        val id = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) {
            Subscriber.create(
                id,
                "user@example.com",
                "",
                Language.ENGLISH,
                ipRaw,
                hasher,
                workspaceId = workspaceId,
            )
        }
    }

    @Test
    fun `create throws when ip is not a 64 char hex string`() {
        val id = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) {
            // We pass a hasher that returns invalid hash to trigger validation in Subscriber init
            val invalidHasher = object : Hasher {
                override fun hash(input: String): String = "invalid-hash"
            }
            Subscriber.create(
                id,
                "user@example.com",
                "web",
                Language.ENGLISH,
                ipRaw,
                invalidHasher,
                workspaceId = workspaceId,
            )
        }
    }

    @Test
    fun `updateStatus changes the subscriber status`() {
        val id = UUID.randomUUID()
        val subscriber = Subscriber.create(
            id,
            "user@example.com",
            "web",
            Language.ENGLISH,
            ipRaw,
            hasher,
            workspaceId = workspaceId,
        )
        assertEquals(SubscriberStatus.PENDING, subscriber.status)
        subscriber.updateStatus(SubscriberStatus.CONFIRMED)
        assertEquals(SubscriberStatus.CONFIRMED, subscriber.status)
    }
}
