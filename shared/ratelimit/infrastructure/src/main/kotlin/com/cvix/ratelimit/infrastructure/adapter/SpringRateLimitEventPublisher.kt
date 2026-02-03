package com.cvix.ratelimit.infrastructure.adapter

import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.ratelimit.domain.event.RateLimitExceededEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Spring adapter for publishing rate limit events.
 * Bridges the domain EventPublisher port to Spring's ApplicationEventPublisher.
 *
 * @property applicationEventPublisher Spring's event publisher
 */
@Component
class SpringRateLimitEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : EventPublisher<RateLimitExceededEvent> {

    override suspend fun publish(event: RateLimitExceededEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
