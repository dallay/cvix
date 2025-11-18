import { ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import type {
	ResumeValidator,
	ValidationError,
} from "@/core/resume/domain/ResumeValidator";
import { JsonResumeValidator } from "@/core/resume/infrastructure/validation/JsonResumeValidator";

// Re-export for convenience
export type { ValidationError };

/**
 * Grouped validation errors by resume section
 */
export interface GroupedErrors {
	[section: string]: ValidationError[];
}

/**
 * Result of JSON import operation
 */
export interface ImportResult {
	success: boolean;
	data?: Resume;
	errors?: ReadonlyArray<ValidationError>;
}

/**
 * Composable for handling JSON Resume import/export operations.
 *
 * This composable is intended for browser-only execution in a client-side Vue 3 SPA.
 * It provides validation, file I/O, and error formatting for JSON Resume files
 * following the JSON Resume schema v1.0.0.
 *
 * @example
 * ```typescript
 * const {
 *   importJson,
 *   exportJson,
 *   groupErrors,
 *   isValidating,
 *   validationErrors
 * } = useJsonResume();
 *
 * // Import from file
 * const result = await importJson(file);
 * if (result.success) {
 *   // Hydrate form with result.data
 * }
 *
 * // Export to file
 * exportJson(resumeData, 'my-resume.json');
 * ```
 */
export function useJsonResume() {
	const validator = new JsonResumeValidator();
	const isValidating = ref(false);
	const validationErrors = ref<ReadonlyArray<ValidationError>>([]);

	/**
	 * Populates validationErrors ref from validator's accumulated errors
	 */
	function populateValidationErrors(validator: ResumeValidator): void {
		const errors = validator.getErrors();
		if (errors.length > 0) {
			// Convert readonly array to mutable array before assignment
			validationErrors.value = [...errors];
		} else {
			// If validator reports no errors but validation failed, add generic error
			validationErrors.value = [
				{
					path: "",
					message: "Validation failed. Please check your data.",
					section: "General",
				},
			];
		}
	}

	/**
	 * Groups validation errors by resume section
	 */
	function groupErrors(errors: ReadonlyArray<ValidationError>): GroupedErrors {
		return errors.reduce((acc, error) => {
			const section = error.section || "General";
			acc[section] ??= [];
			acc[section].push(error);
			return acc;
		}, {} as GroupedErrors);
	}

	/**
	 * Imports and validates a JSON Resume from a File object
	 *
	 * @param file - File object from file input
	 * @returns Promise with import result (success, data, or errors)
	 */
	async function importJson(file: File): Promise<ImportResult> {
		isValidating.value = true;
		validationErrors.value = [];

		try {
			const text = await file.text();
			let data: unknown;

			try {
				data = JSON.parse(text);
			} catch {
				validationErrors.value = [
					{
						path: "",
						message: "Invalid JSON format. Please check your file syntax.",
						section: "General",
					},
				];
				return { success: false, errors: validationErrors.value };
			}

			// Validate against JSON Resume schema
			const isValid = validator.validate(data as Resume);

			if (!isValid) {
				populateValidationErrors(validator);
				return { success: false, errors: validationErrors.value };
			}

			// Type-safe after validation
			const resume = data as Resume;
			return { success: true, data: resume };
		} catch (error) {
			validationErrors.value = [
				{
					path: "",
					message:
						error instanceof Error
							? error.message
							: "Unknown error occurred while reading file",
					section: "General",
				},
			];
			return { success: false, errors: validationErrors.value };
		} finally {
			isValidating.value = false;
		}
	}

	/**
	 * Validates current resume data and exports as JSON file
	 *
	 * @param data - Resume data to export
	 * @param filename - Optional filename (default: resume.json)
	 * @returns true if export successful, false if validation fails
	 */
	function exportJson(data: Resume, filename = "resume.json"): boolean {
		isValidating.value = true;
		validationErrors.value = [];

		try {
			// Validate before export
			const isValid = validator.validate(data);

			if (!isValid) {
				populateValidationErrors(validator);
				return false;
			}

			// Create and download JSON file
			const json = JSON.stringify(data, null, 2);
			const blob = new Blob([json], { type: "application/json" });
			const url = URL.createObjectURL(blob);
			const link = document.createElement("a");
			link.href = url;
			link.download = filename;
			link.click();
			URL.revokeObjectURL(url);

			return true;
		} catch (error) {
			validationErrors.value = [
				{
					path: "",
					message:
						error instanceof Error
							? error.message
							: "Unknown error occurred during export",
					section: "General",
				},
			];
			return false;
		} finally {
			isValidating.value = false;
		}
	}

	/**
	 * Validates resume data without importing or exporting
	 *
	 * @param data - Resume data to validate
	 * @returns Array of validation errors (empty if valid)
	 */
	function validateResume(data: Resume): ReadonlyArray<ValidationError> {
		const errors = validator.validate(data) ? [] : validator.getErrors();
		validationErrors.value = [...errors];
		return validationErrors.value;
	}

	return {
		// State
		isValidating,
		validationErrors,

		// Methods
		importJson,
		exportJson,
		validateResume,
		groupErrors,
	};
}
