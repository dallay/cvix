import type { Resume } from "@/resume/types/resume";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

/**
 * API client for resume generation endpoints.
 */
export const resumeApi = {
	/**
	 * Generate a PDF resume from resume data.
	 * @param resumeData The complete resume data
	 * @param locale The locale for the resume (e.g., "en", "es")
	 * @returns The PDF as a Blob
	 */
	async generateResume(resumeData: Resume, locale = "en"): Promise<Blob> {
		const response = await fetch(`${API_BASE_URL}/resumes`, {
			method: "POST",
			headers: {
				"Content-Type": "application/vnd.api.v1+json",
				"Accept-Language": locale,
			},
			body: JSON.stringify(resumeData),
		});

		if (!response.ok) {
			const error = await response.json().catch(() => ({
				error: { message: "Unknown error occurred" },
			}));
			throw new Error(error.error?.message || `HTTP ${response.status}`);
		}

		return response.blob();
	},
};
