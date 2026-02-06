package com.cvix.identity.infrastructure.authentication.persistence.keycloak

import com.cvix.common.domain.Service
import com.cvix.identity.domain.authentication.UserAuthenticatorLogout
import com.cvix.identity.domain.authentication.error.LogoutFailedException
import com.cvix.identity.infrastructure.authentication.ApplicationSecurityProperties
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Service
class KeycloakLogoutRepository(
    private val applicationSecurityProperties: ApplicationSecurityProperties,
    private val webClient: WebClient = WebClient.builder().build()
) : UserAuthenticatorLogout {
    private val logoutURI = "${applicationSecurityProperties.oauth2.serverUrl.removeSuffix("/")}/realms/" +
        "${applicationSecurityProperties.oauth2.realm}/protocol/openid-connect/logout"

    @Suppress("TooGenericExceptionCaught")
    override suspend fun logout(refreshToken: String) {
        log.debug("Logging out user")
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("client_id", applicationSecurityProperties.oauth2.clientId)
        formData.add("refresh_token", refreshToken)

        try {
            webClient.post()
                .uri(logoutURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .awaitBodyOrNull<Void>()
        } catch (e: Exception) {
            log.error("Could not log out user", e)
            throw LogoutFailedException("Could not log out user", e)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(KeycloakLogoutRepository::class.java)
    }
}
