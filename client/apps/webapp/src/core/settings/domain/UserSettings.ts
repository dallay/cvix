/**
 * Domain entity representing user settings.
 *
 * This entity encapsulates all user-configurable settings for the application.
 * It follows the Single Responsibility Principle by focusing only on settings data
 * and validation logic.
 *
 * @example
 * ```typescript
 * const settings: UserSettings = {
 *   storagePreference: 'local',
 *   theme: 'dark',
 *   language: 'en',
 *   notifications: {
 *     enabled: true,
 *     email: true,
 *     push: false
 *   }
 * };
 * ```
 */
export interface UserSettings {
	/**
	 * The user's preferred storage type for resume data.
	 * Determines where resume data is persisted (session, local storage, IndexedDB, or remote).
	 */
	storagePreference: StoragePreference;

	/**
	 * The user's preferred theme (light, dark, or system).
	 */
	theme?: ThemePreference;

	/**
	 * The user's preferred language code (e.g., 'en', 'es', 'fr').
	 */
	language?: string;

	/**
	 * Notification preferences.
	 */
	notifications?: NotificationSettings;
}

/**
 * Valid storage types for resume data persistence.
 */
export type StoragePreference = "session" | "local" | "indexeddb" | "remote";

/**
 * Valid theme preferences.
 */
export type ThemePreference = "light" | "dark" | "system";

/**
 * Notification settings.
 */
export interface NotificationSettings {
	/**
	 * Whether notifications are enabled globally.
	 */
	enabled: boolean;

	/**
	 * Whether email notifications are enabled.
	 */
	email: boolean;

	/**
	 * Whether push notifications are enabled.
	 */
	push: boolean;
}

/**
 * Default user settings.
 * Used when no settings are persisted or when creating a new settings instance.
 */
export const DEFAULT_USER_SETTINGS: UserSettings = {
	storagePreference: "session",
	theme: "system",
	language: "en",
	notifications: {
		enabled: true,
		email: true,
		push: false,
	},
};

/**
 * Validates if a value is a valid StoragePreference.
 *
 * @param value - The value to validate
 * @returns true if the value is a valid StoragePreference
 */
export function isValidStoragePreference(
	value: unknown,
): value is StoragePreference {
	return (
		typeof value === "string" &&
		["session", "local", "indexeddb", "remote"].includes(value)
	);
}

/**
 * Determines whether a value is a ThemePreference.
 *
 * @returns `true` if the value is one of `"light"`, `"dark"`, or `"system"`, `false` otherwise.
 */
export function isValidThemePreference(
	value: unknown,
): value is ThemePreference {
	return (
		typeof value === "string" && ["light", "dark", "system"].includes(value)
	);
}