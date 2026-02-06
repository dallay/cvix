package com.cvix.identity.infrastructure.user.service

import com.cvix.UnitTest
import com.cvix.identity.domain.authentication.AuthoritiesConstants
import com.cvix.identity.infrastructure.user.persistence.entity.FederatedIdentityEntity
import com.cvix.identity.infrastructure.user.persistence.entity.UserEntity
import com.cvix.identity.infrastructure.user.persistence.repository.FederatedIdentityR2dbcRepository
import com.cvix.identity.infrastructure.user.persistence.repository.UserR2dbcRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@UnitTest
class AccountResourceServiceTest {

    private lateinit var accountResourceService: AccountResourceService
    private lateinit var userRepository: UserR2dbcRepository
    private lateinit var federatedIdentityRepository: FederatedIdentityR2dbcRepository

    private val testUserId = UUID.fromString("3e00cb23-a473-4bea-bf4c-9f20738fcacc")
    private val keycloakUserId = "241bb7c3-daf4-4bb2-b893-1ad631c56d5c"
    private val userDetails: MutableMap<String, Any> = mutableMapOf(
        "sub" to keycloakUserId, // Keycloak ID
        "preferred_username" to "test",
        "email" to "test@localhost",
        "given_name" to "Test",
        "family_name" to "User",
        "roles" to listOf(AuthoritiesConstants.USER),
    )

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        federatedIdentityRepository = mockk()
        accountResourceService = AccountResourceService(userRepository, federatedIdentityRepository)

        // Mock user repository
        coEvery { userRepository.findByEmail("test@localhost") } returns UserEntity(
            id = testUserId,
            email = "test@localhost",
            fullName = "Test User",
        )

        coEvery { userRepository.findById(testUserId) } returns UserEntity(
            id = testUserId,
            email = "test@localhost",
            fullName = "Test User",
        )
    }

    @Test
    fun `should get account information successfully by OAuth2AuthenticationToken with existing federated identity`() {
        // Mock: federated identity already exists
        coEvery {
            federatedIdentityRepository.findByProviderNameAndExternalUserId("oidc", keycloakUserId)
        } returns FederatedIdentityEntity(
            id = UUID.randomUUID(),
            userId = testUserId,
            providerName = "oidc",
            externalUserId = keycloakUserId,
            email = "test@localhost",
            displayName = "Test User",
        )

        val authenticationToken = createMockOAuth2AuthenticationToken(userDetails)
        val userResponse = accountResourceService.getAccount(authenticationToken).block()

        assertEquals(testUserId.toString(), userResponse?.id)
        assertEquals("test", userResponse?.username)
        assertEquals("test@localhost", userResponse?.email)
        assertEquals("Test", userResponse?.firstname)
        assertEquals("User", userResponse?.lastname)
        assertEquals(1, userResponse?.authorities?.size)
        assertEquals(AuthoritiesConstants.USER, userResponse?.authorities?.first())

        // Verify that federated identity was looked up
        coVerify {
            federatedIdentityRepository.findByProviderNameAndExternalUserId(
                "oidc",
                keycloakUserId,
            )
        }
    }

    @Test
    fun `should create federated identity on first login by OAuth2AuthenticationToken`() {
        // Mock: no federated identity exists yet
        coEvery {
            federatedIdentityRepository.findByProviderNameAndExternalUserId("oidc", keycloakUserId)
        } returns null

        // Mock: save new federated identity
        val savedIdentitySlot = slot<FederatedIdentityEntity>()
        coEvery { federatedIdentityRepository.save(capture(savedIdentitySlot)) } answers { savedIdentitySlot.captured }

        val authenticationToken = createMockOAuth2AuthenticationToken(userDetails)
        val userResponse = accountResourceService.getAccount(authenticationToken).block()

        assertEquals(testUserId.toString(), userResponse?.id)
        assertEquals("test", userResponse?.username)

        // Verify that federated identity was created
        coVerify { federatedIdentityRepository.save(any()) }

        val savedIdentity = savedIdentitySlot.captured
        assertEquals(testUserId, savedIdentity.userId)
        assertEquals("oidc", savedIdentity.providerName)
        assertEquals(keycloakUserId, savedIdentity.externalUserId)
        assertEquals("test@localhost", savedIdentity.email)
    }

    @Test
    fun `should get account information successfully with multiple roles by OAuth2AuthenticationToken`() {
        userDetails["roles"] = listOf(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)

        // Mock: federated identity exists
        coEvery {
            federatedIdentityRepository.findByProviderNameAndExternalUserId("oidc", keycloakUserId)
        } returns FederatedIdentityEntity(
            id = UUID.randomUUID(),
            userId = testUserId,
            providerName = "oidc",
            externalUserId = keycloakUserId,
            email = "test@localhost",
            displayName = "Test User",
        )

        val authenticationToken = createMockOAuth2AuthenticationToken(userDetails)
        val userResponse = accountResourceService.getAccount(authenticationToken).block()
        assertEquals(testUserId.toString(), userResponse?.id)
        assertEquals("test", userResponse?.username)
        assertEquals("test@localhost", userResponse?.email)
        assertEquals("Test", userResponse?.firstname)
        assertEquals("User", userResponse?.lastname)
        assertEquals(2, userResponse?.authorities?.size)
        assertTrue(userResponse?.authorities?.contains(AuthoritiesConstants.USER) ?: false)
        assertTrue(userResponse?.authorities?.contains(AuthoritiesConstants.ADMIN) ?: false)
    }

    @Test
    fun `should get account information successfully by JwtAuthenticationToken`() {
        // Mock: federated identity exists
        coEvery {
            federatedIdentityRepository.findByProviderNameAndExternalUserId("oidc", keycloakUserId)
        } returns FederatedIdentityEntity(
            id = UUID.randomUUID(),
            userId = testUserId,
            providerName = "oidc",
            externalUserId = keycloakUserId,
            email = "test@localhost",
            displayName = "Test User",
        )

        val authenticationToken = createMockJwtAuthenticationToken(userDetails)
        val userResponse = accountResourceService.getAccount(authenticationToken).block()
        assertEquals(testUserId.toString(), userResponse?.id)
        assertEquals("test", userResponse?.username)
        assertEquals("test@localhost", userResponse?.email)
        assertEquals("Test", userResponse?.firstname)
        assertEquals("User", userResponse?.lastname)
        assertEquals(1, userResponse?.authorities?.size)
        assertEquals(AuthoritiesConstants.USER, userResponse?.authorities?.first())
    }

    @Test
    fun `should get account information successfully with multiple roles by JwtAuthenticationToken`() {
        userDetails["roles"] = listOf(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)

        // Mock: federated identity exists
        coEvery {
            federatedIdentityRepository.findByProviderNameAndExternalUserId("oidc", keycloakUserId)
        } returns FederatedIdentityEntity(
            id = UUID.randomUUID(),
            userId = testUserId,
            providerName = "oidc",
            externalUserId = keycloakUserId,
            email = "test@localhost",
            displayName = "Test User",
        )

        val authenticationToken = createMockJwtAuthenticationToken(userDetails)
        val userResponse = accountResourceService.getAccount(authenticationToken).block()
        assertEquals(testUserId.toString(), userResponse?.id)
        assertEquals("test", userResponse?.username)
        assertEquals("test@localhost", userResponse?.email)
        assertEquals("Test", userResponse?.firstname)
        assertEquals("User", userResponse?.lastname)
        assertEquals(2, userResponse?.authorities?.size)
        assertTrue(userResponse?.authorities?.contains(AuthoritiesConstants.USER) ?: false)
        assertTrue(userResponse?.authorities?.contains(AuthoritiesConstants.ADMIN) ?: false)
    }

    @Test
    fun `should throw IllegalArgumentException when authentication token is not OAuth2 or JWT`() {
        val authenticationToken = UsernamePasswordAuthenticationToken("test", "test")
        assertThrows<IllegalArgumentException> {
            accountResourceService.getAccount(authenticationToken).block()
        }
    }

    private fun createMockOAuth2AuthenticationToken(userDetails: Map<String, Any>): OAuth2AuthenticationToken {
        val authorities: Collection<GrantedAuthority> =
            listOf<GrantedAuthority>(SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
        val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
            "anonymous",
            "anonymous",
            authorities,
        )
        usernamePasswordAuthenticationToken.details = userDetails
        val user: OAuth2User = DefaultOAuth2User(authorities, userDetails, "sub")
        return OAuth2AuthenticationToken(user, authorities, "oidc")
    }

    private fun createMockJwtAuthenticationToken(userDetails: Map<String, Any>): JwtAuthenticationToken {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", userDetails["sub"])
            .claim("preferred_username", userDetails["preferred_username"])
            .claim("email", userDetails["email"])
            .claim("given_name", userDetails["given_name"])
            .claim("family_name", userDetails["family_name"])
            .claim("roles", userDetails["roles"])
            .build()
        val authorities: Collection<GrantedAuthority> = AuthorityUtils.createAuthorityList(
            AuthoritiesConstants.ANONYMOUS,
        )
        return JwtAuthenticationToken(jwt, authorities)
    }
}
