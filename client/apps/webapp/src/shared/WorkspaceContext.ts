/**
 * Module-level workspace context for sharing the current workspace ID
 * across the application, including non-reactive contexts like HTTP clients.
 *
 * This is a singleton that bridges the gap between Vue's reactive system (Pinia stores)
 * and non-reactive code (Axios HTTP clients instantiated outside Vue components).
 *
 * The workspace store is responsible for updating this context whenever
 * the current workspace changes.
 */

let currentWorkspaceId: string | null = null;

/**
 * Sets the current workspace ID in the global context.
 * Should be called by the workspace store when the workspace changes.
 *
 * @param workspaceId - The UUID of the current workspace, or null to clear
 */
export function setCurrentWorkspaceId(workspaceId: string | null): void {
	currentWorkspaceId = workspaceId;
}

/**
 * Gets the current workspace ID from the global context.
 * Used by HTTP clients to inject the X-Workspace-Id header.
 *
 * @returns The current workspace ID or null if not set
 */
export function getCurrentWorkspaceId(): string | null {
	return currentWorkspaceId;
}

/**
 * Clears the current workspace ID from the global context.
 * Should be called on logout or session reset.
 */
export function clearCurrentWorkspaceId(): void {
	currentWorkspaceId = null;
}
