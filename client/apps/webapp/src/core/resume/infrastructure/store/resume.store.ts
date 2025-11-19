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
 * Resolves a ResumeGenerator from Vue dependency injection, falling back to ResumeHttpClient when none is provided.
 *
 * @returns The resolved ResumeGenerator instance
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
 * resume.store.setResume(myResume);
 *
 * // Save to storage
 * await resume.store.saveToStorage();
 *
 * // Load from storage
 * await resume.store.loadFromStorage();
 *
 * // Generate PDF
 * const pdf = await resume.store.generatePdf('en');
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
	 * Replace the store's current resume with the provided `newResume`.
	 *
	 * @param newResume - Resume object to set as the current resume
	 */
	function setResume(newResume: Resume) {
		resume.value = newResume;
	}

	/**
	 * Clears the stored resume and any recorded generation error.
	 */
	function clearResume() {
		resume.value = null;
		generationError.value = null;
	}

	/**
	 * Determine whether the store's current resume passes validation.
	 *
	 * @returns `true` if the current resume is valid, `false` otherwise
	 */
	function validateResume(): boolean {
		if (!resume.value) {
			return false;
		}
		return validator.validate(resume.value);
	}

	/**
	 * Set the store's generation-in-progress flag.
	 *
	 * @param generating - `true` to mark resume generation as in progress, `false` to mark it stopped
	 */
	function setGenerating(generating: boolean) {
		isGenerating.value = generating;
	}

	/**
	 * Updates the current PDF generation error state.
	 *
	 * @param error - A ProblemDetail describing the generation failure, or `null` to clear the error
	 */
	function setGenerationError(error: ProblemDetail | null) {
		generationError.value = error;
	}

	/**
	 * Generate a PDF from the currently stored resume.
	 *
	 * @param locale - Optional locale to use for generation (e.g., "en", "es")
	 * @returns The generated PDF as a `Blob`
	 * @throws Error if no resume is available to generate
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
	 * Save the current resume to the configured storage.
	 *
	 * @throws Error if no resume is available to save.
	 * @throws Error if the storage operation fails.
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
	 * Load the resume from the currently configured storage and update the store's resume and loading/error state.
	 *
	 * @throws The original error thrown by the storage backend if the load operation fails.
	 */
	async function loadFromStorage(): Promise<void> {
		try {
			isLoading.value = true;
			storageError.value = null;

			const result = await currentStorage.value.load();

			if (result.data) {
				resume.value = result.data;
			}

			isLoading.value = false;
		} catch (error) {
			isLoading.value = false;
			storageError.value =
				error instanceof Error ? error : new Error("Unknown storage error");
			console.error("[resume.store] Error loading from storage:", error);
			throw error;
		}
	}

	/**
	 * Remove the stored resume from the current storage and clear the in-memory resume.
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
	 * Switches the active resume storage implementation and optionally migrates the current resume to it.
	 *
	 * @param migrateData - If `true`, saves the current resume to `newStorage` before switching
	 * @throws Propagates any error encountered while migrating data or updating the storage strategy
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

	/**
	 * Import a resume from an external source and set it as the current resume.
	 * This is a convenience method that wraps setResume for semantic clarity when importing data.
	 *
	 * @param importedResume - Resume object to import
	 * @throws Error if the imported resume fails validation
	 */
	function importResume(importedResume: Resume): void {
		if (!validator.validate(importedResume)) {
			throw new Error(
				"Invalid resume data: imported resume does not conform to JSON Resume Schema",
			);
		}
		setResume(importedResume);
	}

	/**
	 * Export the current resume for external use.
	 * This is a convenience method that returns the current resume for semantic clarity when exporting data.
	 *
	 * @returns The current resume or null if no resume exists
	 */
	function exportResume(): Resume | null {
		return resume.value;
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
		importResume,
		exportResume,
	};
});
