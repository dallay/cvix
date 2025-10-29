import axios, {
	type AxiosError,
	type AxiosInstance,
	type AxiosRequestConfig,
	type InternalAxiosRequestConfig,
} from "axios";

/**
 * Configuration options for BaseHttpClient
 */
export interface HttpClientConfig {
	baseURL?: string;
	timeout?: number;
	withCredentials?: boolean;
	headers?: Record<string, string>;
	xsrfCookieName?: string;
	xsrfHeaderName?: string;
}

/**
 * Generic error response structure
 */
export interface ApiErrorResponse {
	message?: string;
	code?: string;
	errors?: Record<string, string[]>;
}

/**
 * Base HTTP client with common Axios configuration and interceptors
 * Provides foundation for all API clients in the application
 */
export class BaseHttpClient {
	protected readonly client: AxiosInstance;
	protected readonly baseURL: string;

	constructor(config: HttpClientConfig = {}) {
		this.baseURL =
			config.baseURL || import.meta.env.VITE_API_BASE_URL || "/api";

		this.client = axios.create({
			baseURL: this.baseURL,
			timeout: config.timeout || 10000,
			withCredentials: config.withCredentials ?? true,
			headers: {
				"Content-Type": "application/json",
				Accept: "application/vnd.api.v1+json",
				...config.headers,
			},
			xsrfCookieName: config.xsrfCookieName || "XSRF-TOKEN",
			xsrfHeaderName: config.xsrfHeaderName || "X-XSRF-TOKEN",
		});

		this.setupRequestInterceptors();
		this.setupResponseInterceptors();
	}

	/**
	 * Setup request interceptors for common request processing
	 * Can be overridden by subclasses for custom behavior
	 */
	protected setupRequestInterceptors(): void {
		this.client.interceptors.request.use(
			(config) => this.onRequest(config),
			(error) => this.onRequestError(error),
		);
	}

	/**
	 * Setup response interceptors for common error handling
	 * Can be overridden by subclasses for custom behavior
	 */
	protected setupResponseInterceptors(): void {
		this.client.interceptors.response.use(
			(response) => response,
			async (error: AxiosError) => {
				const originalRequest = error.config as
					| (InternalAxiosRequestConfig & { _csrfRetry?: boolean })
					| undefined;

				// If error is 403 CSRF and we haven't retried yet, refresh CSRF token and retry
				if (
					originalRequest &&
					error.response?.status === 403 &&
					!originalRequest._csrfRetry
				) {
					const errorData = this.getErrorData(error);
					const isCsrfError =
						errorData.code === "INVALID_CSRF_TOKEN" ||
						errorData.message?.toLowerCase().includes("csrf");

					if (isCsrfError) {
						originalRequest._csrfRetry = true;

						try {
							// Re-initialize CSRF token
							await this.initializeCsrf();

							// Retry original request
							return this.client(originalRequest);
						} catch {
							// CSRF refresh failed, let the error propagate
							return Promise.reject(error);
						}
					}
				}

				return Promise.reject(this.onResponseError(error));
			},
		);
	}

	/**
	 * Hook called before each request
	 * Subclasses can override to add custom headers, logging, etc.
	 */
	protected onRequest(
		config: InternalAxiosRequestConfig,
	): InternalAxiosRequestConfig {
		// Manually add CSRF token from cookie to header
		// This is needed when using Vite proxy as Axios automatic CSRF handling
		// doesn't work reliably across different ports
		const csrfToken = this.getCsrfTokenFromCookie();
		if (csrfToken && !config.headers["X-XSRF-TOKEN"]) {
			config.headers["X-XSRF-TOKEN"] = csrfToken;
		}
		return config;
	}

	/**
	 * Extract CSRF token from cookie
	 */
	private getCsrfTokenFromCookie(): string | null {
		const name = "XSRF-TOKEN=";
		const decodedCookie = decodeURIComponent(document.cookie);
		const cookieArray = decodedCookie.split(";");

		for (let cookie of cookieArray) {
			cookie = cookie.trim();
			if (cookie.indexOf(name) === 0) {
				return cookie.substring(name.length);
			}
		}
		return null;
	}

	/**
	 * Hook called when a request fails before reaching the server
	 */
	protected onRequestError(error: AxiosError): Promise<never> {
		return Promise.reject(error);
	}

	/**
	 * Hook called when a response error occurs
	 * Subclasses can override for custom error handling
	 */
	protected onResponseError(error: AxiosError): Promise<never> {
		return Promise.reject(error);
	}

	/**
	 * Extract error data from Axios error response
	 */
	protected getErrorData(error: AxiosError): ApiErrorResponse {
		if (!error.response?.data) {
			return {};
		}
		return error.response.data as ApiErrorResponse;
	}

	/**
	 * Make a GET request
	 */
	protected async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
		const response = await this.client.get<T>(url, config);
		return response.data;
	}

	/**
	 * Make a POST request
	 */
	protected async post<T, D = unknown>(
		url: string,
		data?: D,
		config?: AxiosRequestConfig,
	): Promise<T> {
		const response = await this.client.post<T>(url, data, config);
		return response.data;
	}

	/**
	 * Make a PUT request
	 */
	protected async put<T, D = unknown>(
		url: string,
		data?: D,
		config?: AxiosRequestConfig,
	): Promise<T> {
		const response = await this.client.put<T>(url, data, config);
		return response.data;
	}

	/**
	 * Make a PATCH request
	 */
	protected async patch<T, D = unknown>(
		url: string,
		data?: D,
		config?: AxiosRequestConfig,
	): Promise<T> {
		const response = await this.client.patch<T>(url, data, config);
		return response.data;
	}

	/**
	 * Make a DELETE request
	 */
	protected async delete<T>(
		url: string,
		config?: AxiosRequestConfig,
	): Promise<T> {
		const response = await this.client.delete<T>(url, config);
		return response.data;
	}

	/**
	 * Initialize CSRF token by making a GET request to a public endpoint
	 * This ensures the XSRF-TOKEN cookie is set before any POST/PUT/DELETE requests
	 * This method should be called once when the application starts
	 */
	async initializeCsrf(endpoint = "/health-check"): Promise<void> {
		try {
			await this.get(endpoint);
			console.debug("CSRF token initialized successfully");
		} catch (error) {
			console.warn(
				"Failed to initialize CSRF token, will retry on first request:",
				error,
			);
		}
	} /**
	 * Get the underlying Axios instance for advanced use cases
	 */
	getAxiosInstance(): AxiosInstance {
		return this.client;
	}
}
