import type { Resume } from "@/core/resume/domain/Resume.ts";

/**
 * Structured validation error with field path and message
 */
export interface ValidationError {
	path: string;
	message: string;
	section?: string;
}

/**
 * Interface representing a Resume Validator.
 * This interface defines the contract for validating a resume.
 */
export interface ResumeValidator {
	/**
	 * Validates the provided resume data.
	 *
	 * @param {Resume} resume - The resume data to be validated.
	 * @returns {boolean} A boolean indicating whether the resume is valid.
	 */
	validate(resume: Resume): boolean;

	/**
	 * Returns the validation errors from the last validation.
	 *
	 * @returns {ValidationError[]} Array of validation errors
	 */
	getErrors(): ValidationError[];
}
