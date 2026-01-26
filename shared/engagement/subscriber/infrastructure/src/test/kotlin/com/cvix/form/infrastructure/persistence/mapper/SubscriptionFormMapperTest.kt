package com.cvix.form.infrastructure.persistence.mapper

import com.cvix.UnitTest
import com.cvix.form.application.SubscriberFormStub
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriptionFormMapperTest {

    @Test
    fun `should map domain to entity and back`() {
        // Arrange
        val domain = SubscriberFormStub.randomForm()

        // Act
        val entity = SubscriptionFormMapper.toEntity(domain)
        val mappedDomain = SubscriptionFormMapper.toDomain(entity)

        // Assert
        assertEquals(domain.id, mappedDomain.id)
        assertEquals(domain.name, mappedDomain.name)
        assertEquals(domain.description, mappedDomain.description)
        assertEquals(domain.settings.header, mappedDomain.settings.header)
        assertEquals(domain.settings.inputPlaceholder, mappedDomain.settings.inputPlaceholder)
        assertEquals(domain.settings.buttonText, mappedDomain.settings.buttonText)
        assertEquals(domain.settings.buttonColor.value, mappedDomain.settings.buttonColor.value)
        assertEquals(domain.settings.backgroundColor.value, mappedDomain.settings.backgroundColor.value)
        assertEquals(domain.settings.textColor.value, mappedDomain.settings.textColor.value)
        assertEquals(domain.settings.buttonTextColor.value, mappedDomain.settings.buttonTextColor.value)
        assertEquals(domain.status, mappedDomain.status)
        assertEquals(domain.workspaceId, mappedDomain.workspaceId)
    }
}
