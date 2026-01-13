package com.cvix

import com.cvix.config.InfrastructureTestContainers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Base class for controller integration tests.
 *
 * Uses Spring Boot 4's approach where WebTestClient is bound to the ApplicationContext
 * with Spring Security configured. This allows using SecurityMockServerConfigurers
 * like csrf() and mockJwt() for testing secured endpoints.
 *
 * Note: We use MOCK environment (not RANDOM_PORT) to enable security mock configurers.
 * For tests that need the actual HTTP layer, use the realJwt() method with a real token.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
abstract class ControllerIntegrationTest : InfrastructureTestContainers() {
    @Value("\${application.security.oauth2.issuer-uri}")
    protected lateinit var issuerUri: String

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    /**
     * Default test user UUID matching the test SQL fixtures (users.sql).
     * This user (test1@example.com) is set up with ROLE_ADMIN and ROLE_USER.
     */
    protected val testUserId: String = "efc4b2b8-08be-4020-93d5-f795762bf5c9"

    /**
     * WebTestClient bound to the ApplicationContext with Spring Security support.
     * Must be initialized lazily after @Autowired fields are set.
     *
     * The default mock JWT uses [testUserId] as subject (matching test SQL fixtures)
     * and [testUsername] as the email claim.
     */
    protected val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt { jwt ->
                        jwt.subject(testUserId)
                            .claim("preferred_username", testUsername)
                            .claim("email", testUsername)
                            .claim("roles", listOf("ROLE_USER"))
                    }
                    .authorities(SimpleGrantedAuthority("ROLE_USER")),
            )
    }

    /**
     * Creates a WebTestClient with custom JWT claims.
     * Use this when you need specific user claims for a test.
     *
     * @param subject The JWT subject (user ID as UUID string). Defaults to [testUserId].
     * @param username The preferred_username and email claim. Defaults to [testUsername].
     * @param roles The roles to include in the JWT. Defaults to ROLE_USER.
     */
    protected fun webTestClientWithJwt(
        subject: String = testUserId,
        username: String = testUsername,
        roles: List<String> = listOf("ROLE_USER"),
    ): WebTestClient =
        WebTestClient.bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt { jwt ->
                        jwt.subject(subject)
                            .claim("preferred_username", username)
                            .claim("email", username)
                            .claim("roles", roles)
                    }
                    .authorities(*roles.map { SimpleGrantedAuthority(it) }.toTypedArray()),
            )

    /**
     * Creates a real JwtAuthenticationToken by fetching an actual token from Keycloak.
     * Use this when you need to test with a real JWT (e.g., for token validation tests).
     */
    protected fun realJwt(
        username: String = testUsername,
        password: String = testPassword,
    ): JwtAuthenticationToken {
        val token = getAccessToken(username, password)?.token
            ?: error("Failed to obtain access token from Keycloak")

        val jwtDecoder: NimbusJwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder
        val jwt = jwtDecoder.decode(token)

        val authorities = (jwt.getClaimAsStringList("roles") ?: emptyList<String>())
            .map { SimpleGrantedAuthority(it) }

        return JwtAuthenticationToken(jwt, authorities)
    }
}
