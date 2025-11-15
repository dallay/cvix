package com.loomify.resume.domain.port

import com.loomify.resume.domain.model.ResumeData

/**
 * Output port for template rendering.
 * Implemented by infrastructure layer (LatexTemplateRenderer using StringTemplate 4).
 *
 * Security: Must sanitize all user input to prevent LaTeX injection.
 * Special characters that must be escaped: \ { } $ & % # _ ^ ~
 */
fun interface TemplateRenderer {
    /**
     * Renders resume data into LaTeX source code using the appropriate template.
     *
     * @param resumeData The validated resume data to render
     * @param locale The locale for template selection (e.g., "en", "es")
     * @return Complete LaTeX document source ready for compilation
     * @throws com.loomify.resume.domain.exception.TemplateRenderingException if rendering fails
     */
    fun render(resumeData: ResumeData, locale: String): String
}
