/**
 * Base authentication domain error
 */
export class AuthenticationError extends Error {
  constructor(message: string, public readonly code: string,
    public readonly cause?: Error,
    public readonly context?: Record<string, unknown>,) {
    super(message);
    this.name = "AuthenticationError";
    Object.setPrototypeOf(this, AuthenticationError.prototype);
  }
  /**
   * Creates a serializable representation of the error
   */
  toJSON(): Record<string, unknown> {
    return {
      name: this.name,
      message: this.message,
      code: this.code,
      context: this.context,
      stack: this.stack,
    };
  }
}
/**
 * Invalid credentials error
 */
export class InvalidCredentialsError extends AuthenticationError {
  constructor(context?: { username?: string; attemptCount?: number }) {
    super(
      "Invalid username or password",
      "INVALID_CREDENTIALS",
      undefined,
      context,
    );
  }
}

/**
 * Account not activated error
 */
export class AccountNotActivatedError extends AuthenticationError {
  constructor(context?: { email?: string; activationToken?: string }) {
    super(
      "Account is not activated",
      "ACCOUNT_NOT_ACTIVATED",
      undefined,
      context,
    );
  }
}

/**
 * Access denied error
 */
export class AccessDeniedError extends AuthenticationError {
  constructor(context?: {
    resource?: string;
    requiredRole?: string;
    userRole?: string;
  }) {
    const resourceContext = context?.resource ? ` for ${context.resource}` : "";
    super(
      `Access denied${resourceContext}`,
      "ACCESS_DENIED",
      undefined,
      context,
    );
  }
}

/**
 * Session expired error
 */
export class SessionExpiredError extends AuthenticationError {
  constructor(context?: { expiredAt?: Date; sessionId?: string }) {
    super("Session has expired", "SESSION_EXPIRED", undefined, context);
  }
}

/**
 * Password reset error
 */
export class PasswordResetError extends AuthenticationError {
  constructor(
    message = "Password reset failed",
    context?: { email?: string; token?: string; reason?: string },
  ) {
    super(message, "PASSWORD_RESET_FAILED", undefined, context);
  }
}

/**
 * Registration error
 */
export class RegistrationError extends AuthenticationError {
  constructor(
    message = "Registration failed",
    context?: { email?: string; validationErrors?: string[] },
  ) {
    super(message, "REGISTRATION_FAILED", undefined, context);
  }
}

/**
 * Authentication error codes for consistent error handling
 */
export const AuthenticationErrorCodes = {
  INVALID_CREDENTIALS: "INVALID_CREDENTIALS",
  ACCOUNT_NOT_ACTIVATED: "ACCOUNT_NOT_ACTIVATED",

  ACCESS_DENIED: "ACCESS_DENIED",
  SESSION_EXPIRED: "SESSION_EXPIRED",
  PASSWORD_RESET_FAILED: "PASSWORD_RESET_FAILED",
  REGISTRATION_FAILED: "REGISTRATION_FAILED",
} as const;

/**
 * Type for authentication error codes
 */
export type AuthenticationErrorCode =
  (typeof AuthenticationErrorCodes)[keyof typeof AuthenticationErrorCodes];

/**
 * Type guard to check if an error is an authentication error
 */
export function isAuthenticationError(
  error: unknown,
): error is AuthenticationError {
  return error instanceof AuthenticationError;
}

/**
 * Type guard to check if an error has a specific authentication error code
 */
export function hasAuthenticationErrorCode(
  error: unknown,
  code: AuthenticationErrorCode,
): error is AuthenticationError {
  return isAuthenticationError(error) && error.code === code;
}

/**
 * Factory for creating authentication errors with consistent patterns
 */
export const AuthenticationErrorFactory = {
  invalidCredentials(context?: {
    username?: string;
    attemptCount?: number;
  }): InvalidCredentialsError {
    return new InvalidCredentialsError(context);
  },

  accountNotActivated(context?: {
    email?: string;
    activationToken?: string;
  }): AccountNotActivatedError {
    return new AccountNotActivatedError(context);
  },

  accessDenied(context?: {
    resource?: string;
    requiredRole?: string;
    userRole?: string;
  }): AccessDeniedError {
    return new AccessDeniedError(context);
  },

  sessionExpired(context?: {
    expiredAt?: Date;
    sessionId?: string;
  }): SessionExpiredError {
    return new SessionExpiredError(context);
  },

  passwordResetFailed(
    message?: string,
    context?: { email?: string; token?: string; reason?: string },
  ): PasswordResetError {
    return new PasswordResetError(message, context);
  },

  registrationFailed(
    message?: string,
    context?: { email?: string; validationErrors?: string[] },
  ): RegistrationError {
    return new RegistrationError(message, context);
  },
};
