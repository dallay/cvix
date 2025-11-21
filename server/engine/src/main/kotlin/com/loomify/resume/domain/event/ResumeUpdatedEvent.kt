package com.loomify.resume.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent

/**
 * Event published when a resume is updated.
 *
 * @property resumeId The ID of the created resume
 * @property userId The ID of the user who created the resume
 * @property workspaceId The ID of the workspace where the resume was created
 */
data class ResumeUpdatedEvent(
    val resumeId: String,
    val userId: String,
    val workspaceId: String,
) : BaseDomainEvent()
