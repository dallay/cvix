/**
 * Barrel export for the workspace feature
 * Public API for the workspace selection feature
 */

// Application layer
export { useWorkspaceLoader } from "./application/useWorkspaceLoader.ts";
export { useWorkspaceSelection } from "./application/useWorkspaceSelection.ts";

// Domain layer
export type { Workspace } from "./domain/WorkspaceEntity.ts";
export { WorkspaceId } from "./domain/WorkspaceId.ts";
export { WorkspaceName } from "./domain/WorkspaceName.ts";
export { determineWorkspaceToLoad } from "./domain/WorkspaceSelectionService.ts";

// Infrastructure layer
export type { WorkspaceApiClient } from "./infrastructure/api/WorkspaceApiClient.ts";
export { workspaceHttpClient } from "./infrastructure/http/workspaceHttpClient.ts";
// Router guards
export {
	resetWorkspaceGuardSession,
	workspaceGuard,
} from "./infrastructure/router/workspaceGuard.ts";
export { workspaceLocalStorage } from "./infrastructure/storage/workspaceLocalStorage.ts";
export { useWorkspaceStore } from "./infrastructure/store/workspaceStore.ts";

// Presentation layer
export { default as WorkspaceSelector } from "./presentation/components/WorkspaceSelector.vue";
export { default as WorkspaceSelectorItem } from "./presentation/components/WorkspaceSelectorItem.vue";
