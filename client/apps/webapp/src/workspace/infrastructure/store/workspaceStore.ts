import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Workspace } from "../../domain/WorkspaceEntity";
import type { WorkspaceError } from "../../domain/WorkspaceError";
import { WorkspaceErrorCode } from "../../domain/WorkspaceError";
import { WorkspaceId } from "../../domain/WorkspaceId";
import { workspaceHttpClient } from "../http/workspaceHttpClient";
import { saveLastSelected } from "../storage/workspaceLocalStorage";

const CACHE_TTL = 5 * 60 * 1000; // 5 minutes in milliseconds

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

		try {
			const data = await workspaceHttpClient.getAllWorkspaces();
			workspaces.value = data;
			lastFetchedAt.value = new Date();
		} catch (err) {
			// Handle error
			const errorMessage = err instanceof Error ? err.message : "Unknown error";
			error.value = {
				code: WorkspaceErrorCode.NETWORK_ERROR,
				message: errorMessage,
				timestamp: new Date(),
			};
			workspaces.value = [];
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Sets the current workspace
	 */
	function setCurrentWorkspace(workspace: Workspace): void {
		currentWorkspace.value = workspace;
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

	return {
		// State
		workspaces,
		currentWorkspace,
		isLoading,
		error,
		lastFetchedAt,
		lastSelectedId,
		lastSelectedAt,
		// Getters
		hasWorkspaces,
		defaultWorkspace,
		// Actions
		loadWorkspaces,
		setCurrentWorkspace,
		selectWorkspace,
		clearError,
	};
});
