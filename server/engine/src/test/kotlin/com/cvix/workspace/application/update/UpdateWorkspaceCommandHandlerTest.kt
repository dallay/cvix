package com.cvix.workspace.application.update

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.workspace.WorkspaceStub
import com.cvix.workspace.domain.Workspace
import com.cvix.workspace.domain.WorkspaceFinderRepository
import com.cvix.workspace.domain.WorkspaceRepository
import com.cvix.workspace.domain.event.WorkspaceUpdatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class UpdateWorkspaceCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WorkspaceUpdatedEvent>
    private lateinit var workspaceRepository: WorkspaceRepository
    private lateinit var workspaceFinderRepository: WorkspaceFinderRepository
    private lateinit var workspaceUpdater: WorkspaceUpdater
    private lateinit var updateWorkspaceCommandHandler: UpdateWorkspaceCommandHandler
    private lateinit var workspace: Workspace

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk()
        workspaceRepository = mockk()
        workspaceFinderRepository = mockk()
        workspaceUpdater =
            WorkspaceUpdater(workspaceRepository, workspaceFinderRepository, eventPublisher)
        updateWorkspaceCommandHandler = UpdateWorkspaceCommandHandler(workspaceUpdater)
        workspace = WorkspaceStub.create()

        coEvery { workspaceRepository.update(any()) } returns Unit
        coEvery { workspaceFinderRepository.findById(any()) } returns workspace
        coEvery { eventPublisher.publish(any(WorkspaceUpdatedEvent::class)) } returns Unit
    }

    @Test
    fun `should update an workspace`() = runTest {
        // Given
        val command = UpdateWorkspaceCommand(
            id = workspace.id.value,
            name = workspace.name,
            description = workspace.description,
        )

        // When
        updateWorkspaceCommandHandler.handle(command)

        // Then
        coVerify(exactly = 1) {
            workspaceRepository.update(
                withArg { updatedWorkspace ->
                    assertEquals(
                        workspace.id.toString(),
                        updatedWorkspace.id.toString(),
                    )
                    assertEquals(workspace.name, updatedWorkspace.name)
                    assertEquals(workspace.description, updatedWorkspace.description)
                    assertEquals(workspace.ownerId, updatedWorkspace.ownerId)
                    assertEquals(
                        1,
                        updatedWorkspace.members.size,
                        "Owner should be added as a member",
                    )
                    assertEquals(
                        workspace.ownerId.toString(),
                        updatedWorkspace.members.first().toString(),
                    )
                },
            )
        }
        coVerify(exactly = 1) { eventPublisher.publish(ofType<WorkspaceUpdatedEvent>()) }
    }
}
