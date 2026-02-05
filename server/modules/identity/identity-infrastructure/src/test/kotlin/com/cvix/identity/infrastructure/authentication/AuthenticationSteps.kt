package com.cvix.identity.infrastructure.authentication

import com.cvix.identity.domain.authentication.Role
import com.cvix.identity.infrastructure.config.CucumberAuthenticationConfiguration
import io.cucumber.java.en.Given
import io.jsonwebtoken.Jwts
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions

class AuthenticationSteps(
    private var webTestClient: WebTestClient
) {

    private val users: Map<String, User> = UsersBuilder()
        .add("admin", Role.ADMIN)
        .add("user", Role.USER)
        .build()

    @Given("I am logged in as {string}")
    fun authenticateUser(username: String) {
        val userToAuthenticate =
            users[username] ?: throw IllegalArgumentException(unknownUserMessage(username))

        val token = userToAuthenticate.token()

        webTestClient = webTestClient.mutate()
            .filter(
                ExchangeFilterFunctions
                    .basicAuthentication(username, token),
            )
            .build()
    }

    private fun unknownUserMessage(user: String): String =
        "Trying to authenticate an unknown user: $user"

    @Given("I am logged out")
    fun logout() {
        webTestClient = webTestClient.mutate()
            .filter(
                ExchangeFilterFunctions
                    .basicAuthentication("", ""),
            )
            .defaultHeader(HttpHeaders.AUTHORIZATION, "")
            .build()
    }

    private class UsersBuilder {
        private val users: MutableMap<String, User> = ConcurrentHashMap()

        fun add(username: String, vararg roles: Role): UsersBuilder {
            users[username] = User(username, roles)
            return this
        }

        fun build(): Map<String, User> = Collections.unmodifiableMap(users)
    }

    private class User(username: String, roles: Array<out Role>) {
        private val claims: Map<String, Any> = mapOf(
            "preferred_username" to username,
            "roles" to roles.map { it.key() },
        )

        fun token(): String {
            return Jwts.builder()
                .subject("authentication")
                .signWith(CucumberAuthenticationConfiguration.JWT_KEY)
                .claims(claims)
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .compact()
        }
    }
}
