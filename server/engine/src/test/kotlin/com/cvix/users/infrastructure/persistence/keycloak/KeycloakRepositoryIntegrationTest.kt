package com.cvix.users.infrastructure.persistence.keycloak

import com.cvix.CredentialGenerator
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.name.FirstName
import com.cvix.common.domain.vo.name.LastName
import com.cvix.config.InfrastructureTestContainers
import com.cvix.users.domain.UserCreator
import com.cvix.users.domain.UserStoreException
import com.cvix.users.infrastructure.persistence.repository.UserR2dbcRepository
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class KeycloakRepositoryIntegrationTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var userCreator: UserCreator

    @Autowired
    private lateinit var userR2dbcRepository: UserR2dbcRepository

    private val faker = Faker()

    @Test
    fun `should create a new user`() = runTest {
        val email = faker.internet().emailAddress()
        val firstName = faker.name().firstName()
        val lastName = faker.name().lastName()

        val createdUser = userCreator.create(
            Email(email),
            CredentialGenerator.generate(), FirstName(firstName),
            LastName(lastName),
        )
        assertNotNull(createdUser)
        assertEquals(createdUser.email.value, email)
        assertEquals(createdUser.username.value, email)
        assertEquals(createdUser.name?.firstName?.value ?: "", firstName ?: "")
        assertEquals(createdUser.name?.lastName?.value ?: "", lastName ?: "")

        // Assert local users row exists
        val found = userR2dbcRepository.findById(createdUser.id.id)
        assertNotNull(found, "Local users row should be created after Keycloak registration")
        assertEquals(createdUser.id.id, found!!.id, "Found user's ID should match created user's ID")
        assertEquals(email, found.email)
        assertEquals("$firstName $lastName".trim(), found.fullName)
    }

    @Test
    fun `should not create a new user with an existing email`() = runTest {
        val email = faker.internet().emailAddress()
        val firstName = faker.name().firstName()
        val lastName = faker.name().lastName()

        val createdUser = userCreator.create(
            Email(email),
            CredentialGenerator.generate(), FirstName(firstName),
            LastName(lastName),
        )
        assertNotNull(createdUser)
        assertEquals(createdUser.email.value, email)
        assertEquals(createdUser.username.value, email)
        assertEquals(createdUser.name?.firstName?.value ?: "", firstName ?: "")
        assertEquals(createdUser.name?.lastName?.value ?: "", lastName ?: "")

        // Act & Assert (try to create duplicate user)
        val exception = assertThrows<UserStoreException> {
            userCreator.create(
                Email(email),
                CredentialGenerator.generate(), FirstName(faker.name().firstName()),
                LastName(faker.name().lastName()),
            )
        }
        assertEquals(
            "User with email: $email or username: $email already exists.",
            exception.message,
        )
    }
}
