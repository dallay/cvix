import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";

/**
 * Local storage implementation for resume persistence.
 *
 * Data is stored in the browser's localStorage and persists across
 * browser sessions and tab closures.
 *
 * Best for:
 * - Users who want to save their work between sessions
 * - Long-term draft editing
 * - Offline-first workflows
 *
 * @example
 * ```typescript
 * const storage = new LocalStorageResumeStorage();
 * await storage.save(resume);
 * const result = await storage.load();
 * ```
 */
export class LocalStorageResumeStorage implements ResumeStorage {
	private readonly key = "cvix:resume";

	constructor(private readonly storage: Storage = window.localStorage) {}

	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		try {
			const serialized = JSON.stringify(resume);
			this.storage.setItem(this.key, serialized);

			return {
				data: resume,
				timestamp: new Date().toISOString(),
				storageType: "local",
			};
		} catch (error) {
			throw new Error(
				`Failed to save resume to local storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async load(): Promise<PersistenceResult<Resume | null>> {
		try {
			const data = this.storage.getItem(this.key);

			return {
				data: data ? (JSON.parse(data) as Resume) : null,
				timestamp: new Date().toISOString(),
				storageType: "local",
			};
		} catch (error) {
			throw new Error(
				`Failed to load resume from local storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async clear(): Promise<void> {
		try {
			this.storage.removeItem(this.key);
		} catch (error) {
			throw new Error(
				`Failed to clear resume from local storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	type(): StorageType {
		return "local";
	}
}
