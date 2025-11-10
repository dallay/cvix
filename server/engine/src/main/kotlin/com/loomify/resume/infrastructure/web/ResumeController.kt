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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
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

        // Extract locale from Accept-Language header
        val locale = try {
            // Use only the primary language subtag (e.g., "en" from "en-US") to match template names
            val languageCode = exchange.request.headers.acceptLanguage
                .firstOrNull()
                ?.range
                ?.split("-")
                ?.first()
                ?.lowercase()
                ?: "en"

            com.loomify.resume.application.command.Locale.from(languageCode)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported locale", e)
        }

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
 * Supports both new JSON Resume Schema fields and legacy fields for backward compatibility.
 */
private fun GenerateResumeRequest.toDomain(): ResumeData {
    return ResumeData(
        basics = PersonalInfo(
            fullName = FullName(personalInfo.fullName),
            label = personalInfo.label?.let { JobTitle(it) },
            image = personalInfo.image?.let { Url(it) },
            email = com.loomify.common.domain.vo.email.Email(personalInfo.email),
            phone = PhoneNumber(personalInfo.phone),
            url = personalInfo.url?.let { Url(it) } ?: personalInfo.website?.let { Url(it) },
            summary = personalInfo.summary?.let { Summary(it) },
            location = personalInfo.location?.let { loc ->
                Location(
                    address = loc.address,
                    postalCode = loc.postalCode,
                    city = loc.city,
                    countryCode = loc.countryCode,
                    region = loc.region
                )
            },
            profiles = buildList {
                // Add new profiles from the profiles field
                personalInfo.profiles?.forEach { profile ->
                    add(SocialProfile(profile.network, profile.username ?: "", profile.url))
                }
                // Add legacy LinkedIn profile if not already in profiles
                if (personalInfo.linkedin != null && 
                    personalInfo.profiles?.none { it.network.equals("LinkedIn", ignoreCase = true) } != false) {
                    add(SocialProfile("LinkedIn", "", personalInfo.linkedin))
                }
                // Add legacy GitHub profile if not already in profiles
                if (personalInfo.github != null && 
                    personalInfo.profiles?.none { it.network.equals("GitHub", ignoreCase = true) } != false) {
                    add(SocialProfile("GitHub", "", personalInfo.github))
                }
            },
        ),
        work = workExperience?.map { work ->
            WorkExperience(
                company = CompanyName(work.name ?: work.company ?: ""),
                position = JobTitle(work.position),
                startDate = work.startDate,
                endDate = work.endDate,
                location = work.location,
                summary = work.summary ?: work.description,
                highlights = work.highlights?.map { Highlight(it) },
                url = work.url?.let { Url(it) },
            )
        } ?: emptyList(),
        volunteer = volunteer?.map { vol ->
            com.loomify.resume.domain.model.Volunteer(
                organization = vol.organization,
                position = vol.position,
                url = vol.url?.let { Url(it) },
                startDate = vol.startDate,
                endDate = vol.endDate,
                summary = vol.summary,
                highlights = vol.highlights,
            )
        } ?: emptyList(),
        education = education?.map { edu ->
            Education(
                institution = InstitutionName(edu.institution),
                area = edu.area?.let { FieldOfStudy(it) } ?: edu.degree?.let { FieldOfStudy(it) },
                studyType = edu.studyType?.let { DegreeType(it) } ?: edu.degree?.let { DegreeType(it) },
                startDate = edu.startDate,
                endDate = edu.endDate,
                score = edu.score ?: edu.gpa,
                url = edu.url?.let { Url(it) },
                courses = edu.courses,
            )
        } ?: emptyList(),
        awards = awards?.map { award ->
            com.loomify.resume.domain.model.Award(
                title = award.title,
                date = award.date,
                awarder = award.awarder,
                summary = award.summary,
            )
        } ?: emptyList(),
        certificates = certificates?.map { cert ->
            com.loomify.resume.domain.model.Certificate(
                name = cert.name,
                date = cert.date,
                url = cert.url?.let { Url(it) },
                issuer = cert.issuer,
            )
        } ?: emptyList(),
        publications = publications?.map { pub ->
            com.loomify.resume.domain.model.Publication(
                name = pub.name,
                publisher = pub.publisher,
                releaseDate = pub.releaseDate,
                url = pub.url?.let { Url(it) },
                summary = pub.summary,
            )
        } ?: emptyList(),
        skills = skills?.map { skill ->
            SkillCategory(
                name = SkillCategoryName(skill.name),
                level = skill.level,
                keywords = skill.keywords.map { Skill(it) },
            )
        } ?: emptyList(),
        languages = languages?.map { lang ->
            Language(
                language = lang.language,
                fluency = lang.fluency,
            )
        } ?: emptyList(),
        interests = interests?.map { interest ->
            com.loomify.resume.domain.model.Interest(
                name = interest.name,
                keywords = interest.keywords,
            )
        } ?: emptyList(),
        references = references?.map { ref ->
            com.loomify.resume.domain.model.Reference(
                name = ref.name,
                reference = ref.reference,
            )
        } ?: emptyList(),
        projects = projects?.map { project ->
            Project(
                name = project.name,
                description = project.description,
                url = project.url,
                startDate = project.startDate?.let { parseFlexibleDate(it) },
                endDate = project.endDate?.let { parseFlexibleDate(it) },
                highlights = project.highlights,
                keywords = project.keywords,
                roles = project.roles,
                entity = project.entity,
                type = project.type,
            )
        } ?: emptyList(),
        meta = meta?.let { m ->
            com.loomify.resume.domain.model.Meta(
                canonical = m.canonical,
                version = m.version,
                lastModified = m.lastModified,
            )
        },
    )
}

/**
 * Parses a flexible ISO 8601 date (YYYY-MM-DD, YYYY-MM, or YYYY).
 * Returns the parsed LocalDate or null if invalid.
 */
private fun parseFlexibleDate(date: String): LocalDate? {
    return try {
        when {
            date.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> LocalDate.parse(date)
            date.matches(Regex("""\d{4}-\d{2}""")) -> LocalDate.parse("$date-01")
            date.matches(Regex("""\d{4}""")) -> LocalDate.parse("$date-01-01")
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
