package com.cvix.authentication.application.query

import com.cvix.authentication.domain.UserSession
import com.cvix.common.domain.bus.query.Query

data class GetUserSessionQuery(val accessToken: String) : Query<UserSession>
