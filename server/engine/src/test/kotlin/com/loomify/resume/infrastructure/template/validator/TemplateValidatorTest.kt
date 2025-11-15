package com.loomify.resume.infrastructure.template.validator

import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.model.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

/**
 * Validation tests for TemplateValidator ensuring comprehensive coverage of all
 * user-controlled string fields. Each field is individually mutated with a
 * malicious LaTeX command to verify detection.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TemplateValidatorTest {

    companion object {
        private const val MALICIOUS = "\\input{evil}" // Matches DANGEROUS_PATTERN

        private fun baseResume(): ResumeData {
            val basics = PersonalInfo(
                name = FullName("John Doe"),
                label = JobTitle("Engineer"),
                image = null,
                email = Email("john.doe@example.com"),
                phone = PhoneNumber("+1-555-1234"),
                url = com.loomify.resume.domain.model.Url("https://example.com"),
                summary = Summary("Experienced engineer."),
                location = Location(
                    address = "123 Main St",
                    postalCode = "12345",
                    city = "Metropolis",
                    countryCode = "US",
                    region = "CA",
                ),
                profiles = listOf(
                    SocialProfile(network = "LinkedIn", username = "johndoe", url = "https://linkedin.com/in/johndoe"),
                ),
            )

            val work = listOf(
                WorkExperience(
                    company = CompanyName("Acme Corp"),
                    position = JobTitle("Developer"),
                    startDate = "2023-01-01",
                    endDate = null,
                    location = "Remote",
                    summary = "Worked on platform.",
                    highlights = listOf(Highlight("Improved performance")),
                    url = com.loomify.resume.domain.model.Url("https://acme.example.com"),
                ),
            )

            val education = listOf(
                Education(
                    institution = InstitutionName("State University"),
                    area = FieldOfStudy("Computer Science"),
                    studyType = DegreeType("BS"),
                    startDate = "2019-01-01",
                    endDate = "2022-01-01",
                    score = "4.0/4.0",
                    url = com.loomify.resume.domain.model.Url("https://university.example.edu"),
                    courses = listOf("Algorithms"),
                ),
            )

            val skills = listOf(
                SkillCategory(
                    name = SkillCategoryName("Backend"),
                    level = "Senior",
                    keywords = listOf(Skill("Kotlin")),
                ),
            )

            val projects = listOf(
                Project(
                    name = "Inventory System",
                    description = "Manages stock levels.",
                    url = "https://inventory.example.com",
                    startDate = LocalDate.parse("2023-02-01"),
                    endDate = null,
                    highlights = listOf("Reduced errors"),
                    keywords = listOf("Kotlin"),
                    roles = listOf("Lead"),
                    entity = "Acme",
                    type = "Internal",
                ),
            )

            val volunteer = listOf(
                Volunteer(
                    organization = "Code Club",
                    position = "Mentor",
                    url = com.loomify.resume.domain.model.Url("https://codeclub.example.org"),
                    startDate = "2024-01-01",
                    endDate = null,
                    summary = "Helped students.",
                    highlights = listOf("Weekly sessions"),
                ),
            )

            val awards = listOf(
                Award(
                    title = "Best Developer",
                    date = "2023-12-01",
                    awarder = "Acme Corp",
                    summary = "Recognized for outstanding contributions.",
                ),
            )

            val certificates = listOf(
                Certificate(
                    name = "Cloud Cert",
                    date = "2024-02-01",
                    url = com.loomify.resume.domain.model.Url("https://cert.example.org"),
                    issuer = "Cloud Board",
                ),
            )

            val publications = listOf(
                Publication(
                    name = "Distributed Systems Paper",
                    publisher = "Journal A",
                    releaseDate = "2024-03-01",
                    url = com.loomify.resume.domain.model.Url("https://journal.example.org/paper"),
                    summary = "Explores scaling strategies.",
                ),
            )

            val interests = listOf(
                Interest(
                    name = "Cycling",
                    keywords = listOf("Road"),
                ),
            )

            val references = listOf(
                Reference(
                    name = "Jane Smith",
                    reference = "Former manager at Acme.",
                ),
            )

            val languages = listOf(Language(language = "English", fluency = "Native"))

            return ResumeData(
                basics = basics,
                work = work,
                volunteer = volunteer,
                education = education,
                awards = awards,
                certificates = certificates,
                publications = publications,
                skills = skills,
                languages = languages,
                interests = interests,
                references = references,
                projects = projects,
            )
        }

        @JvmStatic
        fun maliciousFieldProvider(): Stream<org.junit.jupiter.params.provider.Arguments> {
            val base = baseResume()
            return Stream.of(
                // Basics
                arg("basics.name", base.copy(basics = base.basics.copy(name = FullName(MALICIOUS)))),
                arg("basics.label", base.copy(basics = base.basics.copy(label = JobTitle(MALICIOUS)))),
                arg("basics.phone", base.copy(basics = base.basics.copy(phone = PhoneNumber(MALICIOUS)))),
                arg("basics.summary", base.copy(basics = base.basics.copy(summary = Summary(MALICIOUS)))),
                arg("basics.location.city", base.copy(basics = base.basics.copy(location = base.basics.location!!.copy(city = MALICIOUS)))),
                arg("basics.location.address", base.copy(basics = base.basics.copy(location = base.basics.location!!.copy(address = MALICIOUS)))),
                arg("basics.location.region", base.copy(basics = base.basics.copy(location = base.basics.location!!.copy(region = MALICIOUS)))),
                arg("basics.location.postalCode", base.copy(basics = base.basics.copy(location = base.basics.location!!.copy(postalCode = MALICIOUS)))),
                arg("basics.location.countryCode", base.copy(basics = base.basics.copy(location = base.basics.location!!.copy(countryCode = MALICIOUS)))),
                arg("basics.profiles[0].network", base.copy(basics = base.basics.copy(profiles = listOf(base.basics.profiles[0].copy(network = MALICIOUS))))),
                arg("basics.profiles[0].username", base.copy(basics = base.basics.copy(profiles = listOf(base.basics.profiles[0].copy(username = MALICIOUS))))),
                // Work
                arg("work[0].company", base.copy(work = listOf(base.work[0].copy(company = CompanyName(MALICIOUS))))),
                arg("work[0].position", base.copy(work = listOf(base.work[0].copy(position = JobTitle(MALICIOUS))))),
                arg("work[0].location", base.copy(work = listOf(base.work[0].copy(location = MALICIOUS)))),
                arg("work[0].summary", base.copy(work = listOf(base.work[0].copy(summary = MALICIOUS)))),
                arg("work[0].highlights[0]", base.copy(work = listOf(base.work[0].copy(highlights = listOf(Highlight(MALICIOUS)))))),
                // Education
                arg("education[0].institution", base.copy(education = listOf(base.education[0].copy(institution = InstitutionName(MALICIOUS))))),
                arg("education[0].area", base.copy(education = listOf(base.education[0].copy(area = FieldOfStudy(MALICIOUS))))),
                arg("education[0].studyType", base.copy(education = listOf(base.education[0].copy(studyType = DegreeType(MALICIOUS))))),
                arg("education[0].score", base.copy(education = listOf(base.education[0].copy(score = MALICIOUS)))),
                arg("education[0].courses[0]", base.copy(education = listOf(base.education[0].copy(courses = listOf(MALICIOUS))))),
                // Skills
                arg("skills[0].name", base.copy(skills = listOf(base.skills[0].copy(name = SkillCategoryName(MALICIOUS))))),
                arg("skills[0].level", base.copy(skills = listOf(base.skills[0].copy(level = MALICIOUS)))),
                arg("skills[0].keywords[0]", base.copy(skills = listOf(base.skills[0].copy(keywords = listOf(Skill(MALICIOUS)))))),
                // Projects
                arg("projects[0].name", base.copy(projects = listOf(base.projects[0].copy(name = MALICIOUS)))),
                arg("projects[0].description", base.copy(projects = listOf(base.projects[0].copy(description = MALICIOUS)))),
                arg("projects[0].highlights[0]", base.copy(projects = listOf(base.projects[0].copy(highlights = listOf(MALICIOUS))))),
                arg("projects[0].keywords[0]", base.copy(projects = listOf(base.projects[0].copy(keywords = listOf(MALICIOUS))))),
                arg("projects[0].roles[0]", base.copy(projects = listOf(base.projects[0].copy(roles = listOf(MALICIOUS))))),
                arg("projects[0].entity", base.copy(projects = listOf(base.projects[0].copy(entity = MALICIOUS)))),
                arg("projects[0].type", base.copy(projects = listOf(base.projects[0].copy(type = MALICIOUS)))),
                // Volunteer
                arg("volunteer[0].organization", base.copy(volunteer = listOf(base.volunteer[0].copy(organization = MALICIOUS)))),
                arg("volunteer[0].position", base.copy(volunteer = listOf(base.volunteer[0].copy(position = MALICIOUS)))),
                arg("volunteer[0].summary", base.copy(volunteer = listOf(base.volunteer[0].copy(summary = MALICIOUS)))),
                arg("volunteer[0].highlights[0]", base.copy(volunteer = listOf(base.volunteer[0].copy(highlights = listOf(MALICIOUS))))),
                // Awards
                arg("awards[0].title", base.copy(awards = listOf(base.awards[0].copy(title = MALICIOUS)))),
                arg("awards[0].awarder", base.copy(awards = listOf(base.awards[0].copy(awarder = MALICIOUS)))),
                arg("awards[0].summary", base.copy(awards = listOf(base.awards[0].copy(summary = MALICIOUS)))),
                // Certificates
                arg("certificates[0].name", base.copy(certificates = listOf(base.certificates[0].copy(name = MALICIOUS)))),
                arg("certificates[0].issuer", base.copy(certificates = listOf(base.certificates[0].copy(issuer = MALICIOUS)))),
                // Publications
                arg("publications[0].name", base.copy(publications = listOf(base.publications[0].copy(name = MALICIOUS)))),
                arg("publications[0].publisher", base.copy(publications = listOf(base.publications[0].copy(publisher = MALICIOUS)))),
                arg("publications[0].summary", base.copy(publications = listOf(base.publications[0].copy(summary = MALICIOUS)))),
                // Interests
                arg("interests[0].name", base.copy(interests = listOf(base.interests[0].copy(name = MALICIOUS)))),
                arg("interests[0].keywords[0]", base.copy(interests = listOf(base.interests[0].copy(keywords = listOf(MALICIOUS))))),
                // Languages
                arg("languages[0].language", base.copy(languages = listOf(base.languages[0].copy(language = MALICIOUS)))),
                arg("languages[0].fluency", base.copy(languages = listOf(base.languages[0].copy(fluency = MALICIOUS)))),
                // References
                arg("references[0].name", base.copy(references = listOf(base.references[0].copy(name = MALICIOUS)))),
                arg("references[0].reference", base.copy(references = listOf(base.references[0].copy(reference = MALICIOUS)))),
            )
        }

        private fun arg(field: String, resume: ResumeData): org.junit.jupiter.params.provider.Arguments =
            org.junit.jupiter.params.provider.Arguments.of(field, resume)
    }

    @Test
    @Order(1)
    fun `should pass validation for safe resume`() {
        assertDoesNotThrow { TemplateValidator.validateContent(baseResume()) }
    }

    @ParameterizedTest(name = "{0} should trigger LaTeXInjectionException")
    @MethodSource("maliciousFieldProvider")
    @Order(2)
    fun `should detect malicious LaTeX commands in all fields`(field: String, maliciousResume: ResumeData) {
        assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(maliciousResume)
        }
    }
}
