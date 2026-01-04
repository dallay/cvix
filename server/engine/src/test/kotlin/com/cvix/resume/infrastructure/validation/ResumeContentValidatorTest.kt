package com.cvix.resume.infrastructure.validation

import com.cvix.UnitTest
import com.cvix.resume.infrastructure.http.request.GenerateResumeRequest
import com.cvix.resume.infrastructure.http.request.ResumeContentRequest
import com.cvix.resume.infrastructure.http.request.dto.BasicsDto
import com.cvix.resume.infrastructure.http.request.dto.EducationDto
import com.cvix.resume.infrastructure.http.request.dto.SkillCategoryDto
import com.cvix.resume.infrastructure.http.request.dto.WorkExperienceDto
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@UnitTest
class ResumeContentValidatorTest {
    private val validator = ResumeContentValidator()

    @Test
    fun `should return true when request is null`() {
        // Act
        val result = validator.isValid(null, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when work experience is present`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics(),
            work = listOf(
                WorkExperienceDto(
                    name = "Acme Corp",
                    position = "Software Engineer",
                    startDate = "2020-01-01"
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when education is present`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics(),
            education = listOf(
                EducationDto(
                    institution = "University of Example",
                    startDate = "2015-09-01"
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when skills are present`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics(),
            skills = listOf(
                SkillCategoryDto(
                    name = "Programming",
                    keywords = listOf("Kotlin", "TypeScript")
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when all sections are present`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics(),
            work = listOf(
                WorkExperienceDto(
                    name = "Acme Corp",
                    position = "Software Engineer",
                    startDate = "2020-01-01"
                )
            ),
            education = listOf(
                EducationDto(
                    institution = "University of Example",
                    startDate = "2015-09-01"
                )
            ),
            skills = listOf(
                SkillCategoryDto(
                    name = "Programming",
                    keywords = listOf("Kotlin", "TypeScript")
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false when all sections are empty`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics(),
            work = emptyList(),
            education = emptyList(),
            skills = emptyList()
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should return false when all sections are null`() {
        // Arrange
        val request = GenerateResumeRequest(
            templateId = "template-123",
            basics = createBasics()
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe false
    }

    private fun createBasics() = BasicsDto(
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "+1234567890"
    )
}

@UnitTest
class ResumeContentRequestValidatorTest {
    private val validator = ResumeContentRequestValidator()

    @Test
    fun `should return true when request is null`() {
        // Act
        val result = validator.isValid(null, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when work experience is present`() {
        // Arrange
        val request = ResumeContentRequest(
            basics = createBasics(),
            work = listOf(
                WorkExperienceDto(
                    name = "Acme Corp",
                    position = "Software Engineer",
                    startDate = "2020-01-01"
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when education is present`() {
        // Arrange
        val request = ResumeContentRequest(
            basics = createBasics(),
            education = listOf(
                EducationDto(
                    institution = "University of Example",
                    startDate = "2015-09-01"
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return true when skills are present`() {
        // Arrange
        val request = ResumeContentRequest(
            basics = createBasics(),
            skills = listOf(
                SkillCategoryDto(
                    name = "Programming",
                    keywords = listOf("Kotlin", "TypeScript")
                )
            )
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false when all sections are empty`() {
        // Arrange
        val request = ResumeContentRequest(
            basics = createBasics(),
            work = emptyList(),
            education = emptyList(),
            skills = emptyList()
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should return false when all sections are null`() {
        // Arrange
        val request = ResumeContentRequest(
            basics = createBasics()
        )

        // Act
        val result = validator.isValid(request, null)

        // Assert
        result shouldBe false
    }

    private fun createBasics() = BasicsDto(
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "+1234567890"
    )
}
