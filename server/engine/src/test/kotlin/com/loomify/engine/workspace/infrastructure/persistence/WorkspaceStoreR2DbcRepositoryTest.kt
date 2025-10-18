package com.loomify.engine.workspace.infrastructure.persistence

import com.loomify.IntegrationTest
import com.loomify.engine.users.domain.UserId
import com.loomify.engine.workspace.WorkspaceStub
import com.loomify.engine.workspace.domain.Workspace
import com.loomify.engine.workspace.domain.WorkspaceException
import com.loomify.engine.workspace.domain.WorkspaceId
import com.loomify.engine.workspace.domain.WorkspaceRole
import com.loomify.engine.workspace.infrastructure.persistence.repository.WorkspaceMemberR2dbcRepository
import com.loomify.engine.workspace.infrastructure.persistence.repository.WorkspaceR2dbcRepository
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

@IntegrationTest
@Sql(
    scripts = ["/db/user/users.sql", "/db/workspace/workspace.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/db/workspace/clean.sql", "/db/user/clean.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
internal class WorkspaceStoreR2DbcRepositoryTest {
    @Autowired
    private lateinit var workspaceStoreR2dbcRepository: WorkspaceStoreR2DbcRepository

    @Autowired
    private lateinit var directWorkspaceR2dbcRepository: WorkspaceR2dbcRepository

    @Autowired
    private lateinit var directWorkspaceMemberR2dbcRepository: WorkspaceMemberR2dbcRepository

    private lateinit var ownerId: UserId
    private lateinit var memberId1: UserId
    private lateinit var memberId2: UserId
    private lateinit var workspace1: Workspace
    private lateinit var workspace2: Workspace
    private lateinit var workspace: Workspace

    private val commonExistingUserId =
        UserId(UUID.fromString("efc4b2b8-08be-4020-93d5-f795762bf5c9"))

    @BeforeEach
    fun setUp() {
        ownerId = commonExistingUserId
        memberId1 = commonExistingUserId
        memberId2 = commonExistingUserId

        workspace1 = Workspace(
            id = WorkspaceId(UUID.randomUUID()),
            name = "Workspace 1",
            description = "Description for Workspace 1",
            ownerId = ownerId,
            members = mutableSetOf(ownerId),
        )

        workspace2 = Workspace(
            id = WorkspaceId(UUID.randomUUID()),
            name = "Workspace 2",
            description = "Description for Workspace 2",
            ownerId = ownerId,
            members = mutableSetOf(ownerId),
        )

        val stub = WorkspaceStub.create()
        workspace = stub.copy(ownerId = ownerId, members = mutableSetOf(ownerId))

        runTest {
            directWorkspaceMemberR2dbcRepository.deleteAll()
            directWorkspaceR2dbcRepository.deleteAll()
            workspaceStoreR2dbcRepository.create(workspace1)
            workspaceStoreR2dbcRepository.create(workspace2)
        }
    }

    @AfterEach
    fun tearDown() = runTest {
        directWorkspaceMemberR2dbcRepository.deleteAll()
        directWorkspaceR2dbcRepository.deleteAll()
    }

    @Test
    fun `should create workspace`() = runTest {
        val stubWorkspace = WorkspaceStub.create(name = "Test Create")
        val workspaceToCreate = stubWorkspace.copy(
            ownerId = commonExistingUserId,
            members = mutableSetOf(commonExistingUserId),
        )

        workspaceStoreR2dbcRepository.create(workspaceToCreate)

        val fetched = directWorkspaceR2dbcRepository.findById(workspaceToCreate.id.value)
        assertNotNull(fetched)
        assertEquals(workspaceToCreate.name, fetched!!.name)
        assertEquals(workspaceToCreate.ownerId.value, fetched.ownerId)

        val members =
            directWorkspaceMemberR2dbcRepository.findByWorkspaceId(workspaceToCreate.id.value)
                .toList()
        assertEquals(1, members.size)
        assertTrue(members.any { it.userId == commonExistingUserId.value })
    }

    @Test
    fun `should update workspace`() = runTest {
        val workspaceToUpdate = workspace1.copy(name = "Updated Name")
        workspaceStoreR2dbcRepository.update(workspaceToUpdate)

        val fetched = directWorkspaceR2dbcRepository.findById(workspaceToUpdate.id.value)
        assertNotNull(fetched)
        assertEquals("Updated Name", fetched!!.name)
    }

    @Test
    fun `should handle unexpected error during workspace update`(): Unit = runTest {
        val nonExistentWorkspace =
            WorkspaceStub.create(id = UUID.randomUUID()).copy(ownerId = commonExistingUserId)
        assertThrows<WorkspaceException> { // Expecting WorkspaceException due to not found for update
            workspaceStoreR2dbcRepository.update(nonExistentWorkspace)
        }
    }

    @Test
    fun `should handle error when the form does not exist during update`(): Unit = runTest {
        val nonExistentWorkspace =
            WorkspaceStub.create(id = UUID.randomUUID()).copy(ownerId = commonExistingUserId)
        assertThrows<WorkspaceException> {
            workspaceStoreR2dbcRepository.update(nonExistentWorkspace)
        }
    }

    @Test
    fun `should delete workspace`() = runTest {
        workspaceStoreR2dbcRepository.delete(workspace1.id)

        val fetched = directWorkspaceR2dbcRepository.findById(workspace1.id.value)
        assertNull(fetched)
        val members =
            directWorkspaceMemberR2dbcRepository.findByWorkspaceId(workspace1.id.value).toList()
        assertTrue(members.isEmpty())
    }

    @Test
    fun `should find workspace by id`() = runTest {
        val result = workspaceStoreR2dbcRepository.findById(workspace1.id)

        assertNotNull(result)
        assertEquals(workspace1.id, result!!.id)
        assertEquals(workspace1.name, result.name)
    }

    @Test
    fun `should find all workspaces by memberId`() = runTest {
        val result = workspaceStoreR2dbcRepository.findByMemberId(ownerId)

        assertTrue(result.isNotEmpty())
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == workspace1.id })
        assertTrue(result.any { it.id == workspace2.id })
    }

    @Test
    fun `should find workspace members by workspace id`() = runTest {
        val members = workspaceStoreR2dbcRepository.findByWorkspaceId(workspace1.id.value)
        assertEquals(1, members.size)
        assertTrue(members.any { it.id.userId == ownerId.value })
    }

    @Test
    fun `should find workspace members by user id`() = runTest {
        val workspacesForOwner = workspaceStoreR2dbcRepository.findByUserId(ownerId.value)
        val workspacesForMember1 = workspaceStoreR2dbcRepository.findByUserId(memberId1.value)

        assertEquals(2, workspacesForOwner.size)
        assertTrue(workspacesForOwner.any { it.id.workspaceId == workspace1.id.value })
        assertTrue(workspacesForOwner.any { it.id.workspaceId == workspace2.id.value })

        assertEquals(2, workspacesForMember1.size)
        assertTrue(workspacesForMember1.any { it.id.workspaceId == workspace1.id.value })
        assertTrue(workspacesForMember1.any { it.id.workspaceId == workspace2.id.value })
    }

    @Test
    fun `should check if user is member of workspace`() = runTest {
        val isOwnerMemberOfWorkspace1 = workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
            workspace1.id.value,
            ownerId.value,
        )
        val isMember1MemberOfWorkspace1 =
            workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
                workspace1.id.value,
                memberId1.value,
            )

        val nonMemberUUID = UUID.randomUUID()
        val isNonMemberInWorkspace1 = workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
            workspace1.id.value,
            nonMemberUUID,
        )

        assertTrue(isOwnerMemberOfWorkspace1)
        assertTrue(isMember1MemberOfWorkspace1)
        assertFalse(isNonMemberInWorkspace1)
    }

    @Test
    fun `should insert and delete workspace member`() = runTest {
        val existingMemberId = ownerId
        assertTrue(
            workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
                workspace1.id.value,
                existingMemberId.value,
            ),
        )

        val membersBeforeDelete =
            workspaceStoreR2dbcRepository.findByWorkspaceId(workspace1.id.value)
        val initialCount = membersBeforeDelete.size

        val deleteResult = workspaceStoreR2dbcRepository.deleteByWorkspaceIdAndUserId(
            workspace1.id.value,
            existingMemberId.value,
        )
        assertEquals(1, deleteResult)

        assertFalse(
            workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
                workspace1.id.value,
                existingMemberId.value,
            ),
        )
        val membersAfterDelete =
            workspaceStoreR2dbcRepository.findByWorkspaceId(workspace1.id.value)
        assertEquals(initialCount - 1, membersAfterDelete.size)

        val insertResult = workspaceStoreR2dbcRepository.insertWorkspaceMember(
            workspace1.id.value,
            existingMemberId.value,
            WorkspaceRole.VIEWER.name,
        )
        assertEquals(1, insertResult)
        assertTrue(
            workspaceStoreR2dbcRepository.existsByWorkspaceIdAndUserId(
                workspace1.id.value,
                existingMemberId.value,
            ),
        )
        val membersAfterReInsert =
            workspaceStoreR2dbcRepository.findByWorkspaceId(workspace1.id.value)
        assertEquals(initialCount, membersAfterReInsert.size)
        assertTrue(
            membersAfterReInsert.any {
                it.id.userId == existingMemberId.value &&
                    it.role == WorkspaceRole.VIEWER
            },
        )
    }
}
