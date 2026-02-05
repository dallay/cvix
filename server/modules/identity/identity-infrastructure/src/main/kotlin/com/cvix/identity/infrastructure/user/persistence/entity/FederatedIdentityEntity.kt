package com.cvix.identity.infrastructure.user.persistence.entity

import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * R2DBC entity representing a row in the federated_identities table.
 * This table stores the mapping between local users and their federated identity provider accounts.
 *
 * @property id The unique identifier for this federated identity record
 * @property userId The ID of the user in our local database
 * @property providerName The name of the identity provider (e.g., "keycloak", "google", "microsoft")
 * @property externalUserId The user's ID in the external identity provider (e.g., Keycloak's sub claim)
 * @property email The email address associated with this federated identity
 * @property displayName The display name from the identity provider
 * @property createdAt Timestamp when this record was created
 * @property updatedAt Timestamp when this record was last updated
 */
@Table("federated_identities")
data class FederatedIdentityEntity(
    @Id
    val id: UUID? = null,

    @Column("user_id")
    val userId: UUID,

    @Column("provider_name")
    val providerName: String,

    @Column("external_user_id")
    val externalUserId: String,

    val email: String?,

    @Column("display_name")
    val displayName: String?,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null,
)
