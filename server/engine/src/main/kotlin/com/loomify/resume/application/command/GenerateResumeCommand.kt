package com.loomify.resume.application.command

import com.loomify.resume.domain.model.ResumeData

/**
 * Command to generate a PDF resume from resume data.
 * Part of the CQRS pattern in the application layer.
 */
data class GenerateResumeCommand(
    val resumeData: ResumeData,
    val locale: String = "en"
)
