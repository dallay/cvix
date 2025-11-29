package com.cvix.resume.application.generate

import com.cvix.common.domain.bus.command.CommandWithResult
import com.cvix.resume.domain.Locale
import com.cvix.resume.domain.Resume
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
