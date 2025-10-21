/**
 * Base authentication error
 */
export class AuthenticationError extends Error {
	constructor(
		message: string,
		public readonly code: string,
		public readonly statusCode?: number,
	) {
		super(message);
		this.name = "AuthenticationError";
		Object.setPrototypeOf(this, AuthenticationError.prototype);
	}
}

/**
 * Invalid credentials error
 */
export class InvalidCredentialsError extends AuthenticationError {
	constructor(message = "Invalid email or password") {
		super(message, "INVALID_CREDENTIALS", 401);
		this.name = "InvalidCredentialsError";
		Object.setPrototypeOf(this, InvalidCredentialsError.prototype);
	}
}

/**
 * User already exists error
 */
export class UserAlreadyExistsError extends AuthenticationError {
	constructor(message = "An account with this email already exists") {
		super(message, "USER_ALREADY_EXISTS", 409);
		this.name = "UserAlreadyExistsError";
		Object.setPrototypeOf(this, UserAlreadyExistsError.prototype);
	}
}

/**
 * Session expired error
 */
export class SessionExpiredError extends AuthenticationError {
	constructor(message = "Your session has expired. Please log in again.") {
		super(message, "SESSION_EXPIRED", 401);
		this.name = "SessionExpiredError";
		Object.setPrototypeOf(this, SessionExpiredError.prototype);
	}
}

/**
 * Token refresh error
 */
export class TokenRefreshError extends AuthenticationError {
	constructor(message = "Unable to refresh authentication token") {
		super(message, "TOKEN_REFRESH_ERROR", 401);
		this.name = "TokenRefreshError";
		Object.setPrototypeOf(this, TokenRefreshError.prototype);
	}
}

/**
 * Network error
 */
export class NetworkError extends AuthenticationError {
	constructor(
		message = "Network error. Please check your connection and try again.",
	) {
		super(message, "NETWORK_ERROR", 0);
		this.name = "NetworkError";
		Object.setPrototypeOf(this, NetworkError.prototype);
	}
}

/**
 * Validation error
 */
export class ValidationError extends AuthenticationError {
	constructor(
		message: string,
		public readonly errors: Record<string, string[]>,
	) {
		super(message, "VALIDATION_ERROR", 400);
		this.name = "ValidationError";
		Object.setPrototypeOf(this, ValidationError.prototype);
	}
}
