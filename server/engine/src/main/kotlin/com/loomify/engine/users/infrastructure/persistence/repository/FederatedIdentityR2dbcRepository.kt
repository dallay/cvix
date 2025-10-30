package com.loomify.engine.users.infrastructure.persistence.repository

import com.loomify.engine.users.infrastructure.persistence.entity.FederatedIdentityEntity
import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * R2DBC repository for federated_identities table operations.
 * This repository manages the mapping between local users and their federated identity provider accounts.
 */
@Repository
interface FederatedIdentityR2dbcRepository : CoroutineCrudRepository<FederatedIdentityEntity, UUID> {

    /**
     * Find a federated identity by provider name and external user ID.
     * This is used to look up which local user is associated with a given external identity.
     *
     * @param providerName The name of the identity provider (e.g., "keycloak", "google")
     * @param externalUserId The user's ID in the external identity provider
     * @return The federated identity entity if found, null otherwise
     */
    suspend fun findByProviderNameAndExternalUserId(
        providerName: String,
        externalUserId: String,
    ): FederatedIdentityEntity?

    /**
     * Find all federated identities for a given user.
     * This can be used to see all the identity providers a user has linked to their account.
     *
     * @param userId The local user's ID
     * @return List of all federated identities for this user
     */
    suspend fun findByUserId(userId: UUID): List<FederatedIdentityEntity>

    /**
     * Check if a federated identity exists for a given provider and external user ID.
     *
     * @param providerName The name of the identity provider
     * @param externalUserId The user's ID in the external identity provider
     * @return true if exists, false otherwise
     */
    suspend fun existsByProviderNameAndExternalUserId(
        providerName: String,
        externalUserId: String,
    ): Boolean
}
