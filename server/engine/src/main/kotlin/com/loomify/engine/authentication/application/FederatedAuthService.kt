package com.loomify.engine.authentication.application

import java.util.UUID
import reactor.core.publisher.Mono

/**
 * Data class representing a federated identity link.
 *
 * @property userId The ID of the user in the system
 * @property provider The identity provider name (google, microsoft, github)
 * @property externalUserId The user's ID in the external identity provider
 * @property email The email address from the identity provider
 * @property displayName The display name from the identity provider
 */
data class FederatedIdentity(
    val userId: UUID,
    val provider: String,
    val externalUserId: String,
    val email: String,
    val displayName: String
)

/**
 * Service interface for managing federated identity authentication.
 *
 * Handles account creation, linking, and authentication via OAuth2/OIDC providers.
 *
 * **Requirements**: FR-024, FR-025, FR-026
 */
interface FederatedAuthService {
    /**
     * Finds or creates a user account for a federated identity.
     *
     * If an account with the same email exists, it links the federated identity.
     * If no account exists, it creates a new user account.
     *
     * @param provider The identity provider name
     * @param externalUserId The user's ID in the external identity provider
     * @param email The email address from the identity provider
     * @param firstName The first name from the identity provider
     * @param lastName The last name from the identity provider
     * @param displayName The display name from the identity provider
     * @return Mono with the user information
     */
    fun findOrCreateUser(
        provider: String,
        externalUserId: String,
        email: String,
        firstName: String,
        lastName: String,
        displayName: String
    ): Mono<UserInfo>

    /**
     * Links a federated identity to an existing user account.
     *
     * @param userId The ID of the user to link the identity to
     * @param provider The identity provider name
     * @param externalUserId The user's ID in the external identity provider
     * @param email The email address from the identity provider
     * @return Mono with the federated identity
     */
    fun linkFederatedIdentity(
        userId: UUID,
        provider: String,
        externalUserId: String,
        email: String
    ): Mono<FederatedIdentity>

    /**
     * Finds a user by their federated identity.
     *
     * @param provider The identity provider name
     * @param externalUserId The user's ID in the external identity provider
     * @return Mono with the user information, or empty if not found
     */
    fun findUserByFederatedIdentity(
        provider: String,
        externalUserId: String
    ): Mono<UserInfo>

    /**
     * Unlinking a federated identity from a user account.
     *
     * @param userId The ID of the user
     * @param provider The identity provider name
     * @return Mono completing when the identity is unlinked
     */
    fun unlinkFederatedIdentity(
        userId: UUID,
        provider: String
    ): Mono<Void>
}
