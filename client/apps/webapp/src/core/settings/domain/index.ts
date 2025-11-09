/**
 * Domain layer barrel export for settings.
 *
 * This module provides the public API for the settings domain layer,
 * including entities, value objects, and repository interfaces.
 */

export type {
	SettingsRepository,
	SettingsResult,
} from "./SettingsRepository";
export {
	DEFAULT_USER_SETTINGS,
	isValidStoragePreference,
	isValidThemePreference,
	type NotificationSettings,
	type StoragePreference,
	type ThemePreference,
	type UserSettings,
} from "./UserSettings";
