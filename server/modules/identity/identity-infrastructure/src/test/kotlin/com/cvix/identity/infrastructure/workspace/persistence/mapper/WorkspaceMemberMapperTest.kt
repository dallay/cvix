package com.cvix.identity.infrastructure.workspace.persistence.mapper

import com.cvix.identity.domain.workspace.WorkspaceMember
import com.cvix.identity.domain.workspace.WorkspaceMemberId
import com.cvix.identity.domain.workspace.WorkspaceRole
import com.cvix.identity.infrastructure.workspace.persistence.entity.WorkspaceMemberEntity
import java.time.LocalDateTime
import java.util.*
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
