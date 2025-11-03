import type { Ref } from "vue";
import { onMounted, onUnmounted, watch } from "vue";
import type { Resume } from "../types/resume";

const SESSION_STORAGE_KEY = "resume_form_data";
const AUTO_SAVE_DEBOUNCE_MS = 1000;

/**
 * Composable for persisting resume form data to sessionStorage
 *
 * Features:
 * - Auto-saves form data on changes (debounced)
 * - Restores data on page load
 * - Clears data on successful generation
 * - Data persists only within the current browser session
 */
export function useResumeSession(resumeData: Ref<Resume>) {
	let saveTimeout: ReturnType<typeof setTimeout> | null = null;

	/**
	 * Save resume data to sessionStorage
	 */
	const saveToSession = () => {
		try {
			const dataToSave = JSON.stringify(resumeData.value);
			sessionStorage.setItem(SESSION_STORAGE_KEY, dataToSave);
		} catch (error) {
			console.warn("Failed to save resume data to session:", error);
		}
	};

	/**
	 * Load resume data from sessionStorage
	 * @returns The saved resume data, or null if not found or invalid
	 */
	const loadFromSession = (): Resume | null => {
		try {
			const savedData = sessionStorage.getItem(SESSION_STORAGE_KEY);
			if (!savedData) {
				return null;
			}
			return JSON.parse(savedData) as Resume;
		} catch (error) {
			console.warn("Failed to load resume data from session:", error);
			return null;
		}
	};

	/**
	 * Clear saved resume data from sessionStorage
	 */
	const clearSession = () => {
		try {
			sessionStorage.removeItem(SESSION_STORAGE_KEY);
		} catch (error) {
			console.warn("Failed to clear session data:", error);
		}
	};

	/**
	 * Debounced save handler
	 */
	const debouncedSave = () => {
		if (saveTimeout) {
			clearTimeout(saveTimeout);
		}
		saveTimeout = setTimeout(() => {
			saveToSession();
		}, AUTO_SAVE_DEBOUNCE_MS);
	};

	/**
	 * Restore data on component mount
	 */
	onMounted(() => {
		const savedData = loadFromSession();
		if (savedData) {
			// Merge saved data with current data to preserve any defaults
			resumeData.value = {
				...resumeData.value,
				...savedData,
			};
		}
	});

	/**
	 * Watch for changes and auto-save
	 */
	const stopWatching = watch(
		resumeData,
		() => {
			debouncedSave();
		},
		{ deep: true },
	);

	/**
	 * Clean up on unmount
	 */
	onUnmounted(() => {
		if (saveTimeout) {
			clearTimeout(saveTimeout);
		}
		stopWatching();
	});

	return {
		saveToSession,
		loadFromSession,
		clearSession,
	};
}
