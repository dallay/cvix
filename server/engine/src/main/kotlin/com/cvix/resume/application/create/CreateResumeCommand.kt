package com.cvix.resume.application.create

import com.cvix.common.domain.bus.command.Command
import com.cvix.resume.domain.Resume
import java.util.*

/**
 * Command to create a new resume document.
 * Part of the CQRS pattern in the application layer.
 *
 * @property id The unique identifier for the new resume
 * @property userId The authenticated user creating the resume
 * @property workspaceId The workspace where the resume will be created
 * @property title The title of the resume (optional)
 * @property content The resume data following JSON Resume schema
 * @property createdBy Username or email of the creator
 */
data class CreateResumeCommand(
    val id: UUID,
    val userId: UUID,
    val workspaceId: UUID,
    val title: String?,
    val content: Resume,
    val createdBy: String,
) : Command
