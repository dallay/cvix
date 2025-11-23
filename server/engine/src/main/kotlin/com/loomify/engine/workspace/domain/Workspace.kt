package com.loomify.engine.workspace.domain

import com.loomify.common.domain.AggregateRoot
import com.loomify.engine.users.domain.UserId
import com.loomify.engine.workspace.domain.event.WorkspaceCreatedEvent
import com.loomify.engine.workspace.domain.event.WorkspaceUpdatedEvent
import java.time.Instant
import java.util.UUID

/**
 * Workspace domain model. This is the root of the workspace aggregate.
 * It contains all the information about a workspace.
 *
 * @property id The unique identifier of the workspace.
 * @property name The name of the workspace.
 * @property description The description of the workspace.
 * @property ownerId The ID of the user who owns the workspace.
 * @property isDefault Whether this is the default workspace for the owner.
 * @property members The list of user IDs who are members of the workspace.
 * @property createdAt The date and time when the workspace was created.
 * @property updatedAt The date and time when the workspace was last updated.
 */
data class Workspace(
    override val id: WorkspaceId,
    var name: String,
    var description: String? = null,
    val ownerId: UserId,
    val isDefault: Boolean = false,
    val members: MutableSet<UserId> = mutableSetOf(),
    override val createdAt: Instant = Instant.now(),
    override val createdBy: String = "system",
    override var updatedAt: Instant? = createdAt,
    override var updatedBy: String? = null
) : AggregateRoot<WorkspaceId>() {

    init {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            throw WorkspaceException("Workspace name cannot be blank")
        }
        if (trimmedName.length > NAME_MAX_LENGTH) {
            throw WorkspaceException("Workspace name must be at most $NAME_MAX_LENGTH characters long")
        }
        name = trimmedName
        // Owner is always a member
        members.add(ownerId)
    }

    /**
     * Adds a user to the workspace.
     *
     * @param userId The ID of the user to add.
     * @return True if the user was added, false if the user was already a member.
     */
    fun addMember(userId: UUID): Boolean = members.add(UserId(userId))

    /**
     * Removes a user from the workspace.
     *
     * @param userId The ID of the user to remove.
     * @return True if the user was removed, false if the user was not a member or is the owner.
     */
    fun removeMember(userId: UUID): Boolean {
        // Owner cannot be removed
        if (UserId(userId) == ownerId) {
            return false
        }
        return members.remove(UserId(userId))
    }

    /**
     * Checks if a user is a member of the workspace.
     *
     * @param userId The ID of the user to check.
     * @return True if the user is a member, false otherwise.
     */
    fun isMember(userId: UUID): Boolean = members.contains(UserId(userId))

    /**
     * Checks if a user is the owner of the workspace.
     *
     * @param userId The ID of the user to check.
     * @return True if the user is the owner, false otherwise.
     */
    fun isOwner(userId: UUID): Boolean = ownerId == UserId(userId)

    /**
     * Updates the workspace information.
     *
     * @param name The new name of the workspace.
     * @param description The new description of the workspace. If null, the existing description is retained.
     */
    fun update(name: String, description: String? = this.description) {
        this.name = name
        // Update description only if it's not null
        if (description != null) {
            this.description = description
        }
        this.updatedAt = Instant.now()

        // Record the workspace updated event
        record(
            WorkspaceUpdatedEvent(
                id = this.id.id.toString(),
                workspaceName = this.name,
                ownerId = this.ownerId.id.toString(),
            ),
        )
    }

    companion object {
        /**
         * Maximum allowed length for a workspace name. Keep in sync with DB schema (varchar(100)).
         * Prefer referencing this constant instead of duplicating literal values.
         */
        const val NAME_MAX_LENGTH: Int = 100

        /**
         * Creates a new workspace with the given information.
         *
         * @param id The unique identifier for the workspace.
         * @param name The name of the workspace.
         * @param description The description of the workspace.
         * @param ownerId The ID of the user who owns the workspace.
         * @param isDefault Whether this is the default workspace for the owner.
         * @return The newly created Workspace object.
         */
        fun create(
            id: UUID,
            name: String,
            description: String? = null,
            ownerId: UUID,
            isDefault: Boolean = false
        ): Workspace {
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) {
                throw WorkspaceException("Workspace name cannot be blank")
            }
            if (trimmedName.length > NAME_MAX_LENGTH) {
                throw WorkspaceException("Workspace name must be at most $NAME_MAX_LENGTH characters long")
            }
            val workspaceId = WorkspaceId(id)
            val owner = UserId(ownerId)
            val workspace = Workspace(
                id = workspaceId,
                name = trimmedName,
                description = description,
                ownerId = owner,
                isDefault = isDefault,
                members = mutableSetOf(owner),
            )

            // Record the workspace created event
            workspace.record(
                WorkspaceCreatedEvent(
                    id = workspace.id.id.toString(),
                    name = workspace.name,
                    ownerId = workspace.ownerId.id.toString(),
                ),
            )

            return workspace
        }
    }
}
