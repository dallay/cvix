import { computed } from "vue";
import { useWorkspaceStore } from "../infrastructure/store/workspaceStore";

/**
 * Composable for workspace selection functionality
 * Provides reactive access to workspaces and selection methods
 *
 * @example
 * ```typescript
 * const { workspaces, currentWorkspace, selectWorkspace } = useWorkspaceSelection();
 *
 * // Select a workspace
 * await selectWorkspace(workspaceId, userId);
 * ```
 */
export function useWorkspaceSelection() {
	const store = useWorkspaceStore();

	/**
	 * Select a workspace and persist the selection
	 * @param workspaceId - The workspace UUID to select
	 * @param userId - The user's UUID for persistence
	 * @throws Error if selection fails
	 */
	async function selectWorkspace(
		workspaceId: string,
		userId: string,
	): Promise<void> {
		// Call store action to select workspace (store handles persistence)
		await store.selectWorkspace(workspaceId, userId);
	}

	// Computed properties for convenience
	const hasWorkspaces = computed(() => store.workspaces.length > 0);
	const hasCurrentWorkspace = computed(() => store.currentWorkspace !== null);

	const availableWorkspaces = computed(() => {
		if (!store.currentWorkspace) {
			return store.workspaces;
		}

		// Return workspaces excluding the current one
		return store.workspaces.filter((w) => w.id !== store.currentWorkspace?.id);
	});

	const defaultWorkspace = computed(
		() => store.workspaces.find((w) => w.isDefault) ?? null,
	);
	return {
		// Direct store access (reactive)
		workspaces: computed(() => store.workspaces),
		currentWorkspace: computed(() => store.currentWorkspace),
		isLoading: computed(() => store.isLoading),
		error: computed(() => store.error),

		// Computed properties
		hasWorkspaces,
		hasCurrentWorkspace,
		availableWorkspaces,
		defaultWorkspace,

		// Actions
		selectWorkspace,
	};
}
