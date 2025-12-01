package com.cvix.users.application.register

import com.cvix.UnitTest
import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.email.Email
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Test

@UnitTest
internal class RegisterUserCommandHandlerTest {
    private val faker = Faker()
    @Test
    fun `should map command to value objects and delegate to UserRegistrator`() = runTest {
        // Given
        val userRegistrator: UserRegistrator = mockk(relaxed = true)
        val handler = RegisterUserCommandHandler(userRegistrator)

        val emailValue = faker.internet().emailAddress()
        val password = Credential.generateRandomCredentialPassword()
        val firstname = faker.name().firstName()
        val lastname = faker.name().lastName()
        val expectedUserId = UUID.randomUUID()

        val command = RegisterUserCommand(
            email = emailValue,
            password = password,
            firstname = firstname,
            lastname = lastname,
        )

        // Stub for the specific expected email object to avoid MockK generating invalid placeholder emails
        coEvery {
            userRegistrator.registerNewUser(
                email = Email(emailValue),
                credential = any(),
                firstName = any(),
                lastName = any(),
            )
        } returns expectedUserId

        // When
        val actualUserId = handler.handle(command)

        // Then
        assertEquals(expectedUserId, actualUserId)

        // Verify invocation with concrete email instance (no dynamic matcher)
        coVerify(exactly = 1) {
            userRegistrator.registerNewUser(
                email = Email(emailValue),
                credential = any(),
                firstName = any(),
                lastName = any(),
            )
        }

        confirmVerified(userRegistrator)
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

        // When & Then - expect IllegalArgumentException from Email value object construction
        assertFailsWith<IllegalArgumentException> {
            handler.handle(command)
        }

        // No need for coVerify with any() (would trigger invalid placeholder generation). Just ensure no interactions.
        confirmVerified(userRegistrator)
    }
}
