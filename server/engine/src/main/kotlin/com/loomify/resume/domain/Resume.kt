package com.loomify.resume.domain

/**
 * Aggregate root representing a complete resume.
 * Enforces business rule BR-001: Resume must contain at least one of work experience, education, or skills.
 */
data class Resume(
    val basics: PersonalInfo,
    val work: List<WorkExperience> = emptyList(),
    val volunteer: List<Volunteer> = emptyList(),
    val education: List<Education> = emptyList(),
    val awards: List<Award> = emptyList(),
    val certificates: List<Certificate> = emptyList(),
    val publications: List<Publication> = emptyList(),
    val skills: List<SkillCategory> = emptyList(),
    val languages: List<Language> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val references: List<Reference> = emptyList(),
    val projects: List<Project> = emptyList(),
) {
    init {
        // BR-001: Resume must have at least one of work experience, education, or skills
        require(work.isNotEmpty() || education.isNotEmpty() || skills.isNotEmpty()) {
            "Resume must have at least one of: work experience, education, or skills"
        }
    }

    /**
     * Calculates content metrics for the resume.
     * Used to determine template layout and prioritization.
     */
    fun contentMetrics(): ContentMetrics {
        val skillsCount = skills.flatMap { it.keywords }.size
        val experienceEntries = work.size
        val experienceYears = work.sumOf { it.durationInYears() }
        val educationEntries = education.size

        return ContentMetrics(
            skillsCount = skillsCount,
            experienceEntries = experienceEntries,
            experienceYears = experienceYears,
            educationEntries = educationEntries,
        )
    }
}

/**
 * Content metrics for a resume.
 * Used for adaptive layout decisions.
 */
data class ContentMetrics(
    val skillsCount: Int,
    val experienceEntries: Int,
    val experienceYears: Double,
    val educationEntries: Int,
)
