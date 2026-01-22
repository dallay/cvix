package com.cvix.workspace

import com.cvix.users.domain.UserId
import com.cvix.workspace.domain.Workspace
import com.cvix.workspace.domain.WorkspaceId
import com.cvix.workspace.infrastructure.http.request.CreateWorkspaceRequest
import com.cvix.workspace.infrastructure.http.request.UpdateWorkspaceRequest
import java.util.*
import net.datafaker.Faker

object WorkspaceStub {
    private val faker = Faker()
    fun create(
        id: UUID = UUID.randomUUID(),
        name: String = generateName(),
        description: String = faker.lorem().sentence(),
        ownerId: UUID = UUID.randomUUID()
    ): Workspace = Workspace(
        id = WorkspaceId(id),
        name = name,
        description = description,
        ownerId = UserId(ownerId),
    )

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

    fun dummyRandomWorkspaces(
        size: Int,
        ownerId: UUID = UUID.randomUUID()
    ): List<Workspace> {
        val workspaces = (0 until size).map {
            create(
                id = UUID.randomUUID(),
                name = generateName(),
                description = faker.lorem().sentence(),
                ownerId = ownerId,
            )
        }
        return workspaces
    }

    private fun generateName(): String {
        val randomNum = faker.number().numberBetween(1, 4)
        return "Test: ${faker.lorem().words(randomNum).joinToString(" ")}"
    }
}
