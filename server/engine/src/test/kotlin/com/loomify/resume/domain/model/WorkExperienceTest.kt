package com.loomify.resume.domain.model

import com.loomify.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDate
import java.util.Locale
import org.junit.jupiter.api.Test

/**
 * Unit tests for WorkExperience entity.
 *
 * Tests validation rules:
 * - Company name required, max 100 chars
 * - Position required, max 100 chars
 * - Date validation: endDate >= startDate
 * - Duration calculation
 */
@UnitTest
class WorkExperienceTest {

    @Test
    fun `should create work experience with all fields`() {
        // Arrange & Act
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Software Engineer"),
            startDate = "2020-01-01",
            endDate = "2023-12-31",
            location = "San Francisco, CA",
            summary = "Developed scalable web applications",
            highlights = listOf(
                Highlight("Led team of 5 developers"),
                Highlight("Reduced deployment time by 50%"),
            ),
            url = Url("https://acme.com"),
        )

        // Assert
        workExperience shouldNotBe null
        workExperience.name.value shouldBe "ACME Corp"
        workExperience.position.value shouldBe "Software Engineer"
        workExperience.startDate shouldBe "2020-01-01"
        workExperience.endDate shouldBe "2023-12-31"
    }

    @Test
    fun `should create work experience with minimum required fields`() {
        // Arrange & Act
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Developer"),
            startDate = "2020-01-01",
            endDate = null,
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Assert
        workExperience shouldNotBe null
        workExperience.endDate shouldBe null
    }

    @Test
    fun `should calculate duration in years for completed experience`() {
        // Arrange
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Engineer"),
            startDate = "2020-01-01",
            endDate = "2023-12-31",
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Act
        val duration = workExperience.durationInYears()

        // Assert
        duration.shouldBeWithinPercentageOf(4.0, 0.5) // Within 0.5% of 4 years
    }

    @Test
    fun `should calculate duration in years for current experience`() {
        // Arrange
        val startDate = LocalDate.now().minusYears(2).minusMonths(6).toString()
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Engineer"),
            startDate = startDate,
            endDate = null, // Current job
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Act
        val duration = workExperience.durationInYears()

        // Assert
        duration.shouldBeWithinPercentageOf(2.5, 1.0) // Within 1% of 2.5 years
    }

    @Test
    fun `should format period with both dates`() {
        // Arrange
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Engineer"),
            startDate = "2020-01-01",
            endDate = "2023-12-31",
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Act
        val period = workExperience.formatPeriod(Locale.ENGLISH)

        // Assert
        period shouldBe "2020-01-01 -- 2023-12-31"
    }

    @Test
    fun `should format period for current job in English`() {
        // Arrange
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Engineer"),
            startDate = "2020-01-01",
            endDate = null,
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Act
        val period = workExperience.formatPeriod(Locale.ENGLISH)

        // Assert
        period shouldBe "2020-01-01 -- Present"
    }

    @Test
    fun `should format period for current job in Spanish`() {
        // Arrange
        val workExperience = WorkExperience(
            name = CompanyName("ACME Corp"),
            position = JobTitle("Engineer"),
            startDate = "2020-01-01",
            endDate = null,
            location = null,
            summary = null,
            highlights = null,
            url = null,
        )

        // Act
        val period = workExperience.formatPeriod(Locale("es"))

        // Assert
        period shouldBe "2020-01-01 -- Presente"
    }

    @Test
    fun `should fail when end date is before start date`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            WorkExperience(
                name = CompanyName("ACME Corp"),
                position = JobTitle("Engineer"),
                startDate = "2023-12-31",
                endDate = "2020-01-01", // Before start date
                location = null,
                summary = null,
                highlights = null,
                url = null,
            )
        }.message shouldBe "End date (2020-01-01) must be on or after start date (2023-12-31)"
    }
}
