package com.loomify.resume.application.generate

import com.loomify.common.domain.bus.command.CommandWithResult
import com.loomify.resume.domain.Locale
import com.loomify.resume.domain.Resume
import java.io.InputStream

/**
 * Command to generate a PDF resume from resume data.
 * Part of the CQRS pattern in the application layer.
 *
 * Supported locales: "en", "es"
 */
data class GenerateResumeCommand(
    val resume: Resume,
    val locale: Locale = Locale.EN
) : CommandWithResult<InputStream>
