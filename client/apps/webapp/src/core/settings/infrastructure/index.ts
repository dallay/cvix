/**
 * Infrastructure layer barrel export for settings.
 *
 * This module provides the public API for the settings infrastructure layer,
 * including store, storage implementations, and DI configuration.
 */

export { SETTINGS_REPOSITORY_KEY } from "./di";
export {
	getUserStoragePreference,
	LocalStorageSettingsRepository,
	USER_SETTINGS_STORAGE_KEY,
} from "./storage";
export { useSettingsStore } from "./store";
