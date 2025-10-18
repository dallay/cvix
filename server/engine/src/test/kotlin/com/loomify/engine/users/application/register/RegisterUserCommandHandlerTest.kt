package com.loomify.engine.users.application.register

import com.loomify.UnitTest
import com.loomify.common.domain.error.EmailNotValidException
import com.loomify.common.domain.vo.credential.Credential
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Test

@UnitTest
class RegisterUserCommandHandlerTest {

    private val faker = Faker()

    @Test
    fun `should map command to value objects and delegate to UserRegistrator`() = runTest {
        // Given
        val userRegistrator: UserRegistrator = mockk(relaxed = true)
        val handler = RegisterUserCommandHandler(userRegistrator)

        val email = faker.internet().emailAddress()
        val password = Credential.generateRandomCredentialPassword()
        val firstname = faker.name().firstName()
        val lastname = faker.name().lastName()
        val expectedUserId = UUID.randomUUID()

        val command = RegisterUserCommand(
            email = email,
            password = password,
            firstname = firstname,
            lastname = lastname,
        )

        // Mock the registrator to return a UUID
        coEvery {
            userRegistrator.registerNewUser(
                any(),
                any(),
                any(),
                any(),
            )
        } returns expectedUserId

        // When
        val actualUserId = handler.handle(command)

        // Then - verify the returned UUID
        assertEquals(expectedUserId, actualUserId)

        // Verify that the registrator is invoked with correctly mapped VOs
        coVerify(exactly = 1) {
            userRegistrator.registerNewUser(
                email = match { it.value == email },
                credential = match { it.credentialValue == password },
                firstName = match { it?.value == firstname },
                lastName = match { it?.value == lastname },
            )
        }
    }

    @Test
    fun `should not delegate and throw when command has invalid email`() = runTest {
        // Given
        val userRegistrator: UserRegistrator = mockk(relaxed = true)
        val handler = RegisterUserCommandHandler(userRegistrator)

        val invalidEmail = "invalid-email-without-at"
        val password = Credential.generateRandomCredentialPassword()
        val firstname = "John"
        val lastname = "Doe"

        val command = RegisterUserCommand(
            email = invalidEmail,
            password = password,
            firstname = firstname,
            lastname = lastname,
        )

        // When & Then - Email value object should fail validation before delegating
        assertFailsWith<EmailNotValidException> {
            handler.handle(command)
        }

        // Ensure no delegation happened
        coVerify(exactly = 0) { userRegistrator.registerNewUser(any(), any(), any(), any()) }
    }
}
