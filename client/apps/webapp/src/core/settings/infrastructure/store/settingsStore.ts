import { defineStore } from "pinia";
import { computed, getCurrentInstance, ref } from "vue";
import type { StoragePreference, UserSettings } from "../../domain";
import { DEFAULT_USER_SETTINGS } from "../../domain";
import type { SettingsRepository } from "../../domain/SettingsRepository";
import { SETTINGS_REPOSITORY_KEY } from "../di";
import { LocalStorageSettingsRepository } from "../storage";

/**
 * Gets the settings repository instance using Vue's provide/inject system.
 * Falls back to LocalStorageSettingsRepository if no repository is provided.
 *
 * @returns The settings repository instance
 */
function getSettingsRepository(): SettingsRepository {
	const instance = getCurrentInstance();
	if (instance) {
		const injected = instance.appContext.config.globalProperties.$settingsRepo;
		if (injected) return injected;

		// Try inject
		try {
			const repo =
				instance.appContext.app._context.provides[
					SETTINGS_REPOSITORY_KEY as symbol
				];
			if (repo) return repo;
		} catch {
			// Inject failed, use fallback
		}
	}

	// Fallback to default implementation
	return new LocalStorageSettingsRepository();
}

/**
 * Pinia store for managing user settings.
 *
 * This store provides a reactive interface for managing user settings,
 * including loading, saving, and updating settings. It uses the Repository
 * pattern to abstract the persistence mechanism.
 *
 * @example
 * ```typescript
 * const settingsStore = useSettingsStore();
 *
 * // Load settings
 * await settingsStore.loadSettings();
 *
 * // Update storage preference
 * await settingsStore.updateStoragePreference('local');
 *
 * // Access current settings
 * console.log(settingsStore.settings.storagePreference);
 * ```
 */
export const useSettingsStore = defineStore("settings", () => {
	// Dependencies
	const repository = getSettingsRepository();

	// State
	const settings = ref<UserSettings>({ ...DEFAULT_USER_SETTINGS });
	const isLoading = ref(false);
	const isSaving = ref(false);
	const error = ref<Error | null>(null);

	// Computed
	/**
	 * The current storage preference.
	 */
	const storagePreference = computed(() => settings.value.storagePreference);

	/**
	 * Whether the store has been initialized (settings loaded).
	 */
	const isInitialized = ref(false);

	// Actions

	/**
	 * Loads user settings from the repository.
	 *
	 * @returns Promise resolving when settings are loaded
	 */
	async function loadSettings(): Promise<void> {
		if (isLoading.value) return;

		isLoading.value = true;
		error.value = null;

		try {
			const result = await repository.load();

			if (result.success) {
				// Use loaded settings or defaults if null
				settings.value = result.data ?? { ...DEFAULT_USER_SETTINGS };
				isInitialized.value = true;
			} else {
				throw result.error;
			}
		} catch (err) {
			error.value =
				err instanceof Error ? err : new Error("Failed to load settings");
			// On error, use defaults
			settings.value = { ...DEFAULT_USER_SETTINGS };
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Saves the current settings to the repository.
	 *
	 * @returns Promise resolving when settings are saved
	 */
	async function saveSettings(): Promise<void> {
		if (isSaving.value) return;

		isSaving.value = true;
		error.value = null;

		try {
			const result = await repository.save(settings.value);

			if (!result.success) {
				throw result.error;
			}
		} catch (err) {
			error.value =
				err instanceof Error ? err : new Error("Failed to save settings");
			throw error.value;
		} finally {
			isSaving.value = false;
		}
	}

	/**
	 * Updates the storage preference and saves to repository.
	 *
	 * @param newPreference - The new storage preference
	 * @returns Promise resolving when the preference is updated and saved
	 */
	async function updateStoragePreference(
		newPreference: StoragePreference,
	): Promise<void> {
		// Update local state
		settings.value = {
			...settings.value,
			storagePreference: newPreference,
		};

		// Persist to repository
		await saveSettings();
	}

	/**
	 * Updates multiple settings at once.
	 *
	 * @param updates - Partial settings to update
	 * @returns Promise resolving when settings are updated and saved
	 */
	async function updateSettings(updates: Partial<UserSettings>): Promise<void> {
		// Update local state
		settings.value = {
			...settings.value,
			...updates,
		};

		// Persist to repository
		await saveSettings();
	}

	/**
	 * Resets settings to defaults and clears from repository.
	 *
	 * @returns Promise resolving when settings are reset
	 */
	async function resetSettings(): Promise<void> {
		error.value = null;

		try {
			// Clear from repository
			const result = await repository.clear();

			if (!result.success) {
				throw result.error;
			}

			// Reset to defaults
			settings.value = { ...DEFAULT_USER_SETTINGS };
		} catch (err) {
			error.value =
				err instanceof Error ? err : new Error("Failed to reset settings");
			throw error.value;
		}
	}

	return {
		// State
		settings,
		isLoading,
		isSaving,
		error,
		isInitialized,

		// Computed
		storagePreference,

		// Actions
		loadSettings,
		saveSettings,
		updateStoragePreference,
		updateSettings,
		resetSettings,
	};
});
