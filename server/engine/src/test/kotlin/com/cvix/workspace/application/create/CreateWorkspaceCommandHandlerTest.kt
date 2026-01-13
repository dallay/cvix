package com.cvix.workspace.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.workspace.domain.WorkspaceFinderRepository
import com.cvix.workspace.domain.WorkspaceMetrics
import com.cvix.workspace.domain.WorkspaceRepository
import com.cvix.workspace.domain.event.WorkspaceCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class CreateWorkspaceCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WorkspaceCreatedEvent>
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceFinderRepository: WorkspaceFinderRepository
    private lateinit var workspaceMetrics: WorkspaceMetrics
    private lateinit var workspaceCreator: WorkspaceCreator
    private lateinit var createWorkspaceCommandHandler: CreateWorkspaceCommandHandler

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk()
        workspaceRepository = mockk()
        workspaceFinderRepository = mockk()
        workspaceMetrics = mockk(relaxed = true)
        workspaceCreator = WorkspaceCreator(
            workspaceRepository,
            workspaceFinderRepository,
            workspaceMetrics,
            eventPublisher,
        )
        createWorkspaceCommandHandler = CreateWorkspaceCommandHandler(workspaceCreator)

        coEvery { workspaceRepository.create(any()) } returns Unit
        coEvery { eventPublisher.publish(any<WorkspaceCreatedEvent>()) } returns Unit
        coEvery { workspaceFinderRepository.findByOwnerId(any()) } returns emptyList()
    }

    @Test
    fun `should create workspace and publish event when handle is called`() = runTest {
        // Given
        val workspaceId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
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
                    assertEquals(workspaceId, it.id.id)
                    assertEquals(name, it.name)
                    assertEquals("A test workspace", it.description)
                    assertEquals(ownerId, it.ownerId.id)
                    assertEquals(1, it.members.size) // Owner is added as a member
                    assertEquals(ownerId, it.members.first().id)
                },
            )
        }
        coVerify { eventPublisher.publish(ofType<WorkspaceCreatedEvent>()) }
    }
}
