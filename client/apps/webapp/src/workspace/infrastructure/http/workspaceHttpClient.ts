import { BaseHttpClient } from "../../../shared/BaseHttpClient";
import type { Workspace } from "../../domain/WorkspaceEntity";
import type {
	GetAllWorkspacesResponse,
	GetWorkspaceResponse,
} from "../../domain/WorkspaceError";
import type { WorkspaceApiClient } from "../api/WorkspaceApiClient";

type ApiWorkspace = {
	id: string;
	name: string;
	description?: string | null;
	isDefault?: boolean;
	ownerId?: string;
	createdAt?: string | Date;
	updatedAt?: string | Date;
};

type WorkspacePayload = Workspace | ApiWorkspace;

/**
 * HTTP client for workspace API endpoints
 * Extends BaseHttpClient to inherit Bearer token auth, CSRF handling, and error parsing
 */
export class WorkspaceHttpClient
	extends BaseHttpClient
	implements WorkspaceApiClient
{
	private normalize(workspace: WorkspacePayload): Workspace {
		const api = workspace as ApiWorkspace;
		const createdAtSource = api.createdAt ?? new Date();
		const updatedAtSource = api.updatedAt ?? new Date();
		const ownerIdSource = api.ownerId ?? "";
		const isDefaultSource = api.isDefault ?? false;

		return {
			id: workspace.id,
			name: workspace.name,
			description: workspace.description ?? null,
			isDefault: isDefaultSource,
			ownerId: ownerIdSource,
			createdAt:
				createdAtSource instanceof Date
					? createdAtSource
					: new Date(createdAtSource),
			updatedAt:
				updatedAtSource instanceof Date
					? updatedAtSource
					: new Date(updatedAtSource),
		};
	}

	/**
	 * Fetches all workspaces for the authenticated user
	 * @returns Promise<Workspace[]>
	 * @throws Error if the API call fails
	 */
	async getAllWorkspaces(): Promise<Workspace[]> {
		const response = await this.get<GetAllWorkspacesResponse>("/workspace");
		return response.data.map((workspace) => this.normalize(workspace));
	}

	/**
	 * Fetches a specific workspace by ID
	 * @param id - The workspace UUID
	 * @returns Promise<Workspace | null>
	 * @throws Error if the API call fails
	 */
	async getWorkspace(id: string): Promise<Workspace | null> {
		const response = await this.get<GetWorkspaceResponse>(`/workspace/${id}`);
		if (!response?.data) {
			return null;
		}

		return this.normalize(response.data as WorkspacePayload);
	}
}

/**
 * Singleton instance of the workspace HTTP client
 */
export const workspaceHttpClient = new WorkspaceHttpClient();
