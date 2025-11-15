package com.loomify.resume.infrastructure.template.validator

import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.domain.exception.LaTeXInjectionException
import com.loomify.resume.domain.model.Award
import com.loomify.resume.domain.model.Certificate
import com.loomify.resume.domain.model.CompanyName
import com.loomify.resume.domain.model.DegreeType
import com.loomify.resume.domain.model.Education
import com.loomify.resume.domain.model.FieldOfStudy
import com.loomify.resume.domain.model.FullName
import com.loomify.resume.domain.model.Highlight
import com.loomify.resume.domain.model.InstitutionName
import com.loomify.resume.domain.model.Interest
import com.loomify.resume.domain.model.JobTitle
import com.loomify.resume.domain.model.Language
import com.loomify.resume.domain.model.Location
import com.loomify.resume.domain.model.PersonalInfo
import com.loomify.resume.domain.model.PhoneNumber
import com.loomify.resume.domain.model.Project
import com.loomify.resume.domain.model.Publication
import com.loomify.resume.domain.model.Reference
import com.loomify.resume.domain.model.ResumeData
import com.loomify.resume.domain.model.Skill
import com.loomify.resume.domain.model.SkillCategory
import com.loomify.resume.domain.model.SkillCategoryName
import com.loomify.resume.domain.model.SocialProfile
import com.loomify.resume.domain.model.Summary
import com.loomify.resume.domain.model.Url
import com.loomify.resume.domain.model.Volunteer
import com.loomify.resume.domain.model.WorkExperience
import java.time.LocalDate
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * Validation tests for TemplateValidator ensuring comprehensive coverage of all
 * user-controlled string fields. Each field is individually mutated with a
 * malicious LaTeX command to verify detection.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TemplateValidatorTest {
    @Test
    @Order(1)
    fun `should pass validation for safe resume`() {
        assertDoesNotThrow { TemplateValidator.validateContent(baseResume()) }
    }

    @Test
    @Order(2)
    fun `should detect malicious content in nested work section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in work experience
        val maliciousWork = WorkExperience(
            name = CompanyName("Evil Corp"),
            position = JobTitle("Developer"),
            startDate = "2023-01-01",
            endDate = null,
            location = "Remote",
            summary = "\\input{evil}", // Malicious LaTeX command
            highlights = listOf(Highlight("Clean highlight")),
            url = Url("https://example.com"),
        )

        val resume = baseResume().copy(
            work = listOf(maliciousWork),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\input"))
    }

    @Test
    @Order(3)
    fun `should detect malicious content in nested education section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in education
        val maliciousEducation = Education(
            institution = InstitutionName("University"),
            area = FieldOfStudy("\\def\\hack{}"), // Malicious LaTeX command
            studyType = DegreeType("BS"),
            startDate = "2019-01-01",
            endDate = "2022-01-01",
            score = "4.0",
            url = Url("https://university.edu"),
            courses = listOf("Algorithms"),
        )

        val resume = baseResume().copy(
            education = listOf(maliciousEducation),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\def"))
    }

    @Test
    @Order(4)
    fun `should detect malicious content in nested projects section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in project
        val maliciousProject = Project(
            name = "System\\write{hack}", // Malicious LaTeX command
            description = "A legitimate project description",
            url = "https://project.example.com",
            startDate = LocalDate.parse("2023-02-01"),
            endDate = null,
            highlights = listOf("Feature implementation"),
            keywords = listOf("Kotlin"),
            roles = listOf("Lead"),
            entity = "Company",
            type = "Internal",
        )

        val resume = baseResume().copy(
            projects = listOf(maliciousProject),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\write"))
    }

    @Test
    @Order(5)
    fun `should detect malicious content in nested languages section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in language
        val maliciousLanguage = Language(
            language = "English",
            fluency = "\\newcommand{\\evil}{}", // Malicious LaTeX command
        )

        val resume = baseResume().copy(
            languages = listOf(maliciousLanguage),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\newcommand"))
    }

    @Test
    @Order(6)
    fun `should detect malicious content in nested volunteer section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in volunteer
        val maliciousVolunteer = Volunteer(
            organization = "Community\\include{bad}", // Malicious LaTeX command
            position = "Mentor",
            url = Url("https://volunteer.org"),
            startDate = "2024-01-01",
            endDate = null,
            summary = "Helped students",
            highlights = listOf("Weekly sessions"),
        )

        val resume = baseResume().copy(
            volunteer = listOf(maliciousVolunteer),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\include"))
    }

    @Test
    @Order(7)
    fun `should detect malicious content in nested awards section using real resume data`() {
        // Arrange: Create real ResumeData with malicious command in award
        val maliciousAward = Award(
            title = "Best Developer",
            date = "2023-12-01",
            awarder = "Acme Corp\\catcode{evil}", // Malicious LaTeX command
            summary = "Outstanding contributions",
        )

        val resume = baseResume().copy(
            awards = listOf(maliciousAward),
        )

        // Act & Assert
        val exception = assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(resume)
        }

        assertTrue(exception.message!!.contains("\\catcode"))
    }

    @ParameterizedTest(name = "{0} should trigger LaTeXInjectionException")
    @MethodSource("maliciousFieldProvider")
    @Order(8)
    fun `should detect malicious LaTeX commands in all fields`(
        @Suppress("UNUSED_PARAMETER") field: String,
        maliciousResume: ResumeData
    ): LaTeXInjectionException? =
        assertThrows(LaTeXInjectionException::class.java) {
            TemplateValidator.validateContent(maliciousResume)
        }
    companion object {
        private const val MALICIOUS = "\\input{evil}" // Matches DANGEROUS_PATTERN

        private fun baseResume(): ResumeData {
            val basics = personalInfo()
            val work = experiences()
            val education = educations()
            val skills = skillCategories()
            val projects = projects()
            val volunteer = volunteers()
            val awards = awards()
            val certificates = certificates()
            val publications = publications()
            val interests = interests()
            val references = references()
            val languages = languages()
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

        private fun languages(): List<Language> =
            listOf(Language(language = "English", fluency = "Native"))

        private fun references(): List<Reference> = listOf(
            Reference(
                name = "Jane Smith",
                reference = "Former manager at Acme.",
            ),
        )

        private fun interests(): List<Interest> = listOf(
            Interest(
                name = "Cycling",
                keywords = listOf("Road"),
            ),
        )

        private fun publications(): List<Publication> = listOf(
            Publication(
                name = "Distributed Systems Paper",
                publisher = "Journal A",
                releaseDate = "2024-03-01",
                url = Url("https://journal.example.org/paper"),
                summary = "Explores scaling strategies.",
            ),
        )

        private fun certificates(): List<Certificate> = listOf(
            Certificate(
                name = "Cloud Cert",
                date = "2024-02-01",
                url = Url("https://cert.example.org"),
                issuer = "Cloud Board",
            ),
        )

        private fun awards(): List<Award> = listOf(
            Award(
                title = "Best Developer",
                date = "2023-12-01",
                awarder = "Acme Corp",
                summary = "Recognized for outstanding contributions.",
            ),
        )

        private fun volunteers(): List<Volunteer> = listOf(
            Volunteer(
                organization = "Code Club",
                position = "Mentor",
                url = Url("https://codeclub.example.org"),
                startDate = "2024-01-01",
                endDate = null,
                summary = "Helped students.",
                highlights = listOf("Weekly sessions"),
            ),
        )

        private fun projects(): List<Project> = listOf(
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

        private fun skillCategories(): List<SkillCategory> = listOf(
            SkillCategory(
                name = SkillCategoryName("Backend"),
                level = "Senior",
                keywords = listOf(Skill("Kotlin")),
            ),
        )

        private fun educations(): List<Education> = listOf(
            Education(
                institution = InstitutionName("State University"),
                area = FieldOfStudy("Computer Science"),
                studyType = DegreeType("BS"),
                startDate = "2019-01-01",
                endDate = "2022-01-01",
                score = "4.0/4.0",
                url = Url("https://university.example.edu"),
                courses = listOf("Algorithms"),
            ),
        )

        private fun personalInfo(): PersonalInfo = PersonalInfo(
            name = FullName("John Doe"),
            label = JobTitle("Engineer"),
            image = null,
            email = Email("john.doe@example.com"),
            phone = PhoneNumber("+1-555-1234"),
            url = Url("https://example.com"),
            summary = Summary("Experienced engineer."),
            location = Location(
                address = "123 Main St",
                postalCode = "12345",
                city = "Metropolis",
                countryCode = "US",
                region = "CA",
            ),
            profiles = listOf(
                SocialProfile(
                    network = "LinkedIn",
                    username = "johndoe",
                    url = "https://linkedin.com/in/johndoe",
                ),
            ),
        )

        private fun experiences(): List<WorkExperience> = listOf(
            WorkExperience(
                name = CompanyName("Acme Corp"),
                position = JobTitle("Developer"),
                startDate = "2023-01-01",
                endDate = null,
                location = "Remote",
                summary = "Worked on platform.",
                highlights = listOf(Highlight("Improved performance")),
                url = Url("https://acme.example.com"),
            ),
        )

        @JvmStatic
        @Suppress("MaximumLineLength", "LongMethod")
        fun maliciousFieldProvider(): Stream<Arguments> {
            val base = baseResume()
            val location = base.basics.location!!
            return Stream.of(
                // Basics
                arg(
                    "basics.name",
                    base.copy(basics = base.basics.copy(name = FullName(MALICIOUS))),
                ),
                arg(
                    "basics.label",
                    base.copy(basics = base.basics.copy(label = JobTitle(MALICIOUS))),
                ),
                arg(
                    "basics.phone",
                    base.copy(basics = base.basics.copy(phone = PhoneNumber(MALICIOUS))),
                ),
                arg(
                    "basics.summary",
                    base.copy(basics = base.basics.copy(summary = Summary(MALICIOUS))),
                ),
                arg(
                    "basics.location.city",
                    base.copy(basics = base.basics.copy(location = location.copy(city = MALICIOUS))),
                ),
                arg(
                    "basics.location.address",
                    base.copy(basics = base.basics.copy(location = location.copy(address = MALICIOUS))),
                ),
                arg(
                    "basics.location.region",
                    base.copy(basics = base.basics.copy(location = location.copy(region = MALICIOUS))),
                ),
                arg(
                    "basics.location.postalCode",
                    base.copy(
                        basics = base.basics.copy(
                            location = location.copy(postalCode = MALICIOUS),
                        ),
                    ),
                ),
                arg(
                    "basics.location.countryCode",
                    base.copy(
                        basics = base.basics.copy(
                            location = location.copy(countryCode = MALICIOUS),
                        ),
                    ),
                ),
                arg(
                    "basics.profiles[0].network",
                    base.copy(
                        basics = base.basics.copy(
                            profiles = listOf(
                                base.basics.profiles[0].copy(network = MALICIOUS),
                            ),
                        ),
                    ),
                ),
                arg(
                    "basics.profiles[0].username",
                    base.copy(
                        basics = base.basics.copy(
                            profiles = listOf(
                                base.basics.profiles[0].copy(username = MALICIOUS),
                            ),
                        ),
                    ),
                ),
                // Work
                arg(
                    "work[0].name",
                    base.copy(work = listOf(base.work[0].copy(name = CompanyName(MALICIOUS)))),
                ),
                arg(
                    "work[0].position",
                    base.copy(work = listOf(base.work[0].copy(position = JobTitle(MALICIOUS)))),
                ),
                arg(
                    "work[0].location",
                    base.copy(work = listOf(base.work[0].copy(location = MALICIOUS))),
                ),
                arg(
                    "work[0].summary",
                    base.copy(work = listOf(base.work[0].copy(summary = MALICIOUS))),
                ),
                arg(
                    "work[0].highlights[0]",
                    base.copy(
                        work = listOf(
                            base.work[0].copy(
                                highlights = listOf(
                                    Highlight(MALICIOUS),
                                ),
                            ),
                        ),
                    ),
                ),
                // Education
                arg(
                    "education[0].institution",
                    base.copy(
                        education = listOf(
                            base.education[0].copy(
                                institution = InstitutionName(MALICIOUS),
                            ),
                        ),
                    ),
                ),
                arg(
                    "education[0].area",
                    base.copy(
                        education = listOf(
                            base.education[0].copy(
                                area = FieldOfStudy(MALICIOUS),
                            ),
                        ),
                    ),
                ),
                arg(
                    "education[0].studyType",
                    base.copy(
                        education = listOf(
                            base.education[0].copy(
                                studyType = DegreeType(MALICIOUS),
                            ),
                        ),
                    ),
                ),
                arg(
                    "education[0].score",
                    base.copy(education = listOf(base.education[0].copy(score = MALICIOUS))),
                ),
                arg(
                    "education[0].courses[0]",
                    base.copy(education = listOf(base.education[0].copy(courses = listOf(MALICIOUS)))),
                ),
                // Skills
                arg(
                    "skills[0].name",
                    base.copy(skills = listOf(base.skills[0].copy(name = SkillCategoryName(MALICIOUS)))),
                ),
                arg(
                    "skills[0].level",
                    base.copy(skills = listOf(base.skills[0].copy(level = MALICIOUS))),
                ),
                arg(
                    "skills[0].keywords[0]",
                    base.copy(skills = listOf(base.skills[0].copy(keywords = listOf(Skill(MALICIOUS))))),
                ),
                // Projects
                arg(
                    "projects[0].name",
                    base.copy(projects = listOf(base.projects[0].copy(name = MALICIOUS))),
                ),
                arg(
                    "projects[0].description",
                    base.copy(projects = listOf(base.projects[0].copy(description = MALICIOUS))),
                ),
                arg(
                    "projects[0].highlights[0]",
                    base.copy(projects = listOf(base.projects[0].copy(highlights = listOf(MALICIOUS)))),
                ),
                arg(
                    "projects[0].keywords[0]",
                    base.copy(projects = listOf(base.projects[0].copy(keywords = listOf(MALICIOUS)))),
                ),
                arg(
                    "projects[0].roles[0]",
                    base.copy(projects = listOf(base.projects[0].copy(roles = listOf(MALICIOUS)))),
                ),
                arg(
                    "projects[0].entity",
                    base.copy(projects = listOf(base.projects[0].copy(entity = MALICIOUS))),
                ),
                arg(
                    "projects[0].type",
                    base.copy(projects = listOf(base.projects[0].copy(type = MALICIOUS))),
                ),
                // Volunteer
                arg(
                    "volunteer[0].organization",
                    base.copy(volunteer = listOf(base.volunteer[0].copy(organization = MALICIOUS))),
                ),
                arg(
                    "volunteer[0].position",
                    base.copy(volunteer = listOf(base.volunteer[0].copy(position = MALICIOUS))),
                ),
                arg(
                    "volunteer[0].summary",
                    base.copy(volunteer = listOf(base.volunteer[0].copy(summary = MALICIOUS))),
                ),
                arg(
                    "volunteer[0].highlights[0]",
                    base.copy(
                        volunteer = listOf(
                            base.volunteer[0].copy(
                                highlights = listOf(MALICIOUS),
                            ),
                        ),
                    ),
                ),
                // Awards
                arg(
                    "awards[0].title",
                    base.copy(awards = listOf(base.awards[0].copy(title = MALICIOUS))),
                ),
                arg(
                    "awards[0].awarder",
                    base.copy(awards = listOf(base.awards[0].copy(awarder = MALICIOUS))),
                ),
                arg(
                    "awards[0].summary",
                    base.copy(awards = listOf(base.awards[0].copy(summary = MALICIOUS))),
                ),
                // Certificates
                arg(
                    "certificates[0].name",
                    base.copy(certificates = listOf(base.certificates[0].copy(name = MALICIOUS))),
                ),
                arg(
                    "certificates[0].issuer",
                    base.copy(certificates = listOf(base.certificates[0].copy(issuer = MALICIOUS))),
                ),
                // Publications
                arg(
                    "publications[0].name",
                    base.copy(publications = listOf(base.publications[0].copy(name = MALICIOUS))),
                ),
                arg(
                    "publications[0].publisher",
                    base.copy(publications = listOf(base.publications[0].copy(publisher = MALICIOUS))),
                ),
                arg(
                    "publications[0].summary",
                    base.copy(publications = listOf(base.publications[0].copy(summary = MALICIOUS))),
                ),
                // Interests
                arg(
                    "interests[0].name",
                    base.copy(interests = listOf(base.interests[0].copy(name = MALICIOUS))),
                ),
                arg(
                    "interests[0].keywords[0]",
                    base.copy(interests = listOf(base.interests[0].copy(keywords = listOf(MALICIOUS)))),
                ),
                // Languages
                arg(
                    "languages[0].language",
                    base.copy(languages = listOf(base.languages[0].copy(language = MALICIOUS))),
                ),
                arg(
                    "languages[0].fluency",
                    base.copy(languages = listOf(base.languages[0].copy(fluency = MALICIOUS))),
                ),
                // References
                arg(
                    "references[0].name",
                    base.copy(references = listOf(base.references[0].copy(name = MALICIOUS))),
                ),
                arg(
                    "references[0].reference",
                    base.copy(references = listOf(base.references[0].copy(reference = MALICIOUS))),
                ),
            )
        }

        private fun arg(
            field: String,
            resume: ResumeData
        ): Arguments =
            Arguments.of(field, resume)
    }
}
