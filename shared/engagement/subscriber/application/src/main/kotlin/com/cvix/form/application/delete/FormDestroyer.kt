package com.cvix.form.application.delete

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.event.EventBroadcaster
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.SubscriptionFormRepository
import com.cvix.form.domain.event.SubscriptionFormDeletedEvent
import org.slf4j.LoggerFactory

@Service
class FormDestroyer(
    private val formRepository: SubscriptionFormRepository,
    private val formFinder: SubscriptionFormFinderRepository,
    eventPublisher: EventPublisher<SubscriptionFormDeletedEvent>
) {
    private val eventPublisher = EventBroadcaster<SubscriptionFormDeletedEvent>()

    init {
        this.eventPublisher.use(eventPublisher)
    }

    suspend fun delete(workspaceId: WorkspaceId, subscriptionFormId: SubscriptionFormId) {
        log.info("Deleting subscription form with ID: $subscriptionFormId from workspace: $workspaceId")
        val form = formFinder.findByFormIdAndWorkspaceId(subscriptionFormId, workspaceId)
            ?: run {
                log.warn("Subscription form with ID: $subscriptionFormId not found in workspace: $workspaceId")
                return
            }
        formRepository.delete(form.id).also {
            eventPublisher.publish(
                SubscriptionFormDeletedEvent(
                    formId = form.id,
                    workspaceId = workspaceId,
                ),
            )
            log.info("Deleted subscription form with ID: $subscriptionFormId from workspace: $workspaceId")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FormDestroyer::class.java)
    }
}
