import type {
	SettingsRepository,
	SettingsResult,
	UserSettings,
} from "../../domain";
import { DEFAULT_USER_SETTINGS } from "../../domain";

/**
 * Storage key for user settings in localStorage.
 * Exported to ensure consistency across all settings storage operations.
 */
export const USER_SETTINGS_STORAGE_KEY = "cvix:user-settings";

/**
 * LocalStorage implementation of the SettingsRepository.
 *
 * This adapter persists user settings to the browser's localStorage.
 * It implements the SettingsRepository interface defined in the domain layer,
 * following the Adapter pattern and Dependency Inversion Principle.
 *
 * @example
 * ```typescript
 * const repository = new LocalStorageSettingsRepository();
 *
 * // Load settings
 * const result = await repository.load();
 * if (result.success && result.data) {
 *   console.log('Storage preference:', result.data.storagePreference);
 * }
 *
 * // Save settings
 * await repository.save({ storagePreference: 'local' });
 * ```
 */
export class LocalStorageSettingsRepository implements SettingsRepository {
	/**
	 * Loads user settings from localStorage.
	 *
	 * @returns A promise resolving to a SettingsResult containing the settings or null if not found
	 */
	async load(): Promise<SettingsResult<UserSettings | null>> {
		try {
			const stored = localStorage.getItem(USER_SETTINGS_STORAGE_KEY);

			if (!stored) {
				return { success: true, data: null };
			}

			const parsed = JSON.parse(stored);

			// Merge with defaults to ensure all properties exist
			const settings: UserSettings = {
				...DEFAULT_USER_SETTINGS,
				...parsed,
			};

			return { success: true, data: settings };
		} catch (error) {
			return {
				success: false,
				error:
					error instanceof Error ? error : new Error("Failed to load settings"),
			};
		}
	}

	/**
	 * Saves user settings to localStorage.
	 *
	 * @param settings - The settings to save
	 * @returns A promise resolving to a SettingsResult indicating success or failure
	 */
	async save(settings: UserSettings): Promise<SettingsResult<void>> {
		try {
			const serialized = JSON.stringify(settings);
			localStorage.setItem(USER_SETTINGS_STORAGE_KEY, serialized);
			return { success: true, data: undefined };
		} catch (error) {
			return {
				success: false,
				error:
					error instanceof Error ? error : new Error("Failed to save settings"),
			};
		}
	}

	/**
	 * Clears all user settings from localStorage.
	 *
	 * @returns A promise resolving to a SettingsResult indicating success or failure
	 */
	async clear(): Promise<SettingsResult<void>> {
		try {
			localStorage.removeItem(USER_SETTINGS_STORAGE_KEY);
			return { success: true, data: undefined };
		} catch (error) {
			return {
				success: false,
				error:
					error instanceof Error
						? error
						: new Error("Failed to clear settings"),
			};
		}
	}

	/**
	 * Returns the type of storage this repository uses.
	 *
	 * @returns A string identifying the storage type
	 */
	type(): string {
		return "localStorage";
	}
}
