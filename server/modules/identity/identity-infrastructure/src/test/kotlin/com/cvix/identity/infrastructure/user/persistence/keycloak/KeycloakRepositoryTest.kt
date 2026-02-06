package com.cvix.identity.infrastructure.user.persistence.keycloak

import com.cvix.CredentialGenerator
import com.cvix.UnitTest
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.name.FirstName
import com.cvix.common.domain.vo.name.LastName
import com.cvix.identity.domain.user.UserStoreException
import com.cvix.identity.infrastructure.authentication.ApplicationSecurityProperties
import com.cvix.identity.infrastructure.user.persistence.UserStoreR2dbcRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import java.net.URI
import java.util.*
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource

private const val REALM = "realm"
private const val SERVER_URL = "http://localhost/auth"
private const val CLIENT_ID = "client"
private const val CLIENT_SECRET = "secret"

@UnitTest
class KeycloakRepositoryTest {
    private val faker = Faker()
    private lateinit var applicationSecurityProperties: ApplicationSecurityProperties
    private lateinit var keycloak: Keycloak
    private lateinit var keycloakRealm: RealmResource
    private lateinit var keycloakUserResource: UsersResource
    private lateinit var userResource: UserResource
    private lateinit var keycloakRepository: KeycloakRepository
    private lateinit var userStoreR2dbcRepository: UserStoreR2dbcRepository

    @BeforeEach
    fun setUp() {
        applicationSecurityProperties = mockk<ApplicationSecurityProperties>()
        every { applicationSecurityProperties.oauth2.realm } returns REALM
        every { applicationSecurityProperties.oauth2.serverUrl } returns SERVER_URL
        every { applicationSecurityProperties.oauth2.clientId } returns CLIENT_ID
        every { applicationSecurityProperties.oauth2.clientSecret } returns CLIENT_SECRET

        keycloak = mockk()
        keycloakRealm = mockk()
        keycloakUserResource = mockk()
        userResource = mockk()

        every { keycloak.realm(REALM) } returns keycloakRealm
        every { keycloakRealm.users() } returns keycloakUserResource

        userStoreR2dbcRepository = mockk()
        coEvery { userStoreR2dbcRepository.create(any(), any(), any()) } returns Unit

        keycloakRepository = KeycloakRepository(
            applicationSecurityProperties,
            keycloak,
            userStoreR2dbcRepository,
        )
    }

    @Test
    fun `should create user successfully when user does not exist`() = runTest {
        // Given
        val email = Email(faker.internet().emailAddress())
        val credential = CredentialGenerator.generate()
        val firstName = FirstName(faker.name().firstName())
        val lastName = LastName(faker.name().lastName())
        val userId = faker.internet().uuid()

        // Mock successful creation
        val response = mockk<Response>()
        val location = URI.create("/auth/admin/realms/$REALM/users/$userId")
        every { response.location } returns location
        every { response.status } returns 201
        every { response.close() } just Runs
        every { keycloakUserResource.create(any()) } returns response

        // When
        val result = keycloakRepository.create(email, credential, firstName, lastName)

        // Then
        result shouldNotBe null
        result.email.value shouldBe email.value
        result.name?.firstName?.value shouldBe firstName.value
        result.name?.lastName?.value shouldBe lastName.value

        // Verify that userStoreR2dbcRepository.create was called with correct parameters
        coVerify(exactly = 1) {
            userStoreR2dbcRepository.create(
                any(),
                email.value,
                "${firstName.value} ${lastName.value}",
            )
        }

        // Additional verification to ensure userId is not blank
        coVerify {
            userStoreR2dbcRepository.create(
                match<UUID> { it.toString().isNotBlank() },
                any(),
                any(),
            )
        }

        // Verify that keycloakUserResource.create was called with correct UserRepresentation
        verify(exactly = 1) { keycloak.realm(REALM) }
        verify(exactly = 1) { keycloakRealm.users() }
        verify(exactly = 1) {
            keycloakUserResource.create(
                match { userRep ->
                    userRep.email == email.value &&
                        userRep.username == email.value &&
                        userRep.isEnabled == true &&
                        userRep.credentials?.isNotEmpty() == true
                },
            )
        }

        // Verify that no pre-existence checks are performed
        verify(exactly = 0) { keycloakUserResource.searchByEmail(any(), any()) }
        verify(exactly = 0) { keycloakUserResource.searchByUsername(any(), any()) }
    }

    @Test
    fun `should create user successfully with null first and last name`() = runTest {
        // Given
        val email = Email(faker.internet().emailAddress())
        val credential = CredentialGenerator.generate()
        val userId = faker.internet().uuid()

        // Mock successful creation
        val response = mockk<Response>()
        val location = URI.create("/auth/admin/realms/$REALM/users/$userId")
        every { response.location } returns location
        every { response.status } returns 201
        every { response.close() } just Runs
        every { keycloakUserResource.create(any()) } returns response

        // When
        val result = keycloakRepository.create(email, credential, null, null)

        // Then
        result shouldNotBe null
        result.email.value shouldBe email.value
        result.name?.firstName?.value shouldBe null
        result.name?.lastName?.value shouldBe null

        verify { keycloakUserResource.create(any()) }
        // Verify that response.close() was called to ensure resource cleanup
        verify(exactly = 1) { response.close() }
        // Verify that no pre-existence checks are performed
        verify(exactly = 0) { keycloakUserResource.searchByEmail(any(), any()) }
        verify(exactly = 0) { keycloakUserResource.searchByUsername(any(), any()) }
    }

    @Test
    fun `should throw UserStoreException when user already exists (409 conflict)`() = runTest {
        // Given
        val email = Email(faker.internet().emailAddress())
        val credential = CredentialGenerator.generate()
        val firstName = FirstName(faker.name().firstName())
        val lastName = LastName(faker.name().lastName())

        // Mock conflict response from Keycloak
        val response = mockk<Response>()
        every { response.status } returns 409
        every { response.close() } just Runs
        every { keycloakUserResource.create(any()) } returns response

        // When & Then
        val exception = assertThrows<UserStoreException> {
            keycloakRepository.create(email, credential, firstName, lastName)
        }

        exception.message shouldContain "already exists"
        verify { keycloakUserResource.create(any()) }
        // Verify that no pre-existence checks are performed
        verify(exactly = 0) { keycloakUserResource.searchByEmail(any(), any()) }
        verify(exactly = 0) { keycloakUserResource.searchByUsername(any(), any()) }
        // Verify that no local DB writes occur
        coVerify(exactly = 0) { userStoreR2dbcRepository.create(any(), any(), any()) }
    }

    @Test
    fun `should throw UserStoreException when Keycloak returns client error`() = runTest {
        // Given
        val email = Email(faker.internet().emailAddress())
        val credential = CredentialGenerator.generate()
        val firstName = FirstName(faker.name().firstName())
        val lastName = LastName(faker.name().lastName())

        // Mock Keycloak error
        val clientError = ClientErrorException("Bad Request", 400)
        every { keycloakUserResource.create(any()) } throws clientError

        // When & Then
        val exception = assertThrows<UserStoreException> {
            keycloakRepository.create(email, credential, firstName, lastName)
        }

        exception.message shouldContain "Error creating user"
        exception.cause shouldBe clientError
        // Verify that no local DB writes occur
        coVerify(exactly = 0) { userStoreR2dbcRepository.create(any(), any(), any()) }
    }

    @Test
    fun `should throw UserStoreException when Keycloak returns server error`() = runTest {
        // Given
        val email = Email(faker.internet().emailAddress())
        val credential = CredentialGenerator.generate()
        val firstName = FirstName(faker.name().firstName())
        val lastName = LastName(faker.name().lastName())

        // Mock Keycloak error with complete Response mock
        val response = mockk<Response>()
        val statusInfo = mockk<Response.StatusType>()
        every { response.status } returns 500
        every { response.statusInfo } returns statusInfo
        every { statusInfo.family } returns Response.Status.Family.SERVER_ERROR
        every { statusInfo.statusCode } returns 500
        every { statusInfo.reasonPhrase } returns "Internal Server Error"
        every { response.close() } just Runs

        val serverError = InternalServerErrorException("Internal Server Error", response)
        every { keycloakUserResource.create(any()) } throws serverError

        // When & Then
        val exception = assertThrows<UserStoreException> {
            keycloakRepository.create(email, credential, firstName, lastName)
        }

        exception.message shouldContain "Error creating user"
        exception.cause shouldBe serverError
        // Verify that no local DB writes occur
        coVerify(exactly = 0) { userStoreR2dbcRepository.create(any(), any(), any()) }
    }

    @Test
    fun `should send verification email successfully`() = runTest {
        // Given
        val userId = faker.internet().uuid()
        every { keycloakUserResource.get(userId) } returns userResource
        every { userResource.sendVerifyEmail() } just Runs

        // When
        keycloakRepository.verify(userId)

        // Then
        verify { keycloakUserResource.get(userId) }
        verify { userResource.sendVerifyEmail() }
    }

    @Test
    fun `should handle WebApplicationException gracefully when sending verification email`() =
        runTest {
            // Given
            val userId = faker.internet().uuid()
            every { keycloakUserResource.get(userId) } returns userResource
            every { userResource.sendVerifyEmail() } throws WebApplicationException(
                "Email service unavailable",
                503,
            )

            // When & Then (should not throw exception)
            keycloakRepository.verify(userId)

            // Verify the method was called despite the exception
            verify { keycloakUserResource.get(userId) }
            verify { userResource.sendVerifyEmail() }
        }
}
