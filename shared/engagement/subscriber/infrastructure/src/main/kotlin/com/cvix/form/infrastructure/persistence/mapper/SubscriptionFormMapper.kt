package com.cvix.form.infrastructure.persistence.mapper

import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.infrastructure.persistence.entity.SubscriptionFormEntity

/**
 * Mapper between [SubscriptionForm] domain entity and [SubscriptionFormEntity] persistence entity.
 */
object SubscriptionFormMapper {

    fun toDomain(entity: SubscriptionFormEntity): SubscriptionForm {
        return SubscriptionForm(
            id = SubscriptionFormId(entity.id),
            name = entity.name,
            description = entity.description ?: "",
            settings = SubscriptionFormSettings(
                header = entity.header,
                inputPlaceholder = entity.inputPlaceholder,
                buttonText = entity.buttonText,
                buttonColor = HexColor.from(entity.buttonColor),
                backgroundColor = HexColor.from(entity.backgroundColor),
                textColor = HexColor.from(entity.textColor),
                buttonTextColor = HexColor.from(entity.buttonTextColor),
            ),
            status = entity.status,
            workspaceId = WorkspaceId(entity.workspaceId),
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
        )
    }

    fun toEntity(domain: SubscriptionForm): SubscriptionFormEntity {
        return SubscriptionFormEntity(
            id = domain.id.value,
            name = domain.name,
            description = domain.description,
            header = domain.settings.header,
            inputPlaceholder = domain.settings.inputPlaceholder,
            buttonText = domain.settings.buttonText,
            buttonColor = domain.settings.buttonColor.value,
            backgroundColor = domain.settings.backgroundColor.value,
            textColor = domain.settings.textColor.value,
            buttonTextColor = domain.settings.buttonTextColor.value,
            status = domain.status,
            workspaceId = domain.workspaceId.value,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            updatedBy = domain.updatedBy,
            updatedAt = domain.updatedAt,
        )
    }
}
