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
        val mapper = SubscriptionFormMapper()
        val domain = SubscriberFormStub.randomForm()

        // Act
        val entity = with(mapper) { domain.toEntity() }
        val mappedDomain = with(mapper) { entity.toDomain() }

        // Assert - Basic fields
        assertEquals(domain.id, mappedDomain.id)
        assertEquals(domain.name, mappedDomain.name)
        assertEquals(domain.description, mappedDomain.description)
        assertEquals(domain.status, mappedDomain.status)
        assertEquals(domain.workspaceId, mappedDomain.workspaceId)

        // Assert - Behavior settings
        assertEquals(domain.settings.settings.successActionType, mappedDomain.settings.settings.successActionType)
        assertEquals(domain.settings.settings.successMessage, mappedDomain.settings.settings.successMessage)
        assertEquals(domain.settings.settings.redirectUrl, mappedDomain.settings.settings.redirectUrl)
        assertEquals(domain.settings.settings.confirmationRequired, mappedDomain.settings.settings.confirmationRequired)

        // Assert - Styling settings
        assertEquals(domain.settings.styling.backgroundColor.value, mappedDomain.settings.styling.backgroundColor.value)
        assertEquals(domain.settings.styling.textColor.value, mappedDomain.settings.styling.textColor.value)
        assertEquals(domain.settings.styling.buttonColor.value, mappedDomain.settings.styling.buttonColor.value)
        assertEquals(domain.settings.styling.buttonTextColor.value, mappedDomain.settings.styling.buttonTextColor.value)
        assertEquals(domain.settings.styling.borderColor.value, mappedDomain.settings.styling.borderColor.value)
        assertEquals(domain.settings.styling.borderStyle, mappedDomain.settings.styling.borderStyle)
        assertEquals(domain.settings.styling.shadow, mappedDomain.settings.styling.shadow)
        assertEquals(domain.settings.styling.borderThickness, mappedDomain.settings.styling.borderThickness)
        assertEquals(domain.settings.styling.width, mappedDomain.settings.styling.width)
        assertEquals(domain.settings.styling.height, mappedDomain.settings.styling.height)
        assertEquals(domain.settings.styling.horizontalAlignment, mappedDomain.settings.styling.horizontalAlignment)
        assertEquals(domain.settings.styling.verticalAlignment, mappedDomain.settings.styling.verticalAlignment)
        assertEquals(domain.settings.styling.padding, mappedDomain.settings.styling.padding)
        assertEquals(domain.settings.styling.gap, mappedDomain.settings.styling.gap)
        assertEquals(domain.settings.styling.cornerRadius, mappedDomain.settings.styling.cornerRadius)

        // Assert - Content settings
        assertEquals(domain.settings.content.showHeader, mappedDomain.settings.content.showHeader)
        assertEquals(domain.settings.content.showSubheader, mappedDomain.settings.content.showSubheader)
        assertEquals(domain.settings.content.headerTitle, mappedDomain.settings.content.headerTitle)
        assertEquals(domain.settings.content.subheaderText, mappedDomain.settings.content.subheaderText)
        assertEquals(domain.settings.content.inputPlaceholder, mappedDomain.settings.content.inputPlaceholder)
        assertEquals(domain.settings.content.submitButtonText, mappedDomain.settings.content.submitButtonText)
        assertEquals(domain.settings.content.submittingButtonText, mappedDomain.settings.content.submittingButtonText)
        assertEquals(domain.settings.content.showTosCheckbox, mappedDomain.settings.content.showTosCheckbox)
        assertEquals(domain.settings.content.tosText, mappedDomain.settings.content.tosText)
        assertEquals(domain.settings.content.showPrivacyCheckbox, mappedDomain.settings.content.showPrivacyCheckbox)
        assertEquals(domain.settings.content.privacyText, mappedDomain.settings.content.privacyText)

        // Audit fields assertions
        assertEquals(domain.createdAt, mappedDomain.createdAt)
        assertEquals(domain.createdBy, mappedDomain.createdBy)
        assertEquals(domain.updatedAt, mappedDomain.updatedAt)
        assertEquals(domain.updatedBy, mappedDomain.updatedBy)
    }
}
