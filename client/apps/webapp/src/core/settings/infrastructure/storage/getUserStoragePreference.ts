import {
	DEFAULT_USER_SETTINGS,
	isValidStoragePreference,
	type StoragePreference,
} from "../../domain";
import { USER_SETTINGS_STORAGE_KEY } from "./LocalStorageSettingsRepository";

/**
 * Retrieve the user's storage preference from localStorage.
 *
 * This is a shared utility used across the application to read the storage
 * preference consistently. It validates that the stored preference is a valid
 * StoragePreference and returns the default if not found or invalid.
 *
 * Used by:
 * - Application bootstrap (`main.ts`) to initialize the ResumeStorage
 * - Resume store to get storage preference when needed
 *
 * @returns The user's storage preference, or the default if not found/invalid
 */
export function getUserStoragePreference(): StoragePreference {
	try {
		const stored = localStorage.getItem(USER_SETTINGS_STORAGE_KEY);
		if (stored) {
			const parsed = JSON.parse(stored);
			const preference = parsed?.storagePreference;
			if (isValidStoragePreference(preference)) {
				return preference;
			}
		}
	} catch {
		// localStorage might not be available or parsing failed
	}
	return DEFAULT_USER_SETTINGS.storagePreference;
}
