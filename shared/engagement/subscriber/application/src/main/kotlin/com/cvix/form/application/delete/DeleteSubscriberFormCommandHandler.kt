package com.cvix.form.application.delete

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.SubscriptionFormId
import org.slf4j.LoggerFactory

/**
 * Command handler responsible for deleting a subscriber form.
 *
 * Ensures the user has access to the workspace before deleting the form.
 *
 * @property workspaceAuthorization Service to check workspace access permissions.
 * @property formDestroyer Service to perform the deletion of the form.
 *
 * @created 25/1/26
 */
@Service
class DeleteSubscriberFormCommandHandler(
    private val workspaceAuthorization: WorkspaceAuthorization,
    private val formDestroyer: FormDestroyer,
) : CommandHandler<DeleteSubscriberFormCommand> {
    private val log = LoggerFactory.getLogger(DeleteSubscriberFormCommandHandler::class.java)
    /**
     * Handles the deletion of a subscriber form.
     *
     * Verifies user access to the workspace and deletes the specified form.
     *
     * @param command The command containing workspace, user, and form identifiers.
     */
    override suspend fun handle(command: DeleteSubscriberFormCommand) {
        log.info(
            "Deleting subscription form formId=${command.formId} " +
                "workspaceId=${command.workspaceId} userId=${command.userId}",
        )
        workspaceAuthorization.ensureAccess(command.workspaceId, command.userId)
        formDestroyer.delete(
            WorkspaceId(command.workspaceId),
            SubscriptionFormId(command.formId),
        )
    }
}
