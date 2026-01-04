/**
 * Infrastructure storage layer exports for settings.
 *
 * This module provides concrete implementations of the SettingsRepository interface.
 */

export { getUserStoragePreference } from "./getUserStoragePreference";
export {
	LocalStorageSettingsRepository,
	USER_SETTINGS_STORAGE_KEY,
} from "./LocalStorageSettingsRepository";
