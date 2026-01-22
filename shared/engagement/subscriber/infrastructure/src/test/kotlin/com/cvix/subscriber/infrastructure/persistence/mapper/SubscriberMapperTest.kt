package com.cvix.subscriber.infrastructure.persistence.mapper

import com.cvix.UnitTest
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.ip.IpHash
import com.cvix.subscriber.domain.Attributes
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberId
import com.cvix.subscriber.domain.SubscriberStatus
import com.cvix.subscriber.domain.SubscriptionSource
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper.toDomain
import com.cvix.subscriber.infrastructure.persistence.mapper.SubscriberMapper.toEntity
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriberMapperTest {

    private val workspaceId = UUID.randomUUID()
    private val subscriberId = UUID.randomUUID()
    private val now = Instant.now()

    @Test
    fun `should map domain to entity`() {
        // Arrange
        val domain = Subscriber(
            id = SubscriberId(subscriberId),
            email = Email("test@example.com"),
            source = SubscriptionSource("web"),
            sourceRaw = "web",
            status = SubscriberStatus.ENABLED,
            language = Language.ENGLISH,
            ipHash = IpHash.from("a".repeat(64)),
            attributes = Attributes(tags = listOf("tag1"), metadata = mapOf("key" to "value")),
            workspaceId = WorkspaceId(workspaceId),
            createdAt = now,
            createdBy = "admin",
            updatedAt = now,
            updatedBy = "admin",
        )

        // Act
        val entity = domain.toEntity()

        // Assert
        assertEquals(subscriberId, entity.id)
        assertEquals("test@example.com", entity.email)
        assertEquals("web", entity.source)
        assertEquals("web", entity.sourceRaw)
        assertEquals(SubscriberStatus.ENABLED, entity.status)
        assertEquals("en", entity.language)
        assertEquals("a".repeat(64), entity.ipHash)
        assertEquals(domain.attributes, entity.attributes)
        assertEquals(workspaceId, entity.workspaceId)
        assertEquals(now, entity.createdAt)
        assertEquals("admin", entity.createdBy)
        assertEquals(now, entity.updatedAt)
        assertEquals("admin", entity.updatedBy)
    }

    @Test
    fun `should map entity to domain`() {
        // Arrange
        val entity = com.cvix.subscriber.infrastructure.persistence.entity.SubscriberEntity(
            id = subscriberId,
            email = "test@example.com",
            source = "api",
            sourceRaw = "api",
            status = SubscriberStatus.PENDING,
            language = "es",
            ipHash = "b".repeat(64),
            attributes = Attributes(tags = listOf("news")),
            workspaceId = workspaceId,
            createdAt = now,
            createdBy = "system",
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals(SubscriberId(subscriberId), domain.id)
        assertEquals(Email("test@example.com"), domain.email)
        assertEquals(SubscriptionSource("api"), domain.source)
        assertEquals("api", domain.sourceRaw)
        assertEquals(SubscriberStatus.PENDING, domain.status)
        assertEquals(Language.SPANISH, domain.language)
        assertEquals(IpHash.from("b".repeat(64)), domain.ipHash)
        assertEquals(entity.attributes, domain.attributes)
        assertEquals(WorkspaceId(workspaceId), domain.workspaceId)
        assertEquals(now, domain.createdAt)
        assertEquals("system", domain.createdBy)
    }
}
