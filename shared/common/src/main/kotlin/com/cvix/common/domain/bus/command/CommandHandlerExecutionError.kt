package com.cvix.common.domain.bus.command

/**
 * Exception thrown when a command handler fails to execute a command.
 *
 * @param message Detailed error message.
 * @param cause The underlying cause of the failure.
 */
class CommandHandlerExecutionError(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
