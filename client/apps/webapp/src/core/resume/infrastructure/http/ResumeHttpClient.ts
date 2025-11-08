import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import { BaseHttpClient } from "@/shared/BaseHttpClient.ts";

/**
 * HTTP client for resume generation API
 * Extends BaseHttpClient to leverage CSRF protection, cookie handling, and error handling
 */
export class ResumeHttpClient
	extends BaseHttpClient
	implements ResumeGenerator
{
	/**
	 * Generate a PDF resume from resume data
	 * @param resume Resume data
	 * @param locale Language locale (en/es)
	 * @returns Promise with the PDF blob
	 */
	async generatePdf(resume: Resume, locale?: string): Promise<Blob> {
		const response = await this.client.post<Blob>("/resumes", resume, {
			headers: {
				"Accept-Language": locale,
			},
			responseType: "blob",
		});
		return response.data;
	}
}

// Export singleton instance
export const resumeHttpClient = new ResumeHttpClient();
