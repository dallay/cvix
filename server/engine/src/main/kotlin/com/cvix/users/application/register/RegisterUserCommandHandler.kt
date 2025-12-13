package com.cvix.users.application.register

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import com.cvix.common.domain.vo.credential.Credential
import com.cvix.common.domain.vo.email.Email
import com.cvix.common.domain.vo.name.FirstName
import com.cvix.common.domain.vo.name.LastName
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Command handler responsible for processing user registration commands.
 *
 * This class handles the registration of new users by delegating the process
 * to the `UserRegistrator` service. It validates and transforms the input data
 * from the `RegisterUserCommand` into domain objects before passing them to the
 * service layer.
 *
 * @property userRegistrator The service responsible for registering new users.
 */
@Service
class RegisterUserCommandHandler(
    private val userRegistrator: UserRegistrator
) : CommandWithResultHandler<RegisterUserCommand, UUID> {

    /**
     * Handles the registration of a new user.
     *
     * This method processes the `RegisterUserCommand` by extracting the user details,
     * converting them into domain objects, and invoking the `registerNewUser` method
     * of the `UserRegistrator` service.
     *
     * @param command The command containing user registration details such as email,
     * password, first name, and last name.
     * @return The UUID of the created user.
     */
    override suspend fun handle(command: RegisterUserCommand): UUID {
        log.debug("Handling registration for user with email: {}", command.email)

        return userRegistrator.registerNewUser(
            email = Email(command.email),
            credential = Credential.create(command.password),
            firstName = command.firstname.takeIf { it.isNotBlank() }?.let { FirstName(it) },
            lastName = command.lastname.takeIf { it.isNotBlank() }?.let { LastName(it) },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(RegisterUserCommandHandler::class.java)
    }
}
