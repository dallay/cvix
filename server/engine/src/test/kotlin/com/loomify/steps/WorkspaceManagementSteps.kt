package com.loomify.steps

import com.loomify.ControllerIntegrationTest
import com.loomify.engine.workspace.WorkspaceStub
import com.loomify.engine.workspace.infrastructure.http.request.CreateWorkspaceRequest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.jdbc.Sql
import java.util.*

private const val ENDPOINT = "/api/workspace"

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkspaceManagementSteps : ControllerIntegrationTest() {

    private lateinit var request: CreateWorkspaceRequest
    private lateinit var workspaceId: String

    @Given("a user exists with ID {string}")
    @Sql("/db/user/users.sql")
    fun aUserExistsWithID(userId: String) {
        request = WorkspaceStub.generateRequest(ownerId = userId)
        workspaceId = UUID.randomUUID().toString()
    }

    @When("the user creates a new workspace")
    fun theUserCreatesANewWorkspace() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("$ENDPOINT/$workspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
    }

    @Then("the workspace should be created successfully")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun theWorkspaceShouldBeCreatedSuccessfully() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("$ENDPOINT/$workspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody().isEmpty
    }

    @Given("a workspace with ID {string} already exists")
    @Sql("/db/user/users.sql", "/db/workspace/all-workspaces.sql")
    fun aWorkspaceWithIDAlreadyExists(id: String) {
        request = WorkspaceStub.generateRequest()
        workspaceId = id
    }

    @When("the user tries to create a workspace with the same ID")
    fun theUserTriesToCreateAWorkspaceWithTheSameID() {
        // The action is the same as the 'then' step in this case
    }

    @Then("the workspace creation should fail")
    @Sql("/db/workspace/clean.sql", "/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun theWorkspaceCreationShouldFail() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("$ENDPOINT/$workspaceId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://loomify.com/errors/bad-request")
            .jsonPath("$.title").isEqualTo("Bad request")
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.detail").isEqualTo("Error creating workspace")
            .jsonPath("$.instance").isEqualTo("$ENDPOINT/$workspaceId")
            .jsonPath("$.errorCategory").isEqualTo("BAD_REQUEST")
            .jsonPath("$.timestamp").isNumber
    }

    @Given("a workspace with ID {string} exists")
    @Sql("/db/user/users.sql", "/db/workspace/workspace.sql")
    fun aWorkspaceWithIDExists(id: String) {
        workspaceId = id
    }

    @When("the user deletes the workspace")
    fun theUserDeletesTheWorkspace() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).delete()
            .uri("$ENDPOINT/$workspaceId")
            .exchange()
    }

    @Then("the workspace should be deleted successfully")
    @Sql("/db/workspace/clean.sql", "/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun theWorkspaceShouldBeDeletedSuccessfully() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).delete()
            .uri("$ENDPOINT/$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    @Given("a workspace with ID {string} does not exist")
    fun aWorkspaceWithIDDoesNotExist(id: String) {
        workspaceId = id
    }

    @Then("the workspace deletion should return OK")
    fun theWorkspaceDeletionShouldReturnOK() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).delete()
            .uri("$ENDPOINT/$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty
    }

    @When("the user updates the workspace with valid data")
    fun theUserUpdatesTheWorkspaceWithValidData() {
        val request = WorkspaceStub.generateUpdateRequest()
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("/api/workspace/$workspaceId/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
    }

    @Then("the workspace should be updated successfully")
    @Sql("/db/workspace/clean.sql", "/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun theWorkspaceShouldBeUpdatedSuccessfully() {
        val request = WorkspaceStub.generateUpdateRequest()
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("/api/workspace/$workspaceId/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("Workspace updated successfully")
    }

    @Then("the workspace update should return 404")
    fun theWorkspaceUpdateShouldReturn404() {
        val request = WorkspaceStub.generateUpdateRequest()
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf()).put()
            .uri("/api/workspace/$workspaceId/update")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://loomify.com/errors/entity-not-found")
            .jsonPath("$.title").isEqualTo("Entity not found")
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.detail").isEqualTo("Workspace not found")
            .jsonPath("$.instance")
            .isEqualTo("/api/workspace/$workspaceId/update")
            .jsonPath("$.errorCategory").isEqualTo("NOT_FOUND")
            .jsonPath("$.timestamp").isNotEmpty
    }

    @Given("several workspaces exist")
    @Sql("/db/user/users.sql", "/db/workspace/all-workspaces.sql")
    fun severalWorkspacesExist() {
        // Data is loaded via @Sql annotation
    }

    @When("the user requests all workspaces")
    fun theUserRequestsAllWorkspaces() {
        webTestClient
            .get()
            .uri("/api/workspace")
            .exchange()
    }

    @Then("a list of all workspaces should be returned")
    @Sql("/db/workspace/clean.sql", "/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aListOfAllWorkspacesShouldBeReturned() {
        webTestClient
            .get()
            .uri("/api/workspace")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(3)
    }

    @When("the user requests the workspace by its ID")
    fun theUserRequestsTheWorkspaceByItsID() {
        webTestClient
            .get()
            .uri("/api/workspace/$workspaceId")
            .exchange()
    }

    @Then("the workspace details should be returned")
    @Sql("/db/workspace/clean.sql", "/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun theWorkspaceDetailsShouldBeReturned() {
        webTestClient
            .get()
            .uri("/api/workspace/$workspaceId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(workspaceId)
            .jsonPath("$.name").isEqualTo("Test: My First Workspace")
            .jsonPath("$.ownerId").isEqualTo("efc4b2b8-08be-4020-93d5-f795762bf5c9")
            .jsonPath("$.createdAt").isNotEmpty
            .jsonPath("$.updatedAt").isNotEmpty
    }

    @Then("the workspace should not be found")
    fun theWorkspaceShouldNotBeFound() {
        webTestClient.get()
            .uri("/api/workspace/$workspaceId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.type").isEqualTo("https://loomify.com/errors/entity-not-found")
            .jsonPath("$.title").isEqualTo("Entity not found")
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.detail").isEqualTo("Workspace not found")
            .jsonPath("$.instance")
            .isEqualTo("/api/workspace/$workspaceId")
            .jsonPath("$.errorCategory").isEqualTo("NOT_FOUND")
            .jsonPath("$.timestamp").isNotEmpty
    }
}
