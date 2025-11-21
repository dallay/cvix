package com.loomify.resume.domain

import com.loomify.common.domain.BaseId
import java.util.UUID

data class ResumeDocumentId(private val id: UUID) : BaseId<UUID>(id) {
    constructor(id: String) : this(UUID.fromString(id))
}
