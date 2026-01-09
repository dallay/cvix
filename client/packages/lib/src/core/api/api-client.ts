/**
 * Lightweight HTTP client for Astro/marketing site
 * Provides common functionality for API calls with CSRF token handling
 */

const CSRF_COOKIE_NAME = "XSRF-TOKEN";

/**
 * Get CSRF token from cookie
 * @returns CSRF token value or null if not found
 */
function getCsrfToken(): string | null {
	if (typeof document === "undefined") {
		return null;
	}

	const name = `${CSRF_COOKIE_NAME}=`;
	const decodedCookie = decodeURIComponent(document.cookie);
	const cookies = decodedCookie.split(";");

	for (let cookie of cookies) {
		cookie = cookie.trim();
		if (cookie.startsWith(name)) {
			return cookie.substring(name.length);
		}
	}

	return null;
}

/**
 * RFC 7807 Problem Details for HTTP APIs
 * Standard format for API error responses
 */
export interface ProblemDetail {
	type?: string; // URI reference identifying the problem type
	title?: string; // Short, human-readable summary
	status: number; // HTTP status code
	detail?: string; // Human-readable explanation
	instance?: string; // URI reference identifying the specific occurrence
	timestamp?: string; // When the error occurred
	errorCategory?: string; // Category of the error
	fieldErrors?: Record<string, string>; // Field-level validation errors
	message?: string; // Alternative message field (for backwards compatibility)
	[key: string]: unknown; // Allow additional properties
}

/**
 * Configuration options for ApiClient
 */
export interface ApiClientConfig {
	baseURL?: string;
	timeout?: number;
	headers?: Record<string, string>;
}

/**
 * Base API client with common HTTP functionality
 * Handles CSRF tokens, headers, and error responses
 */
export class ApiClient {
	protected readonly baseURL: string;
	protected readonly timeout: number;
	protected readonly defaultHeaders: Record<string, string>;

	constructor(config: ApiClientConfig = {}) {
		this.baseURL = config.baseURL || "";
		this.timeout = config.timeout || 10000;
		this.defaultHeaders = {
			"Content-Type": "application/json",
			Accept: "application/vnd.api.v1+json",
			...config.headers,
		};
	}

	/**
	 * Build headers for a request
	 * Includes default headers, CSRF token, and custom headers
	 */
	protected buildHeaders(
		customHeaders?: Record<string, string>,
	): Record<string, string> {
		const headers = { ...this.defaultHeaders, ...customHeaders };

		// Add CSRF token if available
		const csrfToken = getCsrfToken();
		if (csrfToken) {
			headers["X-XSRF-TOKEN"] = csrfToken;
		}

		return headers;
	}

	/**
	 * Make a GET request
	 */
	protected async get<T>(
		url: string,
		options?: {
			headers?: Record<string, string>;
			signal?: AbortSignal;
		},
	): Promise<T> {
		const controller = new AbortController();
		const timeoutId = setTimeout(() => controller.abort(), this.timeout);

		try {
			const response = await fetch(`${this.baseURL}${url}`, {
				method: "GET",
				headers: this.buildHeaders(options?.headers),
				credentials: "include",
				signal: options?.signal || controller.signal,
			});

			clearTimeout(timeoutId);

			if (!response.ok) {
				const error = await this.handleErrorResponse(response);
				throw error;
			}

			return await response.json();
		} catch (error) {
			clearTimeout(timeoutId);
			if (error instanceof Error) {
				throw error;
			}
			throw this.handleNetworkError(error);
		}
	}

	/**
	 * Make a POST request
	 */
	protected async post<T, D = unknown>(
		url: string,
		data?: D,
		options?: {
			headers?: Record<string, string>;
			signal?: AbortSignal;
		},
	): Promise<T> {
		const controller = new AbortController();
		const timeoutId = setTimeout(() => controller.abort(), this.timeout);

		try {
			const response = await fetch(`${this.baseURL}${url}`, {
				method: "POST",
				headers: this.buildHeaders(options?.headers),
				body: data ? JSON.stringify(data) : undefined,
				credentials: "include",
				signal: options?.signal || controller.signal,
			});

			clearTimeout(timeoutId);

			if (!response.ok) {
				const error = await this.handleErrorResponse(response);
				throw error;
			}

			return await response.json();
		} catch (error) {
			clearTimeout(timeoutId);
			if (error instanceof Error) {
				throw error;
			}
			throw this.handleNetworkError(error);
		}
	}

	/**
	 * Handle HTTP error responses
	 * Parses RFC 7807 Problem Details format
	 */
	protected async handleErrorResponse(response: Response): Promise<Error> {
		try {
			const problemDetail: ProblemDetail = await response.json();
			const message =
				problemDetail.detail ||
				problemDetail.message ||
				problemDetail.title ||
				`HTTP ${response.status}: ${response.statusText}`;

			const error = new Error(message) as Error & {
				problemDetail?: ProblemDetail;
				status?: number;
			};
			error.problemDetail = problemDetail;
			error.status = response.status;
			return error;
		} catch {
			// Failed to parse error response as JSON
			return new Error(`HTTP ${response.status}: ${response.statusText}`);
		}
	}

	/**
	 * Handle network errors (timeout, abort, etc.)
	 */
	protected handleNetworkError(error: unknown): Error {
		if (error instanceof Error) {
			if (error.name === "AbortError") {
				return new Error("Request timeout");
			}
			return error;
		}
		return new Error("Network error occurred");
	}
}
