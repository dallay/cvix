import type { APIRequestContext } from "@playwright/test";
import { E2E_CONFIG } from "../config/environment";

/**
 * Workspace test fixture data
 */
export interface WorkspaceFixture {
	id: string;
	name: string;
	description?: string | null;
	isDefault: boolean;
	ownerId: string;
	createdAt?: string | Date;
	updatedAt?: string | Date;
}

/**
 * User test fixture data
 */
export interface UserFixture {
	id: string;
	email: string;
	password: string;
}

/**
 * Test fixtures for workspace E2E tests
 * Provides utilities to set up and tear down test data via API calls
 */
export class WorkspaceTestFixtures {
	private readonly apiBaseURL: string;
	private readonly keycloakURL: string;
	private readonly keycloakClientId: string;

	constructor(private request: APIRequestContext) {
		this.apiBaseURL = E2E_CONFIG.apiBaseURL;
		this.keycloakURL = E2E_CONFIG.keycloakURL;
		this.keycloakClientId = E2E_CONFIG.keycloakClientId;
	}

	/**
	 * Check if API endpoints are accessible
	 * @returns True if API is accessible, false otherwise
	 */
	async checkApiAvailability(): Promise<boolean> {
		try {
			const response = await this.request.get(
				`${this.apiBaseURL.replace("/api/v1", "")}/actuator/health`,
				{
					timeout: 5000,
				},
			);
			return response.ok();
		} catch {
			return false;
		}
	}

	/**
	 * Authenticate and get an access token for API calls using Keycloak
	 * @param email - User email
	 * @param password - User password
	 * @returns Access token
	 */
	async getAuthToken(email: string, password: string): Promise<string> {
		const response = await this.request.post(
			`${this.keycloakURL}/protocol/openid-connect/token`,
			{
				headers: {
					"Content-Type": "application/x-www-form-urlencoded",
				},
				form: {
					grant_type: "password",
					client_id: this.keycloakClientId,
					username: email,
					password: password,
				},
			},
		);

		if (!response.ok()) {
			const errorText = await response.text().catch(() => "Unknown error");
			throw new Error(
				`Failed to authenticate: ${response.status()} ${response.statusText()} - ${errorText}`,
			);
		}

		const data = await response.json();
		return data.access_token;
	}

	/**
	 * Create a user account (for test setup)
	 * @param email - User email
	 * @param password - User password
	 * @param userId - Optional user ID
	 * @returns Created user data
	 */
	async createUser(
		email: string,
		password: string,
		userId?: string,
	): Promise<UserFixture> {
		const response = await this.request.post(
			`${this.apiBaseURL}/auth/register`,
			{
				headers: {
					"Content-Type": "application/json",
					Accept: "application/vnd.api.v1+json",
				},
				data: {
					email,
					password,
					id: userId,
				},
			},
		);

		if (!response.ok()) {
			// User might already exist, try to get user info
			if (response.status() === 409 || response.status() === 400) {
				console.warn(`User ${email} might already exist`);
				return {
					id: userId || this.generateUUID(),
					email,
					password,
				};
			}
			throw new Error(
				`Failed to create user: ${response.status()} ${response.statusText()}`,
			);
		}

		const data = await response.json();
		return {
			id: data.id || data.userId || userId || this.generateUUID(),
			email,
			password,
		};
	}

	/**
	 * Ensure a test user exists (create if not exists)
	 * @param email - User email
	 * @param password - User password
	 * @param userId - Optional user ID
	 * @returns User data
	 */
	async ensureUserExists(
		email: string,
		password: string,
		userId?: string,
	): Promise<UserFixture> {
		try {
			// Try to authenticate - if successful, user exists
			await this.getAuthToken(email, password);
			return {
				id: userId || this.generateUUID(),
				email,
				password,
			};
		} catch {
			// User doesn't exist or wrong password, try to create
			return await this.createUser(email, password, userId);
		}
	}

	/**
	 * Create a workspace for a user
	 * @param token - Auth token
	 * @param userId - User ID
	 * @param workspace - Workspace data
	 * @returns Created workspace
	 */
	async createWorkspace(
		token: string,
		userId: string,
		workspace: Omit<WorkspaceFixture, "id" | "createdAt" | "updatedAt">,
	): Promise<WorkspaceFixture> {
		const workspaceId = this.generateUUID();
		const response = await this.request.put(
			`${this.apiBaseURL}/organization/${workspaceId}`,
			{
				headers: {
					Authorization: `Bearer ${token}`,
					Accept: "application/vnd.api.v1+json",
					"Content-Type": "application/json",
				},
				data: {
					name: workspace.name,
					description: workspace.description,
					userId: userId,
					isDefault: workspace.isDefault,
				},
			},
		);

		if (!response.ok()) {
			throw new Error(
				`Failed to create workspace: ${response.status()} ${response.statusText()}`,
			);
		}

		const data = await response.json();
		return {
			id: data.id || workspaceId,
			name: data.name,
			description: data.description,
			isDefault: workspace.isDefault,
			ownerId: userId,
			createdAt: data.createdAt || data.created_at,
			updatedAt: data.updatedAt || data.updated_at,
		};
	}

	/**
	 * Get all workspaces for a user
	 * @param token - Auth token
	 * @returns Array of workspaces
	 */
	async getAllWorkspaces(token: string): Promise<WorkspaceFixture[]> {
		const response = await this.request.get(`${this.apiBaseURL}/workspace`, {
			headers: {
				Authorization: `Bearer ${token}`,
				Accept: "application/vnd.api.v1+json",
			},
		});

		if (!response.ok()) {
			throw new Error(
				`Failed to get workspaces: ${response.status()} ${response.statusText()}`,
			);
		}

		const data = await response.json();
		return (data.data || data).map((ws: WorkspaceFixture) => ({
			id: ws.id,
			name: ws.name,
			description: ws.description,
			isDefault: ws.isDefault,
			ownerId: ws.ownerId,
			createdAt: ws.createdAt,
			updatedAt: ws.updatedAt,
		}));
	}

	/**
	 * Delete a workspace
	 * @param token - Auth token
	 * @param workspaceId - Workspace ID
	 */
	async deleteWorkspace(token: string, workspaceId: string): Promise<void> {
		const response = await this.request.delete(
			`${this.apiBaseURL}/workspace/${workspaceId}`,
			{
				headers: {
					Authorization: `Bearer ${token}`,
					Accept: "application/vnd.api.v1+json",
				},
			},
		);

		if (!response.ok() && response.status() !== 404) {
			throw new Error(
				`Failed to delete workspace: ${response.status()} ${response.statusText()}`,
			);
		}
	}

	/**
	 * Delete all workspaces for a user (cleanup)
	 * @param token - Auth token
	 */
	async deleteAllWorkspaces(token: string): Promise<void> {
		const workspaces = await this.getAllWorkspaces(token);
		for (const workspace of workspaces) {
			await this.deleteWorkspace(token, workspace.id);
		}
	}

	/**
	 * Set up a user with only non-default workspaces
	 * @param email - User email
	 * @param password - User password
	 * @param userId - User ID
	 * @param workspaceNames - Names of workspaces to create (all will be non-default)
	 * @returns Created workspaces
	 */
	async setupUserWithNonDefaultWorkspaces(
		email: string,
		password: string,
		userId: string,
		workspaceNames: string[] = ["Workspace Alpha", "Workspace Beta"],
	): Promise<WorkspaceFixture[]> {
		// Ensure user exists
		await this.ensureUserExists(email, password, userId);

		// Authenticate
		const token = await this.getAuthToken(email, password);

		// Clean up any existing workspaces
		await this.deleteAllWorkspaces(token);

		// Create non-default workspaces
		const workspaces: WorkspaceFixture[] = [];
		for (const name of workspaceNames) {
			const workspace = await this.createWorkspace(token, userId, {
				name,
				description: `Test workspace: ${name}`,
				isDefault: false,
				ownerId: userId,
			});
			workspaces.push(workspace);
		}

		return workspaces;
	}

	/**
	 * Set up a user with a default workspace
	 * @param email - User email
	 * @param password - User password
	 * @param userId - User ID
	 * @param workspaceName - Name of the default workspace
	 * @returns Created workspace
	 */
	async setupUserWithDefaultWorkspace(
		email: string,
		password: string,
		userId: string,
		workspaceName = "Default Workspace",
	): Promise<WorkspaceFixture> {
		// Ensure user exists
		await this.ensureUserExists(email, password, userId);

		const token = await this.getAuthToken(email, password);
		await this.deleteAllWorkspaces(token);

		return await this.createWorkspace(token, userId, {
			name: workspaceName,
			description: "Default test workspace",
			isDefault: true,
			ownerId: userId,
		});
	}

	/**
	 * Set up a user with no workspaces
	 * Useful for testing the "no workspaces available" scenario
	 * @param email - User email
	 * @param password - User password
	 * @param userId - User ID
	 */
	async setupUserWithNoWorkspaces(
		email: string,
		password: string,
		userId: string,
	): Promise<void> {
		// Ensure user exists
		await this.ensureUserExists(email, password, userId);

		// Authenticate and clean up all workspaces
		const token = await this.getAuthToken(email, password);
		await this.deleteAllWorkspaces(token);
	}

	/**
	 * Clean up all workspaces for a user
	 * @param email - User email
	 * @param password - User password
	 */
	async cleanup(email: string, password: string): Promise<void> {
		try {
			const token = await this.getAuthToken(email, password);
			await this.deleteAllWorkspaces(token);
		} catch (error) {
			// Ignore cleanup errors (user might not exist)
			console.warn(`Cleanup failed for ${email}:`, error);
		}
	}

	/**
	 * Generate a random UUID v4
	 * @returns UUID string
	 */
	private generateUUID(): string {
		return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
			const r = (Math.random() * 16) | 0;
			const v = c === "x" ? r : (r & 0x3) | 0x8;
			return v.toString(16);
		});
	}
}
