package com.cvix.resume.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.resume.ResumeTestFixtures
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeRepository
import com.cvix.resume.domain.event.ResumeCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class CreateResumeCommandHandlerTest {
    private lateinit var createResumeCommandHandler: CreateResumeCommandHandler
    private val eventPublisher: EventPublisher<ResumeCreatedEvent> = mockk()
    private val resumeRepository: ResumeRepository = mockk()
    private val resumeCreator: ResumeCreator = ResumeCreator(resumeRepository, eventPublisher)

    @BeforeEach
    fun setUp() {
        createResumeCommandHandler = CreateResumeCommandHandler(resumeCreator)
        coEvery { resumeRepository.save(any(ResumeDocument::class)) } answers {
            val arg = firstArg() as ResumeDocument
            arg
        }
        coEvery { eventPublisher.publish(any<ResumeCreatedEvent>()) } returns Unit
    }

    @Test
    fun `handle should create resume and publish event`() = runTest {
        val command = ResumeTestFixtures.createCreateCommand()

        createResumeCommandHandler.handle(command)

        // Verify that the resume was saved and event was published
        coVerify {
            resumeRepository.save(
                withArg {
                    assertEquals(command.id, it.id.id)
                    assertEquals(command.userId, it.userId)
                    assertEquals(command.workspaceId, it.workspaceId)
                    // When title is null, it is extracted from content (name)
                    assertEquals("John Doe", it.title)
                    assertEquals(command.content, it.content)
                    assertEquals(command.createdBy, it.createdBy)
                },
            )
        }
        val eventSlot = slot<ResumeCreatedEvent>()

        coVerify(exactly = 1) {
            eventPublisher.publish(capture(eventSlot))
        }

        assertEquals(command.id, eventSlot.captured.resumeId)
        assertEquals(command.userId, eventSlot.captured.userId)
        assertEquals(command.workspaceId, eventSlot.captured.workspaceId)
    }
}
