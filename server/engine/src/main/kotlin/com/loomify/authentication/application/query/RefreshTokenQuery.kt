package com.loomify.authentication.application.query

import com.loomify.authentication.domain.AccessToken
import com.loomify.common.domain.bus.query.Query
import java.util.*

/**
 *
 * @created 31/7/23
 */
data class RefreshTokenQuery(
    val id: UUID = UUID.randomUUID(),
    val refreshToken: String,
) : Query<AccessToken>
