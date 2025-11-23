import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";

/**
 * Session storage implementation for resume persistence.
 *
 * This is the default storage strategy. Data is stored in the browser's
 * sessionStorage and is cleared when the browser tab/window is closed.
 *
 * Best for:
 * - Temporary resume editing sessions
 * - Privacy-conscious users (data doesn't persist)
 * - Quick draft work without commitment to save
 *
 * @example
 * ```typescript
 * const storage = new SessionStorageResumeStorage();
 * await storage.save(resume);
 * const result = await storage.load();
 * ```
 */
export class SessionStorageResumeStorage implements ResumeStorage {
	private readonly key = "cvix:resume";

	constructor(private readonly storage: Storage = window.sessionStorage) {}

	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		try {
			const serialized = JSON.stringify(resume);
			this.storage.setItem(this.key, serialized);

			return {
				data: resume,
				timestamp: new Date().toISOString(),
				storageType: "session",
			};
		} catch (error) {
			throw new Error(
				`Failed to save resume to session storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async load(): Promise<PersistenceResult<Resume | null>> {
		try {
			const data = this.storage.getItem(this.key);

			return {
				data: data ? (JSON.parse(data) as Resume) : null,
				timestamp: new Date().toISOString(),
				storageType: "session",
			};
		} catch (error) {
			throw new Error(
				`Failed to load resume from session storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async clear(): Promise<void> {
		try {
			this.storage.removeItem(this.key);
		} catch (error) {
			throw new Error(
				`Failed to clear resume from session storage: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	type(): StorageType {
		return "session";
	}
}
