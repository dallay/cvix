package com.loomify.steps

import com.loomify.IntegrationTest
import com.loomify.engine.users.domain.UserId
import com.loomify.engine.workspace.WorkspaceStub
import com.loomify.engine.workspace.domain.Workspace
import com.loomify.engine.workspace.domain.WorkspaceException
import com.loomify.engine.workspace.domain.WorkspaceId
import com.loomify.engine.workspace.domain.WorkspaceRole
import com.loomify.engine.workspace.infrastructure.persistence.WorkspaceStoreR2DbcRepository
import com.loomify.engine.workspace.infrastructure.persistence.repository.WorkspaceMemberR2dbcRepository
import com.loomify.engine.workspace.infrastructure.persistence.repository.WorkspaceR2dbcRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IntegrationTest
@Sql(
    scripts = ["/db/user/users.sql", "/db/workspace/workspace.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/db/workspace/clean.sql", "/db/user/clean.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class WorkspaceRepositorySteps {

    @Autowired
    private lateinit var workspaceStoreR2dbcRepository: WorkspaceStoreR2DbcRepository

    @Autowired
    private lateinit var directWorkspaceR2dbcRepository: WorkspaceR2dbcRepository

    @Autowired
    private lateinit var directWorkspaceMemberR2dbcRepository: WorkspaceMemberR2dbcRepository

    private lateinit var workspace: Workspace
    private val commonExistingUserId =
        UserId(UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9"))

    @Given("a workspace to be created")
    fun aWorkspaceToBeCreated() {
        workspace = WorkspaceStub.create(name = "Test Create").copy(
            ownerId = commonExistingUserId,
            members = mutableSetOf(commonExistingUserId),
        )
    }

    @When("the workspace is created")
    fun theWorkspaceIsCreated() = runTest {
        workspaceStoreR2dbcRepository.create(workspace)
    }

    @Then("the workspace should be saved in the database")
    fun theWorkspaceShouldBeSavedInTheDatabase() = runTest {
        val fetched = directWorkspaceR2dbcRepository.findById(workspace.id.value)
        Assertions.assertNotNull(fetched)
        Assertions.assertEquals(workspace.name, fetched!!.name)
        Assertions.assertEquals(workspace.ownerId.value, fetched.ownerId)

        val members =
            directWorkspaceMemberR2dbcRepository.findByWorkspaceId(workspace.id.value)
                .toList()
        Assertions.assertEquals(1, members.size)
        Assertions.assertTrue(members.any { it.userId == commonExistingUserId.value })
    }

    @Given("an existing workspace")
    fun anExistingWorkspace() = runTest {
        workspace = WorkspaceStub.create().copy(ownerId = commonExistingUserId, members = mutableSetOf(commonExistingUserId))
        workspaceStoreR2dbcRepository.create(workspace)
    }

    @When("the workspace is updated")
    fun theWorkspaceIsUpdated() = runTest {
        workspace = workspace.copy(name = "Updated Name")
        workspaceStoreR2dbcRepository.update(workspace)
    }

    @Then("the workspace should be updated in the database")
    fun theWorkspaceShouldBeUpdatedInTheDatabase() = runTest {
        val fetched = directWorkspaceR2dbcRepository.findById(workspace.id.value)
        Assertions.assertNotNull(fetched)
        Assertions.assertEquals("Updated Name", fetched!!.name)
    }

    @Given("a non-existent workspace")
    fun aNonExistentWorkspace() {
        workspace = WorkspaceStub.create(id = UUID.randomUUID()).copy(ownerId = commonExistingUserId)
    }

    @Then("a workspace exception should be thrown")
    fun aWorkspaceExceptionShouldBeThrown() {
        assertThrows<WorkspaceException> {
            runTest {
                workspaceStoreR2dbcRepository.update(workspace)
            }
        }
    }

    @When("the workspace is deleted")
    fun theWorkspaceIsDeleted() = runTest {
        workspaceStoreR2dbcRepository.delete(workspace.id)
    }

    @Then("the workspace should be removed from the database")
    fun theWorkspaceShouldBeRemovedFromTheDatabase() = runTest {
        val fetched = directWorkspaceR2dbcRepository.findById(workspace.id.value)
        Assertions.assertNull(fetched)
        val members =
            directWorkspaceMemberR2dbcRepository.findByWorkspaceId(workspace.id.value).toList()
        Assertions.assertTrue(members.isEmpty())
    }

    @When("the workspace is searched by id")
    fun theWorkspaceIsSearchedById() {
        // No action needed here, the search is done in the 'then' step
    }

    @Then("the workspace should be found")
    fun theWorkspaceShouldBeFound() = runTest {
        val result = workspaceStoreR2dbcRepository.findById(workspace.id)
        Assertions.assertNotNull(result)
        Assertions.assertEquals(workspace.id, result!!.id)
        Assertions.assertEquals(workspace.name, result.name)
    }

    @Given("an existing workspace with a member")
    fun anExistingWorkspaceWithAMember() = runTest {
        workspace = WorkspaceStub.create().copy(ownerId = commonExistingUserId, members = mutableSetOf(commonExistingUserId))
        workspaceStoreR2dbcRepository.create(workspace)
    }

    @When("the workspaces are searched by member id")
    fun theWorkspacesAreSearchedByMemberId() {
        // No action needed here, the search is done in the 'then' step
    }

    @Then("the workspaces should be found")
    fun theWorkspacesShouldBeFound() = runTest {
        val result = workspaceStoreR2dbcRepository.findByMemberId(commonExistingUserId)
        Assertions.assertTrue(result.isNotEmpty())
    }

    @When("the members are searched by workspace id")
    fun theMembersAreSearchedByWorkspaceId() {
        // No action needed here, the search is done in the 'then' step
    }

    @Then("the members should be found")
    fun theMembersShouldBeFound() = runTest {
        val members = workspaceStoreR2dbcRepository.findByWorkspaceId(workspace.id.value)
        Assertions.assertEquals(1, members.size)
        Assertions.assertTrue(members.any { it.id.userId == commonExistingUserId.value })
    }

    @When("the members are searched by user id")
    fun theMembersAreSearchedByUserId() {
        // No action needed here, the search is done in the 'then' step
    }

    @Then("the members should be found by user id")
    fun theMembersShouldBeFoundByUserId() = runTest {
        val workspacesForOwner = workspaceStoreR2dbcRepository.findByUserId(commonExistingUserId.value)
        Assertions.assertTrue(workspacesForOwner.isNotEmpty())
    }

    @When("checking if the user is a member of the workspace")
    fun checkingIfTheUserIsAMemberOfTheWorkspace() {
        // No action needed here, the check is done in the 'then' step
    }

    @Then("the result should be true")
    fun theResultShouldBeTrue() = runTest {
        val isOwnerMemberOfWorkspace = workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
            workspace.id.value,
            commonExistingUserId.value,
        )
        Assertions.assertTrue(isOwnerMemberOfWorkspace)
    }

    @When("the member is deleted and then inserted again")
    fun theMemberIsDeletedAndThenInsertedAgain() = runTest {
        workspaceStoreR2dbcRepository.deleteByWorkspaceIdAndUserId(
            workspace.id.value,
            commonExistingUserId.value,
        )
        workspaceStoreR2dbcRepository.insertWorkspaceMember(
            workspace.id.value,
            commonExistingUserId.value,
            WorkspaceRole.VIEWER.name,
        )
    }

    @Then("the member should be correctly deleted and inserted")
    fun theMemberShouldBeCorrectlyDeletedAndInserted() = runTest {
        val members = workspaceStoreR2dbcRepository.findByWorkspaceId(workspace.id.value)
        Assertions.assertEquals(1, members.size)
        Assertions.assertTrue(
            members.any {
                it.id.userId == commonExistingUserId.value &&
                    it.role == WorkspaceRole.VIEWER
            },
        )
    }
}
