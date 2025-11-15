package com.loomify.resume.domain.model

import com.loomify.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for ResumeData aggregate root.
 *
 * Tests validation rules:
 * - BR-001: Resume MUST contain at least one non-empty section (work, education, or skills)
 * - Content metrics calculation for adaptive layout
 */
@UnitTest
class ResumeDataTest {

    @Test
    fun `should create resume data with work experience only`() {
        // Arrange
        val personalInfo = createValidPersonalInfo()
        val workExperience = listOf(createValidWorkExperience())

        // Act
        val resumeData = ResumeData(
            basics = personalInfo,
            work = workExperience,
            education = emptyList(),
            skills = emptyList(),
        )

        // Assert
        resumeData shouldNotBe null
        resumeData.basics shouldBe personalInfo
        resumeData.work shouldBe workExperience
    }

    @Test
    fun `should create resume data with education only`() {
        // Arrange
        val personalInfo = createValidPersonalInfo()
        val education = listOf(createValidEducation())

        // Act
        val resumeData = ResumeData(
            basics = personalInfo,
            work = emptyList(),
            education = education,
            skills = emptyList(),
        )

        // Assert
        resumeData shouldNotBe null
        resumeData.education shouldBe education
    }

    @Test
    fun `should create resume data with skills only`() {
        // Arrange
        val personalInfo = createValidPersonalInfo()
        val skills = listOf(createValidSkillCategory())

        // Act
        val resumeData = ResumeData(
            basics = personalInfo,
            work = emptyList(),
            education = emptyList(),
            skills = skills,
        )

        // Assert
        resumeData shouldNotBe null
        resumeData.skills shouldBe skills
    }

    @Test
    fun `should fail when resume has no content sections`() {
        // Arrange
        val personalInfo = createValidPersonalInfo()

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            ResumeData(
                basics = personalInfo,
                work = emptyList(),
                education = emptyList(),
                skills = emptyList(),
            )
        }.message shouldBe "Resume must have at least one of: work experience, education, or skills"
    }

    @Test
    fun `should calculate content metrics for skills-heavy resume`() {
        // Arrange
        val resumeData = ResumeData(
            basics = createValidPersonalInfo(),
            work = emptyList(),
            education = emptyList(),
            skills = listOf(
                createValidSkillCategory(keywords = listOf("Java", "Kotlin", "Spring")),
                createValidSkillCategory(keywords = listOf("React", "Vue", "TypeScript")),
            ),
        )

        // Act
        val metrics = resumeData.contentMetrics()

        // Assert
        metrics.skillsCount shouldBe 6
        metrics.experienceYears shouldBe 0.0
        metrics.experienceEntries shouldBe 0
        metrics.educationEntries shouldBe 0
    }

    @Test
    fun `should calculate content metrics for experience-heavy resume`() {
        // Arrange
        val resumeData = ResumeData(
            basics = createValidPersonalInfo(),
            work = listOf(
                createValidWorkExperience(startDate = "2020-01-01", endDate = "2022-12-31"),
                createValidWorkExperience(startDate = "2018-06-01", endDate = "2019-12-31"),
            ),
            education = emptyList(),
            skills = emptyList(),
        )

        // Act
        val metrics = resumeData.contentMetrics()

        // Assert
        metrics.experienceEntries shouldBe 2
        metrics.experienceYears.shouldBeWithinPercentageOf(4.5, 2.0) // Within 2% of 4.5 years
        metrics.skillsCount shouldBe 0
    }

    // Helper functions to create valid test data
    private fun createValidPersonalInfo() = PersonalInfo(
        name = FullName("John Doe"),
        label = JobTitle("Software Engineer"),
        email = com.loomify.common.domain.vo.email.Email("john.doe@example.com"),
        phone = PhoneNumber("+1234567890"),
        url = Url("https://johndoe.com"),
        summary = Summary("Experienced software engineer"),
        location = null,
        profiles = emptyList(),
    )

    private fun createValidWorkExperience(
        startDate: String = "2020-01-01",
        endDate: String? = "2023-12-31"
    ) = WorkExperience(
        name = CompanyName("ACME Corp"),
        position = JobTitle("Software Engineer"),
        startDate = startDate,
        endDate = endDate,
        location = "San Francisco, CA",
        summary = "Developed web applications",
        highlights = listOf(Highlight("Led team of 5 developers")),
        url = null,
    )

    private fun createValidEducation() = Education(
        institution = InstitutionName("MIT"),
        area = FieldOfStudy("Computer Science"),
        studyType = DegreeType("Bachelor"),
        startDate = "2015-09-01",
        endDate = "2019-05-31",
        score = "3.8",
        courses = null,
    )

    private fun createValidSkillCategory(
        keywords: List<String> = listOf("Java", "Kotlin")
    ) = SkillCategory(
        name = SkillCategoryName("Programming Languages"),
        level = "Advanced",
        keywords = keywords.map { Skill(it) },
    )
}
