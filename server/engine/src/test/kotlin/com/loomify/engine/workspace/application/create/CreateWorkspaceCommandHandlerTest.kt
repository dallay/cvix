package com.loomify.engine.workspace.application.create

import com.loomify.UnitTest
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.engine.workspace.domain.WorkspaceRepository
import com.loomify.engine.workspace.domain.event.WorkspaceCreatedEvent
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class CreateWorkspaceCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WorkspaceCreatedEvent>
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceCreator: WorkspaceCreator
    private lateinit var createWorkspaceCommandHandler: CreateWorkspaceCommandHandler
    private lateinit var meterRegistry: SimpleMeterRegistry

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk()
        workspaceRepository = mockk()
        meterRegistry = SimpleMeterRegistry()
        workspaceCreator = WorkspaceCreator(workspaceRepository, eventPublisher)
        createWorkspaceCommandHandler = CreateWorkspaceCommandHandler(workspaceCreator, meterRegistry)

        coEvery { workspaceRepository.create(any()) } returns Unit
        coEvery { eventPublisher.publish(any<WorkspaceCreatedEvent>()) } returns Unit
    }

    @Test
    fun `should create workspace and publish event when handle is called`() = runTest {
        // Given
        val workspaceId = UUID.randomUUID().toString()
        val ownerId = UUID.randomUUID().toString()
        val name = "Test Workspace"
        val command = CreateWorkspaceCommand(
            id = workspaceId,
            name = name,
            description = "A test workspace",
            ownerId = ownerId,
        )

        // When
        createWorkspaceCommandHandler.handle(command)

        // Then
        coVerify {
            workspaceRepository.create(
                withArg {
                    assertEquals(workspaceId, it.id.id.toString())
                    assertEquals(name, it.name)
                    assertEquals("A test workspace", it.description)
                    assertEquals(ownerId, it.ownerId.id.toString())
                    assertEquals(1, it.members.size) // Owner is added as a member
                    assertEquals(ownerId, it.members.first().id.toString())
                },
            )
        }
        coVerify { eventPublisher.publish(ofType<WorkspaceCreatedEvent>()) }
    }
}
