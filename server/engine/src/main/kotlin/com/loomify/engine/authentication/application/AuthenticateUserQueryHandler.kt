package com.loomify.engine.authentication.application

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.credential.CredentialId
import com.loomify.engine.authentication.application.query.AuthenticateUserQuery
import com.loomify.engine.authentication.domain.AccessToken
import com.loomify.engine.authentication.domain.Username
import java.util.UUID
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
        val sanitizedEmail = query.email.replace("\n", "").replace("\r", "")
        log.info("Authenticating user (rememberMe: {})", query.rememberMe)
        val username = Username(query.email)
        val password = Credential(CredentialId(UUID.randomUUID()), query.password)
        return authenticator.authenticate(username, password, query.rememberMe)
    }
    companion object {
        private val log = LoggerFactory.getLogger(AuthenticateUserQueryHandler::class.java)
    }
}
