/**
 * Infrastructure layer exports for workspace feature
 * Contains framework-specific implementations (Pinia, API clients, storage adapters)
 */

// API Client Interface
export * from "./api/WorkspaceApiClient";

// HTTP Client (also serves as API client implementation)
export * from "./http/workspaceHttpClient";
// Re-export HTTP client instance as API client for convenience
export { workspaceHttpClient as workspaceApiClient } from "./http/workspaceHttpClient";
// Storage Adapter
export * from "./storage/workspaceLocalStorage";
// Pinia Store
export * from "./store/workspaceStore";
