import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";
import {
	type ResumeDocumentResponse,
	ResumeHttpClient,
} from "@/core/resume/infrastructure/http/ResumeHttpClient";

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

	/** Workspace ID for the resume documents */
	workspaceId: string;

	/** Optional resume ID (if not provided, a new UUID will be generated on save) */
	resumeId?: string;
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
 * Best for:
 * - Users wanting cloud backup and cross-device access
 * - Production resume data that should be permanently stored
 * - Collaborative features (future)
 *
 * @example
 * ```typescript
 * const storage = new RemoteResumeStorage({
 *   workspaceId: 'workspace-uuid',
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

	constructor(
		config: RemoteStorageConfig,
		client: ResumeHttpClient = new ResumeHttpClient(),
	) {
		this.client = client;
		this.config = {
			initialRetryDelay: config.initialRetryDelay ?? 1000,
			maxRetryDelay: config.maxRetryDelay ?? 30000,
			maxRetries: config.maxRetries ?? 3,
			workspaceId: config.workspaceId,
			resumeId: config.resumeId,
		};
		this.currentResumeId = config.resumeId ?? null;
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

	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		// Generate new UUID if we don't have one yet
		if (!this.currentResumeId) {
			this.currentResumeId = crypto.randomUUID();
		}

		const response = await this.withRetry(async () => {
			// Try update first if we have an ID, otherwise create
			if (this.currentResumeId) {
				try {
					return await this.client.updateResume(
						this.currentResumeId,
						resume as Resume,
						undefined, // title is optional
					);
				} catch (error) {
					// If update fails with 404, the resume doesn't exist yet, create it
					if (
						error &&
						typeof error === "object" &&
						"status" in error &&
						error.status === 404
					) {
						return await this.client.createResume(
							this.currentResumeId,
							this.config.workspaceId,
							resume as Resume,
							undefined, // title is optional
						);
					}
					throw error;
				}
			}

			// No ID yet, create new resume
			if (!this.currentResumeId) {
				throw new Error("Resume ID is required for creation");
			}
			return await this.client.createResume(
				this.currentResumeId,
				this.config.workspaceId,
				resume as Resume,
				undefined, // title is optional
			);
		}, "Save resume");

		// Store server timestamp and ID
		this.lastServerTimestamp = response.updatedAt ?? response.createdAt;
		this.currentResumeId = response.id;

		return {
			data: resume,
			timestamp: response.updatedAt ?? response.createdAt,
			storageType: "remote",
			metadata: {
				id: response.id,
				userId: response.userId,
				workspaceId: response.workspaceId,
				createdAt: response.createdAt,
				updatedAt: response.updatedAt,
				retryCount: this.retryCount,
			},
		};
	}

	async load(): Promise<PersistenceResult<Resume | null>> {
		if (!this.currentResumeId && !this.config.resumeId) {
			// No ID to load, return null
			return {
				data: null,
				timestamp: new Date().toISOString(),
				storageType: "remote",
			};
		}

		const id = this.currentResumeId ?? this.config.resumeId;
		if (!id) {
			throw new Error("Resume ID is required for loading");
		}

		try {
			const response = await this.withRetry(
				async () => this.client.getResume(id),
				"Load resume",
			);

			// Update our tracking
			this.currentResumeId = response.id;
			this.lastServerTimestamp = response.updatedAt ?? response.createdAt;

			return {
				data: this.mapResponseToResume(response),
				timestamp: response.updatedAt ?? response.createdAt,
				storageType: "remote",
				metadata: {
					id: response.id,
					userId: response.userId,
					workspaceId: response.workspaceId,
					createdAt: response.createdAt,
					updatedAt: response.updatedAt,
					retryCount: this.retryCount,
				},
			};
		} catch (error) {
			// If resume not found, return null instead of throwing
			if (
				error &&
				typeof error === "object" &&
				"status" in error &&
				error.status === 404
			) {
				return {
					data: null,
					timestamp: new Date().toISOString(),
					storageType: "remote",
				};
			}
			throw error;
		}
	}

	async clear(): Promise<void> {
		if (!this.currentResumeId && !this.config.resumeId) {
			// Nothing to clear
			return;
		}

		const id = this.currentResumeId ?? this.config.resumeId;
		if (!id) {
			return; // Nothing to clear
		}

		await this.withRetry(
			async () => this.client.deleteResume(id),
			"Delete resume",
		);

		// Clear our tracking
		this.currentResumeId = null;
		this.lastServerTimestamp = null;
		this.retryCount = 0;
	}

	type(): StorageType {
		return "remote";
	}

	/**
	 * Calculate the next retry delay using exponential backoff
	 * Formula: min(initialDelay * 2^retryCount, maxDelay)
	 */
	private calculateRetryDelay(): number {
		const delay = this.config.initialRetryDelay * 2 ** this.retryCount;
		return Math.min(delay, this.config.maxRetryDelay);
	}

	/**
	 * Execute an operation with retry logic
	 */
	private async withRetry<T>(
		operation: () => Promise<T>,
		operationName: string,
	): Promise<T> {
		let lastError: Error | null = null;

		for (let attempt = 0; attempt <= this.config.maxRetries; attempt++) {
			try {
				const result = await operation();
				// Reset retry count on success
				this.retryCount = 0;
				return result;
			} catch (error) {
				lastError = error instanceof Error ? error : new Error("Unknown error");
				this.retryCount = attempt + 1;

				// Don't retry on the last attempt
				if (attempt < this.config.maxRetries) {
					const delay = this.calculateRetryDelay();
					console.warn(
						`[RemoteResumeStorage] ${operationName} failed (attempt ${attempt + 1}/${this.config.maxRetries + 1}). Retrying in ${delay}ms...`,
						lastError,
					);

					// Wait before retrying
					await new Promise((resolve) => setTimeout(resolve, delay));
				}
			}
		}

		// All retries exhausted
		throw new Error(
			`${operationName} failed after ${this.config.maxRetries + 1} attempts: ${lastError?.message}`,
		);
	}

	/**
	 * Map backend response to domain Resume
	 */
	private mapResponseToResume(response: ResumeDocumentResponse): Resume {
		return response.content;
	}
}
