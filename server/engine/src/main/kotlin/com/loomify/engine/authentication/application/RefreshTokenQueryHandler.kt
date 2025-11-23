package com.loomify.engine.authentication.application

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.query.QueryHandler
import com.loomify.engine.authentication.application.query.RefreshTokenQuery
import com.loomify.engine.authentication.domain.AccessToken
import com.loomify.engine.authentication.domain.RefreshToken
import com.loomify.engine.authentication.domain.RefreshTokenManager
import org.slf4j.LoggerFactory

/**
 * Class for handling a RefreshTokenQuery and returning an AccessToken.
 *
 * @property refreshTokenManager The manager for refreshing tokens.
 */
@Service
class RefreshTokenQueryHandler(private val refreshTokenManager: RefreshTokenManager) :
    QueryHandler<RefreshTokenQuery, AccessToken> {

    /**
     * Handles the given query.
     * @param query The query to handle.
     * @return The response of the query.
     */
    override suspend fun handle(query: RefreshTokenQuery): AccessToken {
        log.debug("Handling refresh token query")
        return refreshTokenManager.refresh(RefreshToken(query.refreshToken))
    }
    companion object {
        private val log = LoggerFactory.getLogger(RefreshTokenQueryHandler::class.java)
    }
}
