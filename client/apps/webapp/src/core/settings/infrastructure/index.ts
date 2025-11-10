/**
 * Infrastructure layer barrel export for settings.
 *
 * This module provides the public API for the settings infrastructure layer,
 * including store, storage implementations, and DI configuration.
 */

export { SETTINGS_REPOSITORY_KEY } from "./di";
export { LocalStorageSettingsRepository } from "./storage";
export { useSettingsStore } from "./store";
