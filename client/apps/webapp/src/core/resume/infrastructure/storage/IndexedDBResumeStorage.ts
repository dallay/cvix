import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";

/**
 * IndexedDB implementation for resume persistence.
 *
 * Uses the browser's IndexedDB API for structured, asynchronous storage.
 * Ideal for large resume data or when needing to store multiple versions.
 *
 * Best for:
 * - Large resume files with extensive data
 * - Storing multiple resume versions or drafts
 * - Advanced offline-first applications
 * - Better performance with complex queries
 *
 * @example
 * ```typescript
 * const storage = new IndexedDBResumeStorage();
 * await storage.save(resume);
 * const result = await storage.load();
 * ```
 */
export class IndexedDBResumeStorage implements ResumeStorage {
	private readonly dbName = "cvix-db";
	private readonly storeName = "resumes";
	private readonly version = 1;
	private readonly key = "current-resume";

	/**
	 * Opens or creates the IndexedDB database.
	 *
	 * @returns Promise resolving to the database instance
	 * @private
	 */
	private async openDatabase(): Promise<IDBDatabase> {
		return new Promise((resolve, reject) => {
			const request = indexedDB.open(this.dbName, this.version);

			request.onerror = () => {
				reject(
					new Error(`Failed to open IndexedDB: ${request.error?.message}`),
				);
			};

			request.onsuccess = () => {
				resolve(request.result);
			};

			request.onupgradeneeded = (event) => {
				const db = (event.target as IDBOpenDBRequest).result;

				// Create object store if it doesn't exist
				if (!db.objectStoreNames.contains(this.storeName)) {
					db.createObjectStore(this.storeName);
				}
			};
		});
	}

	async save(
		resume: Resume | PartialResume,
	): Promise<PersistenceResult<Resume | PartialResume>> {
		try {
			const db = await this.openDatabase();
			const transaction = db.transaction([this.storeName], "readwrite");
			const store = transaction.objectStore(this.storeName);

			const timestamp = new Date().toISOString();
			const dataToStore = {
				resume,
				timestamp,
			};

			return new Promise((resolve, reject) => {
				const request = store.put(dataToStore, this.key);

				request.onsuccess = () => {
					db.close();
					resolve({
						data: resume,
						timestamp,
						storageType: "indexeddb",
					});
				};

				request.onerror = () => {
					db.close();
					reject(
						new Error(
							`Failed to save resume to IndexedDB: ${request.error?.message}`,
						),
					);
				};
			});
		} catch (error) {
			throw new Error(
				`Failed to save resume to IndexedDB: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async load(): Promise<PersistenceResult<Resume | null>> {
		try {
			const db = await this.openDatabase();
			const transaction = db.transaction([this.storeName], "readonly");
			const store = transaction.objectStore(this.storeName);

			return new Promise((resolve, reject) => {
				const request = store.get(this.key);

				request.onsuccess = () => {
					db.close();
					const result = request.result as
						| { resume: Resume; timestamp: string }
						| undefined;

					resolve({
						data: result?.resume ?? null,
						timestamp: new Date().toISOString(),
						storageType: "indexeddb",
						metadata: result ? { savedAt: result.timestamp } : undefined,
					});
				};

				request.onerror = () => {
					db.close();
					reject(
						new Error(
							`Failed to load resume from IndexedDB: ${request.error?.message}`,
						),
					);
				};
			});
		} catch (error) {
			throw new Error(
				`Failed to load resume from IndexedDB: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	async clear(): Promise<void> {
		try {
			const db = await this.openDatabase();
			const transaction = db.transaction([this.storeName], "readwrite");
			const store = transaction.objectStore(this.storeName);

			return new Promise((resolve, reject) => {
				const request = store.delete(this.key);

				request.onsuccess = () => {
					db.close();
					resolve();
				};

				request.onerror = () => {
					db.close();
					reject(
						new Error(
							`Failed to clear resume from IndexedDB: ${request.error?.message}`,
						),
					);
				};
			});
		} catch (error) {
			throw new Error(
				`Failed to clear resume from IndexedDB: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
		}
	}

	type(): StorageType {
		return "indexeddb";
	}
}
