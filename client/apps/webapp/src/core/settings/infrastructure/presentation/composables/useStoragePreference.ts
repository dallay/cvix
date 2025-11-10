import { computed } from "vue";
import type { StorageType } from "@/core/resume/domain/ResumeStorage.ts";
import { useSettingsStore } from "@/core/settings";

/**
 * Composable for managing storage preference in the UI.
 *
 * This composable provides a reactive interface for managing the user's
 * storage preference, integrating with both the settings store and the
 * resume storage system.
 *
 * @example
 * ```typescript
 * const {
 *   storagePreference,
 *   setStoragePreference,
 *   availableStorageTypes,
 *   isStorageAvailable
 * } = useStoragePreference();
 *
 * // Get current preference
 * console.log(storagePreference.value); // 'session' | 'local' | 'indexeddb'
 *
 * // Change preference
 * setStoragePreference('local');
 *
 * // Check if a storage type is available
 * if (isStorageAvailable('indexeddb')) {
 *   // IndexedDB is supported
 * }
 * ```
 */
export function useStoragePreference() {
	const settingsStore = useSettingsStore();

	/**
	 * Current storage preference (reactive).
	 * Synced with the settings store.
	 */
	const storagePreference = computed<StorageType>({
		get: () => settingsStore.storagePreference as StorageType,
		set: (value) => {
			settingsStore.updateStoragePreference(value).then((r) => r);
		},
	});

	/**
	 * Update the user's storage preference in the settings store.
	 *
	 * @param type - The desired storage type; one of "session", "local", "indexeddb", or "remote"
	 */
	async function setStoragePreference(type: StorageType): Promise<void> {
		await settingsStore.updateStoragePreference(type);
	}

	/**
	 * Determine whether a given storage type is available in the current environment.
	 *
	 * Remote storage is treated as available regardless of browser APIs; other types
	 * perform feature detection against the corresponding browser APIs.
	 *
	 * @param type - The storage type to check (`"session"`, `"local"`, `"indexeddb"`, or `"remote"`)
	 * @returns `true` if the specified storage type is available, `false` otherwise.
	 */
	function isStorageAvailable(type: StorageType): boolean {
		try {
			switch (type) {
				case "session":
					return typeof sessionStorage !== "undefined";
				case "local":
					return typeof localStorage !== "undefined";
				case "indexeddb":
					return typeof indexedDB !== "undefined";
				case "remote":
					// Remote storage depends on network/auth, not browser API
					return true;
				default:
					return false;
			}
		} catch {
			return false;
		}
	}

	/**
	 * List of all available storage types in the current browser.
	 */
	const availableStorageTypes = computed<StorageType[]>(() => {
		const types: StorageType[] = ["session", "local", "indexeddb", "remote"];
		return types.filter(isStorageAvailable);
	});

	return {
		storagePreference,
		setStoragePreference,
		availableStorageTypes,
		isStorageAvailable,
	};
}
