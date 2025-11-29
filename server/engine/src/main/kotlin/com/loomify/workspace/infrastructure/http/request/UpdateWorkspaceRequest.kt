package com.loomify.workspace.infrastructure.http.request

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
