package com.cvix.common.domain.bus.command

/**
 * A functional interface for handling commands in the command bus architecture.
 *
 * @param T The type of command this handler can process, must be a subtype of [Command]
 */
fun interface CommandHandler<T : Command> {
    /**
     * Handles the given command.
     *
     * @param command The command to be handled
     */
    suspend fun handle(command: T)
}
