package com.cvix.form.infrastructure.persistence.mapper

import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity
import org.springframework.stereotype.Component

/**
 * Mapper between [SubscriptionForm] domain entity and [SubscriptionFormEntity] persistence entity.
 */
@Component
class SubscriptionFormMapper {
    fun SubscriptionFormEntity.toDomain(): SubscriptionForm = runCatching {
        SubscriptionForm(
            id = SubscriptionFormId(this.id),
            name = this.name,
            description = this.description ?: "",
            settings = SubscriptionFormSettings(
                header = this.header,
                inputPlaceholder = this.inputPlaceholder,
                buttonText = this.buttonText,
                buttonColor = HexColor.from(this.buttonColor),
                backgroundColor = HexColor.from(this.backgroundColor),
                textColor = HexColor.from(this.textColor),
                buttonTextColor = HexColor.from(this.buttonTextColor),
                confirmationRequired = this.confirmationRequired,
            ),
            status = this.status,
            workspaceId = WorkspaceId(this.workspaceId),
            createdAt = this.createdAt,
            createdBy = this.createdBy,
            updatedAt = this.updatedAt,
            updatedBy = this.updatedBy,
        )
    }.getOrElse {
        throw DomainMappingException("Failed mapping SubscriptionFormEntity to domain", it)
    }

    fun SubscriptionForm.toEntity(): SubscriptionFormEntity = runCatching {
        SubscriptionFormEntity(
            id = this.id.value,
            name = this.name,
            description = this.description,
            header = this.settings.header,
            inputPlaceholder = this.settings.inputPlaceholder,
            buttonText = this.settings.buttonText,
            buttonColor = this.settings.buttonColor.value,
            backgroundColor = this.settings.backgroundColor.value,
            textColor = this.settings.textColor.value,
            buttonTextColor = this.settings.buttonTextColor.value,
            confirmationRequired = this.settings.confirmationRequired,
            status = this.status,
            workspaceId = this.workspaceId.value,
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            updatedBy = this.updatedBy,
            updatedAt = this.updatedAt,
        )
    }.getOrElse {
        throw DomainMappingException("Failed mapping SubscriptionForm to entity", it)
    }
}
