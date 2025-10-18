package com.loomify.engine.authentication.application.query

import com.loomify.common.domain.bus.query.Query
import com.loomify.engine.authentication.domain.AccessToken
import java.util.*

/**
 *
 * @created 31/7/23
 */
data class AuthenticateUserQuery(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String
) : Query<AccessToken>
