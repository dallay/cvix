package com.cvix.form.application.delete

import com.cvix.UnitTest
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.domain.SubscriptionFormId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class DeleteSubscriberFormCommandHandlerTest {
    private val workspaceAuthorization: WorkspaceAuthorization = mockk(relaxUnitFun = true)
    private val formDestroyer: FormDestroyer = mockk(relaxUnitFun = true)
    private val commandHandler = DeleteSubscriberFormCommandHandler(
        workspaceAuthorization,
        formDestroyer,
    )

    private lateinit var workspaceUuid: UUID
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        workspaceUuid = WorkspaceId.random().value

        coEvery { workspaceAuthorization.ensureAccess(workspaceUuid, userId) } returns Unit
        coEvery { formDestroyer.delete(any(), any()) } returns Unit
    }

    @Test
    fun `should delete a form`() = runTest {
        // Arrange
        val formId = UUID.randomUUID()
        val command = DeleteSubscriberFormCommand(
            workspaceId = workspaceUuid,
            userId = userId,
            formId = formId,
        )

        // Act
        commandHandler.handle(command)

        // Assert - ensure authorization checked and destroyer called with proper value objects
        coVerify(exactly = 1) { workspaceAuthorization.ensureAccess(workspaceUuid, userId) }
        coVerify(exactly = 1) {
            formDestroyer.delete(
                WorkspaceId(workspaceUuid),
                SubscriptionFormId(formId),
            )
        }
    }

    @Test
    fun `should fail when user is not a workspace member`() = runTest {
        // Arrange: make workspaceAuthorization throw when ensuring access
        val otherUser = UUID.randomUUID()
        coEvery {
            workspaceAuthorization.ensureAccess(
                workspaceUuid,
                otherUser,
            )
        } throws IllegalStateException("access denied")

        val command = DeleteSubscriberFormCommand(
            workspaceId = workspaceUuid,
            userId = otherUser,
            formId = UUID.randomUUID(),
        )

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            commandHandler.handle(command)
        }

        // ensure destroyer was not called
        coVerify(exactly = 0) { formDestroyer.delete(any(), any()) }
    }

    @Test
    fun `should propagate exception from formDestroyer`() = runTest {
        // Arrange: formDestroyer fails
        val failingId = UUID.randomUUID()
        coEvery { formDestroyer.delete(any(), any()) } throws RuntimeException("db down")

        val command = DeleteSubscriberFormCommand(
            workspaceId = workspaceUuid,
            userId = userId,
            formId = failingId,
        )

        // Act & Assert
        assertFailsWith<RuntimeException> {
            commandHandler.handle(command)
        }

        coVerify(exactly = 1) { workspaceAuthorization.ensureAccess(workspaceUuid, userId) }
        coVerify(exactly = 1) {
            formDestroyer.delete(
                WorkspaceId(workspaceUuid),
                SubscriptionFormId(failingId),
            )
        }
    }
}
