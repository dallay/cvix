package com.loomify.resume.domain.model

import com.loomify.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for new JSON Resume Schema domain models.
 */
@UnitTest
class JsonResumeSchemaTest {

    @Test
    fun `should create volunteer entry`() {
        val volunteer = Volunteer(
            organization = "Red Cross",
            position = "Volunteer Coordinator",
            url = Url("https://redcross.org"),
            startDate = "2020-01",
            endDate = "2021-06",
            summary = "Coordinated volunteers for disaster relief",
            highlights = listOf("Managed team of 50 volunteers")
        )

        volunteer shouldNotBe null
        volunteer.organization shouldBe "Red Cross"
        volunteer.position shouldBe "Volunteer Coordinator"
    }

    @Test
    fun `should create award entry`() {
        val award = Award(
            title = "Employee of the Year",
            date = "2022-12",
            awarder = "ACME Corp",
            summary = "Recognized for exceptional performance"
        )

        award shouldNotBe null
        award.title shouldBe "Employee of the Year"
    }

    @Test
    fun `should create certificate entry`() {
        val certificate = Certificate(
            name = "AWS Certified Solutions Architect",
            date = "2023-03",
            url = Url("https://aws.amazon.com/certification"),
            issuer = "Amazon Web Services"
        )

        certificate shouldNotBe null
        certificate.name shouldBe "AWS Certified Solutions Architect"
    }

    @Test
    fun `should create publication entry`() {
        val publication = Publication(
            name = "Microservices Architecture",
            publisher = "Tech Journal",
            releaseDate = "2022-01",
            url = Url("https://techjournal.com/article"),
            summary = "An overview of microservices patterns"
        )

        publication shouldNotBe null
        publication.name shouldBe "Microservices Architecture"
    }

    @Test
    fun `should create interest entry`() {
        val interest = Interest(
            name = "Open Source",
            keywords = listOf("Kotlin", "Spring Boot", "React")
        )

        interest shouldNotBe null
        interest.name shouldBe "Open Source"
        interest.keywords?.size shouldBe 3
    }

    @Test
    fun `should create reference entry`() {
        val reference = Reference(
            name = "Jane Smith",
            reference = "John is an exceptional developer with strong leadership skills"
        )

        reference shouldNotBe null
        reference.name shouldBe "Jane Smith"
    }

    @Test
    fun `should create meta entry`() {
        val meta = Meta(
            canonical = "https://example.com/resume.json",
            version = "v1.0.0",
            lastModified = "2024-01-15T10:30:00Z"
        )

        meta shouldNotBe null
        meta.version shouldBe "v1.0.0"
    }

    @Test
    fun `should create language with free-form fluency`() {
        val language = Language(
            language = "English",
            fluency = "Native speaker"
        )

        language shouldNotBe null
        language.fluency shouldBe "Native speaker"

        // Test other fluency descriptions
        val language2 = Language("Spanish", "Professional working proficiency")
        language2.fluency shouldBe "Professional working proficiency"

        val language3 = Language("French", "Basic conversational")
        language3.fluency shouldBe "Basic conversational"
    }
}
