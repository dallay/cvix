import { WorkspaceId } from "./WorkspaceId";
import { WorkspaceName } from "./WorkspaceName";

/**
 * Workspace entity
 * Represents a work environment or project context
 */
export interface Workspace {
	id: string; // UUID v4 format
	name: string; // Display name (1-100 characters)
	description: string | null; // Optional description (max 500 characters)
	isDefault: boolean; // Whether this is the user's default workspace
	ownerId: string; // UUID of workspace owner
	createdAt: Date; // ISO 8601 datetime
	updatedAt: Date; // ISO 8601 datetime
}

/**
 * Workspace selection preference
 * Stores user's last selected workspace
 */
export interface WorkspaceSelectionPreference {
	userId: string; // UUID of authenticated user
	lastSelectedWorkspaceId: string | null; // UUID of last selected workspace
	selectedAt: Date | null; // Timestamp of last selection
}

function validateWorkspaceName(data: Workspace) {
	// Validate name
	new WorkspaceName(data.name); // Throws if invalid
}

/**
 * Creates and validates a workspace entity
 * @param data - Raw workspace data
 * @returns Validated Workspace entity
 * @throws Error if validation fails
 */
export function createWorkspace(data: Workspace): Workspace {
	// Validate ID
	if (!WorkspaceId.isValid(data.id)) {
		throw new Error(`Invalid workspace ID: ${data.id}`);
	}
	validateWorkspaceName(data);

	// Validate owner ID
	if (!WorkspaceId.isValid(data.ownerId)) {
		throw new Error(`Invalid owner ID: ${data.ownerId}`);
	}

	// Trim and validate description length
	if (data.description !== null) {
		const trimmedDescription = data.description.trim();
		if (trimmedDescription.length > 500) {
			throw new Error("Workspace description must not exceed 500 characters");
		}
		data.description = trimmedDescription;
	}

	// Validate dates
	if (data.updatedAt < data.createdAt) {
		throw new Error("updatedAt must be after or equal to createdAt");
	}

	return data;
}

/**
 * Checks if a workspace object is valid
 * @param data - Workspace data to validate
 * @returns true if valid, false otherwise
 */
export function isValidWorkspace(data: Workspace): boolean {
	try {
		createWorkspace(data);
		return true;
	} catch {
		return false;
	}
}
