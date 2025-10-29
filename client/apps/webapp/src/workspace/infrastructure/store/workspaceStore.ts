import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Workspace } from "../../domain/WorkspaceEntity";
import type { WorkspaceError } from "../../domain/WorkspaceError";
import { WorkspaceErrorCode } from "../../domain/WorkspaceError";
import { workspaceHttpClient } from "../http/workspaceHttpClient";

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
		// Getters
		hasWorkspaces,
		defaultWorkspace,
		// Actions
		loadWorkspaces,
		setCurrentWorkspace,
		clearError,
	};
});
