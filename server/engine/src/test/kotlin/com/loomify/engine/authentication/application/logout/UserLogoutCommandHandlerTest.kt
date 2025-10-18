package com.loomify.engine.authentication.application.logout

import com.loomify.UnitTest
import com.loomify.engine.authentication.domain.UserAuthenticatorLogout
import com.loomify.engine.authentication.domain.error.LogoutFailedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class UserLogoutCommandHandlerTest {

    private lateinit var userLogoutService: UserLogoutService
    private val userAuthenticatorLogout: UserAuthenticatorLogout = mockk()
    private lateinit var userLogoutCommandHandler: UserLogoutCommandHandler

    @BeforeEach
    fun setUp() {
        coEvery { userAuthenticatorLogout.logout(any()) } returns Unit
        userLogoutService = UserLogoutService(userAuthenticatorLogout)
        userLogoutCommandHandler = UserLogoutCommandHandler(userLogoutService)
    }

    @Test
    fun `handle LogsOut User Successfully`(): Unit = runTest {
        val refreshToken = "valid_refresh_token"
        val command = UserLogoutCommand(refreshToken)

        userLogoutCommandHandler.handle(command)

        coVerify { userAuthenticatorLogout.logout(refreshToken) }
    }

    @Test
    fun `handle Throws Exception When LogoutFails`(): Unit = runTest {
        val refreshToken = "invalid_refresh_token"
        val command = UserLogoutCommand(refreshToken)

        coEvery {
            userAuthenticatorLogout.logout(refreshToken)
        } throws LogoutFailedException("Could not log out user", RuntimeException())

        assertThrows<LogoutFailedException> {
            userLogoutCommandHandler.handle(command)
        }

        coVerify { userAuthenticatorLogout.logout(refreshToken) }
    }
}
