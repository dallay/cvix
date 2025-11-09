import { defineStore } from "pinia";
import { computed, getCurrentInstance, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import type {
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";
import {
	RESUME_GENERATOR_KEY,
	RESUME_STORAGE_KEY,
	RESUME_VALIDATOR_KEY,
} from "@/core/resume/infrastructure/di";
import { ResumeHttpClient } from "@/core/resume/infrastructure/http/ResumeHttpClient";
import { SessionStorageResumeStorage } from "@/core/resume/infrastructure/storage";
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
 * Gets the storage instance using Vue's provide/inject system.
 * Falls back to SessionStorageResumeStorage if no storage is provided.
 *
 * @returns The storage instance
 */
function getStorage(): ResumeStorage {
	const instance = getCurrentInstance();
	if (instance?.appContext.provides[RESUME_STORAGE_KEY as symbol]) {
		return instance.appContext.provides[
			RESUME_STORAGE_KEY as symbol
		] as ResumeStorage;
	}
	return new SessionStorageResumeStorage();
}

/**
 * Resume store for managing resume state, validation, and persistence.
 *
 * This store uses dependency injection to obtain the ResumeValidator, ResumeGenerator,
 * and ResumeStorage instances. If no dependencies are provided via injection, it falls
 * back to default implementations.
 *
 * @example
 * // In a component
 * const resumeStore = useResumeStore();
 * resumeStore.setResume(myResume);
 * console.log(resumeStore.isValid); // true or false
 *
 * // Save to storage
 * await resumeStore.saveToStorage();
 *
 * // Load from storage
 * await resumeStore.loadFromStorage();
 *
 * // Generate PDF
 * const pdf = await resumeStore.generatePdf('en');
 */
export const useResumeStore = defineStore("resume", () => {
	// Get dependency instances
	const validator = getValidator();
	const generator = getGenerator();
	const initialStorage = getStorage();

	// State - make storage mutable to allow strategy switching
	const currentStorage = ref<ResumeStorage>(initialStorage);
	const resume = ref<Resume | null>(null);
	const isGenerating = ref(false);
	const generationError = ref<ProblemDetail | null>(null);
	const isSaving = ref(false);
	const isLoading = ref(false);
	const storageError = ref<Error | null>(null);
	const currentStorageType = ref<StorageType>(initialStorage.type());

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

	/**
	 * Saves the current resume to the configured storage.
	 *
	 * @returns Promise resolving when save is complete
	 * @throws Error if no resume is available or storage operation fails
	 */
	async function saveToStorage(): Promise<void> {
		if (!resume.value) {
			throw new Error("No resume data available to save");
		}

		try {
			isSaving.value = true;
			storageError.value = null;

			await currentStorage.value.save(resume.value);

			isSaving.value = false;
		} catch (error) {
			isSaving.value = false;
			storageError.value =
				error instanceof Error ? error : new Error("Unknown storage error");
			throw error;
		}
	}

	/**
	 * Loads the resume from the configured storage.
	 *
	 * @returns Promise resolving when load is complete
	 */
	async function loadFromStorage(): Promise<void> {
		try {
			isLoading.value = true;
			storageError.value = null;

			console.log(
				"[resumeStore] Loading from storage:",
				currentStorage.value.type(),
			);
			const result = await currentStorage.value.load();
			console.log("[resumeStore] Load result:", result);

			if (result.data) {
				resume.value = result.data;
				console.log("[resumeStore] Resume set to:", resume.value);
			} else {
				console.log("[resumeStore] No data found in storage");
			}

			isLoading.value = false;
		} catch (error) {
			isLoading.value = false;
			storageError.value =
				error instanceof Error ? error : new Error("Unknown storage error");
			console.error("[resumeStore] Error loading from storage:", error);
			throw error;
		}
	}

	/**
	 * Clears the resume from the configured storage.
	 *
	 * @returns Promise resolving when clear is complete
	 */
	async function clearStorage(): Promise<void> {
		try {
			storageError.value = null;
			await currentStorage.value.clear();
			clearResume();
		} catch (error) {
			storageError.value =
				error instanceof Error ? error : new Error("Unknown storage error");
			throw error;
		}
	}

	/**
	 * Changes the storage strategy and optionally migrates data.
	 *
	 * @param newStorage - The new storage implementation to use
	 * @param migrateData - Whether to migrate existing data to the new storage
	 * @returns Promise resolving when strategy change (and optional migration) is complete
	 */
	async function changeStorageStrategy(
		newStorage: ResumeStorage,
		migrateData = false,
	): Promise<void> {
		try {
			storageError.value = null;

			// If migration is requested and there's data, save it to the new storage
			if (migrateData && resume.value) {
				await newStorage.save(resume.value);
			}

			// Update the storage reference to use the new strategy
			currentStorage.value = newStorage;
			currentStorageType.value = newStorage.type();
		} catch (error) {
			storageError.value =
				error instanceof Error ? error : new Error("Failed to change storage");
			throw error;
		}
	}

	return {
		// State
		resume,
		isGenerating,
		generationError,
		isSaving,
		isLoading,
		storageError,
		currentStorageType,

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
		saveToStorage,
		loadFromStorage,
		clearStorage,
		changeStorageStrategy,
	};
});
