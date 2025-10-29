import type { WorkspaceSelectionPreference } from "../../domain/WorkspaceEntity";
import { WorkspaceId } from "../../domain/WorkspaceId";

/**
 * Storage key for workspace selection preference
 */
export const STORAGE_KEY = "loomify:workspace:lastSelected";

/**
 * Saves the last selected workspace for a user
 * @param userId - The user's UUID
 * @param workspaceId - The selected workspace UUID
 * @throws Error if user ID or workspace ID is invalid
 */
export function saveLastSelected(userId: string, workspaceId: string): void {
	// Validate user ID
	if (!WorkspaceId.isValid(userId)) {
		throw new Error(`Invalid user ID: ${userId}`);
	}

	// Validate workspace ID
	if (!WorkspaceId.isValid(workspaceId)) {
		throw new Error(`Invalid workspace ID: ${workspaceId}`);
	}

	const preference: WorkspaceSelectionPreference = {
		userId,
		lastSelectedWorkspaceId: workspaceId,
		selectedAt: new Date(),
	};

	localStorage.setItem(STORAGE_KEY, JSON.stringify(preference));
}

/**
 * Retrieves the last selected workspace for a user
 * @param userId - The user's UUID
 * @returns WorkspaceSelectionPreference or null if not found
 * @throws Error if user ID is invalid
 */
export function getLastSelected(
	userId: string,
): WorkspaceSelectionPreference | null {
	// Validate user ID
	if (!WorkspaceId.isValid(userId)) {
		throw new Error(`Invalid user ID: ${userId}`);
	}

	const stored = localStorage.getItem(STORAGE_KEY);
	if (!stored) {
		return null;
	}

	try {
		const preference: WorkspaceSelectionPreference = JSON.parse(stored);

		// Check if the stored preference is for this user
		if (preference.userId !== userId) {
			return null;
		}

		return preference;
	} catch {
		// Return null if JSON parsing fails (corrupted data)
		return null;
	}
}

/**
 * Clears the workspace selection preference from localStorage
 */
export function clearLastSelected(): void {
	localStorage.removeItem(STORAGE_KEY);
}

/**
 * Singleton object providing workspace local storage operations
 */
export const workspaceLocalStorage = {
	saveLastSelected,
	getLastSelected,
	clearLastSelected,
};
