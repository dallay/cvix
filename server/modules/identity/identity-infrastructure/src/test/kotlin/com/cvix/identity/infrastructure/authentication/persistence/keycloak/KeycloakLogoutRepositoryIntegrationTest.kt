package com.cvix.identity.infrastructure.authentication.persistence.keycloak

import com.cvix.config.InfrastructureTestContainers
import com.cvix.identity.domain.authentication.RefreshToken
import com.cvix.identity.domain.authentication.UserAuthenticatorLogout
import com.cvix.identity.domain.authentication.error.LogoutFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class KeycloakLogoutRepositoryIntegrationTest : InfrastructureTestContainers() {

    @Autowired
    private lateinit var userAuthenticatorLogout: UserAuthenticatorLogout

    @BeforeEach
    fun setUp() {
        startInfrastructure()
    }

    @Test
    fun logout(): Unit = runTest {
        val accessToken = withContext(Dispatchers.IO) { getAccessToken() }
        val refreshToken = RefreshToken(accessToken?.refreshToken ?: "fake refresh token")
        userAuthenticatorLogout.logout(refreshToken.value)
    }

    @Test
    fun `logout should handle Invalid Token`() = runTest {
        val invalidToken = "invalid_token"
        try {
            userAuthenticatorLogout.logout(invalidToken)
        } catch (e: Exception) {
            assert(e is LogoutFailedException) { "Could not log out user" }
        }
    }
}
