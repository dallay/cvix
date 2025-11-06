import { deepmerge } from "@loomify/utilities";
import type { Ref } from "vue";
import { onMounted, onUnmounted, watch } from "vue";
import type { Resume } from "../types/resume";
import { resumeSchema } from "../validation/resumeSchema";

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
			const parsed = JSON.parse(savedData);
			// Zod schema validation
			const validated = resumeSchema.safeParse(parsed);
			if (!validated.success) {
				console.warn(
					"Invalid resume data in session, ignoring:",
					validated.error,
				);
				return null;
			}
			// Patch: Map skills to include 'category' property for compatibility
			let resume: Resume = validated.data as Resume;
			if (resume.skills) {
				type SkillInput = Omit<
					import("../types/resume").SkillCategory,
					"category"
				> & { category?: string; name?: string };
				resume = {
					...resume,
					skills: resume.skills.map(
						(s: SkillInput): import("../types/resume").SkillCategory => ({
							...s,
							category: s.category || s.name || "",
						}),
					),
				};
			}
			return resume;
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
			// Deep merge to preserve nested structures
			resumeData.value = deepmerge.all([resumeData.value, savedData]) as Resume;
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
