package com.loomify.resume.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent
import java.util.UUID

/**
 * Event published when a new resume is created.
 *
 * @property resumeId The ID of the created resume
 * @property userId The ID of the user who created the resume
 * @property workspaceId The ID of the workspace where the resume was created
 */
data class ResumeCreatedEvent(
    val resumeId: UUID,
    val userId: UUID,
    val workspaceId: UUID,
) : BaseDomainEvent()
