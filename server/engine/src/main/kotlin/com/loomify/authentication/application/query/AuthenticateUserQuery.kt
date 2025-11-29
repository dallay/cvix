package com.loomify.authentication.application.query

import com.loomify.authentication.domain.AccessToken
import com.loomify.common.domain.bus.query.Query
import java.util.*

/**
 * Query to authenticate a user with email and password.
 *
 * @property id Unique identifier for this query.
 * @property email The user's email address.
 * @property password The user's password.
 * @property rememberMe Whether to extend the session duration (affects refresh token TTL).
 * @created 31/7/23
 */
data class AuthenticateUserQuery(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
) : Query<AccessToken>
