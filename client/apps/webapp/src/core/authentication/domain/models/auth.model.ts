/**
 * Represents an authenticated user in the application
 */
export interface User {
	id: string;
	username: string;
	email: string;
	firstName: string | null;
	lastName: string | null;
	roles: string[];
}

/**
 * Represents a user session
 */
export interface Session {
	accessToken: string;
	refreshToken: string;
	expiresIn: number;
	tokenType: string;
	scope: string;
}

/**
 * Represents authentication state
 */
export interface AuthState {
	isAuthenticated: boolean;
	user: User | null;
	session: Session | null;
	isLoading: boolean;
	error: string | null;
}

/**
 * Federated identity provider types
 */
export enum IdentityProvider {
	GOOGLE = "google",
	MICROSOFT = "microsoft",
	GITHUB = "github",
}

/**
 * Authentication event types for logging and analytics
 */
export enum AuthEvent {
	LOGIN_SUCCESS = "login_success",
	LOGIN_FAILURE = "login_failure",
	LOGOUT = "logout",
	REGISTER_SUCCESS = "register_success",
	REGISTER_FAILURE = "register_failure",
	TOKEN_REFRESH_SUCCESS = "token_refresh_success",
	TOKEN_REFRESH_FAILURE = "token_refresh_failure",
	SESSION_EXPIRED = "session_expired",
}
