package com.loomify.resume.infrastructure.template

import com.loomify.UnitTest
import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.Basics
import com.loomify.resume.domain.CompanyName
import com.loomify.resume.domain.Education
import com.loomify.resume.domain.FieldOfStudy
import com.loomify.resume.domain.FullName
import com.loomify.resume.domain.Highlight
import com.loomify.resume.domain.InstitutionName
import com.loomify.resume.domain.JobTitle
import com.loomify.resume.domain.PhoneNumber
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.Skill
import com.loomify.resume.domain.SkillCategory
import com.loomify.resume.domain.SkillCategoryName
import com.loomify.resume.domain.WorkExperience
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotContain
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.jupiter.api.Test

/**
 * Unit test for LatexTemplateRenderer i18n functionality.
 * Tests that the template correctly renders with English and Spanish translations.
 */
@UnitTest
class LatexTemplateRendererI18nTest {

    private val fixedClock = Clock.fixed(Instant.parse("2025-11-15T00:00:00Z"), ZoneId.systemDefault())
    private val renderer = LatexTemplateRenderer(fixedClock)

    @Test
    fun `should render resume with English translations`() {
        // Arrange
        val resumeData = createResumeDataWithAllSections()

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        result.shouldNotBeEmpty()

        // Verify English section titles
        result shouldContain "\\section{Education}"
        result shouldContain "\\section{Experience}"
        result shouldContain "\\section{Skills}"

        // Verify English labels
        result shouldContain "GPA:"
        result shouldContain "Coursework:"
        result shouldContain "Resume/CV"

        // Should NOT contain Spanish translations
        result shouldNotContain "\\section{Educación}"
        result shouldNotContain "\\section{Experiencia}"
        result shouldNotContain "\\section{Habilidades}"
        result shouldNotContain "Promedio:"
    }

    @Test
    fun `should render resume with Spanish translations`() {
        // Arrange
        val resumeData = createResumeDataWithAllSections()

        // Act
        val result = renderer.render(resumeData, "es")

        // Assert
        result.shouldNotBeEmpty()

        // Verify Spanish section titles
        result shouldContain "\\section{Educación}"
        result shouldContain "\\section{Experiencia}"
        result shouldContain "\\section{Habilidades}"

        // Verify Spanish labels
        result shouldContain "Promedio:"
        result shouldContain "Materias Cursadas:"
        result shouldContain "Currículum Vitae"

        // Should NOT contain English translations
        result shouldNotContain "\\section{Education}"
        result shouldNotContain "\\section{Experience}"
        result shouldNotContain "\\section{Skills}"
        result shouldNotContain "GPA:"
    }

    @Test
    fun `should use locale in PDF metadata`() {
        // Arrange
        val resumeData = createResumeDataWithAllSections()

        // Act - English
        val resultEn = renderer.render(resumeData, "en")

        // Assert - English
        resultEn shouldContain "pdflang={en}"

        // Act - Spanish
        val resultEs = renderer.render(resumeData, "es")

        // Assert - Spanish
        resultEs shouldContain "pdflang={es}"
    }

    @Test
    fun `should fallback to English for unsupported locale`() {
        // Arrange
        val resumeData = createResumeDataWithAllSections()

        // Act - Unsupported locale should fallback to English
        val result = renderer.render(resumeData, "fr")

        // Assert - Should use English translations as fallback
        result.shouldNotBeEmpty()
        result shouldContain "\\section{Education}"
        result shouldContain "\\section{Experience}"
    }

    private fun createResumeDataWithAllSections(): Resume {
        return Resume(
            basics = Basics(
                name = FullName("Juan Pérez"),
                email = Email("juan@example.com"),
                phone = PhoneNumber("+34 123 456 789"),
                label = JobTitle("Software Engineer"),
                url = null,
                summary = null,
                location = null,
                profiles = emptyList(),
            ),
            work = listOf(
                WorkExperience(
                    name = CompanyName("Tech Corp"),
                    position = JobTitle("Senior Developer"),
                    startDate = "2020-01-01",
                    endDate = "2023-12-31",
                    location = "Madrid, Spain",
                    summary = "Led development team",
                    highlights = listOf(
                        Highlight("Implemented microservices architecture"),
                        Highlight("Reduced deployment time by 50%"),
                    ),
                    url = null,
                ),
            ),
            education = listOf(
                Education(
                    institution = InstitutionName("Universidad Politécnica"),
                    area = FieldOfStudy("Computer Science"),
                    studyType = null,
                    startDate = "2016-09-01",
                    endDate = "2020-06-01",
                    score = "3.8 GPA",
                    courses = listOf("Algorithms", "Data Structures", "Database Systems"),
                    url = null,
                ),
            ),
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
    }
}
