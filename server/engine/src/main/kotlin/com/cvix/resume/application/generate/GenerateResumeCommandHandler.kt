package com.cvix.resume.application.generate

import com.cvix.common.domain.Service
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import com.cvix.resume.domain.Locale
import java.io.InputStream
import org.slf4j.LoggerFactory

/**
 * Command handler for generating PDF resumes.
 * Orchestrates the template rendering and PDF generation process.
 */
@Service
class GenerateResumeCommandHandler(
    private val pdfGenerator: PdfResumeGenerator
) : CommandWithResultHandler<GenerateResumeCommand, InputStream> {

    /**
     * Handles the resume generation command.
     * @param command The command containing resume data and locale
     * @return The PDF as an InputStream
     */
    override suspend fun handle(command: GenerateResumeCommand): InputStream {
        val locale = try {
            Locale.from(command.locale.code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unsupported locale: ${command.locale}", e)
        }
        log.debug("Handling GenerateResumeCommand - locale={}", locale.code)

        return pdfGenerator.generate(command.resume, locale)
    }

    companion object {
        private val log = LoggerFactory.getLogger(GenerateResumeCommandHandler::class.java)
    }
}
