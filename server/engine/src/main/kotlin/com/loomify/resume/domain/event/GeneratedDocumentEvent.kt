package com.loomify.resume.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent
import com.loomify.resume.domain.DocumentType
import com.loomify.resume.domain.Locale
import java.time.Instant
import java.util.*

/**
 * Domain event emitted when a resume PDF is successfully generated.
 * @property id The unique identifier of the generated document.
 * @property documentType The type of the generated document (e.g., RESUME, COVER_LETTER,
 * etc.).
 * @property locale The locale/language of the generated document.
 * @property sizeInBytes The size of the generated document in bytes.
 * @property generatedAt The timestamp when the document was generated.
 * @see DocumentType
 */
data class GeneratedDocumentEvent(
    val id: UUID,
    val documentType: DocumentType,
    val locale: Locale,
    val sizeInBytes: Long,
    val generatedAt: Instant = Instant.now(),
) : BaseDomainEvent()
