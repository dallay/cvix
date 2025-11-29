package com.loomify.workspace.application.security

import com.loomify.UnitTest
import com.loomify.workspace.domain.WorkspaceAuthorizationException
import com.loomify.workspace.domain.WorkspaceMemberRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class WorkspaceAuthorizationServiceTest {

    private val workspaceMemberRepository: WorkspaceMemberRepository = mockk()
    private val workspaceAuthorizationService = WorkspaceAuthorizationService(workspaceMemberRepository)

    @Test
    fun should_allowAccess_when_userIsMemberOfWorkspace() = runTest {
        val workspaceId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        coEvery { workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId) } returns true

        workspaceAuthorizationService.ensureAccess(workspaceId, userId)
    }

    @Test
    fun should_throwException_when_userIsNotMemberOfWorkspace() = runTest {
        val workspaceId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        coEvery { workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId) } returns false

        val exception = assertThrows<WorkspaceAuthorizationException> {
            workspaceAuthorizationService.ensureAccess(workspaceId, userId)
        }

        assertTrue(exception.message.contains("User $userId has no access to workspace $workspaceId"))
    }

    @Test
    fun should_throwException_when_invalidUUIDStringsProvided(): Unit = runTest {
        val invalidWorkspaceId = "invalid-uuid"
        val invalidUserId = "invalid-uuid"

        assertThrows<IllegalArgumentException> {
            workspaceAuthorizationService.ensureAccess(invalidWorkspaceId, invalidUserId)
        }
    }
}
