package com.cvix.authentication.infrastructure.keycloak

import com.cvix.authentication.domain.Role
import com.cvix.users.infrastructure.persistence.UserStoreR2dbcRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

@DisplayName("Keycloak Federated Auth Service Tests")
class KeycloakFederatedAuthServiceTest {
    private lateinit var userStoreRepository: UserStoreR2dbcRepository
    private lateinit var service: KeycloakFederatedAuthService

    @BeforeEach
    fun setup() {
        userStoreRepository = mockk()
        service = KeycloakFederatedAuthService(userStoreRepository)
    }

    @Nested
    @DisplayName("Find or Create User")
    inner class FindOrCreateUser {
        @Test
        fun `should create new user when no existing user found`() {
            // Given
            val provider = "google"
            val externalUserId = "google-123"
            val email = "newuser@example.com"
            val firstName = "New"
            val lastName = "User"
            val displayName = "New User"

            coEvery { userStoreRepository.create(any(), email, displayName) } returns Unit

            // When
            val result = service.findOrCreateUser(
                provider, externalUserId, email, firstName, lastName, displayName,
            )

            // Then
            StepVerifier.create(result)
                .expectNextMatches { user ->
                    user.email == email &&
                        user.firstName == firstName &&
                        user.lastName == lastName &&
                        user.displayName == displayName &&
                        user.roles.contains(Role.USER)
                }
                .verifyComplete()

            runBlocking {
                coVerify { userStoreRepository.create(any(), email, displayName) }
            }
        }

        @Test
        fun `should create user with empty displayName fallback`() {
            // Given
            val provider = "google"
            val externalUserId = "google-456"
            val email = "existing@example.com"
            val firstName = "Existing"
            val lastName = "User"
            val displayName = ""

            coEvery { userStoreRepository.create(any(), email, "Existing User") } returns Unit

            // When
            val result = service.findOrCreateUser(
                provider, externalUserId, email, firstName, lastName, displayName,
            )

            // Then
            StepVerifier.create(result)
                .expectNextMatches { user ->
                    user.email == email &&
                        user.firstName == firstName &&
                        user.lastName == lastName &&
                        user.displayName == "Existing User"
                }
                .verifyComplete()

            runBlocking {
                coVerify { userStoreRepository.create(any(), email, "Existing User") }
            }
        }
    }

    @Nested
    @DisplayName("Link Federated Identity")
    inner class LinkFederatedIdentity {
        @Test
        fun `should create federated identity link`() {
            // Given
            val userId = UUID.randomUUID()
            val provider = "microsoft"
            val externalUserId = "microsoft-123"
            val email = "user@example.com"

            // When
            val result = service.linkFederatedIdentity(userId, provider, externalUserId, email)

            // Then
            StepVerifier.create(result)
                .expectNextMatches { identity ->
                    identity.userId == userId &&
                        identity.provider == provider &&
                        identity.externalUserId == externalUserId &&
                        identity.email == email
                }
                .verifyComplete()
        }
    }

    @Nested
    @DisplayName("Find User by Federated Identity")
    inner class FindUserByFederatedIdentity {
        @Test
        fun `should return empty when federated identity lookup not implemented`() {
            // Given
            val provider = "google"
            val externalUserId = "google-999"

            // When
            val result = service.findUserByFederatedIdentity(provider, externalUserId)

            // Then
            StepVerifier.create(result)
                .verifyComplete()
        }
    }

    @Nested
    @DisplayName("Unlink Federated Identity")
    inner class UnlinkFederatedIdentity {
        @Test
        fun `should complete without error`() {
            // Given
            val userId = UUID.randomUUID()
            val provider = "github"

            // When
            val result = service.unlinkFederatedIdentity(userId, provider)

            // Then
            StepVerifier.create(result)
                .verifyComplete()
        }
    }
}
