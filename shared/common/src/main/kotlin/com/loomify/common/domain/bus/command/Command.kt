package com.loomify.common.domain.bus.command

/**
 * Marker interface for commands in the command bus architecture.
 *
 * Commands represent requests to perform actions that may change system state.
 * They are handled by implementations of [CommandHandler].
 *
 * @see CommandHandler
 * @see CommandWithResult
 * @created 7/1/24
 */
interface Command
