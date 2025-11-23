package com.loomify.resume.infrastructure.http

import com.loomify.ControllerIntegrationTest
import com.loomify.resume.ResumeTestFixtures.createResumeRequest
import com.loomify.spring.boot.logging.LogMasker
import java.util.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.jdbc.Sql

internal class CreateResumeControllerIntegrationTest : ControllerIntegrationTest() {
    @Test
    @Sql(
        "/db/user/users.sql",
        "/db/workspace/workspace.sql",
    )
    @Sql(
        "/db/user/clean.sql",
        "/db/workspace/clean.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    )
    fun `should create a new resume`() {
        val workspaceId = "a0654720-35dc-49d0-b508-1f7df5d915f1"
        val request = createResumeRequest(UUID.fromString(workspaceId))
        val id = UUID.randomUUID().toString()
        webTestClient.mutateWith(csrf()).put()
            .uri("/api/resume/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().valueMatches("Location", "/api/resume/${LogMasker.mask(id)}")
            .expectBody().isEmpty

        // Follow-up GET to verify persistence
        verifyPersistence(id, workspaceId)
    }

    @Suppress("LongMethod")
    private fun verifyPersistence(id: String, workspaceId: String) {
        webTestClient.mutateWith(csrf()).get()
            .uri("/api/resume/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id)
            .jsonPath("$.userId").isEqualTo("efc4b2b8-08be-4020-93d5-f795762bf5c9")
            .jsonPath("$.workspaceId").isEqualTo(workspaceId)
            .jsonPath("$.title").isEqualTo("My first resume")
            .jsonPath("$.createdBy").isEqualTo("efc4b2b8-08be-4020-93d5-f795762bf5c9")
            .jsonPath("$.updatedBy").isEqualTo("efc4b2b8-08be-4020-93d5-f795762bf5c9")
            .jsonPath("$.createdAt").exists()
            .jsonPath("$.updatedAt").exists()
            // content.basics
            .jsonPath("$.content.basics.name").isEqualTo("John Doe")
            .jsonPath("$.content.basics.label").isEqualTo("Software Engineer")
            .jsonPath("$.content.basics.image").isEqualTo("https://i.pravatar.cc/300")
            .jsonPath("$.content.basics.email").isEqualTo("john.doe@example.com")
            .jsonPath("$.content.basics.phone").isEqualTo("+1234567890")
            .jsonPath("$.content.basics.url").isEqualTo("https://johndoe.dev")
            .jsonPath("$.content.basics.summary")
            .isEqualTo("Experienced software engineer with a passion for building scalable applications.")
            // content.basics.location
            .jsonPath("$.content.basics.location.address").isEqualTo("123 Main St")
            .jsonPath("$.content.basics.location.postalCode").isEqualTo("12345")
            .jsonPath("$.content.basics.location.city").isEqualTo("San Francisco")
            .jsonPath("$.content.basics.location.countryCode").isEqualTo("US")
            .jsonPath("$.content.basics.location.region").isEqualTo("California")
            // content.basics.profiles
            .jsonPath("$.content.basics.profiles[0].network").isEqualTo("LinkedIn")
            .jsonPath("$.content.basics.profiles[0].username").isEqualTo("johndoe")
            .jsonPath("$.content.basics.profiles[0].url")
            .isEqualTo("https://linkedin.com/in/johndoe")
            .jsonPath("$.content.basics.profiles[1].network").isEqualTo("GitHub")
            .jsonPath("$.content.basics.profiles[1].username").isEqualTo("johndoe")
            .jsonPath("$.content.basics.profiles[1].url").isEqualTo("https://github.com/johndoe")
            // work
            .jsonPath("$.content.work[0].name").isEqualTo("ACME Corp")
            .jsonPath("$.content.work[0].position").isEqualTo("Software Engineer")
            .jsonPath("$.content.work[0].startDate").isEqualTo("2020-01-01")
            .jsonPath("$.content.work[0].endDate").isEqualTo("2023-06-30")
            .jsonPath("$.content.work[0].location").isEqualTo("Remote")
            .jsonPath("$.content.work[0].summary").isEqualTo("Developed scalable backend services.")
            .jsonPath("$.content.work[0].highlights[0]").isEqualTo("Led migration to Kotlin")
            .jsonPath("$.content.work[0].highlights[1]").isEqualTo("Implemented CI/CD pipeline")
            // volunteer
            .jsonPath("$.content.volunteer[0].organization").isEqualTo("Open Source Org")
            .jsonPath("$.content.volunteer[0].position").isEqualTo("Contributor")
            .jsonPath("$.content.volunteer[0].url").isEqualTo("https://opensource.org")
            .jsonPath("$.content.volunteer[0].startDate").isEqualTo("2019-01-01")
            .jsonPath("$.content.volunteer[0].endDate").isEqualTo("2019-12-31")
            .jsonPath("$.content.volunteer[0].summary")
            .isEqualTo("Contributed to open source projects.")
            .jsonPath("$.content.volunteer[0].highlights[0]").isEqualTo("Fixed critical bugs")
            .jsonPath("$.content.volunteer[0].highlights[1]").isEqualTo("Reviewed PRs")
            // education
            .jsonPath("$.content.education[0].institution").isEqualTo("MIT")
            .jsonPath("$.content.education[0].area").isEqualTo("Computer Science")
            .jsonPath("$.content.education[0].studyType").isEqualTo("Bachelor")
            .jsonPath("$.content.education[0].startDate").isEqualTo("2015-09-01")
            .jsonPath("$.content.education[0].endDate").isEqualTo("2019-06-30")
            .jsonPath("$.content.education[0].score").isEqualTo("4.0")
            .jsonPath("$.content.education[0].url").isEqualTo("https://mit.edu")
            .jsonPath("$.content.education[0].courses[0]").isEqualTo("Algorithms")
            .jsonPath("$.content.education[0].courses[1]").isEqualTo("Distributed Systems")
            // awards
            .jsonPath("$.content.awards[0].title").isEqualTo("Employee of the Year")
            .jsonPath("$.content.awards[0].date").isEqualTo("2022-12-01")
            .jsonPath("$.content.awards[0].awarder").isEqualTo("ACME Corp")
            .jsonPath("$.content.awards[0].summary")
            .isEqualTo("Recognized for outstanding performance.")
            // certificates
            .jsonPath("$.content.certificates[0].name").isEqualTo("AWS Certified Developer")
            .jsonPath("$.content.certificates[0].date").isEqualTo("2021-05-01")
            .jsonPath("$.content.certificates[0].url")
            .isEqualTo("https://aws.amazon.com/certification")
            .jsonPath("$.content.certificates[0].issuer").isEqualTo("Amazon")
            // publications
            .jsonPath("$.content.publications[0].name").isEqualTo("Kotlin for Backend Development")
            .jsonPath("$.content.publications[0].publisher").isEqualTo("Tech Books")
            .jsonPath("$.content.publications[0].releaseDate").isEqualTo("2023-01-15")
            .jsonPath("$.content.publications[0].url")
            .isEqualTo("https://techbooks.com/kotlin-backend")
            .jsonPath("$.content.publications[0].summary")
            .isEqualTo("A comprehensive guide to Kotlin in backend systems.")
            // skills
            .jsonPath("$.content.skills[0].name").isEqualTo("Kotlin")
            .jsonPath("$.content.skills[0].level").isEqualTo("Expert")
            .jsonPath("$.content.skills[0].keywords[0]").isEqualTo("Spring Boot")
            .jsonPath("$.content.skills[0].keywords[1]").isEqualTo("Coroutines")
            .jsonPath("$.content.skills[0].keywords[2]").isEqualTo("WebFlux")
            .jsonPath("$.content.skills[1].name").isEqualTo("TypeScript")
            .jsonPath("$.content.skills[1].level").isEqualTo("Advanced")
            .jsonPath("$.content.skills[1].keywords[0]").isEqualTo("Vue.js")
            .jsonPath("$.content.skills[1].keywords[1]").isEqualTo("Astro")
            .jsonPath("$.content.skills[1].keywords[2]").isEqualTo("Vite")
            // languages
            .jsonPath("$.content.languages[0].language").isEqualTo("English")
            .jsonPath("$.content.languages[0].fluency").isEqualTo("Native")
            .jsonPath("$.content.languages[1].language").isEqualTo("Spanish")
            .jsonPath("$.content.languages[1].fluency").isEqualTo("Professional")
            // interests
            .jsonPath("$.content.interests[0].name").isEqualTo("Open Source")
            .jsonPath("$.content.interests[0].keywords[0]").isEqualTo("Kotlin")
            .jsonPath("$.content.interests[0].keywords[1]").isEqualTo("OSS")
            .jsonPath("$.content.interests[0].keywords[2]").isEqualTo("Community")
            // references
            .jsonPath("$.content.references[0].name").isEqualTo("Jane Smith")
            .jsonPath("$.content.references[0].reference").isEqualTo("Former manager at ACME Corp.")
            // projects
            .jsonPath("$.content.projects[0].name").isEqualTo("Resume Generator")
            .jsonPath("$.content.projects[0].description")
            .isEqualTo("A SaaS for generating professional resumes.")
            .jsonPath("$.content.projects[0].url").isEqualTo("https://resume-gen.dev")
            .jsonPath("$.content.projects[0].startDate[0]").isEqualTo(2023)
            .jsonPath("$.content.projects[0].startDate[1]").isEqualTo(2)
            .jsonPath("$.content.projects[0].startDate[2]").isEqualTo(1)
            .jsonPath("$.content.projects[0].endDate[0]").isEqualTo(2023)
            .jsonPath("$.content.projects[0].endDate[1]").isEqualTo(8)
            .jsonPath("$.content.projects[0].endDate[2]").isEqualTo(1)
            .jsonPath("$.content.projects[0].highlights[0]").isEqualTo("Used Kotlin and Vue.js")
            .jsonPath("$.content.projects[0].highlights[1]").isEqualTo("Deployed on AWS")
            .jsonPath("$.content.projects[0].keywords[0]").isEqualTo("Kotlin")
            .jsonPath("$.content.projects[0].keywords[1]").isEqualTo("Vue.js")
            .jsonPath("$.content.projects[0].keywords[2]").isEqualTo("AWS")
            .jsonPath("$.content.projects[0].roles[0]").isEqualTo("Lead Developer")
            .jsonPath("$.content.projects[0].entity").isEqualTo("Personal")
            .jsonPath("$.content.projects[0].type").isEqualTo("application")
    }
}
