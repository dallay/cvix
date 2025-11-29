package com.cvix.resume.domain.event

import com.cvix.common.domain.bus.event.BaseDomainEvent
import com.cvix.resume.domain.DocumentType
import com.cvix.resume.domain.Locale
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
