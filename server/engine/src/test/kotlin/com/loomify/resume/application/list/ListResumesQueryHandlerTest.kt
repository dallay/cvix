package com.loomify.resume.application.list

import com.loomify.UnitTest
import com.loomify.resume.application.ResumeTestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class ListResumesQueryHandlerTest {
    private lateinit var listResumesQueryHandler: ListResumesQueryHandler
    private val resumeCatalog: ResumeCatalog = mockk()

    @BeforeEach
    fun setUp() {
        listResumesQueryHandler = ListResumesQueryHandler(resumeCatalog)
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
        assertEquals(expectedDocuments[0].id.value, result.data[0].id)
        assertEquals(expectedDocuments[1].id.value, result.data[1].id)
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
        assertEquals(expectedDocuments[0].id.value, result.data[0].id)
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
            resumeCatalog.listResumes(
                eq(userId),
                eq(workspaceId),
                eq(5),
                isNull(),
            )
        }
    }
}
