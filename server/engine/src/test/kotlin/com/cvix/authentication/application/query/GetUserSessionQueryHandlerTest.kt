package com.cvix.authentication.application.query

import com.cvix.UnitTest
import com.cvix.authentication.domain.Role
import com.cvix.authentication.domain.error.InvalidTokenException
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

@UnitTest
internal class GetUserSessionQueryHandlerTest {

    private lateinit var reactiveJwtDecoder: ReactiveJwtDecoder
    private lateinit var handler: GetUserSessionQueryHandler

    @BeforeEach
    fun setUp() {
        reactiveJwtDecoder = mockk()
        handler = GetUserSessionQueryHandler(reactiveJwtDecoder)
    }

    @Test
    @DisplayName("should return session response for a valid access token")
    fun `should return session response for a valid access token`() = runTest {
        val accessToken = "valid-jwt"
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val roles = setOf(Role.USER)

        val claims = mapOf(
            "sub" to userId.toString(),
            "email" to email,
            "roles" to roles.map { it.key() },
        )
        val jwt = Jwt(
            accessToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            claims,
        )

        every { reactiveJwtDecoder.decode(accessToken) } returns Mono.just(jwt)

        val result = handler.handle(GetUserSessionQuery(accessToken))

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(roles.map { it.key() }, result.roles)
    }

    @Test
    @DisplayName("should throw InvalidTokenException when access token is invalid")
    fun `should throw InvalidTokenException when access token is invalid`() = runTest {
        val invalidAccessToken = "invalid-jwt"

        every { reactiveJwtDecoder.decode(invalidAccessToken) } returns Mono.error(JwtException("Invalid JWT"))

        val exception = assertThrows<InvalidTokenException> {
            handler.handle(GetUserSessionQuery(invalidAccessToken))
        }

        assertEquals("JWT decoding failed", exception.message)
    }

    @Test
    @DisplayName("should throw InvalidTokenException when sub claim is missing")
    fun `should throw InvalidTokenException when sub Claim Is Missing`() = runTest {
        val accessToken = "jwt-missing-sub"
        val claims = mapOf(
            "email" to "test@example.com",
            "roles" to listOf("USER"),
        )
        val jwt = Jwt(
            accessToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            claims,
        )
        every { reactiveJwtDecoder.decode(accessToken) } returns Mono.just(jwt)

        val exception = assertThrows<InvalidTokenException> {
            handler.handle(GetUserSessionQuery(accessToken))
        }
        assertEquals("Unexpected error during token processing", exception.message)
    }

    @Test
    @DisplayName("should throw InvalidTokenException when sub claim is not a valid UUID")
    fun `should throw InvalidTokenException when sub claim is not a valid UUID`() = runTest {
        val accessToken = "jwt-invalid-sub"
        val claims = mapOf(
            "sub" to "not-a-uuid",
            "email" to "test@example.com",
            "roles" to listOf("USER"),
        )
        val jwt = Jwt(
            accessToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            claims,
        )
        every { reactiveJwtDecoder.decode(accessToken) } returns Mono.just(jwt)

        val exception = assertThrows<InvalidTokenException> {
            handler.handle(GetUserSessionQuery(accessToken))
        }
        assertEquals("Invalid UUID format in token subject", exception.message)
    }

    @Test
    @DisplayName("should return session with empty roles when roles claim is missing")
    fun `should return session with empty roles when roles claim is missing`() = runTest {
        val accessToken = "jwt-missing-roles"
        val userId = UUID.randomUUID()
        val claims = mapOf(
            "sub" to userId.toString(),
            "email" to "test@example.com",
            // No "roles" claim
        )
        val jwt = Jwt(
            accessToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            claims,
        )
        every { reactiveJwtDecoder.decode(accessToken) } returns Mono.just(jwt)

        val result = handler.handle(GetUserSessionQuery(accessToken))

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals("test@example.com", result.email)
        assertEquals(emptyList<String>(), result.roles)
    }
}
