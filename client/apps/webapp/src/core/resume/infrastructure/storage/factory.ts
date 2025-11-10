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
 * Create a ResumeStorage implementation for the given storage type.
 *
 * @param type - Storage type to instantiate (`"session"`, `"local"`, `"indexeddb"`, or `"remote"`)
 * @returns A ResumeStorage instance corresponding to `type`
 * @throws Error if `type` is `"remote"` (not implemented) or an unknown storage type
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
 * Metadata for each supported resume storage option, suitable for display and selection in the UI.
 *
 * @returns An array of StorageMetadata objects describing the available storage types
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