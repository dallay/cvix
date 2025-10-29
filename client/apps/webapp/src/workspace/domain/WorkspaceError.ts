import type { Workspace } from "./WorkspaceEntity";

/**
 * Workspace error codes
 */
export enum WorkspaceErrorCode {
	NETWORK_ERROR = "NETWORK_ERROR",
	API_ERROR = "API_ERROR",
	VALIDATION_ERROR = "VALIDATION_ERROR",
	NO_WORKSPACES = "NO_WORKSPACES",
	WORKSPACE_NOT_FOUND = "WORKSPACE_NOT_FOUND",
	NO_DEFAULT_WORKSPACE = "NO_DEFAULT_WORKSPACE",
	UNAUTHORIZED = "UNAUTHORIZED",
	SELECTION_FAILED = "SELECTION_FAILED",
}

/**
 * Workspace error interface
 */
export interface WorkspaceError {
	code: WorkspaceErrorCode;
	message: string;
	details?: unknown;
	timestamp: Date;
}

/**
 * API response for getting all workspaces
 */
export interface GetAllWorkspacesResponse {
	data: Workspace[];
	meta: {
		total: number;
		hasMore: boolean;
	};
}

/**
 * API response for getting a single workspace
 */
export interface GetWorkspaceResponse {
	data: Workspace;
}

/**
 * API error response
 */
export interface ErrorResponse {
	error: {
		code: string;
		message: string;
		errors?: Array<{
			field: string;
			message: string;
		}>;
	};
}
