package com.cvix.authentication.infrastructure.keycloak

import com.cvix.authentication.application.FederatedAuthService
import com.cvix.authentication.application.FederatedIdentity
import com.cvix.authentication.application.UserInfo
import com.cvix.authentication.domain.Role
import com.cvix.users.infrastructure.persistence.UserStoreR2dbcRepository
import java.util.UUID
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Implementation of [FederatedAuthService] that integrates with Keycloak.
 *
 * This service manages federated identity authentication by creating or linking
 * user accounts based on OAuth2/OIDC authentication results.
 *
 * **Requirements**: FR-024, FR-025, FR-026
 *
 * @property userStoreRepository Repository for user persistence
 */
@Service
class KeycloakFederatedAuthService(
    private val userStoreRepository: UserStoreR2dbcRepository
) : FederatedAuthService {

    private val logger = LoggerFactory.getLogger(KeycloakFederatedAuthService::class.java)

    override fun findOrCreateUser(
        provider: String,
        externalUserId: String,
        email: String,
        firstName: String,
        lastName: String,
        displayName: String
    ): Mono<UserInfo> {
        logger.info("Finding or creating user for federated identity: provider=$provider, email=***")

        // First, try to find user by federated identity
        return findUserByFederatedIdentity(provider, externalUserId)
            .switchIfEmpty(
                // Create new user account
                createNewUser(email, firstName, lastName, displayName, provider, externalUserId),
            )
    }

    override fun linkFederatedIdentity(
        userId: UUID,
        provider: String,
        externalUserId: String,
        email: String
    ): Mono<FederatedIdentity> {
        logger.info("Linking federated identity: userId=$userId, provider=$provider")

        // In a production system, this would store the federated identity link in a separate table
        // For now, we'll return a FederatedIdentity object to satisfy the interface
        // Note: proper federated identity storage will be implemented when the federated_identity table is created

        return Mono.just(
            FederatedIdentity(
                userId = userId,
                provider = provider,
                externalUserId = externalUserId,
                email = email,
                displayName = "",
            ),
        )
    }

    override fun findUserByFederatedIdentity(
        provider: String,
        externalUserId: String
    ): Mono<UserInfo> {
        // Note: federated identity lookup is intentionally unimplemented until a persistence schema is available.
        // Returning empty will trigger the fallback flow which creates a new local user.
        logger.debug(
            "Federated identity lookup not yet implemented: provider=$provider, externalUserId=$externalUserId",
        )
        return Mono.empty()
    }

    override fun unlinkFederatedIdentity(userId: UUID, provider: String): Mono<Void> {
        logger.info("Unlinking federated identity: userId=$userId, provider=$provider")

        // Note: unlink operation is a no-op until federated identity persistence is available.
        return Mono.empty()
    }

    private fun createNewUser(
        email: String,
        firstName: String,
        lastName: String,
        displayName: String,
        provider: String,
        externalUserId: String
    ): Mono<UserInfo> {
        logger.info("Creating new user for federated identity: email=***, provider=$provider")

        val userId = UUID.randomUUID()
        val fullName = displayName.ifEmpty { "$firstName $lastName".trim() }

        return mono {
            // Create user in local database
            userStoreRepository.create(userId, email, fullName)

            // Create UserInfo
            UserInfo(
                id = userId,
                email = email,
                username = email,
                firstName = firstName.takeIf { it.isNotBlank() },
                lastName = lastName.takeIf { it.isNotBlank() },
                displayName = fullName,
                roles = setOf(Role.USER),
            )
        }
            .flatMap { userInfo ->
                // Link federated identity
                linkFederatedIdentity(userId, provider, externalUserId, email)
                    .then(Mono.just(userInfo))
            }
            .doOnSuccess { user ->
                logger.info("Successfully created user for federated identity: userId=${user.id}")
            }
            .doOnError { error ->
                logger.error("Failed to create user for federated identity", error)
            }
    }
}
