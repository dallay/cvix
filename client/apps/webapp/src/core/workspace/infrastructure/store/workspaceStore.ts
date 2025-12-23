import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Workspace } from "@/core/workspace";
import { WorkspaceId, workspaceHttpClient } from "@/core/workspace";
import type { WorkspaceError } from "@/core/workspace/domain";
import { WorkspaceErrorCode } from "@/core/workspace/domain";
import { saveLastSelected } from "@/core/workspace/infrastructure";
import {
	clearCurrentWorkspaceId,
	setCurrentWorkspaceId,
} from "@/shared/WorkspaceContext";

const CACHE_TTL = 5 * 60 * 1000; // 5 minutes in milliseconds
let loadPromise: Promise<void> | null = null;

/**
 * Pinia store for workspace state management
 */
export const useWorkspaceStore = defineStore("workspace", () => {
	// State
	const workspaces = ref<Workspace[]>([]);
	const currentWorkspace = ref<Workspace | null>(null);
	const isLoading = ref(false);
	const error = ref<WorkspaceError | null>(null);
	const lastFetchedAt = ref<Date | null>(null);
	const lastSelectedId = ref<string | null>(null);
	const lastSelectedAt = ref<Date | null>(null);
	const loadedInSession = ref(false);

	// Getters
	const hasWorkspaces = computed(() => workspaces.value.length > 0);
	const defaultWorkspace = computed(() =>
		workspaces.value.find((w) => w.isDefault),
	);

	// Actions
	/**
	 * Loads all workspaces from the API
	 * @param force - Force refetch even if cache is fresh
	 */
	async function loadWorkspaces(force = false): Promise<void> {
		// If a load is already in progress, return the same promise (unless forced)
		if (loadPromise && !force) {
			return loadPromise;
		}

		// Check cache freshness
		if (!force && lastFetchedAt.value) {
			const cacheAge = Date.now() - lastFetchedAt.value.getTime();
			if (cacheAge < CACHE_TTL) {
				// Cache is still fresh, skip fetch
				return;
			}
		}

		isLoading.value = true;
		error.value = null;

		loadPromise = (async () => {
			try {
				const data = await workspaceHttpClient.getAllWorkspaces();
				workspaces.value = data;
				lastFetchedAt.value = new Date();
			} catch (err) {
				// Handle error
				const errorMessage =
					err instanceof Error ? err.message : "Unknown error";
				error.value = {
					code: WorkspaceErrorCode.NETWORK_ERROR,
					message: errorMessage,
					timestamp: new Date(),
				};
				workspaces.value = [];
				// Rethrow normalized error so callers can handle/retry
				throw {
					code: WorkspaceErrorCode.NETWORK_ERROR,
					message: errorMessage,
					timestamp: new Date(),
				};
			} finally {
				isLoading.value = false;
				loadPromise = null;
			}
		})();

		return loadPromise;
	}

	/**
	 * Sets the current workspace
	 */
	function setCurrentWorkspace(workspace: Workspace): void {
		currentWorkspace.value = workspace;
		// Sync with global context for HTTP clients
		setCurrentWorkspaceId(workspace.id);
	}

	/**
	 * Selects a workspace by ID and updates current workspace
	 * @param workspaceId - The workspace UUID to select
	 * @param userId - The user's UUID (for persistence)
	 * @throws Error if workspace not found or selection fails
	 */
	async function selectWorkspace(
		workspaceId: string,
		userId: string,
	): Promise<void> {
		isLoading.value = true;
		error.value = null;

		try {
			if (!WorkspaceId.isValid(workspaceId)) {
				throw new Error(`Invalid workspace ID: ${workspaceId}`);
			}

			if (!WorkspaceId.isValid(userId)) {
				throw new Error(`Invalid user ID: ${userId}`);
			}

			let workspace = workspaces.value.find((w) => w.id === workspaceId);

			if (!workspace) {
				const fetchedWorkspace =
					await workspaceHttpClient.getWorkspace(workspaceId);

				if (!fetchedWorkspace) {
					throw new Error(`Workspace not found: ${workspaceId}`);
				}

				workspace = fetchedWorkspace;

				if (!workspaces.value.some((w) => w.id === workspaceId)) {
					workspaces.value = [...workspaces.value, fetchedWorkspace];
				}
			}

			currentWorkspace.value = workspace;
			lastSelectedId.value = workspace.id;
			lastSelectedAt.value = new Date();

			// Sync with global context for HTTP clients
			setCurrentWorkspaceId(workspace.id);

			saveLastSelected(userId, workspace.id);
		} catch (err) {
			const errorInstance = err instanceof Error ? err : new Error(String(err));
			let code = WorkspaceErrorCode.SELECTION_FAILED;

			if (errorInstance.message.includes("Invalid workspace ID")) {
				code = WorkspaceErrorCode.VALIDATION_ERROR;
			} else if (errorInstance.message.includes("Invalid user ID")) {
				code = WorkspaceErrorCode.VALIDATION_ERROR;
			} else if (errorInstance.message.includes("not found")) {
				code = WorkspaceErrorCode.WORKSPACE_NOT_FOUND;
			}

			error.value = {
				code,
				message: errorInstance.message,
				timestamp: new Date(),
			};

			throw errorInstance;
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Clears the error state
	 */
	function clearError(): void {
		error.value = null;
	}

	/**
	 * Resets the session state (for logout or session changes)
	 */
	function resetSession(): void {
		loadedInSession.value = false;
		currentWorkspace.value = null;
		lastSelectedId.value = null;
		lastSelectedAt.value = null;
		error.value = null;
		// Clear global context for HTTP clients
		clearCurrentWorkspaceId();
	}

	return {
		// State
		workspaces,
		currentWorkspace,
		isLoading,
		error,
		lastFetchedAt,
		lastSelectedId,
		lastSelectedAt,
		loadedInSession,
		// Getters
		hasWorkspaces,
		defaultWorkspace,
		// Actions
		loadWorkspaces,
		setCurrentWorkspace,
		selectWorkspace,
		clearError,
		resetSession,
	};
});
