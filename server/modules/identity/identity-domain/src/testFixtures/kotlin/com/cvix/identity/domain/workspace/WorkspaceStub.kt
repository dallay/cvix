package com.cvix.identity.domain.workspace

import com.cvix.identity.domain.user.UserId
import java.util.*
import net.datafaker.Faker

object WorkspaceStub {
    private val faker = Faker()
    private const val MIN_RANDOM_WORDS = 1
    private const val MAX_RANDOM_WORDS = 4

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
        val randomNum = faker.number().numberBetween(MIN_RANDOM_WORDS, MAX_RANDOM_WORDS)
        return "Test: ${faker.lorem().words(randomNum).joinToString(" ")}"
    }
}
