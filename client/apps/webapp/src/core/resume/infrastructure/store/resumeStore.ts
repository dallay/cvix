import { defineStore } from "pinia";
import { computed, getCurrentInstance, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";
import { RESUME_VALIDATOR_KEY } from "@/core/resume/infrastructure/di";
import { JsonResumeValidator } from "@/core/resume/infrastructure/validation";
import type { ProblemDetail } from "@/shared/BaseHttpClient.ts";

/**
 * Gets the validator instance using Vue's provide/inject system.
 * Falls back to JsonResumeValidator if no validator is provided.
 *
 * @returns The validator instance
 */
function getValidator(): ResumeValidator {
	const instance = getCurrentInstance();
	if (instance?.appContext.provides[RESUME_VALIDATOR_KEY as symbol]) {
		return instance.appContext.provides[
			RESUME_VALIDATOR_KEY as symbol
		] as ResumeValidator;
	}
	return new JsonResumeValidator();
}

/**
 * Resume store for managing resume state and validation.
 *
 * This store uses dependency injection to obtain the ResumeValidator instance.
 * If no validator is provided via injection, it falls back to JsonResumeValidator.
 *
 * @example
 * // In a component
 * const resumeStore = useResumeStore();
 * resumeStore.setResume(myResume);
 * console.log(resumeStore.isValid); // true or false
 */
export const useResumeStore = defineStore("resume", () => {
	// Get the validator instance
	const validator = getValidator();

	// State
	const resume = ref<Resume | null>(null);
	const isGenerating = ref(false);
	const generationError = ref<ProblemDetail | null>(null);

	// Computed properties
	/**
	 * Indicates whether the current resume is valid according to JSON Resume Schema.
	 */
	const isValid = computed(() => {
		if (!resume.value) {
			return false;
		}
		return validator.validate(resume.value);
	});

	/**
	 * Returns validation errors if the resume is invalid.
	 * Note: Current implementation returns a boolean, could be extended to return detailed errors.
	 */
	const hasResume = computed(() => resume.value !== null);

	// Actions
	/**
	 * Sets the resume and validates it.
	 *
	 * @param newResume - The resume data to set
	 */
	function setResume(newResume: Resume) {
		resume.value = newResume;
	}

	/**
	 * Clears the current resume data.
	 */
	function clearResume() {
		resume.value = null;
		generationError.value = null;
	}

	/**
	 * Validates the current resume.
	 *
	 * @returns true if the resume is valid, false otherwise
	 */
	function validateResume(): boolean {
		if (!resume.value) {
			return false;
		}
		return validator.validate(resume.value);
	}

	/**
	 * Sets the generation state.
	 *
	 * @param generating - Whether resume generation is in progress
	 */
	function setGenerating(generating: boolean) {
		isGenerating.value = generating;
	}

	/**
	 * Sets a generation error.
	 *
	 * @param error - The error details
	 */
	function setGenerationError(error: ProblemDetail | null) {
		generationError.value = error;
	}

	return {
		// State
		resume,
		isGenerating,
		generationError,

		// Computed
		isValid,
		hasResume,

		// Actions
		setResume,
		clearResume,
		validateResume,
		setGenerating,
		setGenerationError,
	};
});
