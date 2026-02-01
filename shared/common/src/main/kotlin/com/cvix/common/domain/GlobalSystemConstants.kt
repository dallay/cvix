package com.cvix.common.domain

import java.util.UUID

/**
 * The default system actor used when no explicit actor is provided.
 */
const val SYSTEM_USER: String = "system"

/**
 * The UUID representation of the system actor.
 * Uses the nil UUID (all zeros) to represent system actions.
 */
val SYSTEM_USER_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
