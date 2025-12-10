import type { SectionVisibility } from "../../domain/SectionVisibility";

/**
 * Persisted visibility preference with metadata.
 * Stored in localStorage with TTL enforcement.
 */
export interface StoredSectionVisibility {
	/** The visibility preferences */
	visibility: SectionVisibility;

	/** Unix timestamp when preferences were last saved */
	savedAt: number;

	/** Schema version for migration support */
	version: number;
}

/**
 * Current schema version.
 * Increment when making breaking changes to SectionVisibility structure.
 */
export const VISIBILITY_SCHEMA_VERSION = 1;

/**
 * Time-to-live for stored preferences (30 days in milliseconds).
 */
export const VISIBILITY_TTL_MS = 30 * 24 * 60 * 60 * 1000;

/**
 * Storage service for section visibility preferences.
 * Handles localStorage persistence with TTL and versioning.
 */
export class SectionVisibilityStorageData {
	private readonly storageKeyPrefix = "cvix-section-visibility-";

	/**
	 * Generates the storage key for a specific resume.
	 */
	private getStorageKey(resumeId: string): string {
		return `${this.storageKeyPrefix}${resumeId}`;
	}

	/**
	 * Saves visibility preferences to localStorage.
	 *
	 * @param visibility - The visibility preferences to save
	 */
	save(visibility: SectionVisibility): void {
		try {
			const storageData: StoredSectionVisibility = {
				visibility,
				savedAt: Date.now(),
				version: VISIBILITY_SCHEMA_VERSION,
			};
			const key = this.getStorageKey(visibility.resumeId);
			localStorage.setItem(key, JSON.stringify(storageData));
		} catch (error) {
			console.error("Failed to save section visibility preferences:", error);
		}
	}

	/**
	 * Loads visibility preferences from localStorage.
	 * Returns null if preferences don't exist or have expired.
	 *
	 * @param resumeId - The resume ID to load preferences for
	 * @returns The stored visibility preferences, or null if not found/expired
	 */
	load(resumeId: string): SectionVisibility | null {
		try {
			const key = this.getStorageKey(resumeId);
			const stored = localStorage.getItem(key);

			if (!stored) {
				return null;
			}

			const data: StoredSectionVisibility = JSON.parse(stored);

			// Check schema version - if different, discard and return null
			if (data.version !== VISIBILITY_SCHEMA_VERSION) {
				this.remove(resumeId);
				return null;
			}

			// Check TTL
			const age = Date.now() - data.savedAt;
			if (age > VISIBILITY_TTL_MS) {
				this.remove(resumeId);
				return null;
			}

			return data.visibility;
		} catch (error) {
			console.error("Failed to load section visibility preferences:", error);
			return null;
		}
	}

	/**
	 * Removes visibility preferences from localStorage.
	 *
	 * @param resumeId - The resume ID to remove preferences for
	 */
	remove(resumeId: string): void {
		try {
			const key = this.getStorageKey(resumeId);
			localStorage.removeItem(key);
		} catch (error) {
			console.error("Failed to remove section visibility preferences:", error);
		}
	}

	/**
	 * Checks if preferences exist and are not expired.
	 *
	 * ⚠️ Side effect: This method calls `load(resumeId)`, which will remove expired or version-mismatched entries from storage.
	 * This is not a pure read-only check; it validates and may clean up stale data.
	 *
	 * @param resumeId - The resume ID to check
	 * @returns true if valid preferences exist
	 */
	has(resumeId: string): boolean {
		return this.load(resumeId) !== null;
	}

	/**
	 * Clears all section visibility preferences from localStorage.
	 */
	clear(): void {
		try {
			const keys = Object.keys(localStorage);
			keys.forEach((key) => {
				if (key.startsWith(this.storageKeyPrefix)) {
					localStorage.removeItem(key);
				}
			});
		} catch (error) {
			console.error("Failed to clear section visibility preferences:", error);
		}
	}
}

/**
 * Singleton instance of the storage service.
 */
export const sectionVisibilityStorage = new SectionVisibilityStorageData();
