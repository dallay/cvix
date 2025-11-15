package com.loomify.resume.application.handler

import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.application.command.Locale
import com.loomify.resume.domain.port.PdfGenerator
import com.loomify.resume.domain.port.TemplateRenderer
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
    private val templateRenderer: TemplateRenderer,
    private val pdfGenerator: PdfGenerator
) {
    private val logger = LoggerFactory.getLogger(GenerateResumeCommandHandler::class.java)

    /**
     * Handles the resume generation command.
     * @param command The command containing resume data and locale
     * @return A Mono emitting the PDF as an InputStream
     */
    fun handle(command: GenerateResumeCommand): Mono<InputStream> {
        val locale = try {
            Locale.from(command.locale.code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unsupported locale: ${command.locale}", e)
        }

        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        logger.info(
            "Resume generation started - requestId={}, locale={}, hasWork={}, hasEducation={}, hasSkills={}",
            requestId,
            locale.code,
            command.resumeData.work.isNotEmpty(),
            command.resumeData.education.isNotEmpty(),
            command.resumeData.skills.isNotEmpty(),
        )

        return Mono.defer {
            // Step 1: Render LaTeX template with resume data
            logger.debug("Rendering template - requestId={}", requestId)
            val latexSource = templateRenderer.render(command.resumeData, locale.code)

            // Step 2: Generate PDF from LaTeX source
            logger.debug("Generating PDF - requestId={}", requestId)
            pdfGenerator.generatePdf(latexSource, locale.code)
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
    }
}
