package com.cvix.identity.application.workspace.delete

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.identity.domain.workspace.WorkspaceRepository
import com.cvix.identity.domain.workspace.event.WorkspaceDeletedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class DeleteWorkspaceCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WorkspaceDeletedEvent>
    private lateinit var repository: WorkspaceRepository
    private lateinit var destroyer: WorkspaceDestroyer
    private lateinit var deleteWorkspaceCommandHandler: DeleteWorkspaceCommandHandler
    private lateinit var workspaceId: UUID

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk()
        repository = mockk()
        destroyer = WorkspaceDestroyer(repository, eventPublisher)
        deleteWorkspaceCommandHandler = DeleteWorkspaceCommandHandler(destroyer)
        workspaceId = UUID.randomUUID()

        coEvery { repository.delete(any()) } returns Unit
        coEvery { eventPublisher.publish(any<WorkspaceDeletedEvent>()) } returns Unit
    }

    @Test
    fun `should delete an workspace and publish event when handle is called`() = runTest {
        // Given
        val command = DeleteWorkspaceCommand(id = workspaceId)

        // When
        deleteWorkspaceCommandHandler.handle(command)

        // Then - ensure delete was invoked (avoid capturing inline/value class to prevent class cast issues)
        coVerify { repository.delete(any()) }

        // And ensure the published event contains the expected id
        val slot = slot<WorkspaceDeletedEvent>()
        coVerify { eventPublisher.publish(capture(slot)) }
        assert(slot.captured.id == workspaceId.toString())
    }
}
