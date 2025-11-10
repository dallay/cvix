import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";

/**
 * Application service for resume persistence operations.
 *
 * This service implements the Strategy pattern, delegating persistence
 * operations to the configured storage implementation. This allows
 * switching between different storage mechanisms (session, local, IndexedDB, remote)
 * without changing the application logic.
 *
 * @example
 * ```typescript
 * // Using session storage (default)
 * const service = new ResumePersistenceService(new SessionStorageResumeStorage());
 * await service.save(resume);
 *
 * // Switching to local storage
 * const service = new ResumePersistenceService(new LocalStorageResumeStorage());
 * await service.save(resume);
 * ```
 */
export class ResumePersistenceService {
	constructor(private strategy: ResumeStorage) {}

	/**
	 * Saves a complete or partial resume using the configured storage strategy.
	 *
	 * @param resume - The resume data to persist
	 * @returns Promise resolving to persistence metadata
	 * @throws Error if storage operation fails
	 */
	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		return await this.strategy.save(resume);
	}

	/**
	 * Loads the stored resume using the configured storage strategy.
	 *
	 * @returns Promise resolving to the resume or null if not found
	 * @throws Error if storage operation fails
	 */
	async load(): Promise<PersistenceResult<Resume | null>> {
		return await this.strategy.load();
	}

	/**
	 * Clears the stored resume using the configured storage strategy.
	 *
	 * @returns Promise resolving when deletion is complete
	 * @throws Error if storage operation fails
	 */
	async clear(): Promise<void> {
		await this.strategy.clear();
	}

	/**
	 * Gets the current storage type being used.
	 *
	 * @returns The storage mechanism identifier
	 */
	getStorageType(): StorageType {
		return this.strategy.type();
	}

	/**
	 * Changes the storage strategy at runtime.
	 *
	 * Note: This does not automatically migrate data from one storage to another.
	 * If you need to preserve data, load it first, change the strategy, then save it.
	 *
	 * @param newStrategy - The new storage implementation to use
	 *
	 * @example
	 * ```typescript
	 * // Load from session storage
	 * const result = await service.load();
	 *
	 * // Switch to local storage
	 * service.setStrategy(new LocalStorageResumeStorage());
	 *
	 * // Save to the new storage
	 * if (result.data) {
	 *   await service.save(result.data);
	 * }
	 * ```
	 */
	setStrategy(newStrategy: ResumeStorage): void {
		this.strategy = newStrategy;
	}
}
