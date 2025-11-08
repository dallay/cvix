import type { Resume } from "@/core/resume/domain/Resume.ts";

/**
 * Interface representing a Resume Generator.
 * This interface defines the contract for generating a PDF from resume data.
 */
export interface ResumeGenerator {
	/**
	 * Generates a PDF file from the provided resume data.
	 *
	 * @param {Resume} resumeData - The resume data to be used for generating the PDF.
	 * @param {string} [locale] - Optional locale to customize the generated PDF (e.g., for translations).
	 * @returns {Promise<Blob>} A promise that resolves to a Blob representing the generated PDF.
	 */
	generatePdf(resumeData: Resume, locale?: string): Promise<Blob>;
}
