package com.cvix.identity.infrastructure.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.time.Instant
import javax.crypto.SecretKey
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

@TestConfiguration
class CucumberAuthenticationConfiguration {
    @Bean
    @Primary
    fun jwtDecoder(): ReactiveJwtDecoder {
        val decoder = Jwts.parser().verifyWith(JWT_KEY).build()
        return ReactiveJwtDecoder { token: String? ->
            Mono.just(
                Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(120),
                    mapOf("issuer" to "http://dev"),
                    decoder.parseSignedClaims(token).payload,
                ),
            )
        }
    }

    companion object {
        val JWT_KEY: SecretKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode("OTdhNzE2OTQwNWVmYmZhOWRiOTA4MzI2ZDRmNDg1NzMwNDlhNDZmMQ=="),
        )
    }
}
