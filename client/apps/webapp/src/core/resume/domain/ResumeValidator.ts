import type { Resume } from "@/core/resume/domain/Resume.ts";

/**
 * Structured validation error with field path and message.
 * Represents a single validation issue detected during resume validation.
 */
export interface ValidationError {
	path: string;
	message: string;
	section?: string;
}

/**
 * Interface representing a Resume Validator.
 * This interface defines the contract for validating a resume.
 *
 * Lifecycle:
 * - The `validate` method MUST reset/clear the internal error collection at the start.
 * - The `validate` method performs validation and repopulates the internal error collection.
 * - The `getErrors` method returns the current snapshot of validation errors without side effects.
 *
 * Expected Behavior for Consumers:
 * - Call `validate` before accessing `getErrors` to ensure errors are up-to-date.
 * - `getErrors` provides a read-only view of the validation state.
 */
export interface ResumeValidator {
	/**
	 * Validates the provided resume data.
	 *
	 * @param {Resume} resume - The resume data to be validated.
	 * @returns {boolean} A boolean indicating whether the resume is valid.
	 *
	 * Note: This method MUST reset the internal error collection before validation.
	 */
	validate(resume: Resume): boolean;

	/**
	 * Returns the validation errors from the last validation.
	 *
	 * @returns {ValidationError[]} Array of validation errors.
	 *
	 * Note: This method MUST NOT have side effects and should return the current snapshot of errors.
	 */
	getErrors(): ValidationError[];
}
