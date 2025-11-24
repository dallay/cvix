package com.loomify.resume.application.delete

import com.loomify.common.domain.bus.command.Command
import java.util.UUID

/**
 * Command to delete a resume document.
 * Part of the CQRS pattern in the application layer.
 *
 * @property id The resume document ID to delete
 * @property userId The authenticated user (must be owner)
 */
data class DeleteResumeCommand(
    val id: UUID,
    val userId: UUID,
) : Command
