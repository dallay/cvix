package com.cvix.workspace.domain

import com.cvix.common.domain.BaseId
import java.util.*

/**
 * Value object representing a workspace identifier.
 *
 * @property id The UUID value of the workspace identifier.
 */
data class WorkspaceId(override val id: UUID) : BaseId<UUID>(id) {
    constructor(id: String) : this(UUID.fromString(id))

    companion object {
        private const val serialVersionUID: Long = 1L
        fun create() = WorkspaceId(UUID.randomUUID())
    }
}
