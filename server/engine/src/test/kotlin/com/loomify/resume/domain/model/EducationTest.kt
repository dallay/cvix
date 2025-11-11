package com.loomify.resume.domain.model

import com.loomify.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.Locale
import org.junit.jupiter.api.Test

/**
 * Unit tests for Education entity.
 *
 * Tests validation rules:
 * - Institution name required, max 100 chars
 * - Field of study required, max 100 chars
 * - Degree type required, max 100 chars
 * - Date validation: endDate >= startDate
 */
@UnitTest
class EducationTest {

    @Test
    fun `should create education with all fields`() {
        // Arrange & Act
        val education = Education(
            institution = InstitutionName("MIT"),
            area = FieldOfStudy("Computer Science"),
            studyType = DegreeType("Bachelor of Science"),
            startDate = "2015-09-01",
            endDate = "2019-05-31",
            score = "3.8",
            courses = listOf("Data Structures", "Algorithms", "Operating Systems"),
        )

        // Assert
        education shouldNotBe null
        education.institution.value shouldBe "MIT"
        education.area?.value shouldBe "Computer Science"
        education.studyType?.value shouldBe "Bachelor of Science"
        education.score shouldBe "3.8"
        education.courses?.size shouldBe 3
    }

    @Test
    fun `should create education with minimum required fields`() {
        // Arrange & Act
        val education = Education(
            institution = InstitutionName("Stanford University"),
            area = FieldOfStudy("Engineering"),
            studyType = DegreeType("Master"),
            startDate = "2020-09-01",
            endDate = null, // Currently enrolled
            score = null,
            courses = null,
        )

        // Assert
        education shouldNotBe null
        education.endDate shouldBe null
        education.score shouldBe null
        education.courses shouldBe null
    }

    @Test
    fun `should format period with both dates`() {
        // Arrange
        val education = Education(
            institution = InstitutionName("MIT"),
            area = FieldOfStudy("Computer Science"),
            studyType = DegreeType("Bachelor"),
            startDate = "2015-09-01",
            endDate = "2019-05-31",
            score = null,
            courses = null,
        )

        // Act
        val period = education.formatPeriod(Locale.ENGLISH)

        // Assert
        period shouldBe "2015-09-01 -- 2019-05-31"
    }

    @Test
    fun `should format period for ongoing education in English`() {
        // Arrange
        val education = Education(
            institution = InstitutionName("MIT"),
            area = FieldOfStudy("Computer Science"),
            studyType = DegreeType("PhD"),
            startDate = "2020-09-01",
            endDate = null,
            score = null,
            courses = null,
        )

        // Act
        val period = education.formatPeriod(Locale.ENGLISH)

        // Assert
        period shouldBe "2020-09-01 -- Present"
    }

    @Test
    fun `should format period for ongoing education in Spanish`() {
        // Arrange
        val education = Education(
            institution = InstitutionName("MIT"),
            area = FieldOfStudy("Computer Science"),
            studyType = DegreeType("PhD"),
            startDate = "2020-09-01",
            endDate = null,
            score = null,
            courses = null,
        )

        // Act
        val period = education.formatPeriod(Locale.forLanguageTag("es"))

        // Assert
        period shouldBe "2020-09-01 -- Presente"
    }

    @Test
    fun `should fail when end date is before start date`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            Education(
                institution = InstitutionName("MIT"),
                area = FieldOfStudy("Computer Science"),
                studyType = DegreeType("Bachelor"),
                startDate = "2019-05-31",
                endDate = "2015-09-01", // Before start date
                score = null,
                courses = null,
            )
        }.message shouldBe "End date must be after or equal to start date"
    }
}
