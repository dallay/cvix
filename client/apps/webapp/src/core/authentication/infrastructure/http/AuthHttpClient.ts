import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { BaseHttpClient } from "@/shared/BaseHttpClient.ts";
import {
	AuthenticationError,
	InvalidCredentialsError,
	NetworkError,
	SessionExpiredError,
	TokenRefreshError,
	UserAlreadyExistsError,
	ValidationError,
} from "../../domain/errors/auth.errors.ts";
import type { Session, User } from "../../domain/models/auth.model.ts";
import type {
	LoginFormData,
	RegisterFormData,
} from "../../domain/validators/auth.schema.ts";
import { isValidUser } from "../../domain/validators/user.validator.ts";

/**
 * Authentication API response types matching the OpenAPI contract
 */
interface AuthResponse {
	accessToken: string;
	expiresIn: number;
	tokenType: string;
	user: {
		id: string;
		email: string;
		firstname: string;
		lastname: string;
		displayName: string;
		accountStatus: string;
	};
}

interface TokenRefreshResponse {
	accessToken: string;
	expiresIn: number;
	tokenType: string;
}

interface UserResponse {
	id: string;
	username: string;
	email: string;
	firstname: string | null;
	lastname: string | null;
	authorities: string[];
}

/**
 * HTTP client for authentication operations
 * Extends BaseHttpClient with authentication-specific logic
 */
export class AuthHttpClient extends BaseHttpClient {
	/**
	 * Construct the AuthHttpClient.
	 * Reads only BACKEND_URL from environment variables.
	 */
	constructor(baseURL?: string) {
		const envRecord = import.meta.env as unknown as Record<string, unknown>;
		const backend = envRecord.BACKEND_URL as string | undefined;

		// Prioritize explicit baseURL, fallback to BACKEND_URL, then default
		const candidate = baseURL ?? backend ?? "/api";

		// Ensure /api path is appended if needed
		function ensureApiPath(urlCandidate: string): string {
			try {
				if (/^https?:\/\//i.test(urlCandidate)) {
					const u = new URL(urlCandidate);
					if (
						u.pathname &&
						u.pathname.split("/").filter(Boolean)[0] === "api"
					) {
						return urlCandidate.replace(/\/$/, "");
					}
					return new URL("/api", u.origin).toString().replace(/\/$/, "");
				}
				return urlCandidate;
			} catch (err) {
				console.warn(
					"[AuthHttpClient] ensureApiPath parse failed for",
					urlCandidate,
					err,
				);
				return urlCandidate;
			}
		}

		const resolvedBase = ensureApiPath(candidate);
		super({ baseURL: resolvedBase });
	}

	/**
	 * Setup response interceptors with token refresh logic
	 */
	protected override setupResponseInterceptors(): void {
		this.client.interceptors.response.use(
			(response) => response,
			async (error: AxiosError) => {
				const originalRequest = error.config as
					| (InternalAxiosRequestConfig & { _retry?: boolean })
					| undefined;

				// If error is 401 and we haven't retried yet, attempt token refresh
				if (
					originalRequest &&
					error.response?.status === 401 &&
					!originalRequest._retry &&
					!originalRequest.url?.includes("/auth/token/refresh") &&
					!originalRequest.url?.includes("/auth/login")
				) {
					originalRequest._retry = true;

					try {
						// Attempt to refresh token
						await this.refreshToken();

						// Retry original request
						return this.client(originalRequest);
					} catch {
						// Refresh failed, let the error propagate
						throw this.handleError(error);
					}
				}

				throw this.handleError(error);
			},
		);
	}

	/**
	 * Handle API errors and transform them to domain errors
	 */
	private handleError(error: AxiosError): Error {
		if (!error.response) {
			return new NetworkError();
		}

		const { status } = error.response;
		const errorData = this.getErrorData(error);

		switch (status) {
			case 400:
				if (errorData.errors) {
					return new ValidationError(
						errorData.message || "Validation failed",
						errorData.errors,
					);
				}
				return new AuthenticationError(
					errorData.message || "Bad request",
					"BAD_REQUEST",
					400,
				);

			case 401:
				if (errorData.code === "SESSION_EXPIRED") {
					return new SessionExpiredError();
				}
				return new InvalidCredentialsError(
					errorData.message || "Invalid email or password",
				);

			case 403:
				return new AuthenticationError(
					errorData.message || "Access denied",
					"FORBIDDEN",
					403,
				);

			case 409:
				return new UserAlreadyExistsError(
					errorData.message || "An account with this email already exists",
				);

			case 429:
				return new AuthenticationError(
					errorData.message || "Too many requests. Please try again later.",
					"RATE_LIMIT_EXCEEDED",
					429,
				);

			case 500:
				return new AuthenticationError(
					"An unexpected error occurred. Please try again later.",
					"INTERNAL_SERVER_ERROR",
					500,
				);

			default:
				return new AuthenticationError(
					errorData.message || "An error occurred",
					"UNKNOWN_ERROR",
					status,
				);
		}
	}

	/**
	 * Register a new user - Maps to POST /auth/register
	 * Follows the registration flow diagram
	 */
	async register(data: RegisterFormData): Promise<void> {
		// POST /auth/register returns 201 with AuthResponse but this method discards it
		// as it only performs registration (no auto-login).
		// Callers must handle subsequent login separately if needed.
		// Note: Error handling is done by the response interceptor which transforms
		// AxiosError to domain errors (UserAlreadyExistsError, ValidationError, etc.)
		await this.post<AuthResponse>("/auth/register", {
			email: data.email,
			password: data.password,
			firstname: data.firstName,
			lastname: data.lastName,
		});
	}

	/**
	 * Login with email and password - Maps to POST /auth/login
	 * Follows the login flow diagram
	 * Note: Error handling is done by the response interceptor which transforms
	 * AxiosError to domain errors (InvalidCredentialsError, etc.)
	 */
	async login(data: LoginFormData): Promise<Session> {
		// POST /auth/login returns AuthResponse with tokens and user data
		const response = await this.post<AuthResponse>("/auth/login", {
			email: data.email,
			password: data.password,
			rememberMe: data.rememberMe,
		});

		// Map AuthResponse to Session domain model
		return {
			accessToken: response.accessToken,
			refreshToken: "", // Refresh token is in HTTP-only cookie
			expiresIn: response.expiresIn,
			tokenType: response.tokenType,
			scope: "", // Scope not returned in v1 API
		};
	}

	/**
	 * Logout the current user - Maps to POST /auth/logout
	 * Follows the logout flow diagram
	 * Note: Error handling is done by the response interceptor
	 */
	async logout(): Promise<void> {
		// POST /auth/logout returns 204 No Content
		await this.post("/auth/logout");
	}

	/**
	 * Refresh the access token - Maps to POST /auth/token/refresh
	 * Uses refresh token from HTTP-only cookie
	 */
	async refreshToken(): Promise<Session> {
		try {
			// POST /auth/token/refresh returns TokenRefreshResponse
			const response = await this.post<TokenRefreshResponse>(
				"/auth/token/refresh",
			);

			return {
				accessToken: response.accessToken,
				refreshToken: "", // Refresh token is in HTTP-only cookie
				expiresIn: response.expiresIn,
				tokenType: response.tokenType,
				scope: "",
			};
		} catch {
			throw new TokenRefreshError();
		}
	}

	/**
	 * Get the current authenticated user - Maps to GET /api/account
	 * Note: Error handling is done by the response interceptor
	 * @throws AuthenticationError if response is not valid JSON or missing required fields
	 */
	async getCurrentUser(): Promise<User> {
		const response = await this.get<UserResponse>("/account");

		// Validate response is a proper object with required fields
		// This catches cases where the server returns HTML (e.g., SPA fallback) instead of JSON
		if (!isValidUser(response as unknown as User)) {
			throw new AuthenticationError(
				"Invalid response from server. Please check your network connection.",
				"INVALID_RESPONSE",
				422,
			);
		}

		return {
			id: response.id,
			username: response.username,
			email: response.email,
			firstName: response.firstname,
			lastName: response.lastname,
			roles: response.authorities,
		};
	}

	/**
	 * Initiate federated login (redirect to identity provider)
	 * Maps to GET /auth/federated/initiate
	 */
	initiateOAuthLogin(provider: string, redirectUri?: string): void {
		const params = new URLSearchParams();
		params.append("provider", provider);
		if (redirectUri) {
			params.append("redirectUri", redirectUri);
		}
		globalThis.location.href = `${this.baseURL}/auth/federated/initiate?${params.toString()}`;
	}
}
