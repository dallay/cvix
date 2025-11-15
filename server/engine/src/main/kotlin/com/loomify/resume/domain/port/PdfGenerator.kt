package com.loomify.resume.domain.port

import java.io.InputStream
import reactor.core.publisher.Mono

/**
 * Output port for PDF generation.
 * Implemented by infrastructure layer (DockerPdfGeneratorAdapter).
 *
 * Security constraints (per plan.md):
 * - Must run in isolated Docker container with --read-only, --no-new-privileges
 * - Must enforce 10-second timeout
 * - Must enforce resource limits (512MB memory, 0.5 CPU)
 * - Must restrict network access
 * - Must clean up temp files on success/failure
 */
fun interface PdfGenerator {
    /**
     * Generates a PDF from the rendered LaTeX source.
     *
     * @param latexSource The complete LaTeX document source (including preamble)
     * @param locale The locale for language-specific formatting (e.g., "en", "es")
     * @return Reactive stream of PDF bytes
     * @throws com.loomify.resume.domain.exception.PdfGenerationException if generation fails
     * @throws com.loomify.resume.domain.exception.PdfGenerationTimeoutException if generation exceeds timeout
     */
    fun generatePdf(latexSource: String, locale: String): Mono<InputStream>
}
