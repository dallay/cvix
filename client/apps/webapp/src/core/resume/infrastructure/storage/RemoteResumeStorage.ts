import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";
import { getCurrentWorkspaceId } from "@/shared/WorkspaceContext";
import {
	type ResumeDocumentResponse,
	ResumeHttpClient,
} from "../http/ResumeHttpClient";

/**
 * Configuration for remote storage with retry mechanism
 */
export interface RemoteStorageConfig {
	/** Initial retry delay in milliseconds (default: 1000ms = 1s) */
	initialRetryDelay?: number;

	/** Maximum retry delay in milliseconds (default: 30000ms = 30s) */
	maxRetryDelay?: number;

	/** Maximum number of retry attempts (default: 3) */
	maxRetries?: number;

	/** Optional resume ID (if not provided, a new UUID will be generated on save) */
	resumeId?: string;
}

/**
 * Type alias for HTTP errors with a response status (Axios-style)
 */
export type HttpErrorWithStatus = {
	response: {
		status: number;
		[key: string]: unknown;
	};
	[key: string]: unknown;
};

/**
 * Type guard for HTTP errors with a response status
 */
function isHttpErrorWithStatus(error: unknown): error is HttpErrorWithStatus {
	return (
		typeof error === "object" &&
		error !== null &&
		"response" in error &&
		typeof (error as { response: unknown }).response === "object" &&
		(error as { response: object }).response !== null &&
		"status" in (error as { response: { status: number } }).response &&
		typeof (error as { response: { status: unknown } }).response.status ===
			"number"
	);
}

/**
 * Type guard for Axios network errors (errors without a response)
 */
function isAxiosNetworkError(
	error: unknown,
): error is { isAxiosError: true; response: undefined } {
	if (!error || typeof error !== "object") {
		return false;
	}
	return (
		"isAxiosError" in error &&
		error.isAxiosError === true &&
		(!("response" in error) ||
			(error as { response: unknown }).response === undefined)
	);
}

/**
 * Remote storage implementation for resume persistence via REST API.
 *
 * This implementation provides server-side persistence with:
 * - Automatic retry with exponential backoff for failed operations
 * - Optimistic locking via updatedAt timestamps
 * - Server-synced timestamps for "Last saved" indicators
 * - Non-blocking error handling with user warnings
 *
 * Note: The workspace ID is automatically sent via the X-Workspace-Id header
 * by the BaseHttpClient. Make sure the workspace is selected before using
 * this storage.
 *
 * Best for:
 * - Users wanting cloud backup and cross-device access
 * - Production resume data that should be permanently stored
 * - Collaborative features (future)
 *
 * @example
 * ```typescript
 * const storage = new RemoteResumeStorage({
 *   resumeId: 'resume-uuid' // optional
 * });
 * await storage.save(resume);
 * const result = await storage.load();
 * ```
 */
export class RemoteResumeStorage implements ResumeStorage {
	private readonly client: ResumeHttpClient;
	private readonly config: Required<Omit<RemoteStorageConfig, "resumeId">> & {
		resumeId?: string;
	};
	private retryCount = 0;
	private lastServerTimestamp: string | null = null;
	private currentResumeId: string | null = null;
	private consecutiveFailures = 0;
	private warningCallback: ((message: string) => void) | null = null;

	constructor(
		config: RemoteStorageConfig,
		client: ResumeHttpClient = new ResumeHttpClient(),
	) {
		this.client = client;
		this.config = {
			initialRetryDelay: config.initialRetryDelay ?? 1000,
			maxRetryDelay: config.maxRetryDelay ?? 30000,
			maxRetries: config.maxRetries ?? 3,
			resumeId: config.resumeId,
		};
		this.currentResumeId = config.resumeId ?? null;
	}

	/**
	 * LocalStorage key for persisting the current resume ID across sessions.
	 * Format: cvix:remote-resume-id:{workspaceId}
	 */
	private getStorageKey(): string {
		const workspaceId = getCurrentWorkspaceId();
		return `cvix:remote-resume-id:${workspaceId}`;
	}

	/**
	 * Persist the current resume ID to localStorage for recovery after page reload.
	 */
	private persistResumeId(id: string | null): void {
		try {
			const key = this.getStorageKey();
			if (id) {
				localStorage.setItem(key, id);
			} else {
				localStorage.removeItem(key);
			}
		} catch {
			// localStorage might be unavailable, ignore
		}
	}

	/**
	 * Try to recover the resume ID from localStorage.
	 */
	private recoverPersistedResumeId(): string | null {
		try {
			const key = this.getStorageKey();
			return localStorage.getItem(key);
		} catch {
			return null;
		}
	}

	/**
	 * Creates a PersistenceResult with metadata from a resume response.
	 * Reduces code duplication between save and load operations.
	 */
	private createResultWithMetadata(
		resume: Resume,
		response: ResumeDocumentResponse,
		retryCount: number,
	): PersistenceResult<Resume> {
		return {
			data: resume,
			timestamp: response.updatedAt ?? response.createdAt,
			storageType: "remote" as StorageType,
			metadata: {
				id: response.id,
				userId: response.userId,
				workspaceId: response.workspaceId,
				createdAt: response.createdAt,
				updatedAt: response.updatedAt,
				retryCount,
			},
		};
	}

	/**
	 * Discover an existing resume for the current workspace.
	 * This is called when no resumeId is configured, to recover from page reloads
	 * or find previously created resumes.
	 *
	 * Priority:
	 * 1. Check localStorage for persisted resumeId
	 * 2. Query the server for existing resumes and use the most recently updated one
	 */
	private async discoverExistingResume(): Promise<void> {
		// First, try to recover from localStorage
		const persistedId = this.recoverPersistedResumeId();
		if (persistedId) {
			this.currentResumeId = persistedId;
			this.config.resumeId = persistedId;
			return;
		}

		// If no persisted ID, query the server for existing resumes
		try {
			const resumes = await this.client.listResumes();
			if (resumes.length > 0) {
				// Sort by most recently updated (updatedAt descending, fallback to createdAt)
				const sorted = resumes.sort((a, b) => {
					const dateA = new Date(a.updatedAt ?? a.createdAt).getTime();
					const dateB = new Date(b.updatedAt ?? b.createdAt).getTime();
					return dateB - dateA;
				});
				const mostRecent = sorted[0];
				if (mostRecent) {
					this.currentResumeId = mostRecent.id;
					this.config.resumeId = mostRecent.id;
					// Persist for future page loads
					this.persistResumeId(mostRecent.id);
				}
			}
		} catch (error) {
			// If discovery fails (network error, etc.), we'll just start fresh
			console.warn(
				"[RemoteResumeStorage] Failed to discover existing resumes:",
				error,
			);
		}
	}

	/**
	 * Get the current resume ID being used for operations
	 */
	getResumeId(): string | null {
		return this.currentResumeId;
	}

	/**
	 * Get the number of consecutive failed retry attempts
	 */
	getRetryCount(): number {
		return this.retryCount;
	}

	/**
	 * Get the last server-synced timestamp
	 */
	getLastServerTimestamp(): string | null {
		return this.lastServerTimestamp;
	}

	/**
	 * Register a callback to receive non-blocking warnings (e.g., after 3 consecutive failures)
	 */
	onWarning(cb: (message: string) => void) {
		this.warningCallback = cb;
	}

	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		this.validateWorkspaceContext();
		let knownNotFound = false;
		// Validate: only full Resume objects are supported
		if (!isFullResume(resume)) {
			throw new Error(
				"RemoteResumeStorage only supports saving complete Resume objects. Partial resumes are not allowed.",
			);
		}
		// Always try update first if resumeId is present
		const resumeId = this.currentResumeId ?? this.config.resumeId;
		const isCreate = !resumeId;
		const response: ResumeDocumentResponse = await this.withRetry(
			async () => {
				if (isCreate || knownNotFound) {
					// Generate a new UUID for creation
					const newId = crypto.randomUUID();
					this.currentResumeId = newId;
					this.config.resumeId = newId;
					return await this.client.createResume(newId, resume, undefined);
				}
				// Try update first
				try {
					return await this.client.updateResume(resumeId, resume, undefined);
				} catch (error) {
					if (isHttpErrorWithStatus(error) && error.response.status === 404) {
						knownNotFound = true;
						return await this.client.createResume(resumeId, resume, undefined);
					}
					throw error;
				}
			},
			"Save resume",
			(error) => {
				if (!isHttpErrorWithStatus(error)) return false;
				const status = error.response.status;
				// Only retry transient errors: 5xx, 408, 429
				return status >= 500 || status === 408 || status === 429;
			},
		);

		// Store server timestamp and ID
		this.lastServerTimestamp = response.updatedAt;
		this.currentResumeId = response.id;
		this.config.resumeId = response.id;
		// Persist the ID for recovery after page reload
		this.persistResumeId(response.id);

		return this.createResultWithMetadata(resume, response, this.retryCount);
	}

	async load(): Promise<PersistenceResult<Resume | null>> {
		this.validateWorkspaceContext();

		// If no resume ID is known, try to discover existing resumes for this workspace
		if (!this.currentResumeId) {
			await this.discoverExistingResume();
		}

		const id = this.currentResumeId;
		if (!id) {
			return {
				data: null,
				timestamp: new Date().toISOString(),
				storageType: "remote",
			};
		}
		let attemptsUsed = 0;
		try {
			const response = await this.withRetry(
				async () => this.client.getResume(id),
				"Load resume",
				(error) =>
					!(isHttpErrorWithStatus(error) && error.response.status === 404),
			);
			attemptsUsed = this.retryCount;
			// Update our tracking
			this.currentResumeId = response.id;
			this.lastServerTimestamp = response.updatedAt;
			this.retryCount = 0;
			return this.createResultWithMetadata(
				this.mapResponseToResume(response),
				response,
				attemptsUsed,
			);
		} catch (error) {
			// If resume not found, return null instead of throwing
			if (isHttpErrorWithStatus(error) && error.response.status === 404) {
				attemptsUsed = this.retryCount;
				this.currentResumeId = null;
				this.config.resumeId = undefined;
				this.retryCount = 0;
				// Reset consecutiveFailures since 404 is a valid "no data" state, not a failure
				this.consecutiveFailures = 0;
				return {
					data: null,
					timestamp: new Date().toISOString(),
					storageType: "remote",
					metadata: {
						retryCount: attemptsUsed,
					},
				};
			}
			this.retryCount = 0;
			throw error;
		}
	}

	async clear(): Promise<void> {
		this.validateWorkspaceContext();
		const id = this.currentResumeId;
		if (!id) {
			// Also clear persisted ID even if currentResumeId is null
			this.persistResumeId(null);
			return;
		}
		await this.withRetry(
			async () => this.client.deleteResume(id),
			"Delete resume",
		);
		// Clear our tracking
		this.currentResumeId = null;
		this.lastServerTimestamp = null;
		this.retryCount = 0;
		this.config.resumeId = undefined;
		// Clear persisted ID
		this.persistResumeId(null);
	}

	type(): StorageType {
		return "remote";
	}

	/**
	 * Calculate the next retry delay using exponential backoff
	 * Formula: min(initialDelay * 2^attempt, maxDelay)
	 */
	private calculateRetryDelay(attempt: number): number {
		// First retry is initialRetryDelay, then doubles each time, capped at maxRetryDelay
		const delay = this.config.initialRetryDelay * 2 ** attempt;
		return Math.min(delay, this.config.maxRetryDelay);
	}

	/**
	 * Execute an operation with retry logic
	 */
	private async withRetry<T>(
		operation: () => Promise<T>,
		operationName: string,
		shouldRetry?: (error: unknown, attempt: number) => boolean,
	): Promise<T> {
		let lastError: Error | null = null;
		for (let attempt = 0; attempt <= this.config.maxRetries; attempt++) {
			try {
				const result = await operation();
				this.retryCount = 0;
				this.consecutiveFailures = 0;
				return result as T;
			} catch (error) {
				// Only retry network errors, 5xx, 429, 408
				let retryable = false;
				if (!error || typeof error !== "object") {
					retryable = false;
				} else if (isHttpErrorWithStatus(error)) {
					const status = error.response.status;
					retryable = status >= 500 || status === 429 || status === 408;
				} else if (isAxiosNetworkError(error)) {
					// Network error (no response)
					retryable = true;
				}
				if (shouldRetry && !shouldRetry(error, attempt)) {
					retryable = false;
				}
				this.handleRetryFailure(
					retryable,
					attempt,
					operationName,
					lastError,
					error,
				);
				lastError = error instanceof Error ? error : new Error("Unknown error");
				this.retryCount = attempt + 1;
				const delay = this.calculateRetryDelay(attempt);
				console.warn(
					`[RemoteResumeStorage] ${operationName} failed (attempt ${attempt + 1}/${
						this.config.maxRetries + 1
					}). Retrying in ${delay}ms...`,
					lastError,
				);
				await new Promise((resolve) => setTimeout(resolve, delay));
			}
		}
		throw new Error(
			`${operationName} failed after ${
				this.config.maxRetries + 1
			} attempts: ${lastError?.message}`,
		);
	}

	private handleRetryFailure = (
		retryable: boolean,
		attempt: number,
		operationName: string,
		lastError: null | Error,
		error: unknown,
	) => {
		if (!retryable || attempt === this.config.maxRetries) {
			// Only increment consecutiveFailures for retryable errors that exhausted retries
			// Non-retryable errors (like 404) should not count as failures
			if (retryable && attempt === this.config.maxRetries) {
				this.consecutiveFailures++;
				if (this.consecutiveFailures >= 3 && this.warningCallback) {
					this.warningCallback(
						`[RemoteResumeStorage] ${operationName} failed ${this.consecutiveFailures} times. Please check your connection or try again later.`,
					);
				}
			}
			this.retryCount = 0;
			throw lastError ?? error;
		}
	};

	/**
	 * Map backend response to domain Resume
	 */
	private mapResponseToResume(response: ResumeDocumentResponse): Resume {
		return response.content;
	}

	/**
	 * Validates that a workspace is selected before performing remote operations.
	 * Throws an error if no workspace ID is found in the global context.
	 */
	private validateWorkspaceContext(): void {
		if (!getCurrentWorkspaceId()) {
			throw new Error(
				"Remote storage operation failed: No workspace selected. You must select a workspace before saving or loading resumes from the cloud.",
			);
		}
	}
}

// Helper to check if an object is a full Resume
function isFullResume(obj: unknown): obj is Resume {
	if (!obj || typeof obj !== "object") return false;
	const requiredKeys = [
		"basics",
		"work",
		"volunteer",
		"education",
		"awards",
		"certificates",
		"publications",
		"skills",
		"languages",
		"interests",
		"references",
		"projects",
	];
	return requiredKeys.every((key) => key in obj);
}
