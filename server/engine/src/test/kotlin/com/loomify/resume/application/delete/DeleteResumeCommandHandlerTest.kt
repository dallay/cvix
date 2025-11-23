package com.loomify.resume.application.delete

import com.loomify.UnitTest
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.event.ResumeDeletedEvent
import com.loomify.resume.domain.exception.ResumeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import java.util.UUID.randomUUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class DeleteResumeCommandHandlerTest {
    private val eventPublisher: EventPublisher<ResumeDeletedEvent> = mockk()
    private val repository: ResumeRepository = mockk()
    private val destroyer: ResumeDestroyer = ResumeDestroyer(repository, eventPublisher)
    private val deleteResumeCommandHandler: DeleteResumeCommandHandler =
        DeleteResumeCommandHandler(destroyer)
    private lateinit var resumeId: UUID
    private lateinit var userId: UUID
    @BeforeEach
    fun setUp() {
        resumeId = randomUUID()
        userId = randomUUID()
        coEvery { repository.existsByIdForUser(resumeId, userId) } returns true
        coEvery { repository.deleteIfAuthorized(eq(resumeId), eq(userId)) } returns 1L
        coEvery { eventPublisher.publish(any<ResumeDeletedEvent>()) } returns Unit
    }

    @Test
    fun `should delete a resume and publish event when handle is called`() = runTest {
        // Given
        val command = DeleteResumeCommand(id = resumeId, userId = userId)

        // When
        deleteResumeCommandHandler.handle(command)

        // Then
        coVerify {
            repository.deleteIfAuthorized(
                withArg {
                    assertEquals(resumeId, it)
                },
                withArg {
                    assertEquals(userId, it)
                },
            )
        }
        coVerify { eventPublisher.publish(ofType<ResumeDeletedEvent>()) }
    }

    @Test
    fun `should throw ResumeNotFoundException when resume does not exist`() = runTest {
        // Given
        val command = DeleteResumeCommand(id = resumeId, userId = userId)
        coEvery { repository.deleteIfAuthorized(resumeId, userId) } returns 0L
        coEvery { repository.existsById(resumeId) } returns false

        // When / Then
        assertFailsWith<ResumeNotFoundException> {
            deleteResumeCommandHandler.handle(command)
        }
        coVerify(exactly = 1) { repository.deleteIfAuthorized(resumeId, userId) }
        coVerify(exactly = 1) { repository.existsById(resumeId) }
        coVerify(exactly = 0) { eventPublisher.publish(any<ResumeDeletedEvent>()) }
    }
}
