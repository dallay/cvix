package com.loomify.resume.infrastructure.template.validator

import com.loomify.UnitTest
import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.Language
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.PhoneNumber
import com.loomify.resume.domain.model.Project
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.Summary
import com.loomify.resume.domain.model.WorkExperience
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class TemplateValidatorTest {

    private fun createMockResumeData(
        name: String = "John Doe",
        email: String = "john.doe@example.com",
        phone: String? = null,
        summary: String? = null,
        work: List<WorkExperience> = emptyList(),
        education: List<Education> = emptyList(),
        skills: List<SkillCategory> = listOf(mockk(relaxed = true)),
        projects: List<Project> = emptyList(),
        languages: List<Language> = emptyList(),
    ): ResumeData {
        val personalInfo = mockk<PersonalInfo>(relaxed = true)

        // Use real instances for inline value classes (they can't be mocked)
        val nameValue = FullName(name)
        val emailValue = Email(email)

        every { personalInfo.name } returns nameValue
        every { personalInfo.email } returns emailValue
        every { personalInfo.phone } returns phone?.let { PhoneNumber(it) }
        every { personalInfo.summary } returns summary?.let { Summary(it) }

        val resumeData = mockk<ResumeData>(relaxed = true)
        every { resumeData.basics } returns personalInfo
        every { resumeData.work } returns work
        every { resumeData.education } returns education
        every { resumeData.skills } returns skills
        every { resumeData.projects } returns projects
        every { resumeData.languages } returns languages

        return resumeData
    }

    @Test
    fun `should throw exception when dangerous LaTeX commands are detected`() {
        val resumeData = createMockResumeData(name = "\\input{malicious.tex}")

        assertThrows<LaTeXInjectionException> {
            TemplateValidator.validateContent(resumeData)
        }
    }

    @Test
    fun `should not throw exception when no dangerous LaTeX commands are present`() {
        val resumeData = createMockResumeData(name = "John Doe")

        TemplateValidator.validateContent(resumeData)
    }

    @Test
    fun `should validate all fields in resume data for dangerous LaTeX commands`() {
        val resumeData = createMockResumeData(
            name = "John Doe",
            summary = "Experienced developer \\include{image.png}",
        )

        assertThrows<LaTeXInjectionException> {
            TemplateValidator.validateContent(resumeData)
        }
    }

    @Test
    fun `should handle null fields gracefully without throwing exceptions`() {
        val resumeData = createMockResumeData(
            name = "John Doe",
            phone = null,
            summary = null,
        )

        TemplateValidator.validateContent(resumeData)
    }
}
