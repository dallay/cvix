package com.loomify.resume.domain.model

import com.loomify.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for SkillCategory entity.
 *
 * Tests validation rules:
 * - Category name required, max 100 chars
 * - At least one skill (keyword) required
 * - Individual skills max 50 chars
 */
@UnitTest
class SkillCategoryTest {

    @Test
    fun `should create skill category with all fields`() {
        // Arrange & Act
        val skillCategory = SkillCategory(
            name = SkillCategoryName("Programming Languages"),
            level = "Advanced",
            keywords = listOf(
                Skill("Java"),
                Skill("Kotlin"),
                Skill("TypeScript"),
                Skill("Python"),
            ),
        )

        // Assert
        skillCategory shouldNotBe null
        skillCategory.name.value shouldBe "Programming Languages"
        skillCategory.level shouldBe "Advanced"
        skillCategory.keywords.size shouldBe 4
    }

    @Test
    fun `should create skill category without proficiency level`() {
        // Arrange & Act
        val skillCategory = SkillCategory(
            name = SkillCategoryName("Frameworks"),
            level = null,
            keywords = listOf(
                Skill("Spring Boot"),
                Skill("React"),
            ),
        )

        // Assert
        skillCategory shouldNotBe null
        skillCategory.level shouldBe null
        skillCategory.keywords.size shouldBe 2
    }

    @Test
    fun `should fail when category name is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            SkillCategoryName("")
        }.message shouldBe "Category name cannot be blank"
    }

    @Test
    fun `should fail when category name exceeds max length`() {
        // Arrange
        val longName = "a".repeat(101)

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            SkillCategoryName(longName)
        }.message shouldBe "Category name cannot exceed 100 characters"
    }

    @Test
    fun `should fail when no keywords provided`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            SkillCategory(
                name = SkillCategoryName("Programming Languages"),
                level = null,
                keywords = emptyList(),
            )
        }.message shouldBe "At least one skill is required"
    }

    @Test
    fun `should fail when skill is blank`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            Skill("")
        }.message shouldBe "Skill cannot be blank"
    }

    @Test
    fun `should fail when skill exceeds max length`() {
        // Arrange
        val longSkill = "a".repeat(51)

        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            Skill(longSkill)
        }.message shouldBe "Skill cannot exceed 50 characters"
    }

    @Test
    fun `should accept skill at max length`() {
        // Arrange
        val maxLengthSkill = "a".repeat(50)

        // Act
        val skill = Skill(maxLengthSkill)

        // Assert
        skill.value.length shouldBe 50
    }
}
