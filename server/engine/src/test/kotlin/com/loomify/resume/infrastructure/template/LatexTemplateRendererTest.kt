package com.loomify.resume.infrastructure.template

import com.loomify.UnitTest
import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.exception.TemplateRenderingException
import com.loomify.resume.domain.model.CompanyName
import com.loomify.resume.domain.model.DegreeType
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.FieldOfStudy
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.InstitutionName
import com.loomify.resume.domain.model.JobTitle
import com.loomify.resume.domain.model.Location
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.PhoneNumber
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.Skill
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.SkillCategoryName
import com.loomify.resume.domain.model.SocialProfile
import com.loomify.resume.domain.model.Summary
import com.loomify.resume.domain.model.Url
import com.loomify.resume.domain.model.WorkExperience
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroupDir

/**
 * Unit test for LatexTemplateRenderer.
 * Tests LaTeX escaping, injection prevention, and template population.
 */
@UnitTest
class LatexTemplateRendererTest {

    private val templateGroup = mockk<STGroupDir>()
    private val renderer = LatexTemplateRenderer(templateGroup)

    @Test
    fun `should render valid resume data successfully`() {
        // Arrange
        val resumeData = createValidResumeData()
        val mockTemplate = mockk<ST>(relaxed = true)

        every { templateGroup.getInstanceOf("resume-template-en") } returns mockTemplate
        every { mockTemplate.render() } returns "\\documentclass{article}..."

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        result shouldBe "\\documentclass{article}..."
        verify { templateGroup.getInstanceOf("resume-template-en") }
        verify { mockTemplate.render() }
    }

    @Test
    fun `should throw TemplateRenderingException when template not found`() {
        // Arrange
        val resumeData = createValidResumeData()
        every { templateGroup.getInstanceOf("resume-template-fr") } returns null

        // Act & Assert
        val exception = shouldThrow<TemplateRenderingException> {
            renderer.render(resumeData, "fr")
        }
        exception.message shouldContain "Template not found for locale: fr"
    }

    @Test
    fun `should detect and reject LaTeX input command injection`() {
        // Arrange
        val maliciousData = createResumeDataWithMaliciousContent("\\input{/etc/passwd}")

        // Act & Assert
        val exception = shouldThrow<LaTeXInjectionException> {
            renderer.render(maliciousData, "en")
        }
        exception.message shouldContain "\\input"
    }

    @Test
    fun `should detect and reject LaTeX include command injection`() {
        // Arrange
        val maliciousData = createResumeDataWithMaliciousContent("\\include{malicious}")

        // Act & Assert
        val exception = shouldThrow<LaTeXInjectionException> {
            renderer.render(maliciousData, "en")
        }
        exception.message shouldContain "\\include"
    }

    @Test
    fun `should detect and reject LaTeX write command injection`() {
        // Arrange
        val maliciousData = createResumeDataWithMaliciousContent("\\write18{rm -rf /}")

        // Act & Assert
        val exception = shouldThrow<LaTeXInjectionException> {
            renderer.render(maliciousData, "en")
        }
        exception.message shouldContain "\\write"
    }

    @Test
    fun `should detect and reject LaTeX def command injection`() {
        // Arrange
        val maliciousData = createResumeDataWithMaliciousContent("\\def\\malicious{hack}")

        // Act & Assert
        val exception = shouldThrow<LaTeXInjectionException> {
            renderer.render(maliciousData, "en")
        }
        exception.message shouldContain "\\def"
    }

    @Test
    fun `should escape LaTeX special characters in name`() {
        // Arrange
        val resumeData = ResumeData(
            basics = PersonalInfo(
                fullName = FullName("John & Doe $ Test"),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = null,
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = listOf(
                WorkExperience(
                    company = CompanyName("Tech"),
                    position = JobTitle("Dev"),
                    startDate = "2020-01-01",
                    endDate = null,
                    location = null,
                    summary = null,
                    highlights = null,
                    url = null,
                ),
            ),
            education = emptyList(),
            skills = emptyList(),
            languages = emptyList(),
            projects = emptyList(),
        )
        val mockTemplate = mockk<ST>(relaxed = true)

        every { templateGroup.getInstanceOf("resume-template-en") } returns mockTemplate
        every { mockTemplate.render() } returns "rendered"

        // Act
        renderer.render(resumeData, "en")

        // Assert - verify that special characters were escaped in lastName
        verify { mockTemplate.add("firstName", "John") }
        verify { mockTemplate.add("lastName", "\\& Doe \\$ Test") }
    }

    @Test
    fun `should handle resume with work experience`() {
        // Arrange
        val resumeData = ResumeData(
            basics = PersonalInfo(
                fullName = FullName("John Doe"),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = null,
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = listOf(
                WorkExperience(
                    company = CompanyName("Tech Corp"),
                    position = JobTitle("Senior Developer"),
                    startDate = "2020-01-01",
                    endDate = "2023-12-31",
                    location = "New York, NY",
                    summary = "Led development team",
                    highlights = null,
                    url = null,
                ),
            ),
            education = emptyList(),
            skills = emptyList(),
            languages = emptyList(),
            projects = emptyList(),
        )
        val mockTemplate = mockk<ST>(relaxed = true)

        every { templateGroup.getInstanceOf("resume-template-en") } returns mockTemplate
        every { mockTemplate.render() } returns "rendered"

        // Act
        renderer.render(resumeData, "en")

        // Assert
        verify { mockTemplate.add("work", any<List<Map<String, String>>>()) }
    }

    @Test
    fun `should handle resume with education`() {
        // Arrange
        val resumeData = ResumeData(
            basics = PersonalInfo(
                fullName = FullName("John Doe"),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = null,
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = emptyList(),
            education = listOf(
                Education(
                    institution = InstitutionName("MIT"),
                    area = FieldOfStudy("Computer Science"),
                    studyType = DegreeType("Bachelor of Science"),
                    startDate = "2016-09-01",
                    endDate = "2020-06-01",
                    score = "4.0 GPA",
                    courses = null,
                ),
            ),
            skills = emptyList(),
            languages = emptyList(),
            projects = emptyList(),
        )
        val mockTemplate = mockk<ST>(relaxed = true)

        every { templateGroup.getInstanceOf("resume-template-en") } returns mockTemplate
        every { mockTemplate.render() } returns "rendered"

        // Act
        renderer.render(resumeData, "en")

        // Assert
        verify { mockTemplate.add("education", any<List<Map<String, String>>>()) }
    }

    @Test
    fun `should handle resume with skills`() {
        // Arrange
        val resumeData = ResumeData(
            basics = PersonalInfo(
                fullName = FullName("John Doe"),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = null,
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = emptyList(),
            education = emptyList(),
            skills = listOf(
                SkillCategory(
                    name = SkillCategoryName("Programming Languages"),
                    level = null,
                    keywords = listOf(
                        Skill("Kotlin"),
                        Skill("Java"),
                        Skill("Python"),
                    ),
                ),
            ),
            languages = emptyList(),
            projects = emptyList(),
        )
        val mockTemplate = mockk<ST>(relaxed = true)

        every { templateGroup.getInstanceOf("resume-template-en") } returns mockTemplate
        every { mockTemplate.render() } returns "rendered"

        // Act
        renderer.render(resumeData, "en")

        // Assert
        verify { mockTemplate.add("skills", any<List<Map<String, Any>>>()) }
    }

    // Helper methods

    private fun createValidResumeData(): ResumeData {
        return ResumeData(
            basics = PersonalInfo(
                fullName = FullName("John Doe"),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = JobTitle("Software Engineer"),
                url = Url("https://johndoe.com"),
                summary = Summary("Experienced software engineer"),
                location = Location(city = "New York"),
                profiles = listOf(
                    SocialProfile("LinkedIn", "johndoe", "https://linkedin.com/in/johndoe"),
                    SocialProfile("GitHub", "johndoe", "https://github.com/johndoe"),
                ),
            ),
            work = listOf(
                WorkExperience(
                    company = CompanyName("Tech Corp"),
                    position = JobTitle("Developer"),
                    startDate = "2020-01-01",
                    endDate = null,
                    location = null,
                    summary = null,
                    highlights = null,
                    url = null,
                ),
            ),
            education = emptyList(),
            skills = emptyList(),
            languages = emptyList(),
            projects = emptyList(),
        )
    }

    private fun createResumeDataWithMaliciousContent(maliciousText: String): ResumeData {
        return ResumeData(
            basics = PersonalInfo(
                fullName = FullName(maliciousText),
                email = Email("john@example.com"),
                phone = PhoneNumber("+1234567890"),
                label = null,
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = listOf(
                WorkExperience(
                    company = CompanyName("Company"),
                    position = JobTitle("Position"),
                    startDate = "2020-01-01",
                    endDate = null,
                    location = null,
                    summary = null,
                    highlights = null,
                    url = null,
                ),
            ),
            education = emptyList(),
            skills = emptyList(),
            languages = emptyList(),
            projects = emptyList(),
        )
    }
}
