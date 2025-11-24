package com.loomify.engine.authentication.application

import com.loomify.common.domain.Service
import com.loomify.common.domain.vo.credential.Credential
import com.loomify.engine.authentication.domain.AccessToken
import com.loomify.engine.authentication.domain.UserAuthenticator
import com.loomify.engine.authentication.domain.Username
import org.slf4j.LoggerFactory

/**
 *
 * @created 31/7/23
 */
@Service
class UserAuthenticatorService(private val userAuthenticator: UserAuthenticator) {
    /**
     * Authenticates a user.
     *
     * @param username the username of the user to be authenticated
     * @param password the password of the user to be authenticated
     * @param rememberMe whether to extend the session duration
     * @return the access token of the user
     */
    suspend fun authenticate(username: Username, password: Credential, rememberMe: Boolean = false): AccessToken {
        log.info("Authenticating user (rememberMe: {})", rememberMe)
        return userAuthenticator.authenticate(username, password, rememberMe)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserAuthenticatorService::class.java)
    }
}
