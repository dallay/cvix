package com.loomify.engine.authentication.domain

import com.loomify.common.domain.vo.credential.Credential

/**
 * Represents a UserLogin that is responsible for logging in a user.
 * @created 31/7/23
 */
interface UserAuthenticator {
    /**
     * Login a user with the given username and password.
     *
     * @param username the username of the user to be logged in
     * @param password the password of the user to be logged in
     * @param rememberMe whether to extend the session duration (affects token TTL)
     * @return the access token of the user
     */
    suspend fun authenticate(username: Username, password: Credential, rememberMe: Boolean): AccessToken

    /**
     * Login a user with the given username and password (without remember me).
     *
     * @param username the username of the user to be logged in
     * @param password the password of the user to be logged in
     * @return the access token of the user
     */
    suspend fun authenticate(username: Username, password: Credential): AccessToken {
        return authenticate(username, password, false)
    }
}
