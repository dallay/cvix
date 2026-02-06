package com.cvix.resume.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import java.util.UUID

/**
 * Event published when a resume is deleted.
 *
 * @property resumeId The ID of the deleted resume
 * @property userId The ID of the user who deleted the resume
 */
data class ResumeDeletedEvent(
    val resumeId: UUID,
    val userId: UUID,
) : BaseDomainEvent()
