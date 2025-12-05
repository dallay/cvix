package com.cvix.resume.infrastructure.template

import com.cvix.FixtureDataLoader
import com.cvix.FixtureDataLoader.readResource
import com.cvix.UnitTest
import com.cvix.common.domain.vo.email.Email
import com.cvix.resume.domain.Basics
import com.cvix.resume.domain.FullName
import com.cvix.resume.domain.JobTitle
import com.cvix.resume.domain.Location
import com.cvix.resume.domain.PhoneNumber
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.Skill
import com.cvix.resume.domain.SkillCategory
import com.cvix.resume.domain.SkillCategoryName
import com.cvix.resume.domain.Summary
import com.cvix.resume.domain.exception.LaTeXInjectionException
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.GenerateResumeRequest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Comprehensive test suite for LaTeX template rendering.
 *
 * This test class validates that the LaTeX renderer correctly handles:
 * - Complete resume data with all fields populated (john-doe fixture)
 * - Minimal resume data with only required fields
 * - Special LaTeX characters that must be escaped ($, %, &, #, _, {, }, ~, ^)
 * - Unicode and international characters (accents, non-ASCII)
 * - Long content that might cause line breaks or pagination issues
 * - Null/empty optional fields
 * - Malicious LaTeX injection attempts
 * - Valid LaTeX compilation (document structure, environments, commands)
 */
@UnitTest
@Suppress("StringShouldBeRawString")
internal class LatexTemplateRendererTest {

    private val fixedClock = Clock.fixed(Instant.parse("2025-11-15T00:00:00Z"), ZoneId.systemDefault())
    private val renderer = LatexTemplateRenderer(fixedClock)

    /**
     * Flag to persist generated LaTeX files for manual inspection.
     * Set to true during development or debugging.
     */
    private val persistGeneratedDocument: Boolean =
        System.getenv("PERSIST_LATEX_FILES")?.toBoolean() ?: false

    /**
     * Test the complete resume fixture with all possible fields populated.
     * This is the primary comprehensive test that validates the full feature set.
     */
    @Test
    fun `should render complete resume with all fields populated`() {
        assertResumeRendersCorrectly("john-doe")
    }

    @Test
    fun `should render complete resume in spanish`() {
        assertResumeRendersCorrectly("spanish-example", "es")
    }

    /**
     * Test rendering with minimal required fields only.
     * Validates that optional fields don't break the template.
     */
    @Test
    fun `should render minimal resume with only required fields`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/minimal-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert - verify LaTeX structure is valid
        assertValidLatexStructure(result)
        assertTrue(result.contains("Jane Minimal"), "Should contain the person's name")
        assertTrue(result.contains("jane@example.com"), "Should contain the email")
    }

    /**
     * Test that all LaTeX special characters are properly escaped.
     * Critical for preventing LaTeX compilation errors and math mode issues.
     */
    @Test
    fun `should properly escape LaTeX special characters`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/special-chars-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert - verify critical escapes
        assertTrue(
            result.contains("\\$5B") || result.contains("\\$10M"),
            "Dollar signs must be escaped",
        )
        assertTrue(
            result.contains("40\\%") || result.contains("50\\%"),
            "Percent signs must be escaped",
        )
        assertTrue(result.contains("\\&"), "Ampersands must be escaped")
        assertTrue(result.contains("\\#"), "Hash symbols must be escaped")
        assertTrue(result.contains("\\_"), "Underscores must be escaped in text")
        assertTrue(
            result.contains("\\textbackslash\\{\\}"),
            "Backslashes must be escaped as \\textbackslash with escaped braces",
        )

        // Verify document compiles without math mode errors
        assertValidLatexStructure(result)

        if (persistGeneratedDocument) {
            persistOutput("special-chars-resume", result)
        }
    }

    /**
     * Test rendering with Unicode characters and international text.
     * Validates UTF-8 encoding and proper handling of accents.
     */
    @Test
    fun `should handle Unicode and international characters`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/unicode-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        assertValidLatexStructure(result)
        assertTrue(result.contains("José García"), "Should preserve Unicode in names")
        assertTrue(result.contains("Développeur"), "Should preserve French accents")
        assertTrue(result.contains("Français"), "Should preserve accented characters")

        if (persistGeneratedDocument) {
            persistOutput("unicode-resume", result)
        }
    }

    /**
     * Test rendering with very long content in various fields.
     * Validates handling of extensive text blocks and large lists.
     */
    @Test
    fun `should handle long content without breaking LaTeX structure`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/long-content-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        assertValidLatexStructure(result)

        // Count items in highlights (should be 10)
        val highlightCount = result.split("\\item").size - 1
        assertTrue(highlightCount >= 10, "Should render all 10 highlights")

        if (persistGeneratedDocument) {
            persistOutput("long-content-resume", result)
        }
    }

    /**
     * Test rendering with null/empty optional fields.
     * Validates that missing data doesn't cause crashes or invalid LaTeX.
     */
    @Test
    fun `should handle null and empty optional fields gracefully`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/null-fields-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        assertValidLatexStructure(result)
        assertTrue(result.contains("Empty Fields Test"), "Should contain the name")

        if (persistGeneratedDocument) {
            persistOutput("null-fields-resume", result)
        }
    }

    /**
     * Test rendering with empty endDate (current employment).
     * Validates that empty endDate strings are treated as ongoing employment
     * and render "Present" (or locale-specific equivalent).
     */
    @Test
    fun `should render empty endDate as Present in English`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/null-fields-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert
        assertValidLatexStructure(result)
        // Should contain the exact formatted range for the Current Company entry
        assertTrue(
            result.contains("2023-03-14 – Present"),
            "Should render full range '2023-03-14 – Present' for ongoing employment in English"
        )
        // Optionally, ensure no unlabelled dangling em-dash (e.g., '2023-03-14 –' followed by whitespace/newline)
        assertFalse(
            Regex("""2023-03-14 –\s*\n""").containsMatchIn(result),
            "Should not have dangling em-dash for empty endDate"
        )

        if (persistGeneratedDocument) {
            persistOutput("null-fields-resume-en", result)
        }
    }

    /**
     * Test rendering with empty endDate in Spanish.
     * Validates locale-specific "Presente" rendering.
     */
    @Test
    fun `should render empty endDate as Presente in Spanish`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/null-fields-resume.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "es")

        // Assert
        assertValidLatexStructure(result)
        // Should contain the exact formatted range for the Current Company entry
        assertTrue(
            result.contains("2023-03-14 – Presente"),
            "Should render full range '2023-03-14 – Presente' for ongoing employment in Spanish"
        )
        // Optionally, ensure no unlabelled dangling em-dash (e.g., '2023-03-14 –' followed by whitespace/newline)
        assertFalse(
            Regex("""2023-03-14 –\s*\n""").containsMatchIn(result),
            "Should not have dangling em-dash for empty endDate"
        )

        if (persistGeneratedDocument) {
            persistOutput("null-fields-resume-es", result)
        }
    }

    /**
     * Parameterized test to verify dangerous LaTeX commands are blocked.
     * Security test to prevent LaTeX injection attacks.
     */
    @ParameterizedTest(name = "should reject {0}")
    @ValueSource(
        strings = [
            "\\input{malicious}", "\\include{evil}", "\\write{file}",
            "\\def\\bad{}", "\\newcommand{\\hack}{}",
        ],
    )
    fun `should reject dangerous LaTeX commands`(dangerousCommand: String) {
        // Arrange: Create minimal resume with malicious command in name field
        val maliciousResume = Resume(
            basics = Basics(
                name = FullName(dangerousCommand),
                label = JobTitle("Engineer"),
                image = null,
                email = Email("test@example.com"),
                phone = PhoneNumber("+1-555-0100"),
                url = null,
                summary = Summary("Clean summary"),
                location = Location(
                    address = "123 Main St",
                    postalCode = "12345",
                    city = "TestCity",
                    countryCode = "US",
                    region = "CA",
                ),
                profiles = emptyList(),
            ),
            work = emptyList(),
            volunteer = emptyList(),
            education = emptyList(),
            awards = emptyList(),
            certificates = emptyList(),
            publications = emptyList(),
            skills = listOf(
                SkillCategory(
                    name = SkillCategoryName("Technical"),
                    level = null,
                    keywords = listOf(Skill("Kotlin")),
                ),
            ),
            languages = emptyList(),
            interests = emptyList(),
            references = emptyList(),
            projects = emptyList(),
        )

        // Act & Assert: Verify LaTeXInjectionException is thrown
        assertThrows(LaTeXInjectionException::class.java) {
            renderer.render(maliciousResume, "en")
        }
    }

    /**
     * Test that all valid LaTeX structure elements are present.
     */
    @Test
    fun `should generate valid LaTeX document structure`() {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/john-doe.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, "en")

        // Assert - verify all critical LaTeX document components
        assertTrue(result.contains("\\documentclass"), "Must have document class")
        assertTrue(result.contains("\\begin{document}"), "Must have document begin")
        assertTrue(result.contains("\\end{document}"), "Must have document end")
        assertTrue(result.contains("\\usepackage"), "Must load packages")
        assertTrue(result.contains("\\section"), "Must have sections")

        // Verify no unescaped math mode triggers from user content
        // The template itself uses $|$ in \sbox\ANDbox which is intentional
        val unescapedDollars = Regex("(?<!\\\\)\\$(?!\\$)(?!\\|\\$)")
        val userContentLines = result.lines().filter {
            !it.contains("\\sbox\\ANDbox") // Skip the intentional template math mode
        }
        val matches = userContentLines.flatMap { line ->
            unescapedDollars.findAll(line).toList()
        }
        assertTrue(
            matches.isEmpty(),
            "Should not have unescaped dollar signs in user content (found: ${matches.map { it.value }})",
        )
    }

    // Helper methods

    /**
     * Reusable assertion to verify a resume fixture renders correctly.
     */
    private fun assertResumeRendersCorrectly(fixtureName: String, locale: String = "en") {
        // Arrange
        val resumeJsonData: GenerateResumeRequest =
            FixtureDataLoader.fromResource("data/json/$fixtureName.json")
        val resumeData = ResumeRequestMapper.toDomain(resumeJsonData)

        // Act
        val result = renderer.render(resumeData, locale)

        if (persistGeneratedDocument) {
            persistOutput(fixtureName, result)
        }

        // Assert
        val expectedLatex: String = readResource("data/latex/$fixtureName.tex")
        assertEquals(
            normalize(expectedLatex),
            normalize(result),
            "Rendered LaTeX differs from expected fixture. See build/test-output/$fixtureName.tex for actual output.",
        )
    }

    /**
     * Assert that the LaTeX output has valid structure.
     */
    private fun assertValidLatexStructure(latex: String) {
        assertTrue(latex.contains("\\documentclass"), "Missing document class")
        assertTrue(latex.contains("\\begin{document}"), "Missing document begin")
        assertTrue(latex.contains("\\end{document}"), "Missing document end")

        // Verify balanced environments
        val beginCount = latex.split("\\begin{").size - 1
        val endCount = latex.split("\\end{").size - 1
        assertEquals(beginCount, endCount, "Unbalanced LaTeX environments")
    }

    /**
     * Persist generated LaTeX to file for manual inspection.
     */
    private fun persistOutput(name: String, content: String) {
        val safeName = name.replace(Regex("[^a-zA-Z0-9_-]"), "")
        val outputPath = "build/test-output/$safeName.tex"
        java.io.File(outputPath).apply {
            parentFile.mkdirs()
            writeText(content)
        }
    }
    /**
     * Normalize line endings for cross-platform comparison.
     */
    private fun normalize(text: String): String =
        text.replace("\r\n", "\n").replace("\r", "\n")
            .trim()
}
