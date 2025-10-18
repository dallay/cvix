import { describe, expect, it } from "vitest";
import {
	AccessDeniedError,
	AccountNotActivatedError,
	AuthenticationError,
	AuthenticationErrorCodes,
	AuthenticationErrorFactory,
	hasAuthenticationErrorCode,
	InvalidCredentialsError,
	isAuthenticationError,
	PasswordResetError,
	RegistrationError,
	SessionExpiredError,
} from "./AuthenticationError";

describe("AuthenticationError", () => {
	describe("Base AuthenticationError", () => {
		it("should create error with message and code", () => {
			const error = new AuthenticationError("Test message", "TEST_CODE");

			expect(error.message).toBe("Test message");
			expect(error.code).toBe("TEST_CODE");
			expect(error.name).toBe("AuthenticationError");
			expect(error.cause).toBeUndefined();
			expect(error.context).toBeUndefined();
		});

		it("should create error with cause and context", () => {
			const cause = new Error("Original error");
			const context = { userId: "123" };
			const error = new AuthenticationError(
				"Test message",
				"TEST_CODE",
				cause,
				context,
			);

			expect(error.cause).toBe(cause);
			expect(error.context).toBe(context);
		});

		it("should serialize to JSON correctly", () => {
			const context = { userId: "123" };
			const error = new AuthenticationError(
				"Test message",
				"TEST_CODE",
				undefined,
				context,
			);
			const json = error.toJSON();

			expect(json.name).toBe("AuthenticationError");
			expect(json.message).toBe("Test message");
			expect(json.code).toBe("TEST_CODE");
			expect(json.context).toBe(context);
			expect(json.stack).toBeDefined();
		});
	});

	describe("InvalidCredentialsError", () => {
		it("should create with default message", () => {
			const error = new InvalidCredentialsError();

			expect(error.message).toBe("Invalid username or password");
			expect(error.code).toBe(AuthenticationErrorCodes.INVALID_CREDENTIALS);
		});

		it("should create with context", () => {
			const context = { username: "testuser", attemptCount: 3 };
			const error = new InvalidCredentialsError(context);

			expect(error.context).toBe(context);
		});
	});

	describe("AccountNotActivatedError", () => {
		it("should create with default message", () => {
			const error = new AccountNotActivatedError();

			expect(error.message).toBe("Account is not activated");
			expect(error.code).toBe(AuthenticationErrorCodes.ACCOUNT_NOT_ACTIVATED);
		});

		it("should create with context", () => {
			const context = { email: "test@example.com" };
			const error = new AccountNotActivatedError(context);

			expect(error.context).toBe(context);
		});
	});

	describe("AccessDeniedError", () => {
		it("should create with default message", () => {
			const error = new AccessDeniedError();

			expect(error.message).toBe("Access denied");
			expect(error.code).toBe(AuthenticationErrorCodes.ACCESS_DENIED);
		});

		it("should create with resource context", () => {
			const context = { resource: "admin-panel", requiredRole: "admin" };
			const error = new AccessDeniedError(context);

			expect(error.message).toBe("Access denied for admin-panel");
			expect(error.context).toBe(context);
		});
	});

	describe("SessionExpiredError", () => {
		it("should create with default message", () => {
			const error = new SessionExpiredError();

			expect(error.message).toBe("Session has expired");
			expect(error.code).toBe(AuthenticationErrorCodes.SESSION_EXPIRED);
		});

		it("should create with context", () => {
			const context = { expiredAt: new Date(), sessionId: "session-123" };
			const error = new SessionExpiredError(context);

			expect(error.context).toBe(context);
		});
	});

	describe("PasswordResetError", () => {
		it("should create with default message", () => {
			const error = new PasswordResetError();

			expect(error.message).toBe("Password reset failed");
			expect(error.code).toBe(AuthenticationErrorCodes.PASSWORD_RESET_FAILED);
		});

		it("should create with custom message and context", () => {
			const context = { email: "test@example.com", reason: "Invalid token" };
			const error = new PasswordResetError("Token expired", context);

			expect(error.message).toBe("Token expired");
			expect(error.context).toBe(context);
		});
	});

	describe("RegistrationError", () => {
		it("should create with default message", () => {
			const error = new RegistrationError();

			expect(error.message).toBe("Registration failed");
			expect(error.code).toBe(AuthenticationErrorCodes.REGISTRATION_FAILED);
		});

		it("should create with validation errors context", () => {
			const context = {
				email: "test@example.com",
				validationErrors: ["Email already exists", "Password too weak"],
			};
			const error = new RegistrationError("Validation failed", context);

			expect(error.message).toBe("Validation failed");
			expect(error.context).toBe(context);
		});
	});

	describe("Type guards", () => {
		it("should identify authentication errors", () => {
			const authError = new AuthenticationError("Test", "TEST");
			const regularError = new Error("Regular error");

			expect(isAuthenticationError(authError)).toBe(true);
			expect(isAuthenticationError(regularError)).toBe(false);
			expect(isAuthenticationError(null)).toBe(false);
			expect(isAuthenticationError(undefined)).toBe(false);
		});

		it("should identify specific error codes", () => {
			const invalidCredentialsError = new InvalidCredentialsError();
			const sessionExpiredError = new SessionExpiredError();

			expect(
				hasAuthenticationErrorCode(
					invalidCredentialsError,
					AuthenticationErrorCodes.INVALID_CREDENTIALS,
				),
			).toBe(true);
			expect(
				hasAuthenticationErrorCode(
					invalidCredentialsError,
					AuthenticationErrorCodes.SESSION_EXPIRED,
				),
			).toBe(false);
			expect(
				hasAuthenticationErrorCode(
					sessionExpiredError,
					AuthenticationErrorCodes.SESSION_EXPIRED,
				),
			).toBe(true);
			expect(
				hasAuthenticationErrorCode(
					new Error("Regular error"),
					AuthenticationErrorCodes.INVALID_CREDENTIALS,
				),
			).toBe(false);
		});
	});

	describe("Error codes constants", () => {
		it("should have all expected error codes", () => {
			expect(AuthenticationErrorCodes.INVALID_CREDENTIALS).toBe(
				"INVALID_CREDENTIALS",
			);
			expect(AuthenticationErrorCodes.ACCOUNT_NOT_ACTIVATED).toBe(
				"ACCOUNT_NOT_ACTIVATED",
			);
			expect(AuthenticationErrorCodes.ACCESS_DENIED).toBe("ACCESS_DENIED");
			expect(AuthenticationErrorCodes.SESSION_EXPIRED).toBe("SESSION_EXPIRED");
			expect(AuthenticationErrorCodes.PASSWORD_RESET_FAILED).toBe(
				"PASSWORD_RESET_FAILED",
			);
			expect(AuthenticationErrorCodes.REGISTRATION_FAILED).toBe(
				"REGISTRATION_FAILED",
			);
		});
	});

	describe("AuthenticationErrorFactory", () => {
		it("should create InvalidCredentialsError", () => {
			const context = { username: "testuser", attemptCount: 3 };
			const error = AuthenticationErrorFactory.invalidCredentials(context);

			expect(error).toBeInstanceOf(InvalidCredentialsError);
			expect(error.context).toBe(context);
		});

		it("should create AccountNotActivatedError", () => {
			const context = { email: "test@example.com" };
			const error = AuthenticationErrorFactory.accountNotActivated(context);

			expect(error).toBeInstanceOf(AccountNotActivatedError);
			expect(error.context).toBe(context);
		});

		it("should create AccessDeniedError", () => {
			const context = { resource: "admin-panel", requiredRole: "admin" };
			const error = AuthenticationErrorFactory.accessDenied(context);

			expect(error).toBeInstanceOf(AccessDeniedError);
			expect(error.context).toBe(context);
		});

		it("should create SessionExpiredError", () => {
			const context = { expiredAt: new Date(), sessionId: "session-123" };
			const error = AuthenticationErrorFactory.sessionExpired(context);

			expect(error).toBeInstanceOf(SessionExpiredError);
			expect(error.context).toBe(context);
		});

		it("should create PasswordResetError", () => {
			const context = { email: "test@example.com", reason: "Invalid token" };
			const error = AuthenticationErrorFactory.passwordResetFailed(
				"Token expired",
				context,
			);

			expect(error).toBeInstanceOf(PasswordResetError);
			expect(error.message).toBe("Token expired");
			expect(error.context).toBe(context);
		});

		it("should create RegistrationError", () => {
			const context = {
				email: "test@example.com",
				validationErrors: ["Email exists"],
			};
			const error = AuthenticationErrorFactory.registrationFailed(
				"Validation failed",
				context,
			);

			expect(error).toBeInstanceOf(RegistrationError);
			expect(error.message).toBe("Validation failed");
			expect(error.context).toBe(context);
		});
	});
});
