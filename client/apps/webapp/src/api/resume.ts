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
			credentials: "include",
			body: JSON.stringify(resumeData),
		});

		if (!response.ok) {
			const problem = (await response.json().catch(() => null)) as {
				detail?: string;
				title?: string;
				error?: { message?: string };
			} | null;
			const detail =
				problem?.detail ??
				problem?.title ??
				problem?.error?.message ??
				`HTTP ${response.status}`;
			throw new Error(detail);
		}

		// Validate Content-Type
		const contentType = response.headers.get("content-type");
		if (
			!contentType ||
			!contentType.toLowerCase().includes("application/pdf")
		) {
			throw new Error(
				`Expected PDF but received ${contentType ?? "<none>"} (HTTP ${response.status})`,
			);
		}

		return response.blob();
	},
};
