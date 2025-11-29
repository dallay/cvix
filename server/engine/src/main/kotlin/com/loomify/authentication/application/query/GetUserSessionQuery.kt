package com.loomify.authentication.application.query

import com.loomify.authentication.domain.UserSession
import com.loomify.common.domain.bus.query.Query

data class GetUserSessionQuery(val accessToken: String) : Query<UserSession>
