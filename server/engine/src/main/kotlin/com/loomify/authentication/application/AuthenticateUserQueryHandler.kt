package com.loomify.authentication.application

import com.loomify.authentication.application.query.AuthenticateUserQuery
import com.loomify.authentication.domain.AccessToken
import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.common.domain.vo.Username
import com.loomify.common.domain.vo.credential.Credential
import org.slf4j.LoggerFactory

/**
 * Handles the [AuthenticateUserQuery] query. This query is used to authenticate a user.
 * @created 31/7/23
 */
@Service
class AuthenticateUserQueryHandler(private val authenticator: UserAuthenticatorService) :
    QueryHandler<AuthenticateUserQuery, AccessToken> {

    /**
     * Handles the given query.
     * @param query The query to handle.
     * @return The response of the query.
     */
    override suspend fun handle(query: AuthenticateUserQuery): AccessToken {
        log.info("Authenticating user (rememberMe: {})", query.rememberMe)
        val username = Username(query.email)
        val password = Credential.create(query.password)
        return authenticator.authenticate(username, password, query.rememberMe)
    }
    companion object {
        private val log = LoggerFactory.getLogger(AuthenticateUserQueryHandler::class.java)
    }
}
