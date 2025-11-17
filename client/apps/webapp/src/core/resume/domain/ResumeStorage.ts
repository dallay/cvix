import type { Resume } from "./Resume";

/**
 * Supported storage types for resume persistence.
 *
 * - `session`: Data cleared when browser tab is closed (sessionStorage)
 * - `local`: Persists across browser sessions (localStorage)
 * - `indexeddb`: Structured async storage for large datasets
 * - `remote`: Backend persistence via REST API (future implementation)
 */
export type StorageType = "session" | "local" | "indexeddb" | "remote";

/**
 * Standardized result for storage operations.
 *
 * Provides type-safe metadata about the persistence operation,
 * including timestamp and storage type used.
 *
 * @template T - The type of data being persisted (Resume, PartialResume, or null)
 */
export interface PersistenceResult<T = Resume> {
	/** The persisted or retrieved data */
	data: T;

	/** ISO 8601 timestamp of the operation */
	timestamp: string;

	/** The storage mechanism used */
	storageType: StorageType;

	/** Optional metadata (e.g., version, sync status) */
	metadata?: Record<string, unknown>;
}

/**
 * Utility type for partial/draft resumes.
 *
 * Allows persisting incomplete resume data during editing.
 * All sections become optional, enabling progressive resume building.
 *
 * @example
 * ```typescript
 * const draft: PartialResume = {
 *   basics: {
 *     name: "John Doe",
 *     email: "john@example.com"
 *   }
 *   // Other sections omitted during draft phase
 * };
 * ```
 */
type DeepPartial<T> = {
	[K in keyof T]?: T[K] extends ReadonlyArray<infer U>
		? ReadonlyArray<U>
		: T[K] extends Date | Map<unknown, unknown> | Set<unknown> | RegExp
			? T[K]
			: T[K] extends object
				? DeepPartial<T[K]>
				: T[K];
};

export type PartialResume = DeepPartial<Resume>;

/**
 * Domain interface for resume storage operations.
 *
 * This interface defines the contract for all resume persistence strategies.
 * All implementations must be:
 * - Type-safe with strict TypeScript typing
 * - Async-first (even for sync storage APIs)
 * - Validated using ResumeValidator
 * - Storage-agnostic (strategy pattern)
 *
 * @example
 * ```typescript
 * // Usage with any storage implementation
 * const storage: ResumeStorage = new SessionStorageResumeStorage();
 * await storage.save(myResume);
 * const result = await storage.load();
 * ```
 */
export interface ResumeStorage {
	/**
	 * Persists a complete or partial resume.
	 *
	 * @param resume - Complete or draft resume data
	 * @returns Promise resolving to persistence metadata
	 * @throws Error if validation fails or storage is unavailable
	 */
	save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>>;

	/**
	 * Retrieves the stored resume.
	 *
	 * @returns Promise resolving to the resume or null if not found
	 */
	load(): Promise<PersistenceResult<Resume | null>>;

	/**
	 * Removes the stored resume.
	 *
	 * @returns Promise resolving when deletion is complete
	 */
	clear(): Promise<void>;

	/**
	 * Returns the storage type identifier.
	 *
	 * @returns The storage mechanism type
	 */
	type(): StorageType;
}
