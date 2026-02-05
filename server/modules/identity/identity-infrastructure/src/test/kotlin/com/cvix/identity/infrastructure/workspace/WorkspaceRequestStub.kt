package com.cvix.identity.infrastructure.workspace

import com.cvix.identity.infrastructure.workspace.http.request.CreateWorkspaceRequest
import com.cvix.identity.infrastructure.workspace.http.request.UpdateWorkspaceRequest
import java.util.*
import net.datafaker.Faker

object WorkspaceRequestStub {
    private val faker = Faker()

    fun generateRequest(
        name: String = generateName(),
        description: String = faker.lorem().sentence(),
        ownerId: UUID = UUID.randomUUID(),
    ): CreateWorkspaceRequest = CreateWorkspaceRequest(
        name = name,
        description = description,
        ownerId = ownerId,
    )

    fun generateUpdateRequest(
        name: String = generateName(),
    ): UpdateWorkspaceRequest = UpdateWorkspaceRequest(
        name = name,
    )

    private fun generateName(): String {
        val randomNum = faker.number().numberBetween(1, 4)
        return "Test: ${faker.lorem().words(randomNum).joinToString(" ")}"
    }
}
