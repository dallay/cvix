import type { WorkspaceSelectionPreference } from "../../domain/WorkspaceEntity.ts";
import { WorkspaceId } from "../../domain/WorkspaceId.ts";

interface StoredWorkspacePreference {
	userId: string;
	lastSelectedWorkspaceId: string | null;
	selectedAt: string | null;
}

/**
 * Storage key for workspace selection preference
 */
export const STORAGE_KEY = "cvix:workspace:lastSelected";

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

	const preference: StoredWorkspacePreference = {
		userId,
		lastSelectedWorkspaceId: workspaceId,
		selectedAt: new Date().toISOString(),
	};

	try {
		localStorage.setItem(STORAGE_KEY, JSON.stringify(preference));
	} catch (err) {
		if (err instanceof DOMException && err.name === "QuotaExceededError") {
			// Optionally: clear old data or notify user
		}
		throw err;
	}
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
		const preference = JSON.parse(stored) as StoredWorkspacePreference;

		if (preference.userId !== userId) {
			return null;
		}

		return {
			userId: preference.userId,
			lastSelectedWorkspaceId: preference.lastSelectedWorkspaceId,
			selectedAt: preference.selectedAt
				? new Date(preference.selectedAt)
				: null,
		};
	} catch {
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
