import {
	BaseHttpClient,
	type HttpClientConfig,
} from "../../../shared/BaseHttpClient";
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
	is_default?: boolean;
	ownerId?: string;
	owner_id?: string;
	createdAt?: string | Date;
	created_at?: string | Date;
	updatedAt?: string | Date;
	updated_at?: string | Date;
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
	/**
	 * Creates a new WorkspaceHttpClient
	 * @param config - Optional HTTP client configuration
	 */
	constructor(config?: HttpClientConfig) {
		super(config);
	}

	private normalize(workspace: WorkspacePayload): Workspace {
		const createdAtSource =
			("createdAt" in workspace ? workspace.createdAt : undefined) ??
			("created_at" in workspace
				? (workspace as ApiWorkspace).created_at
				: undefined) ??
			new Date();

		const updatedAtSource =
			("updatedAt" in workspace ? workspace.updatedAt : undefined) ??
			("updated_at" in workspace
				? (workspace as ApiWorkspace).updated_at
				: undefined) ??
			new Date();

		const ownerIdSource =
			("ownerId" in workspace ? workspace.ownerId : undefined) ??
			("owner_id" in workspace
				? (workspace as ApiWorkspace).owner_id
				: undefined) ??
			"";

		const isDefaultSource =
			("isDefault" in workspace ? workspace.isDefault : undefined) ??
			("is_default" in workspace
				? (workspace as ApiWorkspace).is_default
				: undefined) ??
			false;

		return {
			id: workspace.id,
			name: workspace.name,
			description: workspace.description ?? null,
			isDefault: isDefaultSource ?? false,
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
