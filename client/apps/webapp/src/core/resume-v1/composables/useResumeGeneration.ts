import type { AxiosError } from "axios";
import { ref } from "vue";
import { resumeHttpClient } from "../infrastructure";
import type { ProblemDetail, Resume } from "../types/resume.ts";

/**
 * Composable for resume generation functionality.
 * Handles API calls to generate PDF resumes using BaseHttpClient infrastructure.
 */
export function useResumeGeneration() {
	const isGenerating = ref(false);
	const error = ref<ProblemDetail | null>(null);
	const progress = ref(0);

	/**
	 * Generates a PDF resume from resume data.
	 * Uses BaseHttpClient which handles:
	 * - HttpOnly+Secure+SameSite cookies automatically
	 * - CSRF token management (XSRF-TOKEN cookie and X-XSRF-TOKEN header)
	 * - Automatic retry on CSRF token expiration
	 * - RFC 7807 Problem Details error responses
	 *
	 * @param resume Resume data
	 * @param locale Language locale (en/es)
	 * @returns Promise that resolves to true on success, false on failure
	 */
	async function generateResume(
		resume: Resume,
		locale = "en",
	): Promise<boolean> {
		isGenerating.value = true;
		error.value = null;
		progress.value = 0;

		try {
			progress.value = 25;

			// Use BaseHttpClient which handles cookies, CSRF, and auth automatically
			const blob = await resumeHttpClient.generateResumePdf(resume, locale);

			progress.value = 75;

			// Download PDF
			downloadPdf(blob, `resume-${new Date().toISOString().split("T")[0]}.pdf`);

			progress.value = 100;
			return true;
		} catch (err) {
			// Handle Axios errors with RFC 7807 Problem Details
			const axiosError = err as AxiosError;

			if (axiosError.response?.data) {
				// Server returned an error response (RFC 7807 Problem Details)
				const problemDetail = axiosError.response.data as ProblemDetail;
				error.value = {
					...problemDetail,
					status: axiosError.response.status,
					title: problemDetail.title || "Error",
					detail: problemDetail.detail || "An error occurred",
					timestamp: problemDetail.timestamp || new Date().toISOString(),
				};
			} else {
				// Network or client-side error
				error.value = {
					status: 0,
					title: "Network Error",
					detail: axiosError.message || "Network error occurred",
					timestamp: new Date().toISOString(),
					errorCategory: "NETWORK_ERROR",
				};
			}
			return false;
		} finally {
			isGenerating.value = false;
		}
	} /**
	 * Downloads a blob as a PDF file.
	 */
	function downloadPdf(blob: Blob, filename: string): void {
		const url = URL.createObjectURL(blob);
		const anchor = document.createElement("a");
		anchor.href = url;
		anchor.download = filename;
		document.body.appendChild(anchor);
		anchor.click();
		document.body.removeChild(anchor);
		URL.revokeObjectURL(url);
	}

	return {
		isGenerating,
		error,
		progress,
		generateResume,
	};
}
