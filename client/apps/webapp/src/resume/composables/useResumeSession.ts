import type { Ref } from "vue";
import { onMounted } from "vue";
import type { Resume, SkillCategory } from "../types/resume";
import { resumeSchema } from "../validation/resumeSchema";

const SESSION_STORAGE_KEY = "resume_form_data";

/**
 * Composable for persisting resume form data to sessionStorage
 *
 * Features:
 * - Manual save: Only saves when explicitly called
 * - Restores data on page load
 * - Clears data on successful generation
 * - Data persists only within the current browser session
 */
export function useResumeSession(resumeData: Ref<Resume>) {
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
			const resume: Resume = validated.data as Resume;
			if (resume.skills) {
				type SkillInput = Omit<SkillCategory, "category"> & {
					category?: string;
					name?: string;
				};
				return {
					...resume,
					skills: resume.skills.map(
						(s: SkillInput): SkillCategory => ({
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
	 * Restore data on component mount
	 */
	onMounted(() => {
		const savedData = loadFromSession();
		if (savedData) {
			// Direct assignment is more performant than deep merge
			// The store is already initialized with default values,
			// so we can safely replace with validated saved data
			resumeData.value = savedData;
		}
	});

	return {
		saveToSession,
		loadFromSession,
		clearSession,
	};
}
