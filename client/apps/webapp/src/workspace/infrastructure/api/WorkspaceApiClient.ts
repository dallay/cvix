import type { Workspace } from "../../domain/WorkspaceEntity";

/**
 * Interface for workspace API operations
 * Defines the contract for fetching workspace data
 */
export interface WorkspaceApiClient {
	/**
	 * Fetches all workspaces for the authenticated user
	 */
	getAllWorkspaces(): Promise<Workspace[]>;

	/**
	 * Fetches a specific workspace by ID
	 * @param id - The workspace UUID
	 */
	getWorkspace(id: string): Promise<Workspace | null>;
}
