package com.cvix.workspace.infrastructure.persistence.entity

import com.cvix.common.domain.AuditableEntity
import com.cvix.workspace.domain.Workspace.Companion.NAME_MAX_LENGTH
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * R2DBC entity for workspaces.
 */
@Table("workspaces")
data class WorkspaceEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("name")
    @get:Size(max = NAME_MAX_LENGTH)
    val name: String,

    @Column("description")
    @get:Size(max = 500)
    val description: String?,

    @Column("owner_id")
    val ownerId: UUID,

    @Column("is_default")
    val isDefault: Boolean = false,

    @CreatedBy
    @Column("created_by")
    @get:Size(max = 50)
    override val createdBy: String = "system",

    @CreatedDate
    @Column("created_at")
    override val createdAt: Instant,

    @LastModifiedBy
    @Column("updated_by")
    @get:Size(max = 50)
    override var updatedBy: String? = null,

    @LastModifiedDate
    @Column("updated_at")
    override var updatedAt: Instant? = null
) : AuditableEntity(createdAt, createdBy, updatedAt, updatedBy), Persistable<UUID> {
    /**
     * This method returns the unique identifier of the workspace.
     *
     * @return The unique identifier of the workspace.
     */
    override fun getId(): UUID = id

    /**
     * This method checks if the workspace is new by comparing the creation and update timestamps.
     *
     * @return A boolean indicating whether the workspace is new.
     */
    override fun isNew(): Boolean = updatedAt == null || createdAt == updatedAt
}
