import type { StoragePreference, UserSettings } from "../domain";

/**
 * Update a user's storage preference after validating the new value.
 *
 * @param currentSettings - The existing user settings to base the update on
 * @param newPreference - The storage preference to set (must be one of the allowed values)
 * @returns The updated UserSettings with `storagePreference` set to `newPreference`
 * @throws Error if `newPreference` is not a valid storage preference
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
 * Checks whether a value is an allowed storage preference.
 *
 * @param value - The value to validate
 * @returns `true` if `value` is a `StoragePreference` ('session', 'local', 'indexeddb', or 'remote'), `false` otherwise. When `true`, narrows the type to `StoragePreference`.
 */
function isValidPreference(value: unknown): value is StoragePreference {
	return (
		typeof value === "string" &&
		["session", "local", "indexeddb", "remote"].includes(value)
	);
}