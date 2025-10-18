package com.loomify.engine.users.infrastructure.persistence.entity

import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * R2DBC entity representing a row in the users table.
 */
@Table("users")
data class UserEntity(
    @Id
    @JvmField
    val id: UUID,
    val email: String,
    @Column("full_name")
    val fullName: String,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null,
) : Persistable<UUID> {
    override fun getId(): UUID = id
    // Consider new when createdAt is null so Spring Data issues INSERT
    override fun isNew(): Boolean = createdAt == null
}
