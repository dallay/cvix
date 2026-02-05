package com.cvix.identity.application.user.register

import com.cvix.common.domain.bus.command.CommandWithResult
import java.util.UUID

/**
 * Represents a command to register a new user.
 *
 * @property email The email of the new user.
 * @property password The password of the new user.
 * @property firstname The first name of the new user.
 * @property lastname The last name of the new user.
 */
data class RegisterUserCommand(
    val email: String,
    val password: String,
    val firstname: String,
    val lastname: String
) : CommandWithResult<UUID> {
    override fun toString(): String =
        "RegisterUserCommand(email='$email', password='***REDACTED***', firstname='$firstname', lastname='$lastname')"
}
