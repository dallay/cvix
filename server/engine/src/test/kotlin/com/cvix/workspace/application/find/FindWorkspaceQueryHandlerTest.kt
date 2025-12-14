package com.cvix.workspace.application.find

import com.cvix.UnitTest
import com.cvix.workspace.WorkspaceStub
import com.cvix.workspace.application.WorkspaceResponse
import com.cvix.workspace.domain.WorkspaceId
import com.cvix.workspace.domain.WorkspaceNotFoundException
import io.mockk.coEvery
import io.mockk.mockk
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class FindWorkspaceQueryHandlerTest {

    private lateinit var workspaceFinder: WorkspaceFinder
    private lateinit var findWorkspaceQueryHandler: FindWorkspaceQueryHandler

    @BeforeEach
    fun setup() {
        workspaceFinder = mockk()
        findWorkspaceQueryHandler = FindWorkspaceQueryHandler(workspaceFinder)
    }

    @Test
    fun `should return workspace response when workspace is found`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val workspaceId = WorkspaceId(id)
        val workspace = WorkspaceStub.create()
        val workspaceResponse = WorkspaceResponse.from(workspace)
        coEvery { workspaceFinder.find(workspaceId) } returns workspace

        // When
        val result = findWorkspaceQueryHandler.handle(FindWorkspaceQuery(id))

        // Then
        assertEquals(workspaceResponse, result)
    }

    @Test
    fun `should throw exception when workspace is not found`() = runTest {
        val id = UUID.randomUUID()
        val workspaceId = WorkspaceId(id)
        coEvery { workspaceFinder.find(workspaceId) } returns null

        assertFailsWith(WorkspaceNotFoundException::class) {
            findWorkspaceQueryHandler.handle(FindWorkspaceQuery(id))
        }
    }
}
