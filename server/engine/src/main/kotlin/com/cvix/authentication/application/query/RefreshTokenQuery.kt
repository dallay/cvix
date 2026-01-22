package com.cvix.authentication.application.query

import com.cvix.common.domain.authentication.AccessToken
import com.cvix.common.domain.bus.query.Query
import java.util.UUID

/**
 *
 * @created 31/7/23
 */
data class RefreshTokenQuery(
    val id: UUID = UUID.randomUUID(),
    val refreshToken: String,
) : Query<AccessToken>
