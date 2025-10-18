package com.loomify.engine.authentication.domain

import com.loomify.common.domain.bus.query.Response
import java.util.UUID

data class UserSession(
    val userId: UUID,
    val email: String,
    val roles: List<String>
) : Response
