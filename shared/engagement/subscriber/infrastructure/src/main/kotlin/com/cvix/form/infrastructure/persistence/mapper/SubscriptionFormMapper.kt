package com.cvix.form.infrastructure.persistence.mapper

import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.FormBehaviorSettings
import com.cvix.form.domain.FormContentSettings
import com.cvix.form.domain.FormStylingSettings
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionForm
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.SuccessActionType
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
                settings = FormBehaviorSettings(
                    successActionType = SuccessActionType.valueOf(this.successActionType),
                    successMessage = this.successMessage,
                    redirectUrl = this.redirectUrl,
                    confirmationRequired = this.confirmationRequired ?: true,
                ),
                styling = FormStylingSettings(
                    pageBackgroundColor = HexColor.from(this.pageBackgroundColor ?: DEFAULT_WHITE),
                    backgroundColor = HexColor.from(this.backgroundColor ?: DEFAULT_WHITE),
                    textColor = HexColor.from(this.textColor ?: "#000000"),
                    buttonColor = HexColor.from(this.buttonColor ?: "#06B6D4"),
                    buttonTextColor = HexColor.from(this.buttonTextColor ?: DEFAULT_WHITE),
                    inputTextColor = HexColor.from(this.inputTextColor ?: "#000000"),
                    borderColor = HexColor.from(this.borderColor),
                    borderStyle = this.borderStyle,
                    shadow = this.shadow,
                    borderThickness = this.borderThickness,
                    width = this.formWidth,
                    height = this.formHeight,
                    horizontalAlignment = this.horizontalAlignment,
                    verticalAlignment = this.verticalAlignment,
                    padding = this.padding,
                    gap = this.gap,
                    cornerRadius = this.cornerRadius,
                ),
                content = FormContentSettings(
                    showHeader = this.showHeader,
                    showSubheader = this.showSubheader,
                    headerTitle = this.headerTitle,
                    subheaderText = this.subheaderText,
                    inputPlaceholder = this.inputPlaceholder ?: "Enter your email",
                    submitButtonText = this.submitButtonText,
                    submittingButtonText = this.submittingButtonText,
                    showTosCheckbox = this.showTosCheckbox,
                    tosText = this.tosText,
                    showPrivacyCheckbox = this.showPrivacyCheckbox,
                    privacyText = this.privacyText,
                ),
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
            status = this.status,
            workspaceId = this.workspaceId.value,
            // Legacy fields (use content settings as source of truth)
            header = this.settings.content.headerTitle,
            inputPlaceholder = this.settings.content.inputPlaceholder,
            buttonText = this.settings.content.submitButtonText,
            buttonColor = this.settings.styling.buttonColor.value,
            backgroundColor = this.settings.styling.backgroundColor.value,
            textColor = this.settings.styling.textColor.value,
            buttonTextColor = this.settings.styling.buttonTextColor.value,
            confirmationRequired = this.settings.settings.confirmationRequired,
            // Settings
            successActionType = this.settings.settings.successActionType.name,
            successMessage = this.settings.settings.successMessage,
            redirectUrl = this.settings.settings.redirectUrl,
            // Styling
            pageBackgroundColor = this.settings.styling.pageBackgroundColor.value,
            borderColor = this.settings.styling.borderColor.value,
            inputTextColor = this.settings.styling.inputTextColor.value,
            borderStyle = this.settings.styling.borderStyle,
            shadow = this.settings.styling.shadow,
            borderThickness = this.settings.styling.borderThickness,
            formWidth = this.settings.styling.width,
            formHeight = this.settings.styling.height,
            horizontalAlignment = this.settings.styling.horizontalAlignment,
            verticalAlignment = this.settings.styling.verticalAlignment,
            padding = this.settings.styling.padding,
            gap = this.settings.styling.gap,
            cornerRadius = this.settings.styling.cornerRadius,
            // Content
            showHeader = this.settings.content.showHeader,
            showSubheader = this.settings.content.showSubheader,
            headerTitle = this.settings.content.headerTitle,
            subheaderText = this.settings.content.subheaderText,
            submitButtonText = this.settings.content.submitButtonText,
            submittingButtonText = this.settings.content.submittingButtonText,
            showTosCheckbox = this.settings.content.showTosCheckbox,
            tosText = this.settings.content.tosText,
            showPrivacyCheckbox = this.settings.content.showPrivacyCheckbox,
            privacyText = this.settings.content.privacyText,
            // Audit
            createdBy = this.createdBy,
            createdAt = this.createdAt,
            updatedBy = this.updatedBy,
            updatedAt = this.updatedAt,
        )
    }.getOrElse {
        throw DomainMappingException("Failed mapping SubscriptionForm to entity", it)
    }

    private companion object {
        private const val DEFAULT_WHITE = "#FFFFFF"
    }
}
