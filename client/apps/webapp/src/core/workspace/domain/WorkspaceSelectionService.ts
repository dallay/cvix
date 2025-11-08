import type { Workspace } from "./WorkspaceEntity.ts";
import type { WorkspaceError } from "./WorkspaceError.ts";
import { WorkspaceErrorCode } from "./WorkspaceError.ts";

/**
 * Determines which workspace to load based on priority rules:
 * 1. Last selected workspace (if valid)
 * 2. Default workspace
 * 3. First available workspace
 * 4. null (if no workspaces)
 */
export function determineWorkspaceToLoad(
	workspaces: Workspace[],
	lastSelectedId: string | null,
): Workspace | null {
	if (workspaces.length === 0) {
		return null;
	}

	// Priority 1: Last selected workspace (if valid)
	if (lastSelectedId) {
		const lastSelected = workspaces.find((w) => w.id === lastSelectedId);
		if (lastSelected) {
			return lastSelected;
		}
	}

	// Priority 2: Default workspace
	const defaultWorkspace = findDefaultWorkspace(workspaces);
	if (defaultWorkspace) {
		return defaultWorkspace;
	}

	// Priority 3: First available workspace
	return workspaces[0] ?? null;
}

/**
 * Checks if a workspace ID exists in the provided workspaces list
 */
export function isWorkspaceValid(
	workspaceId: string,
	workspaces: Workspace[],
): boolean {
	return workspaces.some((w) => w.id === workspaceId);
}

/**
 * Finds the default workspace from the list
 */
export function findDefaultWorkspace(
	workspaces: Workspace[],
): Workspace | undefined {
	const defaults = workspaces.filter((w) => w.isDefault);

	if (defaults.length > 1) {
		const isDev = import.meta.env.DEV;
		if (isDev) {
			const conflictingIds = defaults.map((w) => w.id).join(", ");
			console.warn(
				`[Workspace] Multiple default workspaces found (${defaults.length}): ${conflictingIds}. Using first.`,
			);
		}
	}

	return defaults[0];
}

/**
 * Creates an error for when no workspaces are available
 */
export function handleNoWorkspaces(): WorkspaceError {
	return {
		code: WorkspaceErrorCode.NO_WORKSPACES,
		message: "No workspaces available for this user",
		timestamp: new Date(),
	};
}

/**
 * Service class providing workspace selection logic
 */
export class WorkspaceSelectionService {
	determineWorkspaceToLoad(
		workspaces: Workspace[],
		lastSelectedId: string | null,
	): Workspace | null {
		return determineWorkspaceToLoad(workspaces, lastSelectedId);
	}

	isWorkspaceValid(workspaceId: string, workspaces: Workspace[]): boolean {
		return isWorkspaceValid(workspaceId, workspaces);
	}

	findDefaultWorkspace(workspaces: Workspace[]): Workspace | undefined {
		return findDefaultWorkspace(workspaces);
	}

	handleNoWorkspaces(): WorkspaceError {
		return handleNoWorkspaces();
	}
}
