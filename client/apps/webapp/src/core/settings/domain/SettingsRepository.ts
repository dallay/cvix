import type { UserSettings } from "./UserSettings";

/**
 * Result type for settings operations.
 * Follows the Result pattern to handle success and error cases explicitly.
 */
export type SettingsResult<T> =
	| { success: true; data: T }
	| { success: false; error: Error };

/**
 * Repository interface for settings persistence.
 *
 * This interface defines the contract for settings storage implementations,
 * following the Repository pattern and Dependency Inversion Principle.
 * The domain defines the interface, and infrastructure provides implementations.
 *
 * @example
 * ```typescript
 * class LocalStorageSettingsRepository implements SettingsRepository {
 *   async load(): Promise<SettingsResult<UserSettings | null>> {
 *     // Implementation
 *   }
 *
 *   async save(settings: UserSettings): Promise<SettingsResult<void>> {
 *     // Implementation
 *   }
 * }
 * ```
 */
export interface SettingsRepository {
	/**
	 * Loads user settings from storage.
	 *
	 * @returns A promise resolving to a SettingsResult containing the settings or null if not found
	 */
	load(): Promise<SettingsResult<UserSettings | null>>;

	/**
	 * Saves user settings to storage.
	 *
	 * @param settings - The settings to save
	 * @returns A promise resolving to a SettingsResult indicating success or failure
	 */
	save(settings: UserSettings): Promise<SettingsResult<void>>;

	/**
	 * Clears all user settings from storage.
	 *
	 * @returns A promise resolving to a SettingsResult indicating success or failure
	 */
	clear(): Promise<SettingsResult<void>>;

	/**
	 * Returns the type of storage this repository uses.
	 *
	 * @returns A string identifying the storage type (e.g., 'localStorage', 'indexedDB')
	 */
	type(): string;
}
