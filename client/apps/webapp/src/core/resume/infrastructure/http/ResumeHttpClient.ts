import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata.ts";
import type {
	CreateResumeRequest,
	UpdateResumeRequest,
} from "@/core/resume/infrastructure/http/requests/ResumeRequest.ts";
import { BaseHttpClient } from "@/shared/BaseHttpClient.ts";
import type { Resume } from "../../domain/Resume.ts";
import type { ResumeGenerator } from "../../domain/ResumeGenerator.ts";
import {
	mapResumeToGenerateResumeRequest,
	mapResumeToResumeRequest,
} from "./ResumeRequestMapper.ts";

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
	 * @param resume Resume data
	 * @param title Optional resume title
	 * @returns Promise with the created resume document
	 * @note workspaceId is automatically sent via X-Workspace-Id header
	 */

	async createResume(
		id: string,
		resume: Resume,
		title?: string,
	): Promise<ResumeDocumentResponse> {
		const request: CreateResumeRequest = {
			title,
			content: mapResumeToResumeRequest(resume),
		};
		const response = await this.client.put<ResumeDocumentResponse>(
			`/resume/${id}`,
			request,
		);
		return response.data;
	}

	/**
	 * Get a resume by ID
	 * @param id Resume ID
	 * @returns Promise with the resume document
	 */
	async getResume(id: string): Promise<ResumeDocumentResponse> {
		const response = await this.client.get<ResumeDocumentResponse>(
			`/resume/${id}`,
		);
		return response.data;
	}

	/**
	 * Update an existing resume
	 * @param id Resume ID
	 * @param resume Resume data
	 * @param title Optional new title
	 * @returns Promise with the updated resume document
	 */
	async updateResume(
		id: string,
		resume: Resume,
		title?: string,
	): Promise<ResumeDocumentResponse> {
		const request: UpdateResumeRequest = {
			title,
			content: mapResumeToResumeRequest(resume),
		};
		const response = await this.client.put<ResumeDocumentResponse>(
			`/resume/${id}/update`,
			request,
		);
		return response.data;
	}

	/**
	 * Delete a resume by ID
	 * @param id Resume ID
	 * @returns Promise that resolves when deletion is complete
	 */
	async deleteResume(id: string): Promise<void> {
		await this.client.delete(`/resume/${id}`);
	}

	/**
	 * List all resumes for the current user
	 * @returns Promise with array of resume documents
	 */
	async listResumes(): Promise<ResumeDocumentResponse[]> {
		const response = await this.client.get<{ data: ResumeDocumentResponse[] }>(
			"/resume",
		);
		return response.data.data;
	}

	/**
	 * Generate a PDF resume from resume data
	 * @param templateId Template ID to use for generation
	 * @param resume Resume data
	 * @param locale Language locale (en/es)
	 * @returns Promise with the PDF blob
	 */
	async generatePdf(
		templateId: string,
		resume: Resume,
		locale?: "en" | "es",
	): Promise<Blob> {
		// Map frontend Resume (JSON Resume schema) to backend GenerateResumeRequest format
		const generateResumeRequest = mapResumeToGenerateResumeRequest(
			templateId,
			resume,
		);

		const response = await this.client.post<Blob>(
			"/resume/generate",
			generateResumeRequest,
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

	/**
	 * Get list of available resume templates
	 * @returns Promise with list of templates
	 * @note workspaceId is automatically sent via X-Workspace-Id header
	 */
	async getTemplates(): Promise<TemplateMetadata[]> {
		const response = await this.client.get<{
			data: TemplateMetadata[];
		}>("/templates");
		return response.data.data;
	}
}

/**
 * Singleton instance of ResumeHttpClient for use throughout the app
 */
export const resumeHttpClient = new ResumeHttpClient();
