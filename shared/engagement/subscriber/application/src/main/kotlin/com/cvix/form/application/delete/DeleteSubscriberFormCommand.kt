package com.cvix.form.application.delete

import com.cvix.common.domain.bus.command.Command
import java.util.*

/**
 * Command to delete a subscriber form from a workspace.
 *
 * This command represents the intent to remove a subscriber-facing form,
 * ensuring the operation is performed within the correct workspace context
 * and by an authorized user.
 *
 * @property formId Unique identifier of the form to be deleted.
 * @property workspaceId Identifier of the workspace that owns the form.
 * @property userId Identifier of the user requesting the deletion.
 *
 * @created 25/1/26
 */
data class DeleteSubscriberFormCommand(
    val formId: UUID,
    val workspaceId: UUID,
    val userId: UUID,
) : Command
