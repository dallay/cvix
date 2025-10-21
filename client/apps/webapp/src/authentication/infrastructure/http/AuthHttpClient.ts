import axios, {
	type AxiosError,
	type AxiosInstance,
	type InternalAxiosRequestConfig,
} from "axios";
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

/**
 * Authentication API response types
 */
interface LoginResponse {
	accessToken: string;
	refreshToken: string;
	expiresIn: number;
	tokenType: string;
	scope: string;
}

interface RegisterResponse {
	id: string;
	email: string;
	firstName: string;
	lastName: string;
}

interface RefreshTokenResponse {
	accessToken: string;
	refreshToken: string;
	expiresIn: number;
	tokenType: string;
}

interface UserResponse {
	id: string;
	email: string;
	firstName: string;
	lastName: string;
	roles: string[];
	emailVerified: boolean;
	createdAt: string;
	updatedAt: string;
}

/**
 * HTTP client for authentication operations
 */
export class AuthHttpClient {
	private readonly client: AxiosInstance;
	private readonly baseURL: string;

	constructor(baseURL = import.meta.env.VITE_API_BASE_URL || "/api") {
		this.baseURL = baseURL;
		this.client = axios.create({
			baseURL,
			headers: {
				"Content-Type": "application/vnd.api.v1+json",
				Accept: "application/vnd.api.v1+json",
			},
			withCredentials: true, // Enable cookies for HTTP-only tokens
			timeout: 10000,
		});

		this.setupInterceptors();
	}

	/**
	 * Setup request and response interceptors
	 */
	private setupInterceptors(): void {
		// Response interceptor for token refresh and error handling
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
					!originalRequest.url?.includes("/auth/refresh") &&
					!originalRequest.url?.includes("/login")
				) {
					originalRequest._retry = true;

					try {
						// Attempt to refresh token
						await this.refreshToken();

						// Retry original request
						return this.client(originalRequest);
					} catch {
						// Refresh failed, let the error propagate
						return Promise.reject(this.handleError(error));
					}
				}

				return Promise.reject(this.handleError(error));
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

		const { status, data } = error.response;
		const errorData = data as {
			message?: string;
			code?: string;
			errors?: Record<string, string[]>;
		};

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

			case 409:
				return new UserAlreadyExistsError(
					errorData.message || "An account with this email already exists",
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
	 * Register a new user
	 */
	async register(data: RegisterFormData): Promise<User> {
		try {
			const response = await this.client.post<RegisterResponse>(
				"/auth/register",
				{
					email: data.email,
					password: data.password,
					firstName: data.firstName,
					lastName: data.lastName,
				},
			);

			return {
				id: response.data.id,
				email: response.data.email,
				firstName: response.data.firstName,
				lastName: response.data.lastName,
				roles: ["USER"],
				emailVerified: false,
				createdAt: new Date(),
				updatedAt: new Date(),
			};
		} catch (error) {
			throw this.handleError(error as AxiosError);
		}
	}

	/**
	 * Login with email and password
	 */
	async login(data: LoginFormData): Promise<Session> {
		try {
			const response = await this.client.post<LoginResponse>("/login", {
				username: data.email,
				password: data.password,
			});

			return {
				accessToken: response.data.accessToken,
				refreshToken: response.data.refreshToken,
				expiresIn: response.data.expiresIn,
				tokenType: response.data.tokenType,
				scope: response.data.scope,
			};
		} catch (error) {
			throw this.handleError(error as AxiosError);
		}
	}

	/**
	 * Logout the current user
	 */
	async logout(): Promise<void> {
		try {
			await this.client.post("/logout");
		} catch (error) {
			throw this.handleError(error as AxiosError);
		}
	}

	/**
	 * Refresh the access token
	 */
	async refreshToken(): Promise<Session> {
		try {
			const response =
				await this.client.post<RefreshTokenResponse>("/auth/refresh");

			return {
				accessToken: response.data.accessToken,
				refreshToken: response.data.refreshToken,
				expiresIn: response.data.expiresIn,
				tokenType: response.data.tokenType,
				scope: "",
			};
		} catch {
			throw new TokenRefreshError();
		}
	}

	/**
	 * Get the current authenticated user
	 */
	async getCurrentUser(): Promise<User> {
		try {
			const response = await this.client.get<UserResponse>("/auth/user");

			return {
				id: response.data.id,
				email: response.data.email,
				firstName: response.data.firstName,
				lastName: response.data.lastName,
				roles: response.data.roles,
				emailVerified: response.data.emailVerified,
				createdAt: new Date(response.data.createdAt),
				updatedAt: new Date(response.data.updatedAt),
			};
		} catch (error) {
			throw this.handleError(error as AxiosError);
		}
	}

	/**
	 * Initiate federated login (redirect to identity provider)
	 */
	initiateOAuthLogin(provider: string, redirectUri?: string): void {
		const params = new URLSearchParams();
		if (redirectUri) {
			params.append("redirect_uri", redirectUri);
		}

		const url = `${this.baseURL}/oauth2/authorization/${provider}${params.toString() ? `?${params.toString()}` : ""}`;
		window.location.href = url;
	}
}
