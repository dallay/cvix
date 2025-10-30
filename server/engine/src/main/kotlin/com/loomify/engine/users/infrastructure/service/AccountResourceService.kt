package com.loomify.engine.users.infrastructure.service

import com.loomify.engine.authentication.infrastructure.Claims
import com.loomify.engine.users.application.response.UserResponse
import com.loomify.engine.users.infrastructure.persistence.entity.FederatedIdentityEntity
import com.loomify.engine.users.infrastructure.persistence.repository.FederatedIdentityR2dbcRepository
import com.loomify.engine.users.infrastructure.persistence.repository.UserR2dbcRepository
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * The [AccountResourceService] class is responsible for retrieving user account information.
 * It uses federated_identities table to map external identity provider IDs to local user IDs.
 * @created 21/8/23
 */
@Service
class AccountResourceService(
    private val userRepository: UserR2dbcRepository,
    private val federatedIdentityRepository: FederatedIdentityR2dbcRepository,
) {
    /**
     * Retrieves the user account information based on the provided authentication token.
     * This method:
     * 1. Extracts the external user ID (sub claim) and email from the JWT token
     * 2. Looks up the federated_identities table to find the local user ID
     * 3. If not found, falls back to email-based lookup and creates the federated identity mapping
     * 4. Returns the user information with the correct local user ID
     *
     * @param authToken The authentication token. Must be an instance of AbstractAuthenticationToken.
     * @return A Mono containing the user account information as a UserResponse object.
     *         The UserResponse object contains the id, username, email, firstname, lastname,
     *         and authorities of the user.
     * @throws IllegalArgumentException if the authentication token is not an instance of OAuth2AuthenticationToken
     * or JwtAuthenticationToken.
     * @throws IllegalStateException if the user cannot be found or created in the database.
     */
    fun getAccount(authToken: AbstractAuthenticationToken): Mono<UserResponse> {
        log.debug("Getting user account information")
        val attributes: Map<String, Any> = when (authToken) {
            is OAuth2AuthenticationToken -> authToken.principal.attributes

            is JwtAuthenticationToken -> authToken.tokenAttributes

            else -> throw IllegalArgumentException("Authentication token is not OAuth2 or JWT")
        }

        val authorities = Claims.extractAuthorityFromClaims(attributes)
        val externalUserId = attributes["sub"] as String // Keycloak/IdP user ID
        val email = attributes["email"] as String
        val providerName = determineProviderName(authToken)

        // Look up or create the federated identity mapping
        return mono {
            val user = findOrCreateUserFromFederatedIdentity(
                providerName = providerName,
                externalUserId = externalUserId,
                email = email,
                attributes = attributes,
            )

            UserResponse(
                id = user.id.toString(),
                username = attributes["preferred_username"] as String,
                email = email,
                firstname = attributes["given_name"] as String,
                lastname = attributes["family_name"] as String,
                authorities = authorities.map { it.authority }.toSet(),
            )
        }
    }

    /**
     * Finds a user by their federated identity, or creates a new federated identity mapping
     * if this is the first time the user is logging in with this provider.
     *
     * @param providerName The identity provider name (e.g., "keycloak", "google")
     * @param externalUserId The user's ID in the external identity provider
     * @param email The user's email address
     * @param attributes Additional attributes from the identity provider
     * @return The UserEntity from our local database
     * @throws IllegalStateException if the user cannot be found in the database
     */
    private suspend fun findOrCreateUserFromFederatedIdentity(
        providerName: String,
        externalUserId: String,
        email: String,
        attributes: Map<String, Any>,
    ) = run {
        // First, try to find an existing federated identity mapping
        val federatedIdentity = federatedIdentityRepository
            .findByProviderNameAndExternalUserId(providerName, externalUserId)

        if (federatedIdentity != null) {
            // Mapping exists, retrieve the user
            log.debug("Found existing federated identity for provider: $providerName, external ID: $externalUserId")
            val user = userRepository.findById(federatedIdentity.userId)
            check(user != null) { "User not found in database: ${federatedIdentity.userId}" }
            user
        } else {
            // No mapping exists, this is a first-time login
            // Look up the user by email and create the mapping
            log.debug("No federated identity found, looking up user by email: $email")
            val user = userRepository.findByEmail(email)
            check(user != null) { "User not found in database for email: $email" }

            // Create the federated identity mapping
            val displayName = buildDisplayName(attributes)
            val newFederatedIdentity = FederatedIdentityEntity(
                userId = user.id,
                providerName = providerName,
                externalUserId = externalUserId,
                email = email,
                displayName = displayName,
            )

            federatedIdentityRepository.save(newFederatedIdentity)
            log.info("Created federated identity mapping for user ${user.id} with provider $providerName")

            user
        }
    }

    /**
     * Determines the provider name from the authentication token.
     * Prefers the JWT issuer claim if present, otherwise uses the OAuth2 client registration ID
     * or falls back to the default provider name.
     */
    private fun determineProviderName(authToken: AbstractAuthenticationToken): String =
        when (authToken) {
            is OAuth2AuthenticationToken ->
                authToken.authorizedClientRegistrationId
            is JwtAuthenticationToken -> {
                // Prefer the issuer claim if present, otherwise fall back to default
                val issuer = authToken.token.claims["iss"] as? String
                issuer ?: DEFAULT_PROVIDER_NAME
            }
            else ->
                DEFAULT_PROVIDER_NAME
        }

    /**
     * Builds a display name from the token attributes
     */
    private fun buildDisplayName(attributes: Map<String, Any>): String {
        val firstName = attributes["given_name"] as? String ?: ""
        val lastName = attributes["family_name"] as? String ?: ""
        return "$firstName $lastName".trim()
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccountResourceService::class.java)
        private const val DEFAULT_PROVIDER_NAME = "oidc"
    }
}
