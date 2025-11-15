package com.loomify.resume.infrastructure.template

import com.loomify.UnitTest
import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.model.CompanyName
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.FieldOfStudy
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.Highlight
import com.loomify.resume.domain.model.InstitutionName
import com.loomify.resume.domain.model.JobTitle
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.PhoneNumber
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.Skill
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.SkillCategoryName
import com.loomify.resume.domain.model.WorkExperience
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

/**
 * Unit test for LatexTemplateRenderer i18n functionality.
 * Tests that the template correctly renders with English and Spanish translations.
 */
@UnitTest
class LatexTemplateRendererI18nTest {

    private val renderer = LatexTemplateRenderer()

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

    private fun createResumeDataWithAllSections(): ResumeData {
        return ResumeData(
            basics = PersonalInfo(
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
                    company = CompanyName("Tech Corp"),
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
