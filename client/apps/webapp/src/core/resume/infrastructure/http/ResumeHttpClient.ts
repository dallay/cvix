import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import { BaseHttpClient } from "@/shared/BaseHttpClient.ts";
import { mapResumeToBackendRequest } from "./ResumeRequestMapper.ts";

/**
 * Backend resume document response matching server DTO
 */
export interface ResumeDocumentResponse {
	id: string;
	userId: string;
	workspaceId: string;
	title: string;
	content: Resume;
	createdAt: string;
	updatedAt: string | null;
	createdBy: string;
	updatedBy: string | null;
}

/**
 * Request payload for creating a resume
 */
export interface CreateResumeRequest {
	workspaceId: string;
	title?: string;
	content: ReturnType<typeof mapResumeToBackendRequest>;
}

/**
 * Request payload for updating a resume
 */
export interface UpdateResumeRequest {
	title?: string;
	content: ReturnType<typeof mapResumeToBackendRequest>;
}

/**
 * HTTP client for resume operations (CRUD + PDF generation)
 * Extends BaseHttpClient to leverage CSRF protection, cookie handling, and error handling
 */
export class ResumeHttpClient
	extends BaseHttpClient
	implements ResumeGenerator
{
	/**
	 * Create a new resume on the server
	 * @param id Resume ID (UUID)
	 * @param workspaceId Workspace ID
	 * @param resume Resume data
	 * @param title Optional resume title
	 * @returns Promise with the created resume document
	 */

	async createResume(
		id: string,
		workspaceId: string,
		resume: Resume,
		title?: string,
	): Promise<ResumeDocumentResponse> {
		const request: CreateResumeRequest = {
			workspaceId,
			title,
			content: mapResumeToBackendRequest(resume),
		};
		const response = await this.client.put<ResumeDocumentResponse>(
			`/resume/${id}`,
			request,
		);
		return response.data;
	}

	async getResume(id: string): Promise<ResumeDocumentResponse> {
		const response = await this.client.get<ResumeDocumentResponse>(
			`/resume/${id}`,
		);
		return response.data;
	}

	async updateResume(
		id: string,
		resume: Resume,
		title?: string,
	): Promise<ResumeDocumentResponse> {
		const request: UpdateResumeRequest = {
			title,
			content: mapResumeToBackendRequest(resume),
		};
		const response = await this.client.put<ResumeDocumentResponse>(
			`/resume/${id}/update`,
			request,
		);
		return response.data;
	}

	async deleteResume(id: string): Promise<void> {
		await this.client.delete(`/resume/${id}`);
	}

	async listResumes(): Promise<ResumeDocumentResponse[]> {
		const response = await this.client.get<{ data: ResumeDocumentResponse[] }>(
			"/resume",
		);
		return response.data.data;
	}

	/**
	 * Generate a PDF resume from resume data
	 * @param resume Resume data
	 * @param locale Language locale (en/es)
	 * @returns Promise with the PDF blob
	 */
	async generatePdf(resume: Resume, locale?: "en" | "es"): Promise<Blob> {
		// Map frontend Resume (JSON Resume schema) to backend GenerateResumeRequest format
		const backendRequest = mapResumeToBackendRequest(resume);

		const response = await this.client.post<Blob>(
			"/resume/generate",
			backendRequest,
			{
				headers: {
					"Accept-Language": locale,
					Accept: "application/pdf",
				},
				responseType: "blob",
			},
		);
		return response.data;
	}
}

// Export singleton instance
export const resumeHttpClient = new ResumeHttpClient();
