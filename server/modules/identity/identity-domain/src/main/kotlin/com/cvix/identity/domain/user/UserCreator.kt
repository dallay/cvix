package com.cvix.identity.domain.user

import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.name.FirstName
import com.cvix.common.domain.vo.name.LastName

/**
 * Represents a UserCreator that is responsible for creating a user.
 *
 * A UserCreator is a functional interface with a single method `create`, which takes user details
 * and asynchronously creates a user.
 *
 * @see com.cvix.identity.domain.user.User
 * @see com.cvix.common.domain.vo.email.Email
 * @see com.cvix.common.domain.vo.credential.Credential
 * @see com.cvix.common.domain.vo.name.FirstName
 * @see com.cvix.common.domain.vo.name.LastName
 * @created 8/7/23
 */
fun interface UserCreator {
    /**
     * Create a new user.
     *
     * @param email The email of the user to be created.
     * @param credential The credential of the user to be created.
     * @param firstName The first name of the user to be created, nullable if not provided.
     * @param lastName The last name of the user to be created, nullable if not provided.
     * @return The created user.
     */
    suspend fun create(email: Email, credential: Credential, firstName: FirstName?, lastName: LastName?): User
}
