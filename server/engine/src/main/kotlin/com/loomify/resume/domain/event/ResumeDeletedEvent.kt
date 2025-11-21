package com.loomify.resume.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent

data class ResumeDeletedEvent(
    val id: String,
    val userId: String,
) : BaseDomainEvent()
