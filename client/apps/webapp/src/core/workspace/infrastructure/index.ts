/**
 * Infrastructure layer exports for workspace feature
 * Contains framework-specific implementations (Pinia, API clients, storage adapters)
 */

// API Client Interface
export * from "./api/WorkspaceApiClient.ts";

// HTTP Client (also serves as API client implementation)
export * from "./http/workspaceHttpClient.ts";
// Re-export HTTP client instance as API client for convenience
export { workspaceHttpClient as workspaceApiClient } from "./http/workspaceHttpClient.ts";
// Storage Adapter
export * from "./storage/workspaceLocalStorage.ts";
// Pinia Store
export * from "./store/workspaceStore.ts";
