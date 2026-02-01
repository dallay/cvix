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
 * Includes settings, styling, and content configuration.
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

    @Column("status")
    var status: SubscriptionFormStatus,

    @Column("workspace_id")
    val workspaceId: UUID,

    // ==================== LEGACY FIELDS (kept for backward compatibility) ====================
    @Column("header")
    @get:Size(max = 120)
    val header: String? = null,

    @Column("input_placeholder")
    @get:Size(max = 120)
    val inputPlaceholder: String? = null,

    @Column("button_text")
    @get:Size(max = 120)
    val buttonText: String? = null,

    @Column("button_color")
    @get:Size(max = 7)
    val buttonColor: String? = null,

    @Column("background_color")
    @get:Size(max = 7)
    val backgroundColor: String? = null,

    @Column("text_color")
    @get:Size(max = 7)
    val textColor: String? = null,

    @Column("button_text_color")
    @get:Size(max = 7)
    val buttonTextColor: String? = null,

    @Column("input_text_color")
    @get:Size(max = 7)
    val inputTextColor: String = "#000000",

    @Column("confirmation_required")
    val confirmationRequired: Boolean? = null,

    // ==================== SETTINGS ====================
    @Column("success_action_type")
    @get:Size(max = 20)
    val successActionType: String = "SHOW_MESSAGE",

    @Column("success_message")
    val successMessage: String? = null,

    @Column("redirect_url")
    @get:Size(max = 500)
    val redirectUrl: String? = null,

    // ==================== STYLING ====================
    @Column("page_background_color")
    @get:Size(max = 7)
    val pageBackgroundColor: String = "#FFFFFF",

    @Column("border_color")
    @get:Size(max = 7)
    val borderColor: String = "#000000",

    @Column("border_style")
    @get:Size(max = 20)
    val borderStyle: String = "solid",

    @Column("shadow")
    @get:Size(max = 20)
    val shadow: String = "none",

    @Column("border_thickness")
    val borderThickness: Int = 0,

    @Column("form_width")
    @get:Size(max = 20)
    val formWidth: String = "auto",

    @Column("form_height")
    @get:Size(max = 20)
    val formHeight: String = "auto",

    @Column("horizontal_alignment")
    @get:Size(max = 20)
    val horizontalAlignment: String = "center",

    @Column("vertical_alignment")
    @get:Size(max = 20)
    val verticalAlignment: String = "center",

    @Column("padding")
    val padding: Int = 16,

    @Column("gap")
    val gap: Int = 16,

    @Column("corner_radius")
    val cornerRadius: Int = 8,

    // ==================== CONTENT ====================
    @Column("show_header")
    val showHeader: Boolean = true,

    @Column("show_subheader")
    val showSubheader: Boolean = true,

    @Column("header_title")
    @get:Size(max = 120)
    val headerTitle: String = "Join our newsletter",

    @Column("subheader_text")
    @get:Size(max = 500)
    val subheaderText: String? = null,

    @Column("submit_button_text")
    @get:Size(max = 120)
    val submitButtonText: String = "Subscribe",

    @Column("submitting_button_text")
    @get:Size(max = 120)
    val submittingButtonText: String = "Submitting...",

    @Column("show_tos_checkbox")
    val showTosCheckbox: Boolean = false,

    @Column("tos_text")
    @get:Size(max = 500)
    val tosText: String? = null,

    @Column("show_privacy_checkbox")
    val showPrivacyCheckbox: Boolean = false,

    @Column("privacy_text")
    @get:Size(max = 500)
    val privacyText: String? = null,

    // ==================== AUDIT ====================
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
        private const val serialVersionUID: Long = 2L
    }
}
