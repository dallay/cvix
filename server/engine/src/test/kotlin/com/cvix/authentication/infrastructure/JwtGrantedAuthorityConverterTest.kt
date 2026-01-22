package com.cvix.authentication.infrastructure

import com.cvix.UnitTest
import com.cvix.authentication.domain.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms
import org.springframework.security.oauth2.jwt.Jwt

private const val SUBJECT = "cvix"

@UnitTest
internal class JwtGrantedAuthorityConverterTest {
    private lateinit var jwtGrantedAuthorityConverter: JwtGrantedAuthorityConverter

    @BeforeEach
    fun setUp() {
        jwtGrantedAuthorityConverter = JwtGrantedAuthorityConverter()
    }

    @Test
    fun shouldConvert() {
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", JwsAlgorithms.RS256)
            .subject(SUBJECT)
            .claim("roles", listOf("ROLE_ADMIN"))
            .build()
        val result = jwtGrantedAuthorityConverter.convert(jwt)
        assertThat(result).containsExactly(SimpleGrantedAuthority(Role.ADMIN.key()))
    }

    @Test
    fun shouldConvertButEmpty() {
        val jwt =
            Jwt.withTokenValue("token").header("alg", JwsAlgorithms.RS256).subject(SUBJECT).build()
        val result = jwtGrantedAuthorityConverter.convert(jwt)
        assertThat(result).isEmpty()
    }
}
