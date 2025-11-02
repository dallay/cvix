package com.loomify.resume.application.handler

import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.domain.port.PdfGeneratorPort
import com.loomify.resume.domain.port.TemplateRendererPort
import java.io.InputStream
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
    /**
     * Handles the resume generation command.
     * @param command The command containing resume data and locale
     * @return A Mono emitting the PDF as an InputStream
     */
    fun handle(command: GenerateResumeCommand): Mono<InputStream> {
        val locale = command.locale.ifBlank { "en" }

        return Mono.fromCallable {
            // Step 1: Render LaTeX template with resume data
            templateRenderer.render(command.resumeData, locale)
        }
            .flatMap { latexSource ->
                // Step 2: Generate PDF from LaTeX source using Docker
                pdfGenerator.generatePdf(latexSource, locale)
            }
        // Error handling is done by domain exceptions
        // which will be propagated up to the infrastructure layer
    }
}
