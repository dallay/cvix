package com.loomify.resume.infrastructure.web

import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.application.handler.GenerateResumeCommandHandler
import com.loomify.resume.domain.model.CompanyName
import com.loomify.resume.domain.model.DegreeType
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.FieldOfStudy
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.InstitutionName
import com.loomify.resume.domain.model.JobTitle
import com.loomify.resume.domain.model.Language
import com.loomify.resume.domain.model.Location
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.PhoneNumber
import com.loomify.resume.domain.model.Project
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.Skill
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.SkillCategoryName
import com.loomify.resume.domain.model.SocialProfile
import com.loomify.resume.domain.model.Summary
import com.loomify.resume.domain.model.Url
import com.loomify.resume.domain.model.WorkExperience
import com.loomify.resume.infrastructure.web.dto.GenerateResumeRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import java.time.LocalDate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val MAX_BYTES_SUPPORTED = 100 * 1024

/**
 * REST controller for resume generation endpoints.
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class ResumeController(
    private val generateResumeCommandHandler: GenerateResumeCommandHandler,
    private val applicationSecurityProperties: ApplicationSecurityProperties
) {

    @Operation(summary = "Generate a PDF resume from resume data")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Resume PDF generated successfully",
            content = [Content(mediaType = "application/pdf")],
        ),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "422", description = "Business rule validation failed"),
        ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "504", description = "PDF generation timeout"),
    )
    @PostMapping("/resumes")
    fun generateResume(
        @Valid @Validated @RequestBody request: GenerateResumeRequest,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ByteArray>> {
        // Validate payload size (FR-015: reject requests exceeding 100KB)
        val contentLength = exchange.request.headers.contentLength
        if (contentLength > MAX_BYTES_SUPPORTED) { // 100KB in bytes
            return Mono.error(
                IllegalArgumentException(
                    "Request payload too large: $contentLength bytes. Maximum allowed: 102400 bytes (100KB)",
                ),
            )
        }

        // Validate API-Version header (optional but recommended)
        val apiVersion = exchange.request.headers.getFirst("API-Version")
        if (apiVersion != null && apiVersion != "v1" && apiVersion != "1") {
            return Mono.error(
                IllegalArgumentException("Unsupported API version: $apiVersion. Supported versions: v1"),
            )
        }

        // Extract locale from Accept-Language header
        val locale = exchange.request.headers.acceptLanguage
            .firstOrNull()?.range?.lowercase() ?: "en"

        // Convert DTO to domain model
        val resumeData = request.toDomain()

        // Create command
        val command = GenerateResumeCommand(resumeData, locale)

        // Track generation time
        val startTime = System.currentTimeMillis()

        // Execute command and return PDF
        return generateResumeCommandHandler.handle(command)
            .map { inputStream ->
                val pdfBytes = inputStream.readBytes()
                val generationTimeMs = System.currentTimeMillis() - startTime

                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.size.toLong())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .header("X-Generation-Time-Ms", generationTimeMs.toString())
                    .header(CONTENT_SECURITY_POLICY_HEADER, applicationSecurityProperties.contentSecurityPolicy)
                    .header(X_CONTENT_TYPE_OPTIONS_HEADER, X_CONTENT_TYPE_OPTIONS_VALUE)
                    .header(X_FRAME_OPTIONS_HEADER, X_FRAME_OPTIONS_VALUE)
                    .header(REFERRER_POLICY_HEADER, REFERRER_POLICY_VALUE)
                    .body(pdfBytes)
            }
    }

    companion object {
        private const val CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options"
        private const val X_FRAME_OPTIONS_HEADER = "X-Frame-Options"
        private const val REFERRER_POLICY_HEADER = "Referrer-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff"
        private const val X_FRAME_OPTIONS_VALUE = "DENY"
        private const val REFERRER_POLICY_VALUE = "strict-origin-when-cross-origin"
    }
}

/**
 * Extension function to convert DTO to domain model.
 */
private fun GenerateResumeRequest.toDomain(): ResumeData {
    return ResumeData(
        basics = PersonalInfo(
            fullName = FullName(personalInfo.fullName),
            label = null, // Not in DTO yet
            email = com.loomify.common.domain.vo.email.Email(personalInfo.email),
            phone = PhoneNumber(personalInfo.phone),
            url = personalInfo.website?.let { Url(it) },
            summary = personalInfo.summary?.let { Summary(it) },
            location = personalInfo.location?.let {
                Location(city = it) // Simplified for now
            },
            profiles = buildList {
                personalInfo.linkedin?.let {
                    add(SocialProfile("LinkedIn", "", it))
                }
                personalInfo.github?.let {
                    add(SocialProfile("GitHub", "", it))
                }
            },
        ),
        work = workExperience?.map { work ->
            WorkExperience(
                company = CompanyName(work.company),
                position = JobTitle(work.position),
                startDate = work.startDate,
                endDate = work.endDate,
                location = work.location,
                summary = work.description,
                highlights = null,
                url = null,
            )
        } ?: emptyList(),
        education = education?.map { edu ->
            Education(
                institution = InstitutionName(edu.institution),
                area = FieldOfStudy(edu.degree), // Using degree as area for now
                studyType = DegreeType(edu.degree),
                startDate = edu.startDate,
                endDate = edu.endDate,
                score = edu.gpa,
                courses = null,
            )
        } ?: emptyList(),
        skills = skills?.map { skill ->
            SkillCategory(
                name = SkillCategoryName(skill.name),
                level = null,
                keywords = skill.keywords.map { Skill(it) },
            )
        } ?: emptyList(),
        languages = languages?.map { lang ->
            Language(
                language = lang.language,
                fluency = lang.fluency,
            )
        } ?: emptyList(),
        projects = projects?.map { project ->
            Project(
                name = project.name,
                description = project.description,
                url = project.url,
                startDate = project.startDate?.let { LocalDate.parse(it) },
                endDate = project.endDate?.let { LocalDate.parse(it) },
            )
        } ?: emptyList(),
    )
}
