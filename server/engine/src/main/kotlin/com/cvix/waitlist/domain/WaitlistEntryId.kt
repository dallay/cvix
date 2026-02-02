package com.cvix.waitlist.domain

import java.util.*

/**
 * Value object representing a unique identifier for a waitlist entry.
 */
@JvmInline
value class WaitlistEntryId(val id: UUID)
