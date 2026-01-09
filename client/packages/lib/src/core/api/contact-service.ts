/**
 * Contact form service for submitting contact requests to the backend
 */

import { ApiClient, type ApiClientConfig } from "./api-client";

/**
 * Contact form request payload
 */
export interface ContactFormRequest {
	name: string;
	email: string;
	subject: string;
	message: string;
	hcaptchaToken: string;
}

/**
 * Contact form response
 */
export interface ContactFormResponse {
	message: string;
	success: boolean;
}

/**
 * Service for handling contact form submissions
 */
export class ContactService extends ApiClient {
	constructor(config?: ApiClientConfig) {
		super(config);
	}

	/**
	 * Submit contact form to the backend
	 *
	 * @param request - Contact form data with hCaptcha token
	 * @param lang - Language code for localized error messages
	 * @returns Promise with success message
	 * @throws Error with message from backend or generic error
	 */
	async submitContactForm(
		request: ContactFormRequest,
		lang?: string,
	): Promise<ContactFormResponse> {
		const headers: Record<string, string> = {};

		// Add language header if provided
		if (lang) {
			headers["Accept-Language"] = lang;
		}

		return await this.post<ContactFormResponse, ContactFormRequest>(
			"/api/contact",
			request,
			{ headers },
		);
	}
}
