package com.loomify.resume.application.generate

import com.loomify.common.domain.Service
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.resume.domain.DocumentType
import com.loomify.resume.domain.Locale
import com.loomify.resume.domain.PdfGenerator
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.TemplateRenderer
import com.loomify.resume.domain.event.GeneratedDocumentEvent
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class PdfResumeGenerator(
    private val templateRenderer: TemplateRenderer,
    private val pdfGenerator: PdfGenerator,
    eventPublisher: EventPublisher<GeneratedDocumentEvent>
) {
    private val eventBroadcaster = EventBroadcaster<GeneratedDocumentEvent>()

    init {
        this.eventBroadcaster.use(eventPublisher)
    }

    /**
     * Generates a PDF resume from the given resume data and locale.
     *
     * @param resume The resume data following JSON Resume schema
     * @param locale The locale for the resume (default is EN). The locale is passed through without
     * explicit validation; ensure it is supported.
     * @return An InputStream of the generated PDF
     * @throws Exception if there is an error during generation
     */
    suspend fun generate(resume: Resume, locale: Locale = Locale.EN): InputStream {

        val requestId = UUID.randomUUID()
        val startTime = System.currentTimeMillis()

        log.info(
            "Resume generation started - requestId={}, locale={}, hasWork={}, hasEducation={}, hasSkills={}",
            requestId,
            locale.code,
            resume.work.isNotEmpty(),
            resume.education.isNotEmpty(),
            resume.skills.isNotEmpty(),
        )

        return try {
            // Step 1: Render LaTeX template with resume data
            log.debug("Rendering template - requestId={}", requestId)
            val latexSource = templateRenderer.render(resume, locale.code)

            // Step 2: Generate PDF from LaTeX source
            log.debug("Generating PDF - requestId={}", requestId)
            val inputStream = pdfGenerator.generatePdf(latexSource, locale.code).awaitSingle()

            // Read the InputStream fully into a ByteArray to get the exact size
            val pdfBytes = inputStream.readAllBytes()
            val duration = System.currentTimeMillis() - startTime
            log.info(
                "Resume generation completed - requestId={}, duration={}ms",
                requestId,
                duration,
            )
            // Step 3: Publish GeneratedDocumentEvent event
            eventBroadcaster.publish(
                GeneratedDocumentEvent(
                    id = requestId,
                    documentType = DocumentType.RESUME,
                    locale = locale,
                    sizeInBytes = pdfBytes.size.toLong(),
                ),
            )
            ByteArrayInputStream(pdfBytes)
        } catch (error: Throwable) {
            val duration = System.currentTimeMillis() - startTime
            log.error(
                "Resume generation failed - requestId={}, duration={}ms, error={}",
                requestId,
                duration,
                error.message,
                error,
            )
            throw error
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PdfResumeGenerator::class.java)
    }
}
