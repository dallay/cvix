package com.cvix.authentication.application

import com.cvix.authentication.domain.Role
import java.util.UUID

/**
 * Data class representing authenticated user information.
 *
 * This is used to pass user details through the authentication flow,
 * particularly for federated authentication scenarios.
 *
 * @property id The user's unique identifier
 * @property email The user's email address
 * @property username The user's username
 * @property firstName The user's first name
 * @property lastName The user's last name
 * @property displayName The user's display name
 * @property roles The user's roles in the system
 */
data class UserInfo(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val displayName: String?,
    val roles: Set<Role> = setOf(Role.USER)
)
