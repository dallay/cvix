package com.cvix.identity.infrastructure.workspace.http

import com.cvix.ControllerTest
import com.cvix.identity.application.workspace.WorkspaceResponses
import com.cvix.identity.application.workspace.find.member.AllWorkspaceByMemberQuery
import com.cvix.identity.domain.workspace.Workspace
import com.cvix.identity.domain.workspace.WorkspaceStub
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetAllWorkspaceControllerTest : ControllerTest() {
    private val controller: GetAllWorkspaceController = GetAllWorkspaceController(mediator)
    override val webTestClient: WebTestClient = buildWebTestClient(controller)

    private val workspaces: List<Workspace> =
        WorkspaceStub.dummyRandomWorkspaces(size = 6, ownerId = userId)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val query = AllWorkspaceByMemberQuery(userId)
        coEvery { mediator.send(eq(query)) } returns WorkspaceResponses.from(workspaces)
    }

    @Test
    fun `should get all workspaces`() {
        webTestClient
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt { jwt ->
                        jwt.subject(userId.toString())
                            .claim("preferred_username", "test-user")
                            .claim("roles", listOf("ROLE_USER"))
                    }
                    .authorities(AuthorityUtils.createAuthorityList("ROLE_USER")),
            )
            .get()
            .uri("/api/workspace")
            .exchange()
            .expectBody()
            .jsonPath("$.data").isArray
            .jsonPath("$.data.length()").isEqualTo(workspaces.size)
    }
}
