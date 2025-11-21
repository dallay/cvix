package com.loomify.resume.application.create

import com.loomify.UnitTest
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.resume.application.ResumeTestFixtures
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeRepository
import com.loomify.resume.domain.event.ResumeCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
                    assertEquals(command.id, it.id.value)
                    assertEquals(command.userId, it.userId)
                    assertEquals(command.workspaceId, it.workspaceId)
                    // When title is null, it's extracted from content (name)
                    assertEquals("John Doe", it.title)
                    assertEquals(command.content, it.content)
                    assertEquals(command.createdBy, it.createdBy)
                },
            )
        }
        coVerify { eventPublisher.publish(any<ResumeCreatedEvent>()) }
    }
}
