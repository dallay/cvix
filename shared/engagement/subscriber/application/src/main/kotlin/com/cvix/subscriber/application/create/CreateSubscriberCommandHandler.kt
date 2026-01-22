package com.cvix.subscriber.application.create

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandHandler
import com.cvix.common.domain.model.Language

/**
 * Service class responsible for handling the SubscribeNewsletterCommand.
 *
 * @property subscriberRegistrator The service responsible for registering subscribers.
 */
@Service
class CreateSubscriberCommandHandler(
    private val subscriberRegistrator: SubscriberRegistrator
) : CommandHandler<CreateSubscriberCommand> {
    /**
     * Function to handle the SubscribeNewsletterCommand.
     *
     * @param command The command to be handled.
     */
    override suspend fun handle(command: CreateSubscriberCommand) {
        subscriberRegistrator.register(
            command.id,
            command.email,
            command.source,
            Language.fromString(command.language),
            command.ipAddress,
            command.attributes,
            command.workspaceId,
        )
    }
}
