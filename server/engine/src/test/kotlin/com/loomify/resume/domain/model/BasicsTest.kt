package com.loomify.resume.domain.model

import com.loomify.UnitTest
import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.Basics
import com.loomify.resume.domain.FullName
import com.loomify.resume.domain.JobTitle
import com.loomify.resume.domain.Location
import com.loomify.resume.domain.PhoneNumber
import com.loomify.resume.domain.SocialProfile
import com.loomify.resume.domain.Summary
import com.loomify.resume.domain.Url
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for Basics value object.
 *
 * Tests validation rules:
 * - Full name required, max 100 chars
 * - Email required and valid format
 * - Job title optional, max 100 chars
 * - Summary optional, max 500 chars
 */
@UnitTest
class BasicsTest {

    @Test
    fun `should create personal info with all fields`() {
        // Arrange & Act
        val personalInfo = Basics(
            name = FullName("Jane Doe"),
            label = JobTitle("Senior Software Engineer"),
            email = Email("jane.doe@example.com"),
            phone = PhoneNumber("+1 (555) 123-4567"),
            url = Url("https://janedoe.com"),
            summary = Summary("Experienced software engineer with 10+ years building scalable applications"),
            location = Location(
                address = "123 Main St",
                postalCode = "94102",
                city = "San Francisco",
                countryCode = "US",
                region = "California",
            ),
            profiles = listOf(
                SocialProfile(
                    network = "LinkedIn",
                    username = "janedoe",
                    url = "https://linkedin.com/in/janedoe",
                ),
            ),
        )

        // Assert
        personalInfo shouldNotBe null
        personalInfo.name.value shouldBe "Jane Doe"
        personalInfo.label?.value shouldBe "Senior Software Engineer"
        personalInfo.phone?.value shouldBe "+1 (555) 123-4567"
        personalInfo.location?.city shouldBe "San Francisco"
        personalInfo.profiles.size shouldBe 1
    }

    @Test
    fun `should create personal info with minimum required fields`() {
        // Arrange & Act
        val personalInfo = Basics(
            name = FullName("John Smith"),
            label = null,
            email = Email("john@example.com"),
            phone = null,
            url = null,
            summary = null,
            location = null,
            profiles = emptyList(),
        )

        // Assert
        personalInfo shouldNotBe null
        personalInfo.name.value shouldBe "John Smith"
        personalInfo.label shouldBe null
        personalInfo.phone shouldBe null
    }

    @Test
    fun `should fail when full name is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            FullName("")
        }.message shouldBe "Full name cannot be blank"
    }

    @Test
    fun `should fail when full name exceeds max length`() {
        // Arrange
        val longName = "a".repeat(101)

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            FullName(longName)
        }.message shouldBe "Full name cannot exceed 100 characters"
    }

    @Test
    fun `should fail when job title is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            JobTitle("")
        }.message shouldBe "Job title cannot be blank"
    }

    @Test
    fun `should fail when job title exceeds max length`() {
        // Arrange
        val longTitle = "a".repeat(101)

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            JobTitle(longTitle)
        }.message shouldBe "Job title cannot exceed 100 characters"
    }

    @Test
    fun `should fail when summary exceeds max length`() {
        // Arrange
        val longSummary = "a".repeat(601)

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            Summary(longSummary)
        }.message shouldBe "Summary cannot exceed 600 characters"
    }

    @Test
    fun `should accept valid phone number`() {
        // Act
        val phone = PhoneNumber("+1234567890")

        // Assert
        phone.value shouldBe "+1234567890"
    }

    @Test
    fun `should fail when phone number is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            PhoneNumber("")
        }.message shouldBe "Phone number cannot be blank"
    }

    @Test
    fun `should accept valid URL`() {
        // Act
        val url = Url("https://example.com")

        // Assert
        url.value shouldBe "https://example.com"
    }

    @Test
    fun `should fail when URL is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            Url("")
        }.message shouldBe "URL cannot be blank"
    }
}
