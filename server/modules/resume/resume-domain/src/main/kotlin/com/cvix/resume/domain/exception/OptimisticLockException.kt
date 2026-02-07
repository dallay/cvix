package com.cvix.resume.domain.exception

import java.time.Instant
import java.util.UUID

/**
 * Exception thrown when an optimistic lock check fails during update.
 * This occurs when the expectedUpdatedAt timestamp doesn't match the current updatedAt.
 */
class OptimisticLockException(
    val resumeId: UUID,
    val expectedUpdatedAt: Instant,
    val actualUpdatedAt: Instant
) : ResumeException(
    "Optimistic lock failure for resume $resumeId. " +
        "Expected updatedAt: $expectedUpdatedAt, but was: $actualUpdatedAt",
)
