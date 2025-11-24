package com.loomify.resume

import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.application.create.CreateResumeCommand
import com.loomify.resume.application.delete.DeleteResumeCommand
import com.loomify.resume.application.get.GetResumeQuery
import com.loomify.resume.application.list.ListResumesQuery
import com.loomify.resume.application.update.UpdateResumeCommand
import com.loomify.resume.domain.Basics
import com.loomify.resume.domain.CompanyName
import com.loomify.resume.domain.FullName
import com.loomify.resume.domain.JobTitle
import com.loomify.resume.domain.PhoneNumber
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeDocumentId
import com.loomify.resume.domain.WorkExperience
import com.loomify.resume.infrastructure.http.request.CreateResumeRequest
import com.loomify.resume.infrastructure.http.request.GenerateResumeRequest
import com.loomify.resume.infrastructure.http.request.dto.AwardDto
import com.loomify.resume.infrastructure.http.request.dto.BasicsDto
import com.loomify.resume.infrastructure.http.request.dto.CertificateDto
import com.loomify.resume.infrastructure.http.request.dto.EducationDto
import com.loomify.resume.infrastructure.http.request.dto.InterestDto
import com.loomify.resume.infrastructure.http.request.dto.LanguageDto
import com.loomify.resume.infrastructure.http.request.dto.LocationDto
import com.loomify.resume.infrastructure.http.request.dto.ProfileDto
import com.loomify.resume.infrastructure.http.request.dto.ProjectDto
import com.loomify.resume.infrastructure.http.request.dto.PublicationDto
import com.loomify.resume.infrastructure.http.request.dto.ReferenceDto
import com.loomify.resume.infrastructure.http.request.dto.SkillCategoryDto
import com.loomify.resume.infrastructure.http.request.dto.VolunteerDto
import com.loomify.resume.infrastructure.http.request.dto.WorkExperienceDto
import java.time.Instant
import java.util.*

/**
 * Test fixtures for resume CRUD tests.
 * Provides factory functions for creating test data.
 */
object ResumeTestFixtures {

    fun createValidResume(
        name: String = "John Doe",
        email: String = "john.doe@example.com",
        phone: String = "+1234567890"
    ): Resume = Resume(
        basics = Basics(
            name = FullName(name),
            email = Email(email),
            phone = PhoneNumber(phone),
        ),
        work = listOf(
            WorkExperience(
                name = CompanyName("ACME Corp"),
                position = JobTitle("Software Engineer"),
                startDate = "2020-01-01",
            ),
        ),
    )

    fun createResumeDocument(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String = "John Doe",
        content: Resume = createValidResume(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        createdBy: String = "test@example.com",
        updatedBy: String = "test@example.com"
    ): ResumeDocument = ResumeDocument(
        id = ResumeDocumentId(id),
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedBy = updatedBy,
        updatedAt = updatedAt,
    )

    fun createCreateCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String? = null,
        content: Resume = createValidResume(),
        createdBy: String = "test@example.com"
    ): CreateResumeCommand = CreateResumeCommand(
        id = id,
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        createdBy = createdBy,
    )

    fun createUpdateCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String? = null,
        content: Resume = createValidResume(),
        updatedBy: String = "test@example.com",
        expectedUpdatedAt: java.time.Instant? = null
    ): UpdateResumeCommand = UpdateResumeCommand(
        id = id,
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        updatedBy = updatedBy,
        expectedUpdatedAt = expectedUpdatedAt,
    )

    fun createDeleteCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID()
    ): DeleteResumeCommand = DeleteResumeCommand(
        id = id,
        userId = userId,
    )

    fun createGetQuery(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID()
    ): GetResumeQuery = GetResumeQuery(
        id = id,
        userId = userId,
    )

    fun createListQuery(
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        limit: Int = 50,
        cursor: UUID? = null
    ): ListResumesQuery = ListResumesQuery(
        userId = userId,
        workspaceId = workspaceId,
        limit = limit,
        cursor = cursor,
    )

    fun createResumeRequest(
        workspaceId: UUID = UUID.randomUUID(),
        title: String = "My first resume",
        content: GenerateResumeRequest = createValidResumeRequestContent()
    ): CreateResumeRequest = CreateResumeRequest(workspaceId, title, content)

    fun createValidResumeRequestContent(): GenerateResumeRequest {
        return GenerateResumeRequest(
            basics = basicsDto(),
            work = workExperienceDtos(),
            volunteer = volunteerDtos(),
            education = educationDtos(),
            awards = awardDtos(),
            certificates = certificateDtos(),
            publications = publicationDtos(),
            skills = skillCategoryDtos(),
            languages = languageDtos(),
            interests = interestDtos(),
            references = referenceDtos(),
            projects = projectDtos(),
        )
    }

    private fun projectDtos(): List<ProjectDto> = listOf(
        ProjectDto(
            name = "Resume Generator",
            description = "A SaaS for generating professional resumes.",
            highlights = listOf("Used Kotlin and Vue.js", "Deployed on AWS"),
            keywords = listOf("Kotlin", "Vue.js", "AWS"),
            startDate = "2023-02-01",
            endDate = "2023-08-01",
            url = "https://resume-gen.dev",
            roles = listOf("Lead Developer"),
            entity = "Personal",
            type = "application",
        ),
    )

    private fun referenceDtos(): List<ReferenceDto> = listOf(
        ReferenceDto(
            name = "Jane Smith",
            reference = "Former manager at ACME Corp.",
        ),
    )

    private fun interestDtos(): List<InterestDto> = listOf(
        InterestDto(
            name = "Open Source",
            keywords = listOf("Kotlin", "OSS", "Community"),
        ),
    )

    private fun languageDtos(): List<LanguageDto> = listOf(
        LanguageDto(
            language = "English",
            fluency = "Native",
        ),
        LanguageDto(
            language = "Spanish",
            fluency = "Professional",
        ),
    )

    private fun skillCategoryDtos(): List<SkillCategoryDto> = listOf(
        SkillCategoryDto(
            name = "Kotlin",
            level = "Expert",
            keywords = listOf("Spring Boot", "Coroutines", "WebFlux"),
        ),
        SkillCategoryDto(
            name = "TypeScript",
            level = "Advanced",
            keywords = listOf("Vue.js", "Astro", "Vite"),
        ),
    )

    private fun publicationDtos(): List<PublicationDto> = listOf(
        PublicationDto(
            name = "Kotlin for Backend Development",
            publisher = "Tech Books",
            releaseDate = "2023-01-15",
            url = "https://techbooks.com/kotlin-backend",
            summary = "A comprehensive guide to Kotlin in backend systems.",
        ),
    )

    private fun certificateDtos(): List<CertificateDto> = listOf(
        CertificateDto(
            name = "AWS Certified Developer",
            date = "2021-05-01",
            issuer = "Amazon",
            url = "https://aws.amazon.com/certification",
        ),
    )

    private fun awardDtos(): List<AwardDto> = listOf(
        AwardDto(
            title = "Employee of the Year",
            date = "2022-12-01",
            awarder = "ACME Corp",
            summary = "Recognized for outstanding performance.",
        ),
    )

    private fun educationDtos(): List<EducationDto> = listOf(
        EducationDto(
            institution = "MIT",
            area = "Computer Science",
            studyType = "Bachelor",
            startDate = "2015-09-01",
            endDate = "2019-06-30",
            score = "4.0",
            url = "https://mit.edu",
            courses = listOf("Algorithms", "Distributed Systems"),
        ),
    )

    private fun volunteerDtos(): List<VolunteerDto> = listOf(
        VolunteerDto(
            organization = "Open Source Org",
            position = "Contributor",
            url = "https://opensource.org",
            startDate = "2019-01-01",
            endDate = "2019-12-31",
            summary = "Contributed to open source projects.",
            highlights = listOf("Fixed critical bugs", "Reviewed PRs"),
        ),
    )

    private fun workExperienceDtos(): List<WorkExperienceDto> = listOf(
        WorkExperienceDto(
            name = "ACME Corp",
            position = "Software Engineer",
            startDate = "2020-01-01",
            endDate = "2023-06-30",
            location = "Remote",
            summary = "Developed scalable backend services.",
            highlights = listOf("Led migration to Kotlin", "Implemented CI/CD pipeline"),
        ),
    )

    private fun basicsDto(): BasicsDto = BasicsDto(
        name = "John Doe",
        label = "Software Engineer",
        image = "https://i.pravatar.cc/300",
        email = "john.doe@example.com",
        phone = "+1234567890",
        url = "https://johndoe.dev",
        summary = "Experienced software engineer with a passion for building scalable applications.",
        location = LocationDto(
            address = "123 Main St",
            postalCode = "12345",
            city = "San Francisco",
            countryCode = "US",
            region = "California",
        ),
        profiles = listOf(
            ProfileDto(
                network = "LinkedIn",
                username = "johndoe",
                url = "https://linkedin.com/in/johndoe",
            ),
            ProfileDto(
                network = "GitHub",
                username = "johndoe",
                url = "https://github.com/johndoe",
            ),
        ),
    )
}
