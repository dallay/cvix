import { ref } from "vue";
import type { ProblemDetail, Resume } from "../types/resume";

/**
 * Composable for resume generation functionality.
 * Handles API calls to generate PDF resumes.
 */
export function useResumeGeneration() {
	const isGenerating = ref(false);
	const error = ref<ProblemDetail | null>(null);
	const progress = ref(0);

	/**
	 * Generates a PDF resume from resume data.
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
			// Get auth token if available
			const authToken = localStorage.getItem("auth_token");

			const headers: Record<string, string> = {
				"Content-Type": "application/vnd.api.v1+json",
				"Accept-Language": locale,
			};

			if (authToken) {
				headers.Authorization = `Bearer ${authToken}`;
			} // Make API request
			const response = await fetch("/api/resumes", {
				method: "POST",
				headers,
				body: JSON.stringify(resume),
			});

			progress.value = 50;

			if (!response.ok) {
				// Handle error response (RFC 7807 Problem Details)
				const problemDetail = await response.json();
				error.value = {
					status: response.status,
					detail: problemDetail.detail || "An error occurred",
					title: problemDetail.title,
					type: problemDetail.type,
					timestamp: problemDetail.timestamp || new Date().toISOString(),
					errorCategory: problemDetail.errorCategory,
					fieldErrors: problemDetail.fieldErrors,
					...problemDetail, // Include any additional properties
				};
				return false;
			}
			progress.value = 75;

			// Get PDF blob
			const blob = await response.blob();

			progress.value = 90;

			// Download PDF
			downloadPdf(blob, `resume-${new Date().toISOString().split("T")[0]}.pdf`);

			progress.value = 100;
			return true;
		} catch (err) {
			// Handle network errors
			error.value = {
				status: 0,
				title: "Network Error",
				detail: err instanceof Error ? err.message : "Network error occurred",
				timestamp: new Date().toISOString(),
				errorCategory: "NETWORK_ERROR",
			};
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
