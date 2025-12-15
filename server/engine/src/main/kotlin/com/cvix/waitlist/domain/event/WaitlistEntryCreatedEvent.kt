package com.cvix.waitlist.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent

/**
 * Domain event emitted when a new waitlist entry is created.
 *
 * @property id The unique identifier of the waitlist entry.
 * @property email The email address of the user who joined.
 * @property source The source from where the user joined.
 * @property language The user's preferred language.
 */
data class WaitlistEntryCreatedEvent(
    val id: String,
    val email: String,
    val source: String,
    val language: String,
) : BaseDomainEvent()
