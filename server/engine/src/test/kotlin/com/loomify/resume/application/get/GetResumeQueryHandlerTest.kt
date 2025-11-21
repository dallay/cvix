package com.loomify.resume.application.get

import com.loomify.UnitTest
import com.loomify.resume.application.ResumeTestFixtures
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.exception.ResumeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class GetResumeQueryHandlerTest {
    private lateinit var getResumeQueryHandler: GetResumeQueryHandler
    private val resumeRepository: ResumeRepository = mockk()

    @BeforeEach
    fun setUp() {
        getResumeQueryHandler = GetResumeQueryHandler(resumeRepository)
    }

    @Test
    fun `should return resume document when resume exists`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expectedDocument = ResumeTestFixtures.createResumeDocument(
            id = resumeId,
            userId = userId,
        )
        val query = GetResumeQuery(id = resumeId, userId = userId)

        coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns expectedDocument

        // When
        val result = getResumeQueryHandler.handle(query)

        // Then
        assertEquals(expectedDocument.id.value, result.id)
        assertEquals(expectedDocument.userId, result.userId)
        assertEquals(expectedDocument.workspaceId, result.workspaceId)
        assertEquals(expectedDocument.title, result.title)
        assertEquals(expectedDocument.content, result.content)
        coVerify { resumeRepository.findById(eq(resumeId), eq(userId)) }
    }

    @Test
    fun `should throw ResumeNotFoundException when resume does not exist`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val query = GetResumeQuery(id = resumeId, userId = userId)

        coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns null

        // When / Then
        assertThrows<ResumeNotFoundException> {
            getResumeQueryHandler.handle(query)
        }

        coVerify { resumeRepository.findById(eq(resumeId), eq(userId)) }
    }

    @Test
    fun `should throw ResumeNotFoundException when user is not authorized`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val unauthorizedUserId = UUID.randomUUID()
        val query = GetResumeQuery(id = resumeId, userId = unauthorizedUserId)

        coEvery { resumeRepository.findById(eq(resumeId), eq(unauthorizedUserId)) } returns null

        // When / Then
        assertThrows<ResumeNotFoundException> {
            getResumeQueryHandler.handle(query)
        }

        coVerify { resumeRepository.findById(eq(resumeId), eq(unauthorizedUserId)) }
    }
}
