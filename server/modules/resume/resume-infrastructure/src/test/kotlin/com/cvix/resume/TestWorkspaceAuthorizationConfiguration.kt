package com.cvix.resume

import com.cvix.identity.application.workspace.security.WorkspaceAuthorizationService
import com.cvix.identity.domain.workspace.WorkspaceMember
import com.cvix.identity.domain.workspace.WorkspaceMemberRepository
import java.util.UUID
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class TestWorkspaceAuthorizationConfiguration {
    @Bean
    fun workspaceMemberRepository(): WorkspaceMemberRepository =
        object : WorkspaceMemberRepository {
            override suspend fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMember> = emptyList()

            override suspend fun findByUserId(userId: UUID): List<WorkspaceMember> = emptyList()

            override suspend fun existsByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Boolean = true

            override suspend fun insertWorkspaceMember(
                workspaceId: UUID,
                userId: UUID,
                role: String
            ): Int = 1

            override suspend fun deleteByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Int = 1
        }

    @Bean
    fun workspaceAuthorizationService(
        workspaceMemberRepository: WorkspaceMemberRepository
    ): WorkspaceAuthorizationService =
        WorkspaceAuthorizationService(workspaceMemberRepository)
}
