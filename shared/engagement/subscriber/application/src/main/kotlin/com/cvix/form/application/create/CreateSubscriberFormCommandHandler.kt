package com.cvix.form.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.FormBehaviorSettings
import com.cvix.form.domain.FormContentSettings
import com.cvix.form.domain.FormStylingSettings
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionFormSettings
import com.cvix.form.domain.SuccessActionType
import org.slf4j.LoggerFactory

/**
 * Handles the creation of a subscriber form in a workspace.
 *
 * This command handler ensures the user has access to the workspace,
 * constructs the form settings, and delegates form creation to the domain service.
 *
 * @property workspaceAuthorization Service to check workspace access permissions.
 * @property formCreator Domain service responsible for creating subscriber forms.
 *
 * @created 25/1/26
 */
@Service
class CreateSubscriberFormCommandHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val formCreator: SubscriberFormCreator,
) : CommandHandler<CreateSubscriberFormCommand> {
    /**
     * Processes the CreateSubscriberFormCommand by validating access and creating the form.
     *
     * @param command The command containing form creation details.
     */
    override suspend fun handle(command: CreateSubscriberFormCommand) {
        log.debug("Creating form with name: ${command.name}")
        workspaceAuthorization.ensureAccess(command.workspaceId, command.userId)

        // Normalize inputs (trim) and perform small safety validations here so domain can assume cleaned input
        val name = command.name.trim()
        val headerTitle = command.content.headerTitle.trim()

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
                pageBackgroundColor = HexColor(command.styling.pageBackgroundColor),
                backgroundColor = HexColor(command.styling.backgroundColor),
                textColor = HexColor(command.styling.textColor),
                buttonColor = HexColor(command.styling.buttonColor),
                buttonTextColor = HexColor(command.styling.buttonTextColor),
                inputTextColor = HexColor(command.styling.inputTextColor),
                borderColor = HexColor(command.styling.borderColor),
                borderStyle = command.styling.borderStyle,
                shadow = command.styling.shadow,
                borderThickness = command.styling.borderThickness,
                width = command.styling.width,
                height = command.styling.height,
                horizontalAlignment = command.styling.horizontalAlignment,
                verticalAlignment = command.styling.verticalAlignment,
                padding = command.styling.padding,
                gap = command.styling.gap,
                cornerRadius = command.styling.cornerRadius,
            ),
            content = FormContentSettings(
                showHeader = command.content.showHeader,
                showSubheader = command.content.showSubheader,
                headerTitle = headerTitle,
                subheaderText = command.content.subheaderText,
                inputPlaceholder = command.content.inputPlaceholder,
                submitButtonText = command.content.submitButtonText,
                submittingButtonText = command.content.submittingButtonText,
                showTosCheckbox = command.content.showTosCheckbox,
                tosText = command.content.tosText,
                showPrivacyCheckbox = command.content.showPrivacyCheckbox,
                privacyText = command.content.privacyText,
            ),
        )
        formCreator.create(
            formId = command.id,
            name = name,
            description = command.description,
            settings = formSettings,
            workspaceId = command.workspaceId,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateSubscriberFormCommandHandler::class.java)
        private const val MAX_CHARACTERS = 120
    }
}
