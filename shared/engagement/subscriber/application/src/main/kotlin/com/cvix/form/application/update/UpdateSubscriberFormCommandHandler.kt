package com.cvix.form.application.update

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.FormBehaviorSettings
import com.cvix.form.domain.FormContentSettings
import com.cvix.form.domain.FormStylingSettings
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.SuccessActionType
import org.slf4j.LoggerFactory

/**
 * Handles the update of a subscriber form.
 */
@Service
class UpdateSubscriberFormCommandHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val formUpdater: SubscriberFormUpdater,
) : CommandHandler<UpdateSubscriberFormCommand> {

    override suspend fun handle(command: UpdateSubscriberFormCommand) {
        workspaceAuthorization.ensureAccess(command.workspaceId, command.userId)

        log.info(
            "action=updating_subscription_form workspaceId={} formId={} userId={}",
            command.workspaceId,
            command.id,
            command.userId,
        )

        val name = command.name.trim()
        val headerTitle = command.headerTitle.trim()

        require(name.isNotEmpty()) { "Form name must not be blank" }
        require(headerTitle.isNotEmpty()) { "Form header must not be blank" }
        require(name.length <= MAX_CHARACTERS) { "Form name must be at most $MAX_CHARACTERS characters" }
        require(headerTitle.length <= MAX_CHARACTERS) { "Form header must be at most $MAX_CHARACTERS characters" }

        val formSettings = SubscriptionFormSettings(
            settings = FormBehaviorSettings(
                successActionType = SuccessActionType.valueOf(command.successActionType),
                successMessage = command.successMessage,
                redirectUrl = command.redirectUrl,
                confirmationRequired = command.confirmationRequired,
            ),
            styling = FormStylingSettings(
                pageBackgroundColor = HexColor.from(command.pageBackgroundColor),
                backgroundColor = HexColor.from(command.backgroundColor),
                textColor = HexColor.from(command.textColor),
                buttonColor = HexColor.from(command.buttonColor),
                buttonTextColor = HexColor.from(command.buttonTextColor),
                inputTextColor = HexColor.from(command.inputTextColor),
                borderColor = HexColor.from(command.borderColor),
                borderStyle = command.borderStyle,
                shadow = command.shadow,
                borderThickness = command.borderThickness,
                width = command.width,
                height = command.height,
                horizontalAlignment = command.horizontalAlignment,
                verticalAlignment = command.verticalAlignment,
                padding = command.padding,
                gap = command.gap,
                cornerRadius = command.cornerRadius,
            ),
            content = FormContentSettings(
                showHeader = command.showHeader,
                showSubheader = command.showSubheader,
                headerTitle = headerTitle,
                subheaderText = command.subheaderText,
                inputPlaceholder = command.inputPlaceholder,
                submitButtonText = command.submitButtonText,
                submittingButtonText = command.submittingButtonText,
                showTosCheckbox = command.showTosCheckbox,
                tosText = command.tosText,
                showPrivacyCheckbox = command.showPrivacyCheckbox,
                privacyText = command.privacyText,
            ),
        )

        formUpdater.update(
            workspaceId = WorkspaceId(command.workspaceId),
            formId = SubscriptionFormId(command.id),
            name = name,
            description = command.description,
            settings = formSettings,
            updatedBy = command.userId.toString(),
        )

        log.info(
            "action=subscription_form_updated_successfully workspaceId={} formId={} userId={}",
            command.workspaceId,
            command.id,
            command.userId,
        )
    }

    companion object {
        private const val MAX_CHARACTERS = 120
        private val log = LoggerFactory.getLogger(UpdateSubscriberFormCommandHandler::class.java)
    }
}
