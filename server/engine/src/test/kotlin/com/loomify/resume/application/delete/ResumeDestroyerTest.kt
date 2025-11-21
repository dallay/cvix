package com.loomify.resume.application.delete

import com.loomify.UnitTest
import com.loomify.common.domain.bus.event.EventBroadcaster
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.event.ResumeDeletedEvent
import com.loomify.resume.domain.exception.ResumeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class ResumeDestroyerTest {

    private val resumeRepository: ResumeRepository = mockk()
    private val eventBroadcaster: EventBroadcaster<ResumeDeletedEvent> = mockk(relaxed = true)
    private val resumeDestroyer = ResumeDestroyer(resumeRepository, eventBroadcaster)

    @Test
    fun `should delete resume and publish event when authorized`() = runTest {
        // Arrange
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        coEvery { resumeRepository.deleteIfAuthorized(resumeId, userId) } returns 1L

        // Act
        resumeDestroyer.deleteResume(resumeId, userId)

        // Assert
        coVerify { resumeRepository.deleteIfAuthorized(resumeId, userId) }
        coVerify { eventBroadcaster.publish(ResumeDeletedEvent(resumeId.toString(), userId.toString())) }
    }

    @Test
    fun `should throw ResumeNotFoundException when resume does not exist`() = runTest {
        // Arrange
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        coEvery { resumeRepository.deleteIfAuthorized(resumeId, userId) } returns 0L

        // Act & Assert
        assertThrows<ResumeNotFoundException> {
            resumeDestroyer.deleteResume(resumeId, userId)
        }

        coVerify { resumeRepository.deleteIfAuthorized(resumeId, userId) }
        coVerify(exactly = 0) { eventBroadcaster.publish(ofType<ResumeDeletedEvent>()) }
    }
}
