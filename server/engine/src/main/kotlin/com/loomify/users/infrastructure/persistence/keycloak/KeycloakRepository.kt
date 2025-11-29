package com.loomify.users.infrastructure.persistence.keycloak

import com.loomify.authentication.infrastructure.ApplicationSecurityProperties
import com.loomify.common.domain.error.BusinessRuleValidationException
import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.credential.CredentialException
import com.loomify.common.domain.vo.email.Email
import com.loomify.common.domain.vo.name.FirstName
import com.loomify.common.domain.vo.name.LastName
import com.loomify.users.domain.User
import com.loomify.users.domain.UserCreator
import com.loomify.users.domain.UserException
import com.loomify.users.domain.UserStoreException
import com.loomify.users.infrastructure.persistence.UserStoreR2dbcRepository
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.WebApplicationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class KeycloakRepository(
    private val applicationSecurityProperties: ApplicationSecurityProperties,
    private val keycloak: Keycloak,
    private val userStoreR2dbcRepository: UserStoreR2dbcRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserCreator {

    private val keycloakRealm by lazy {
        keycloak.realm(applicationSecurityProperties.oauth2.realm)
    }

    /**
     * Create a new user.
     *
     * This method handles user creation atomically by relying on Keycloak's built-in uniqueness
     * constraints rather than performing separate existence checks that could lead to race conditions.
     *
     * Race condition handling:
     * - If a user with the same email/username is created concurrently, Keycloak will return
     *   HTTP 409 (Conflict), which is handled gracefully by throwing UserStoreException
     * - The method does not perform pre-existence checks to avoid TOCTOU (Time-of-Check-Time-of-Use) issues
     *
     * @param email The email of the user to be created.
     * @param credential The credential of the user to be created.
     * @param firstName The first name of the user to be created, nullable if not provided.
     * @param lastName The last name of the user to be created, nullable if not provided.
     * @return The created user.
     * @throws UserStoreException if user creation fails, including when user already exists
     */
    override suspend fun create(
        email: Email,
        credential: Credential,
        firstName: FirstName?,
        lastName: LastName?
    ): User {
        log.debug("Creating user with email: {}", email.value)

        val message = "Error creating user with email: ${email.value}"

        return withContext(ioDispatcher) {
            try {
                val userId = createKeycloakUser(email, credential, firstName, lastName)
                persistLocalUser(userId, email, firstName, lastName)

                User.create(
                    id = userId,
                    email = email.value,
                    firstName = firstName?.value ?: "",
                    lastName = lastName?.value ?: "",
                    password = credential.credentialValue.value,
                )
            } catch (exception: BusinessRuleValidationException) {
                handleBusinessRuleException(exception, email, message)
            } catch (exception: ClientErrorException) {
                log.error(
                    "Error creating user with email: {}",
                    email.value.sanitizeForLog(),
                    exception,
                )
                throw UserStoreException(message, exception)
            } catch (exception: WebApplicationException) {
                log.error(
                    "Error creating user with email: {}",
                    email.value.sanitizeForLog(),
                    exception,
                )
                throw UserStoreException(message, exception)
            }
        }
    }

    private suspend fun createKeycloakUser(
        email: Email,
        credential: Credential,
        firstName: FirstName?,
        lastName: LastName?
    ): String {
        val credentialRepresentation = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = credential.credentialValue.value
        }

        log.debug(
            "Trying to create user with email: {} and username: {}",
            email.value.sanitizeForLog(),
            email.value.sanitizeForLog(),
        )

        val userRepresentation = getUserRepresentation(email, firstName, lastName, credentialRepresentation)

        return keycloakRealm.users().create(userRepresentation).use { response ->
            when (response.status) {
                HTTP_CREATED -> {
                    val location = response.location
                        ?: throw UserStoreException(
                            "User creation succeeded but no location header returned",
                        )
                    location.path.replace(".*/([^/]+)$".toRegex(), "$1")
                }
                HTTP_CONFLICT -> {
                    log.warn("User creation conflict for email: {}", email.value)
                    throw UserStoreException(
                        "User with email: ${email.value} or username: ${email.value} already exists.",
                    )
                }
                else -> {
                    log.error(
                        "Unexpected response status {} when creating user with email: {}",
                        response.status,
                        email.value,
                    )
                    throw UserStoreException(
                        "Failed to create user with email: ${email.value}, status: ${response.status}",
                    )
                }
            }
        }
    }

    private suspend fun persistLocalUser(
        userId: String,
        email: Email,
        firstName: FirstName?,
        lastName: LastName?
    ) {
        runCatching {
            val id = java.util.UUID.fromString(userId)
            val fullName = listOfNotNull(firstName?.value, lastName?.value)
                .joinToString(" ").trim()
            userStoreR2dbcRepository.create(id, email.value, fullName)
        }.onFailure {
            log.error("Failed to persist local user row for {}", email.value, it)
        }
    }

    private fun handleBusinessRuleException(
        exception: BusinessRuleValidationException,
        email: Email,
        message: String
    ): Nothing {
        log.error(
            "Error creating user with email: {} and username: {}",
            email.value.sanitizeForLog(),
            email.value.sanitizeForLog(),
            exception,
        )
        val wrappedException = when (exception) {
            is UserStoreException -> exception
            is CredentialException -> UserStoreException(message, exception)
            is UserException -> UserStoreException(message, exception)
            else -> UserStoreException(message, exception)
        }
        throw wrappedException
    }

    private fun getUserRepresentation(
        email: Email,
        firstName: FirstName?,
        lastName: LastName?,
        credential: CredentialRepresentation
    ): UserRepresentation = UserRepresentation().apply {
        val emailValue = email.value
        username = emailValue
        this.email = emailValue
        this.firstName = firstName?.value ?: ""
        this.lastName = lastName?.value ?: ""
        isEnabled = true
        groups = listOf(USER_GROUP_NAME)
        credentials = listOf(credential)
    }

    suspend fun verify(userId: String) {
        log.info("Verifying user with id: {}", userId)
        try {
            withContext(ioDispatcher) {
                keycloakRealm.users()[userId].sendVerifyEmail()
            }
        } catch (_: WebApplicationException) {
            log.error("Error sending email verification to user with id: {}", userId)
        }
    }

    private fun String.sanitizeForLog(): String = this.replace("\n", "").replace("\r", "")

    companion object {
        private const val USER_GROUP_NAME = "Users"
        private const val HTTP_CREATED = 201
        private const val HTTP_CONFLICT = 409
        private val log = LoggerFactory.getLogger(KeycloakRepository::class.java)
    }
}
