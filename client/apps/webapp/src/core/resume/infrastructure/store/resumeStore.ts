import { defineStore } from "pinia";
import { computed, getCurrentInstance, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";
import {
	RESUME_GENERATOR_KEY,
	RESUME_VALIDATOR_KEY,
} from "@/core/resume/infrastructure/di";
import { ResumeHttpClient } from "@/core/resume/infrastructure/http/ResumeHttpClient";
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
 * Gets the generator instance using Vue's provide/inject system.
 * Falls back to ResumeHttpClient if no generator is provided.
 *
 * @returns The generator instance
 */
function getGenerator(): ResumeGenerator {
	const instance = getCurrentInstance();
	if (instance?.appContext.provides[RESUME_GENERATOR_KEY as symbol]) {
		return instance.appContext.provides[
			RESUME_GENERATOR_KEY as symbol
		] as ResumeGenerator;
	}
	return new ResumeHttpClient();
}

/**
 * Resume store for managing resume state and validation.
 *
 * This store uses dependency injection to obtain the ResumeValidator and ResumeGenerator instances.
 * If no dependencies are provided via injection, it falls back to default implementations.
 *
 * @example
 * // In a component
 * const resumeStore = useResumeStore();
 * resumeStore.setResume(myResume);
 * console.log(resumeStore.isValid); // true or false
 *
 * // Generate PDF
 * const pdf = await resumeStore.generatePdf('en');
 */
export const useResumeStore = defineStore("resume", () => {
	// Get dependency instances
	const validator = getValidator();
	const generator = getGenerator();

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

	/**
	 * Generates a PDF from the current resume data.
	 *
	 * @param locale - Optional locale for the PDF generation (e.g., 'en', 'es')
	 * @returns A promise that resolves to a Blob containing the PDF
	 * @throws Error if no resume is available
	 */
	async function generatePdf(locale?: string): Promise<Blob> {
		if (!resume.value) {
			throw new Error("No resume data available to generate PDF");
		}

		try {
			setGenerating(true);
			setGenerationError(null);

			const pdfBlob = await generator.generatePdf(resume.value, locale);

			setGenerating(false);
			return pdfBlob;
		} catch (error) {
			setGenerating(false);

			// If it's a ProblemDetail error, store it
			if (error && typeof error === "object" && "title" in error) {
				setGenerationError(error as ProblemDetail);
			}

			throw error;
		}
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
		generatePdf,
	};
});
