import type { StoragePreference, UserSettings } from "../domain";

/**
 * Use case: Update storage preference.
 *
 * This use case encapsulates the business logic for changing the user's
 * storage preference. It validates the input and coordinates the update.
 *
 * @param currentSettings - The current user settings
 * @param newPreference - The new storage preference to set
 * @returns Updated user settings with the new storage preference
 * @throws Error if the new preference is invalid
 *
 * @example
 * ```typescript
 * const settings = await updateStoragePreference(currentSettings, 'local');
 * ```
 */
export function updateStoragePreference(
	currentSettings: UserSettings,
	newPreference: StoragePreference,
): UserSettings {
	// Business rule: Validate the new preference
	if (!isValidPreference(newPreference)) {
		throw new Error(`Invalid storage preference: ${newPreference}`);
	}

	// Return updated settings (immutable update)
	return {
		...currentSettings,
		storagePreference: newPreference,
	};
}

/**
 * Validates a storage preference value.
 */
function isValidPreference(value: unknown): value is StoragePreference {
	return (
		typeof value === "string" &&
		["session", "local", "indexeddb", "remote"].includes(value)
	);
}
