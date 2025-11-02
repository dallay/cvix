package com.loomify.resume.infrastructure.web

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

/**
 * REST controller for resume generation endpoints.
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class ResumeController(
    private val generateResumeCommandHandler: GenerateResumeCommandHandler
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
        // Extract locale from Accept-Language header
        val locale = exchange.request.headers.acceptLanguage
            .firstOrNull()?.range?.lowercase() ?: "en"

        // Convert DTO to domain model
        val resumeData = request.toDomain()

        // Create command
        val command = GenerateResumeCommand(resumeData, locale)

        // Execute command and return PDF
        return generateResumeCommandHandler.handle(command)
            .map { inputStream ->
                val pdfBytes = inputStream.readBytes()
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .body(pdfBytes)
            }
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
