import {
	BaseHttpClient,
	type HttpClientConfig,
} from "../../../shared/BaseHttpClient";
import type { Workspace } from "../../domain/WorkspaceEntity";
import type {
	GetAllWorkspacesResponse,
	GetWorkspaceResponse,
} from "../../domain/WorkspaceError";

/**
 * HTTP client for workspace API endpoints
 * Extends BaseHttpClient to inherit Bearer token auth, CSRF handling, and error parsing
 */
export class WorkspaceHttpClient extends BaseHttpClient {
	/**
	 * Creates a new WorkspaceHttpClient
	 * @param config - Optional HTTP client configuration
	 */
	constructor(config?: HttpClientConfig) {
		super({
			baseURL: "/api/workspace",
			...config,
		});
	}

	/**
	 * Fetches all workspaces for the authenticated user
	 * @returns Promise<Workspace[]>
	 * @throws Error if the API call fails
	 */
	async getAllWorkspaces(): Promise<Workspace[]> {
		const response = await this.get<GetAllWorkspacesResponse>("/workspace");
		return response.data;
	}

	/**
	 * Fetches a specific workspace by ID
	 * @param id - The workspace UUID
	 * @returns Promise<Workspace | null>
	 * @throws Error if the API call fails
	 */
	async getWorkspace(id: string): Promise<Workspace | null> {
		const response = await this.get<GetWorkspaceResponse>(`/workspace/${id}`);
		return response?.data ?? null;
	}
}
