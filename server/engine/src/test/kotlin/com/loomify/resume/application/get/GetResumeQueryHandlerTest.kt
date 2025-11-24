package com.loomify.resume.application.get

import com.loomify.UnitTest
import com.loomify.resume.ResumeTestFixtures
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.exception.ResumeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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
        assertEquals(expectedDocument.id.id, result.id)
        assertEquals(expectedDocument.userId, result.userId)
        assertEquals(expectedDocument.workspaceId, result.workspaceId)
        assertEquals(expectedDocument.title, result.title)
        assertEquals(expectedDocument.content, result.content)
        coVerify { resumeRepository.findById(eq(resumeId), eq(userId)) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["resume does not exist", "user is not authorized"])
    fun `should throw ResumeNotFoundException when repository returns null`(
        @Suppress("UnusedParameter") scenario: String
    ) =
        runTest {
            val resumeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val query = GetResumeQuery(id = resumeId, userId = userId)
            coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns null
            assertFailsWith<ResumeNotFoundException> { getResumeQueryHandler.handle(query) }
            coVerify { resumeRepository.findById(eq(resumeId), eq(userId)) }
        }
}
