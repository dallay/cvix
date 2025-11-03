package com.loomify.resume.application.handler

import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.domain.port.PdfGeneratorPort
import com.loomify.resume.domain.port.TemplateRendererPort
import java.io.InputStream
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Command handler for generating PDF resumes.
 * Orchestrates the template rendering and PDF generation process.
 */
@Service
class GenerateResumeCommandHandler(
    private val templateRenderer: TemplateRendererPort,
    private val pdfGenerator: PdfGeneratorPort
) {
    private val logger = LoggerFactory.getLogger(GenerateResumeCommandHandler::class.java)

    /**
     * Handles the resume generation command.
     * @param command The command containing resume data and locale
     * @return A Mono emitting the PDF as an InputStream
     */
    fun handle(command: GenerateResumeCommand): Mono<InputStream> {
        val locale = command.locale.ifBlank { "en" }
        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        logger.info(
            "Resume generation started - requestId={}, locale={}, hasWork={}, hasEducation={}, hasSkills={}",
            requestId,
            locale,
            command.resumeData.work.isNotEmpty(),
            command.resumeData.education.isNotEmpty(),
            command.resumeData.skills.isNotEmpty(),
        )

        return Mono.fromCallable {
            // Step 1: Render LaTeX template with resume data
            logger.debug("Rendering template - requestId={}", requestId)
            templateRenderer.render(command.resumeData, locale)
        }
            .flatMap { latexSource ->
                // Step 2: Generate PDF from LaTeX source using Docker
                logger.debug("Generating PDF - requestId={}, latexSize={} bytes", requestId, latexSource.length)
                pdfGenerator.generatePdf(latexSource, locale)
            }
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.info(
                    "Resume generation completed - requestId={}, duration={}ms",
                    requestId,
                    duration,
                )
            }
            .doOnError { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(
                    "Resume generation failed - requestId={}, duration={}ms, error={}",
                    requestId,
                    duration,
                    error.message,
                    error,
                )
            }
        // Error handling is done by domain exceptions
        // which will be propagated up to the infrastructure layer
    }
}
