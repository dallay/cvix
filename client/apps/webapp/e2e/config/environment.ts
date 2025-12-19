/**
 * E2E Test Environment Configuration
 * Centralized configuration for all E2E test environment variables
 */

export const E2E_CONFIG = {
	/** Base URL for the frontend application */
	baseURL: process.env.BASE_URL || "http://localhost:9876",

	/** Base URL for API calls */
	apiBaseURL: process.env.API_BASE_URL || "http://localhost:8080/api",

	/** Keycloak authentication URL */
	keycloakURL: process.env.KEYCLOAK_URL || "http://localhost:9080/realms/cvix",

	/** Keycloak client ID */
	keycloakClientId: process.env.KEYCLOAK_CLIENT_ID || "cvix-client",

	/** Test user IDs for different test scenarios */
	testUsers: {
		/** User with default workspace */
		default: {
			id: "efc4b2b8-08be-4020-93d5-f795762bf5c9",
			email: "test@example.com",
			password: "password123",
		},
		/** User with only non-default workspaces */
		noDefault: {
			id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
			email: "nodefault@example.com",
			password: "password123",
		},
		/** User with no workspaces */
		noWorkspace: {
			id: "b2c3d4e5-f6a7-8901-bcde-f12345678901",
			email: "noworkspace@example.com",
			password: "password123",
		},
		/** New user with no history */
		newUser: {
			id: "c3d4e5f6-a7b8-9012-cdef-123456789012",
			email: "newuser@example.com",
			password: "password123",
		},
	},

	/** Timeout values for different operations */
	timeouts: {
		/** Short timeout for fast operations */
		short: 2000,
		/** Medium timeout for API calls */
		medium: 5000,
		/** Long timeout for complex operations */
		long: 10000,
	},
} as const;

/**
 * Validate that all required environment variables are set
 * @throws Error if required environment variables are missing
 */
export function validateE2EConfig(): void {
	const required = [
		{ key: "apiBaseURL", value: E2E_CONFIG.apiBaseURL },
		{ key: "keycloakURL", value: E2E_CONFIG.keycloakURL },
		{ key: "keycloakClientId", value: E2E_CONFIG.keycloakClientId },
	];

	const missing = required.filter(({ value }) => !value);

	if (missing.length > 0) {
		throw new Error(
			`Missing required E2E configuration: ${missing.map((m) => m.key).join(", ")}`,
		);
	}
}

/**
 * Get a test user configuration by scenario
 * @param scenario - The test scenario (default, noDefault, noWorkspace, newUser)
 * @returns User configuration
 */
export function getTestUser(
	scenario: "default" | "noDefault" | "noWorkspace" | "newUser",
) {
	return E2E_CONFIG.testUsers[scenario];
}
