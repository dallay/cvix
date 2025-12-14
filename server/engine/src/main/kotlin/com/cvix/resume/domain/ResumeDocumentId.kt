package com.cvix.resume.domain

import com.cvix.common.domain.BaseId
import java.util.UUID

/**
 * Identifier for a Resume Document.
 *
 * @property id The UUID value of the Resume Document ID
 */
data class ResumeDocumentId(override val id: UUID) : BaseId<UUID>(id) {
    constructor(id: String) : this(UUID.fromString(id))
}
