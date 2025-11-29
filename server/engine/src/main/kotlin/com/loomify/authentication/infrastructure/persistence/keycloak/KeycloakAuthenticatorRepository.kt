package com.loomify.authentication.infrastructure.persistence.keycloak

import com.loomify.authentication.domain.AccessToken
import com.loomify.authentication.domain.UserAuthenticationException
import com.loomify.authentication.domain.UserAuthenticator
import com.loomify.authentication.infrastructure.ApplicationSecurityProperties
import com.loomify.authentication.infrastructure.mapper.AccessTokenResponseMapper.toAccessToken
import com.loomify.common.domain.vo.Username
import com.loomify.common.domain.vo.credential.Credential
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.NotAuthorizedException
import org.keycloak.admin.client.KeycloakBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

/**
 * This class represents a repository for authenticating users using Keycloak. It implements
 * the [UserAuthenticator] interface, which defines the authenticate() method for user authentication.
 *
 * @property applicationSecurityProperties The properties for configuring the Keycloak authentication.
 *
 * @constructor Creates a new instance of KeycloakAuthenticatorRepository.
 * @param applicationSecurityProperties The properties for configuring the Keycloak authentication.
 * @created 31/7/23
 */
@Repository
class KeycloakAuthenticatorRepository(
    private val applicationSecurityProperties: ApplicationSecurityProperties
) : UserAuthenticator {

    /**
     * Creates a new [KeycloakBuilder] instance with password credentials.
     *
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @param rememberMe Whether to use remember-me functionality (extends token lifetime).
     * @return The [KeycloakBuilder] instance configured with the provided username and password.
     */
    private fun newKeycloakBuilderWithPasswordCredentials(
        username: String,
        password: String,
        rememberMe: Boolean = false,
        grantType: String = "password"
    ): KeycloakBuilder {
        val builder = KeycloakBuilder.builder()
            .realm(applicationSecurityProperties.oauth2.realm)
            .serverUrl(applicationSecurityProperties.oauth2.serverUrl)
            .clientId(applicationSecurityProperties.oauth2.clientId)
            .clientSecret(applicationSecurityProperties.oauth2.clientSecret)
            .grantType(grantType)
            .username(username)
            .password(password)

        // Add remember-me scope if requested
        // Keycloak will use different token TTL for remember-me sessions
        if (rememberMe) {
            builder.scope("openid profile email offline_access")
        }

        return builder
    }

    /**
     * Login a user with the given username and password.
     *
     * @param username the username of the user to be logged in
     * @param password the password of the user to be logged in
     * @param rememberMe whether to extend the session duration
     * @return The [AccessToken] object containing the access token and other information.
     */
    override suspend fun authenticate(username: Username, password: Credential, rememberMe: Boolean): AccessToken {
        log.debug("Authenticating user with username: {} (rememberMe: {})", username, rememberMe)
        return try {
            val keycloak = newKeycloakBuilderWithPasswordCredentials(
                username.value,
                password.credentialValue.value,
                rememberMe,
            ).build()
            val accessTokenResponse = keycloak.tokenManager().accessToken
            accessTokenResponse.toAccessToken().also {
                log.info("User authenticated successfully (rememberMe: {})", rememberMe)
            }
        } catch (ex: ClientErrorException) {
            var message: String = ex.message ?: ""
            when (ex) {
                is NotAuthorizedException,
                is BadRequestException -> {
                    log.warn("Unable to authenticate user", ex)
                    message = "Invalid account. User probably hasn't verified email."
                }

                else -> log.error("Unable to authenticate user", ex)
            }
            throw UserAuthenticationException(message, ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(KeycloakAuthenticatorRepository::class.java)
    }
}
