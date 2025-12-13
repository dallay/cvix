package com.cvix.workspace.infrastructure.persistence.mapper

import com.cvix.workspace.domain.WorkspaceMember
import com.cvix.workspace.domain.WorkspaceMemberId
import com.cvix.workspace.domain.WorkspaceRole
import com.cvix.workspace.infrastructure.persistence.entity.WorkspaceMemberEntity
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WorkspaceMemberMapperTest {

    @Test
    fun `should map WorkspaceMemberEntity to WorkspaceMember domain object`() {
        // Given
        val workspaceId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val entity = WorkspaceMemberEntity(
            workspaceId = workspaceId,
            userId = userId,
            role = WorkspaceRole.ADMIN,
            createdAt = LocalDateTime.now(),
        )

        val expectedDomainObject = WorkspaceMember(
            id = WorkspaceMemberId(workspaceId, userId),
            role = WorkspaceRole.ADMIN,
        )

        // When
        val actualDomainObject = entity.toDomain()

        // Then
        assertEquals(expectedDomainObject.id, actualDomainObject.id)
        assertEquals(expectedDomainObject.role, actualDomainObject.role)
    }
}
