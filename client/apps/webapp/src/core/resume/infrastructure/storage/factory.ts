import type {
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";
import {
	IndexedDBResumeStorage,
	LocalStorageResumeStorage,
	SessionStorageResumeStorage,
} from "@/core/resume/infrastructure/storage";

/**
 * Factory function that creates the appropriate storage instance based on type.
 *
 * This factory encapsulates the logic for instantiating storage implementations,
 * making it easy to create storage instances consistently across the application.
 *
 * @param type - The storage type to create
 * @returns A ResumeStorage instance of the requested type
 * @throws Error if the storage type is not supported or not yet implemented
 *
 * @example
 * ```typescript
 * const storage = createResumeStorage('local');
 * await storage.save(resume);
 * ```
 */
export function createResumeStorage(type: StorageType): ResumeStorage {
	switch (type) {
		case "session":
			return new SessionStorageResumeStorage();

		case "local":
			return new LocalStorageResumeStorage();

		case "indexeddb":
			return new IndexedDBResumeStorage();

		case "remote":
			throw new Error(
				"Remote storage is not yet implemented. Coming soon in a future release.",
			);

		default:
			// TypeScript ensures exhaustiveness, but we add this for runtime safety
			throw new Error(`Unknown storage type: ${type satisfies never}`);
	}
}

/**
 * Gets the default storage type based on environment and availability.
 *
 * Priority:
 * 1. Production: Local storage (persistent)
 * 2. Development: Session storage (temporary for easier testing)
 * 3. Fallback: Session storage
 *
 * @returns The recommended default storage type
 */
export function getDefaultStorageType(): StorageType {
	const isDevelopment = import.meta.env.DEV;

	// In development, use session storage for easier testing
	if (isDevelopment) {
		return "session";
	}

	// In production, prefer local storage for persistence
	// Check if localStorage is available
	try {
		if (typeof localStorage !== "undefined") {
			return "local";
		}
	} catch {
		// localStorage might be blocked (privacy mode, etc.)
	}

	// Fallback to session storage
	return "session";
}

/**
 * Storage metadata for UI display.
 */
export interface StorageMetadata {
	type: StorageType;
	label: string;
	description: string;
	icon: string;
	persistence: "session" | "permanent";
	capacity: string;
	recommended?: boolean;
}

/**
 * Gets metadata for all storage types.
 * Useful for displaying storage options in the UI.
 *
 * @returns Array of storage metadata
 */
export function getStorageMetadata(): StorageMetadata[] {
	return [
		{
			type: "session",
			label: "Session Storage",
			description:
				"Data is stored temporarily and cleared when you close the browser tab. Best for privacy.",
			icon: "🔒",
			persistence: "session",
			capacity: "~5-10 MB",
		},
		{
			type: "local",
			label: "Local Storage",
			description:
				"Data persists across browser sessions. Your resume will be available next time you visit.",
			icon: "💾",
			persistence: "permanent",
			capacity: "~5-10 MB",
			recommended: true,
		},
		{
			type: "indexeddb",
			label: "IndexedDB",
			description:
				"Advanced storage for large resumes. Best performance for complex data.",
			icon: "🗄️",
			persistence: "permanent",
			capacity: "~50+ MB",
		},
	];
}

/**
 * Gets metadata for a specific storage type.
 *
 * @param type - The storage type
 * @returns Metadata for the storage type, or undefined if not found
 */
export function getStorageMetadataByType(
	type: StorageType,
): StorageMetadata | undefined {
	return getStorageMetadata().find((meta) => meta.type === type);
}
