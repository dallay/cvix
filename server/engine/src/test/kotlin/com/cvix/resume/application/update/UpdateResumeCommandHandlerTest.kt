package com.cvix.resume.application.update

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeRepository
import com.cvix.resume.domain.event.ResumeUpdatedEvent
import com.cvix.resume.domain.exception.ResumeNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
internal class UpdateResumeCommandHandlerTest {
    private lateinit var updateResumeCommandHandler: UpdateResumeCommandHandler
    private val eventPublisher: EventPublisher<ResumeUpdatedEvent> = mockk()
    private val resumeRepository: ResumeRepository = mockk()
    private val resumeUpdater: ResumeUpdater = ResumeUpdater(resumeRepository, eventPublisher)

    @BeforeEach
    fun setUp() {
        updateResumeCommandHandler = UpdateResumeCommandHandler(resumeUpdater)
        coEvery { eventPublisher.publish(any<ResumeUpdatedEvent>()) } returns Unit
    }

    @Test
    fun `should update resume and publish event when handle is called`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val existingDocument = ResumeTestFixtures.createResumeDocument(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
        )
        val command = ResumeTestFixtures.createUpdateCommand(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
            title = "Updated Resume Title",
        )

        coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns existingDocument
        coEvery { resumeRepository.save(any(ResumeDocument::class)) } answers {
            val arg = firstArg() as ResumeDocument
            arg
        }

        // When
        updateResumeCommandHandler.handle(command)

        // Then
        coVerify {
            resumeRepository.findById(eq(resumeId), eq(userId))
        }
        coVerify {
            resumeRepository.save(
                withArg {
                    assertEquals(resumeId, it.id.value)
                    assertEquals(userId, it.userId)
                    assertEquals(workspaceId, it.workspaceId)
                    assertEquals("Updated Resume Title", it.title)
                    assertEquals(command.content, it.content)
                    assertEquals(command.updatedBy, it.updatedBy)
                },
            )
        }
        val eventSlot = slot<ResumeUpdatedEvent>()

        coVerify(exactly = 1) {
            eventPublisher.publish(capture(eventSlot))
        }

        assertEquals(command.id, eventSlot.captured.resumeId)
        assertEquals(command.userId, eventSlot.captured.userId)
        assertEquals(command.workspaceId, eventSlot.captured.workspaceId)
    }

    @Test
    fun `should extract title from content when title is null`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val workspaceId = UUID.randomUUID()
        val existingDocument = ResumeTestFixtures.createResumeDocument(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
        )
        val command = ResumeTestFixtures.createUpdateCommand(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
            title = null, // No title provided
        )

        coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns existingDocument
        coEvery { resumeRepository.save(any(ResumeDocument::class)) } answers {
            val arg = firstArg() as ResumeDocument
            arg
        }

        // When
        updateResumeCommandHandler.handle(command)

        // Then
        coVerify {
            resumeRepository.save(
                withArg {
                    // Title should be extracted from content.basics.name
                    assertEquals("John Doe", it.title)
                },
            )
        }
        val eventSlot = slot<ResumeUpdatedEvent>()

        coVerify(exactly = 1) {
            eventPublisher.publish(capture(eventSlot))
        }

        assertEquals(command.id, eventSlot.captured.resumeId)
        assertEquals(command.userId, eventSlot.captured.userId)
        assertEquals(command.workspaceId, eventSlot.captured.workspaceId)
    }

    @Test
    fun `should throw ResumeNotFoundException when resume does not exist`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = ResumeTestFixtures.createUpdateCommand(
            id = resumeId,
            userId = userId,
        )

        coEvery { resumeRepository.findById(eq(resumeId), eq(userId)) } returns null

        // When / Then
        assertThrows<ResumeNotFoundException> {
            updateResumeCommandHandler.handle(command)
        }

        coVerify { resumeRepository.findById(eq(resumeId), eq(userId)) }
        coVerify(exactly = 0) { resumeRepository.save(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<ResumeUpdatedEvent>()) }
    }

    @Test
    fun `should throw ResumeNotFoundException when user is not authorized`() = runTest {
        // Given
        val resumeId = UUID.randomUUID()
        val unauthorizedUserId = UUID.randomUUID()
        val command = ResumeTestFixtures.createUpdateCommand(
            id = resumeId,
            userId = unauthorizedUserId,
        )

        coEvery { resumeRepository.findById(eq(resumeId), eq(unauthorizedUserId)) } returns null

        // When / Then
        assertThrows<ResumeNotFoundException> {
            updateResumeCommandHandler.handle(command)
        }

        coVerify { resumeRepository.findById(eq(resumeId), eq(unauthorizedUserId)) }
        coVerify(exactly = 0) { resumeRepository.save(any()) }
        coVerify(exactly = 0) { eventPublisher.publish(any<ResumeUpdatedEvent>()) }
    }
}
