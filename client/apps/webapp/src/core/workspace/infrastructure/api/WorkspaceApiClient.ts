import type { Workspace } from "../../domain/WorkspaceEntity.ts";

/**
 * Interface for workspace API operations
 */
export interface WorkspaceApiClient {
	getAllWorkspaces(): Promise<Workspace[]>;
	getWorkspace(id: string): Promise<Workspace | null>;
}
