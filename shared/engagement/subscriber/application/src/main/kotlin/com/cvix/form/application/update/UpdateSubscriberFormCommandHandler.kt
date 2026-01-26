package com.cvix.form.application.update

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.HexColor
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormSettings

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
            buttonColor = HexColor.from(command.buttonColor),
            backgroundColor = HexColor.from(command.backgroundColor),
            textColor = HexColor.from(command.textColor),
            buttonTextColor = HexColor.from(command.buttonTextColor),
        )

        formUpdater.update(
            workspaceId = WorkspaceId(command.workspaceId),
            formId = SubscriptionFormId(command.id),
            name = name,
            description = command.description,
            settings = formSettings,
            updatedBy = command.userId.toString(),
        )
    }

    companion object {
        private const val MAX_CHARACTERS = 120
    }
}
