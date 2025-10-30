/**
 * Barrel export for the workspace feature
 * Public API for the workspace selection feature
 */

// Application layer
export { useWorkspaceLoader } from "./application/useWorkspaceLoader";
export { useWorkspaceSelection } from "./application/useWorkspaceSelection";

// Domain layer
export type { Workspace } from "./domain/WorkspaceEntity";
export { WorkspaceId } from "./domain/WorkspaceId";
export { WorkspaceName } from "./domain/WorkspaceName";
export { determineWorkspaceToLoad } from "./domain/WorkspaceSelectionService";

// Infrastructure layer
export type { WorkspaceApiClient } from "./infrastructure/api/WorkspaceApiClient";
export { workspaceHttpClient } from "./infrastructure/http/workspaceHttpClient";
// Router guards
export {
	resetWorkspaceGuardSession,
	workspaceGuard,
} from "./infrastructure/router/workspaceGuard";
export { workspaceLocalStorage } from "./infrastructure/storage/workspaceLocalStorage";
export { useWorkspaceStore } from "./infrastructure/store/workspaceStore";

// Presentation layer
export { default as WorkspaceSelector } from "./presentation/components/WorkspaceSelector.vue";
export { default as WorkspaceSelectorItem } from "./presentation/components/WorkspaceSelectorItem.vue";
