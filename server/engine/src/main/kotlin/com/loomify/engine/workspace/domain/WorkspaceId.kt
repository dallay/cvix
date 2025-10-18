package com.loomify.engine.workspace.domain

import com.loomify.common.domain.BaseId
import java.util.*

/**
 * Value object representing a workspace identifier.
 *
 * @property value The UUID value of the workspace identifier.
 */
data class WorkspaceId(private val id: UUID) : BaseId<UUID>(id) {
    constructor(id: String) : this(UUID.fromString(id))

    companion object {
        private const val serialVersionUID: Long = 1L
        fun create() = WorkspaceId(UUID.randomUUID())
    }
}
