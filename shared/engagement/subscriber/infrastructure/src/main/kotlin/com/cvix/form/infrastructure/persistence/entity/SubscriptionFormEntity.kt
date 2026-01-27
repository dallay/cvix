package com.cvix.form.infrastructure.persistence.entity

import com.cvix.common.domain.SYSTEM_USER
import com.cvix.common.domain.model.AuditableEntityFields
import com.cvix.form.domain.SubscriptionFormStatus
import jakarta.validation.constraints.Size
import java.io.Serializable
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
 * Entity representing a subscription form in the persistence layer.
 */
@Table("subscription_forms")
data class SubscriptionFormEntity(
    @Id
    @JvmField
    val id: UUID,

    @Column("name")
    @get:Size(max = 120)
    val name: String,

    @Column("description")
    val description: String?,

    @Column("header")
    @get:Size(max = 120)
    val header: String,

    @Column("input_placeholder")
    @get:Size(max = 120)
    val inputPlaceholder: String,

    @Column("button_text")
    @get:Size(max = 120)
    val buttonText: String,

    @Column("button_color")
    @get:Size(max = 7)
    val buttonColor: String,

    @Column("background_color")
    @get:Size(max = 7)
    val backgroundColor: String,

    @Column("text_color")
    @get:Size(max = 7)
    val textColor: String,

    @Column("button_text_color")
    @get:Size(max = 7)
    val buttonTextColor: String,

    @Column("confirmation_required")
    val confirmationRequired: Boolean = true,

    @Column("status")
    var status: SubscriptionFormStatus,

    @Column("workspace_id")
    val workspaceId: UUID,

    @CreatedBy
    @Column("created_by")
    @get:Size(max = 50)
    override val createdBy: String = SYSTEM_USER,

    @CreatedDate
    @Column("created_at")
    override val createdAt: Instant,

    @LastModifiedBy
    @Column("updated_by")
    @get:Size(max = 50)
    override var updatedBy: String? = null,

    @LastModifiedDate
    @Column("updated_at")
    override var updatedAt: Instant? = null,
) : Serializable, Persistable<UUID>, AuditableEntityFields {

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity()

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
