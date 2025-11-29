package com.loomify.authentication.infrastructure.persistence.keycloak

import com.loomify.IntegrationTest
import com.loomify.authentication.domain.RefreshToken
import com.loomify.authentication.domain.UserAuthenticatorLogout
import com.loomify.authentication.domain.error.LogoutFailedException
import com.loomify.config.InfrastructureTestContainers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@IntegrationTest
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
