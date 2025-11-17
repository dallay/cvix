package com.loomify.resume.domain

/**
 * SkillCategoryName value object with validation.
 * - Required, max 100 characters
 */
@JvmInline
value class SkillCategoryName(val value: String) {
    init {
        require(value.isNotBlank()) { "Category name cannot be blank" }
        require(value.length <= MAX_CATEGORY_LENGTH) { "Category name cannot exceed $MAX_CATEGORY_LENGTH characters" }
    }
    companion object {
        private const val MAX_CATEGORY_LENGTH = 100
    }
}

/**
 * Skill value object representing an individual skill/technology.
 * - Required, max 50 characters
 */
@JvmInline
value class Skill(val value: String) {
    init {
        require(value.isNotBlank()) { "Skill cannot be blank" }
        require(value.length <= MAX_SKILL_LENGTH) { "Skill cannot exceed $MAX_SKILL_LENGTH characters" }
    }
    companion object {
        private const val MAX_SKILL_LENGTH = 50
    }
}

/**
 * Entity representing a category of skills in a resume.
 * Groups related skills with optional proficiency level.
 */
data class SkillCategory(
    val name: SkillCategoryName,
    val level: String? = null,
    val keywords: List<Skill>,
) {
    init {
        require(keywords.isNotEmpty()) { "At least one skill is required" }
    }
}
