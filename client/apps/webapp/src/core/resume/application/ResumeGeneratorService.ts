import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";

/**
 * Service responsible for generating resume PDFs.
 * It validates the resume data before delegating PDF generation to the ResumeGenerator.
 */
export class ResumeGeneratorService {
	constructor(
		private readonly resumeGenerator: ResumeGenerator,
		private readonly resumeValidator: ResumeValidator,
	) {}

	/**
	 * Generate a PDF for the given resume data using the specified template.
	 * @param templateId The ID of the template to use for PDF generation
	 * @param resumeData The resume data to be converted into a PDF
	 * @param locale Optional locale for localization (default is "en")
	 * @returns A Promise that resolves to a Blob representing the generated PDF
	 * @throws Error if the resume data is invalid
	 */
	async generateResumePdf(
		templateId: string,
		resumeData: Resume,
		locale = "en",
	): Promise<Blob> {
		const isValid = this.resumeValidator.validate(resumeData);
		if (!isValid) {
			throw new Error("Invalid resume data");
		}

		return this.resumeGenerator.generatePdf(templateId, resumeData, locale);
	}
}
