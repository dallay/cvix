package com.loomify.resume.domain.event

import java.time.Instant
import java.util.UUID

/**
 * Domain event emitted when a resume PDF is successfully generated.
 */
data class GeneratedDocument(
    val id: UUID,
    val userId: UUID,
    val documentType: String = "resume",
    val locale: String,
    val sizeInBytes: Long,
    val generatedAt: Instant = Instant.now(),
)
