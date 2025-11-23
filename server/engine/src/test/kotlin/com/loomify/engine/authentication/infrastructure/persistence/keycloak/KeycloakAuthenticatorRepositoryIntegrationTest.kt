package com.loomify.engine.authentication.infrastructure.persistence.keycloak

import com.loomify.IntegrationTest
import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.email.Email
import com.loomify.common.domain.vo.name.FirstName
import com.loomify.common.domain.vo.name.LastName
import com.loomify.engine.CredentialGenerator
import com.loomify.engine.authentication.domain.UserAuthenticationException
import com.loomify.engine.authentication.domain.UserAuthenticator
import com.loomify.engine.authentication.domain.Username
import com.loomify.engine.config.InfrastructureTestContainers
import com.loomify.engine.users.domain.UserCreator
import io.kotest.common.runBlocking
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@IntegrationTest
class KeycloakAuthenticatorRepositoryIntegrationTest : InfrastructureTestContainers() {
    // this user is created by default in Keycloak container (see demo-realm-test.json)
    private val email = "john.doe@loomify.com"
    private val username = "john.doe"
    private val password = "S3cr3tP@ssw0rd*123"

    private val faker = Faker()

    @Autowired
    private lateinit var userCreator: UserCreator

    @Autowired
    private lateinit var userAuthenticator: UserAuthenticator

    @BeforeEach
    fun setUp() {
        startInfrastructure()
    }

    @Test
    fun `should authenticate a user by email`(): Unit = runBlocking {
        val username = Username(email)
        val credential = CredentialGenerator.generate(password)
        val accessToken = userAuthenticator.authenticate(username, credential)
        assertNotNull(accessToken)
        assertNotNull(accessToken.token)
        assertNotNull(accessToken.refreshToken)
        assertNotNull(accessToken.expiresIn)
        assertNotNull(accessToken.refreshExpiresIn)
        assertNotNull(accessToken.tokenType)
        assertNotNull(accessToken.scope)
        assertNotNull(accessToken.notBeforePolicy)
        assertNotNull(accessToken.sessionState)
    }

    @Test
    fun `should authenticate a user by username`(): Unit = runBlocking {
        val username = Username(username)
        val credential = CredentialGenerator.generate(password)
        val accessToken = userAuthenticator.authenticate(username, credential)
        assertNotNull(accessToken)
        assertNotNull(accessToken.token)
        assertNotNull(accessToken.refreshToken)
        assertNotNull(accessToken.expiresIn)
        assertNotNull(accessToken.refreshExpiresIn)
        assertNotNull(accessToken.tokenType)
        assertNotNull(accessToken.scope)
        assertNotNull(accessToken.notBeforePolicy)
        assertNotNull(accessToken.sessionState)
    }

    @Test
    fun `should throw exception when user is not found`(): Unit = runBlocking {
        val username = Username("not.found")
        val credential = CredentialGenerator.generate(password)
        val exception = assertThrows(UserAuthenticationException::class.java) {
            runBlocking {
                userAuthenticator.authenticate(username, credential)
            }
        }
        assertEquals("Invalid account. User probably hasn't verified email.", exception.message)
    }

    @Test
    fun `should throw exception when password is wrong`(): Unit = runBlocking {
        val username = Username(email)
        val credential = CredentialGenerator.generate("${password}wrong")
        val exception = assertThrows(UserAuthenticationException::class.java) {
            runBlocking {
                userAuthenticator.authenticate(username, credential)
            }
        }
        assertEquals("Invalid account. User probably hasn't verified email.", exception.message)
    }

    @Test
    fun `should throw exception when user is not verified`(): Unit = runBlocking {
        val randomEmail = faker.internet().emailAddress()
        val randomPassword = faker.credentials().password(8, 80, true, true, true) + "1Aa@"
        val createdUser = userCreator.create(
            Email(randomEmail), Credential.create(randomPassword),
            FirstName(faker.name().firstName()), LastName(faker.name().lastName()),
        )
        assertNotNull(createdUser)
        val username = Username(randomEmail)
        val credential = CredentialGenerator.generate(randomPassword)
        val exception = assertThrows(UserAuthenticationException::class.java) {
            runBlocking {
                userAuthenticator.authenticate(username, credential)
            }
        }
        assertEquals("Invalid account. User probably hasn't verified email.", exception.message)
    }
}
