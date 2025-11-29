package com.loomify.resume.application.list

import com.github.dockerjava.api.exception.UnauthorizedException
import com.loomify.UnitTest
import com.loomify.resume.ResumeTestFixtures
import com.loomify.workspace.application.security.WorkspaceAuthorizationService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class ListResumesQueryHandlerTest {
    private lateinit var listResumesQueryHandler: ListResumesQueryHandler
    private val resumeCatalog: ResumeCatalog = mockk()
    private val workspaceAuthorizationService: WorkspaceAuthorizationService = mockk()

    @BeforeEach
    fun setUp() {
        listResumesQueryHandler =
            ListResumesQueryHandler(resumeCatalog, workspaceAuthorizationService)
        coEvery {
            workspaceAuthorizationService.ensureAccess(any(UUID::class), any(UUID::class))
        } returns Unit
    }

    @Test
    fun `should return list of resume documents when resumes exist`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListResumesQuery(
            userId = userId,
            workspaceId = workspaceId,
            limit = 50,
            cursor = null,
        )
        val expectedDocuments = listOf(
            ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId),
            ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId),
        )

        coEvery {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(50),
                isNull(),
            )
        } returns expectedDocuments

        // When
        val result = listResumesQueryHandler.handle(query)

        // Then
        assertEquals(2, result.data.size)
        assertEquals(expectedDocuments[0].id.id, result.data[0].id)
        assertEquals(expectedDocuments[1].id.id, result.data[1].id)
        coVerify {
            workspaceAuthorizationService.ensureAccess(eq(workspaceId), eq(userId))
        }
        coVerify {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(50),
                isNull(),
            )
        }
    }

    @Test
    fun `should return empty list when no resumes exist`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListResumesQuery(
            userId = userId,
            workspaceId = workspaceId,
            limit = 50,
            cursor = null,
        )

        coEvery {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(50),
                isNull(),
            )
        } returns emptyList()

        // When
        val result = listResumesQueryHandler.handle(query)

        // Then
        assertTrue(result.data.isEmpty())
        coVerify {
            workspaceAuthorizationService.ensureAccess(eq(workspaceId), eq(userId))
        }
        coVerify {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(50),
                isNull(),
            )
        }
    }

    @Test
    fun `should handle pagination with cursor`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val cursorId = UUID.randomUUID()
        val query = ListResumesQuery(
            userId = userId,
            workspaceId = workspaceId,
            limit = 10,
            cursor = cursorId,
        )
        val expectedDocuments = listOf(
            ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId),
        )

        coEvery {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(10),
                eq(cursorId),
            )
        } returns expectedDocuments

        // When
        val result = listResumesQueryHandler.handle(query)

        // Then
        assertEquals(1, result.data.size)
        assertEquals(expectedDocuments[0].id.id, result.data[0].id)
        coVerify {
            workspaceAuthorizationService.ensureAccess(eq(workspaceId), eq(userId))
        }
        coVerify {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(10),
                eq(cursorId),
            )
        }
    }

    @Test
    fun `should respect limit parameter`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListResumesQuery(
            userId = userId,
            workspaceId = workspaceId,
            limit = 5,
            cursor = null,
        )
        val expectedDocuments = (1..5).map {
            ResumeTestFixtures.createResumeDocument(userId = userId, workspaceId = workspaceId)
        }

        coEvery {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(5),
                isNull(),
            )
        } returns expectedDocuments

        // When
        val result = listResumesQueryHandler.handle(query)

        // Then
        assertEquals(5, result.data.size)
        coVerify {
            workspaceAuthorizationService.ensureAccess(eq(workspaceId), eq(userId))
        }
        coVerify {
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(5),
                isNull(),
            )
        }
    }

    @Test
    fun `should fail when user lacks workspace access`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val query = ListResumesQuery(userId, workspaceId, 50, null)

        coEvery {
            workspaceAuthorizationService.ensureAccess(any(UUID::class), any(UUID::class))
        } throws UnauthorizedException("Access denied")

        // When/Then
        assertThrows<UnauthorizedException> {
            listResumesQueryHandler.handle(query)
        }

        coVerify(exactly = 0) { resumeCatalog.listResumes(any(), any(), any(), any()) }
    }
}
