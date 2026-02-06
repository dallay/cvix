package com.cvix.identity.infrastructure.workspace.http.request

import jakarta.validation.constraints.NotBlank

/**
 * Request to update a workspace.
 * @property name The name of the workspace.
 * @property description An optional description of the workspace.
 */
data class UpdateWorkspaceRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
)
