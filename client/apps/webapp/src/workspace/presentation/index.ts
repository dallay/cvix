/**
 * Presentation layer exports for workspace feature
 * Contains Vue components and UI-specific composables
 */

export { default as WorkspaceErrorState } from "./components/WorkspaceErrorState.vue";
export { default as WorkspaceLoadingState } from "./components/WorkspaceLoadingState.vue";
// Components
export { default as WorkspaceSelector } from "./components/WorkspaceSelector.vue";
export { default as WorkspaceSelectorItem } from "./components/WorkspaceSelectorItem.vue";

// UI Composables
export * from "./composables/useWorkspaceSelectorUI";
