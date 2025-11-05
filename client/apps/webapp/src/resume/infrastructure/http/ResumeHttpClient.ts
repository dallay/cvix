import type { AxiosError } from "axios";
import { BaseHttpClient } from "@/shared/BaseHttpClient";
import type { Resume } from "../../types/resume";

/**
 * HTTP client for resume generation API
 * Extends BaseHttpClient to leverage CSRF protection, cookie handling, and error handling
 */
export class ResumeHttpClient extends BaseHttpClient {
	/**
	 * Generate a PDF resume from resume data
	 * @param resume Resume data
	 * @param locale Language locale (en/es)
	 * @returns Promise with the PDF blob
	 */
	async generateResumePdf(resume: Resume, locale = "en"): Promise<Blob> {
		try {
			const response = await this.client.post<Blob>("/resumes", resume, {
				headers: {
					"Accept-Language": locale,
				},
				responseType: "blob",
			});
			return response.data;
		} catch (error) {
			// Re-throw with proper typing
			throw error as AxiosError;
		}
	}
}

// Export singleton instance
export const resumeHttpClient = new ResumeHttpClient();
