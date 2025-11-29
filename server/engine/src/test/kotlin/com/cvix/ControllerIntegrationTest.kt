package com.cvix

import com.cvix.config.InfrastructureTestContainers
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ControllerIntegrationTest : InfrastructureTestContainers() {
    @Value("\${application.security.oauth2.issuer-uri}")
    protected lateinit var issuerUri: String

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @BeforeEach
    protected fun setUp() {
        this.webTestClient = configureCsrfAndJwt(this.webTestClient)
    }

    protected fun configureCsrfAndJwt(webTestClient: WebTestClient = this.webTestClient) =
        webTestClient.mutateWith(csrf())
            .mutateWith(mockAuthentication<SecurityMockServerConfigurers.JwtMutator>(jwt()))

    protected fun jwt(
        username: String = testUsername,
        password: String = testPassword
    ): JwtAuthenticationToken {
        val token = getAccessToken(testUsername, testPassword)?.token ?: "token"

        // Corrected type inference for JwtDecoder
        val jwtDecoder: NimbusJwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder
        val jwt = jwtDecoder.decode(token)

        // Corrected type inference for emptyList
        val authorities = (jwt.getClaimAsStringList("roles") ?: emptyList<String>())
            .map { SimpleGrantedAuthority(it) }

        // Create JwtAuthenticationToken
        return JwtAuthenticationToken(jwt, authorities as? Collection<GrantedAuthority>?)
    }
}
