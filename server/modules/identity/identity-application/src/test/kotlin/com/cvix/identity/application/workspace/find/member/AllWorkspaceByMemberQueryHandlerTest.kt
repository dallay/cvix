package com.cvix.identity.application.workspace.find.member

import com.cvix.UnitTest
import com.cvix.identity.domain.user.UserId
import com.cvix.identity.domain.workspace.Workspace
import com.cvix.identity.domain.workspace.WorkspaceFinderRepository
import com.cvix.identity.domain.workspace.WorkspaceStub
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class AllWorkspaceByMemberQueryHandlerTest {
    private lateinit var repository: WorkspaceFinderRepository
    private lateinit var finder: AllWorkspaceByMemberFinder
    private lateinit var handler: AllWorkspaceByMemberQueryHandler
    private lateinit var workspaces: List<Workspace>
    private var userId: UserId = UserId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        repository = mockk()
        finder = AllWorkspaceByMemberFinder(repository)
        handler = AllWorkspaceByMemberQueryHandler(finder)
        workspaces = WorkspaceStub.dummyRandomWorkspaces(6)
        userId = UserId(UUID.randomUUID())

        coEvery { repository.findByMemberId(userId) } returns workspaces
    }

    @Test
    fun `should find all workspaces`() = runTest {
        // Given
        val query = AllWorkspaceByMemberQuery(userId.value)

        // When
        val response = handler.handle(query)

        // Then
        assertEquals(workspaces.size, response.data.size)
    }

    @Test
    fun `should return empty list when no workspaces found`() = runTest {
        // Given
        coEvery { repository.findByMemberId(any()) } returns emptyList()
        val query = AllWorkspaceByMemberQuery(userId.value)

        // When
        val response = handler.handle(query)

        // Then
        assertEquals(0, response.data.size)
    }

    @Test
    fun `should handle repository exception`(): Unit = runTest {
        // Given
        coEvery { repository.findByMemberId(any()) } throws RuntimeException("Database error")
        val query = AllWorkspaceByMemberQuery(userId.value)

        // When & Then
        val ex = assertFailsWith<RuntimeException> {
            handler.handle(query)
        }
        assertEquals("Database error", ex.message)
    }
}
