package com.cvix.identity.domain.authentication

import com.cvix.common.domain.authentication.AccessToken

/**
 * Represents a [RefreshTokenManager] that is responsible for refreshing the access token of the user.
 */
fun interface RefreshTokenManager {
    /**
     * Refreshes the access token of the user.
     *
     * @param refreshToken the refresh token of the user
     * @return the access token of the user
     */
    suspend fun refresh(refreshToken: RefreshToken): AccessToken
}
