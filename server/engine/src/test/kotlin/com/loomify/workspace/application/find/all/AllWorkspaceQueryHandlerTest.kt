package com.loomify.workspace.application.find.all

import com.loomify.UnitTest
import com.loomify.workspace.WorkspaceStub
import com.loomify.workspace.domain.Workspace
import com.loomify.workspace.domain.WorkspaceFinderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class AllWorkspaceQueryHandlerTest {
    private lateinit var repository: WorkspaceFinderRepository
    private lateinit var finder: AllWorkspaceFinder
    private lateinit var handler: AllWorkspaceQueryHandler
    private lateinit var workspaces: List<Workspace>

    @BeforeEach
    fun setUp() {
        repository = mockk()
        finder = AllWorkspaceFinder(repository)
        handler = AllWorkspaceQueryHandler(finder)
        workspaces = WorkspaceStub.dummyRandomWorkspaces(6)

        coEvery { repository.findAll() } returns workspaces
    }

    @Test
    fun `should find all workspaces`() = runTest {
        // Given
        val query = AllWorkspaceQuery()

        // When
        val response = handler.handle(query)

        // Then
        assertEquals(workspaces.size, response.data.size)
    }
}
