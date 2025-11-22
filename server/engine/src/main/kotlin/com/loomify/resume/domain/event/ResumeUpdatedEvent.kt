package com.loomify.resume.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent
import java.util.UUID

/**
 * Event published when a resume is updated.
 *
 * @property resumeId The ID of the updated resume
 * @property userId The ID of the user who updated the resume
 * @property workspaceId The ID of the workspace where the resume belongs
 */
data class ResumeUpdatedEvent(
    val resumeId: UUID,
    val userId: UUID,
    val workspaceId: UUID,
) : BaseDomainEvent()
