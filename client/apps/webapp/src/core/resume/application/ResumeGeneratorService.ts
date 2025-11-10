import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";

export class ResumeGeneratorService {
	constructor(
		private readonly resumeGenerator: ResumeGenerator,
		private readonly resumeValidator: ResumeValidator,
	) {}

	async generateResumePdf(resumeData: Resume, locale = "en"): Promise<Blob> {
		const isValid = this.resumeValidator.validate(resumeData);
		if (!isValid) {
			throw new Error("Invalid resume data");
		}

		// Generate PDF using the provided ResumeGenerator
		return this.resumeGenerator.generatePdf(resumeData, locale);
	}
}
