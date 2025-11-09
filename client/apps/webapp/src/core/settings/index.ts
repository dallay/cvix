/**
 * Settings feature barrel export.
 *
 * This module provides the public API for the entire settings feature,
 * exposing domain, application, infrastructure, and presentation layers.
 */

// Application layer
export { updateStoragePreference } from "./application";
// Domain layer
export type {
	NotificationSettings,
	SettingsRepository,
	SettingsResult,
	StoragePreference,
	ThemePreference,
	UserSettings,
} from "./domain";
export {
	DEFAULT_USER_SETTINGS,
	isValidStoragePreference,
	isValidThemePreference,
} from "./domain";

// Infrastructure layer
export {
	LocalStorageSettingsRepository,
	SETTINGS_REPOSITORY_KEY,
	useSettingsStore,
} from "./infrastructure";

// Presentation layer
export {
	StorageSelector,
	useStoragePreference,
} from "./infrastructure/presentation";
