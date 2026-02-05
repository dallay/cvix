package com.cvix.identity.application.workspace.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.identity.domain.workspace.WorkspaceFinderRepository
import com.cvix.identity.domain.workspace.WorkspaceMetrics
import com.cvix.identity.domain.workspace.WorkspaceRepository
import com.cvix.identity.domain.workspace.event.WorkspaceCreatedEvent
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
                    assertEquals(workspaceId, it.id.value)
                    assertEquals(name, it.name)
                    assertEquals("A test workspace", it.description)
                    assertEquals(ownerId, it.ownerId.value)
                    assertEquals(1, it.members.size) // Owner is added as a member
                    assertEquals(ownerId, it.members.first().value)
                },
            )
        }
        coVerify { eventPublisher.publish(ofType<WorkspaceCreatedEvent>()) }
    }
}
