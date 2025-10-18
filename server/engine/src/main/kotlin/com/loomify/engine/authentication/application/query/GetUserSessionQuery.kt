package com.loomify.engine.authentication.application.query

import com.loomify.common.domain.bus.query.Query
import com.loomify.engine.authentication.domain.UserSession

data class GetUserSessionQuery(val accessToken: String) : Query<UserSession>
