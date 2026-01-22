package com.cvix.authentication.application

import com.cvix.authentication.application.query.RefreshTokenQuery
import com.cvix.authentication.domain.RefreshToken
import com.cvix.authentication.domain.RefreshTokenManager
import com.cvix.common.domain.Service
import com.cvix.common.domain.authentication.AccessToken
import com.cvix.common.domain.bus.query.QueryHandler
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
