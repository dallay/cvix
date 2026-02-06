package com.cvix.identity.application.authentication

import com.cvix.common.domain.Service
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.common.domain.bus.query.QueryHandler
import com.cvix.common.domain.vo.Username
import com.cvix.common.domain.vo.credential.Credential
import com.cvix.identity.application.authentication.query.AuthenticateUserQuery
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
