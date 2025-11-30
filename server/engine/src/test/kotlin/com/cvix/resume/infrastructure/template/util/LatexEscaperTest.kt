package com.cvix.resume.infrastructure.template.util

import com.cvix.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@UnitTest
@Suppress("StringShouldBeRawString")
internal class LatexEscaperTest {

    @Test
    fun `should escape ampersand character`() {
        val result = LatexEscaper.escape("Smith & Jones")

        result shouldBe "Smith \\& Jones"
    }

    @Test
    fun `should escape percent character`() {
        val result = LatexEscaper.escape("100% complete")

        result shouldBe "100\\% complete"
    }

    @Test
    fun `should escape dollar sign character`() {
        val result = LatexEscaper.escape("Price: $99.99")

        result shouldBe "Price: \\$99.99"
    }

    @Test
    fun `should escape hash character`() {
        val result = LatexEscaper.escape("#hashtag")

        result shouldBe "\\#hashtag"
    }

    @Test
    fun `should escape underscore character`() {
        val result = LatexEscaper.escape("user_name")

        result shouldBe "user\\_name"
    }

    @Test
    fun `should escape opening brace character`() {
        val result = LatexEscaper.escape("function() {")

        result shouldBe "function() \\{"
    }

    @Test
    fun `should escape closing brace character`() {
        val result = LatexEscaper.escape("return value; }")

        result shouldBe "return value; \\}"
    }

    @Test
    fun `should escape tilde character`() {
        val result = LatexEscaper.escape("~/.bashrc")

        result shouldBe "\\textasciitilde{}/.bashrc"
    }

    @Test
    fun `should escape caret character`() {
        val result = LatexEscaper.escape("x^2 + y^2")

        result shouldBe "x\\textasciicircum{}2 + y\\textasciicircum{}2"
    }

    @Test
    fun `should escape backslash followed by other special characters`() {
        val result = LatexEscaper.escape("\\& \\% \\$")

        result shouldBe "\\textbackslash\\{\\}\\& \\textbackslash\\{\\}\\% \\textbackslash\\{\\}\\$"
    }

    @Test
    fun `should escape multiple special characters in one string`() {
        val result = LatexEscaper.escape("Research & Development: 50% complete, Cost: $5,000")

        result shouldBe "Research \\& Development: 50\\% complete, Cost: \\$5,000"
    }

    @Test
    fun `should handle empty string`() {
        val result = LatexEscaper.escape("")

        result shouldBe ""
    }

    @Test
    fun `should handle string with no special characters`() {
        val result = LatexEscaper.escape("This is a normal string")

        result shouldBe "This is a normal string"
    }

    @Test
    fun `should escape email addresses correctly`() {
        val result = LatexEscaper.escape("john_doe@example.com")

        result shouldBe "john\\_doe@example.com"
    }

    @Test
    fun `should escape URLs with special characters`() {
        val result = LatexEscaper.escape("https://example.com/search?q=test&lang=en")

        result shouldBe "https://example.com/search?q=test\\&lang=en"
    }

    @Test
    fun `should escape mathematical expressions`() {
        val result = LatexEscaper.escape("f(x) = ax^2 + bx + c")

        result shouldBe "f(x) = ax\\textasciicircum{}2 + bx + c"
    }

    @Test
    fun `should escape percentages in business context`() {
        val result = LatexEscaper.escape("Increased revenue by 35% year-over-year")

        result shouldBe "Increased revenue by 35\\% year-over-year"
    }

    @Test
    fun `should escape mixed braces and underscores`() {
        val result = LatexEscaper.escape("var_name = {key_1: value_1}")

        result shouldBe "var\\_name = \\{key\\_1: value\\_1\\}"
    }

    @Test
    fun `should escape salary ranges with dollar signs`() {
        val result = LatexEscaper.escape("Salary: $80,000 - $120,000")

        result shouldBe "Salary: \\$80,000 - \\$120,000"
    }

    @Test
    fun `should escape hashtags and social media handles`() {
        val result = LatexEscaper.escape("#OpenSource #Tech @user_name")

        result shouldBe "\\#OpenSource \\#Tech @user\\_name"
    }

    @Test
    fun `should preserve whitespace while escaping`() {
        val result = LatexEscaper.escape("  indented & spaced  ")

        result shouldBe "  indented \\& spaced  "
    }

    @Test
    fun `should escape newlines and tabs with special characters`() {
        val result = LatexEscaper.escape("Line 1\n\tTab & indent")

        result shouldBe "Line 1\n\tTab \\& indent"
    }

    @Test
    fun `should escape common programming patterns`() {
        val result = LatexEscaper.escape("for (int i = 0; i < 10; i++) { sum += arr[i]; }")

        result shouldBe "for (int i = 0; i \\textless{} 10; i++) \\{ sum += arr[i]; \\}"
    }
}
