package com.cvix.identity.application.authentication.query

import com.cvix.common.domain.bus.query.Query
import com.cvix.identity.domain.authentication.UserSession

data class GetUserSessionQuery(val accessToken: String) : Query<UserSession>
