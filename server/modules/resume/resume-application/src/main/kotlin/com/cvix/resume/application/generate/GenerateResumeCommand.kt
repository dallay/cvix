package com.cvix.resume.application.generate

import com.cvix.common.domain.bus.command.CommandWithResult
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.Resume
import java.io.InputStream
import java.util.UUID

/**
 * Command to generate a PDF resume.
 * @param templateId The ID of the resume template to use
 * @param resume The resume data
 * @param userId The ID of the user requesting the resume generation (for permission validation)
 * @param locale The locale for the resume (default is EN)
 * @created 11/12/25
 */
data class GenerateResumeCommand(
    val templateId: String,
    val resume: Resume,
    val userId: UUID,
    val locale: Locale = Locale.EN
) : CommandWithResult<InputStream>
