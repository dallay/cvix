package com.cvix.form.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionFormSettings
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
        val header = command.header.trim()

        require(name.isNotEmpty()) { "Form name must not be blank" }
        require(header.isNotEmpty()) { "Form header must not be blank" }
        require(name.length <= MAX_CHARACTERS) { "Form name must be at most $MAX_CHARACTERS characters" }
        require(header.length <= MAX_CHARACTERS) { "Form header must be at most $MAX_CHARACTERS characters" }

        val formSettings = SubscriptionFormSettings(
            header = header,
            inputPlaceholder = command.inputPlaceholder,
            buttonText = command.buttonText,
            buttonColor = HexColor(command.buttonColor),
            backgroundColor = HexColor(command.backgroundColor),
            textColor = HexColor(command.textColor),
            buttonTextColor = HexColor(command.buttonTextColor),
            confirmationRequired = command.confirmationRequired,
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
