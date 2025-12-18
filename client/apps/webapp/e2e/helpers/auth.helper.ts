import type { Page } from "@playwright/test";

/**
 * Manual API Mocking Helpers for E2E Tests
 *
 * These helpers use Playwright's route API to mock API responses,
 * allowing E2E tests to run WITHOUT a real backend.
 *
 * This approach is more reliable than HAR-based mocking because:
 * - No XSRF token matching issues
 * - More predictable request/response handling
 * - Easier to customize for different test scenarios
 */

const API_CONTENT_TYPE = "application/vnd.api.v1+json";
/**
 * Test user credentials for authentication tests
 */
export const TEST_USERS = {
	/**
	 * Existing user for login tests
	 */
	existingUser: {
		email: "john.doe@cvix.com",
		password: "S3cr3tP@ssw0rd*123",
		firstName: "John",
		lastName: "Doe",
	},
	/**
	 * New user for registration tests
	 */
	newUser: {
		email: "jane.doe@cvix.com",
		password: "S3cr3tP@ssw0rd*123",
		firstName: "Jane",
		lastName: "Doe",
	},
} as const;

/**
 * Mock responses for API endpoints
 */
const MOCK_RESPONSES = {
	healthCheck: {
		status: 200,
		body: "OK",
	},
	accountUnauthenticated: {
		status: 401,
		body: {
			type: "https://cvix.com/problems/unauthorized",
			title: "Unauthorized",
			status: 401,
			detail: "Authentication required",
			instance: "/api/account",
		},
	},
	accountAuthenticated: (user: typeof TEST_USERS.existingUser) => ({
		status: 200,
		body: {
			id: "550e8400-e29b-41d4-a716-446655440000",
			username: user.email,
			email: user.email,
			firstname: user.firstName,
			lastname: user.lastName,
			authorities: ["ROLE_USER"],
		},
	}),
	loginSuccess: (user: typeof TEST_USERS.existingUser) => ({
		status: 200,
		body: {
			accessToken: "mock-access-token-12345",
			expiresIn: 3600,
			tokenType: "Bearer",
			user: {
				id: "550e8400-e29b-41d4-a716-446655440000",
				email: user.email,
				firstname: user.firstName,
				lastname: user.lastName,
				displayName: `${user.firstName} ${user.lastName}`,
				accountStatus: "ACTIVE",
			},
		},
	}),
	registerSuccess: (user: typeof TEST_USERS.newUser) => ({
		status: 201,
		body: {
			accessToken: "mock-access-token-67890",
			expiresIn: 3600,
			tokenType: "Bearer",
			user: {
				id: "660e8400-e29b-41d4-a716-446655440001",
				email: user.email,
				firstname: user.firstName,
				lastname: user.lastName,
				displayName: `${user.firstName} ${user.lastName}`,
				accountStatus: "PENDING_EMAIL_VERIFICATION",
			},
		},
	}),
	workspaces: {
		status: 200,
		body: {
			data: [
				{
					id: "770e8400-e29b-41d4-a716-446655440002",
					name: "Personal",
					description: null,
					isDefault: true,
					ownerId: "550e8400-e29b-41d4-a716-446655440000",
					createdAt: new Date().toISOString(),
					updatedAt: new Date().toISOString(),
				},
			],
			meta: {
				total: 1,
				hasMore: false,
			},
		},
	},
};

/**
 * Sets up API mocking for login flow tests.
 *
 * Mocks the following endpoints:
 * - GET /api/health-check
 * - GET /api/account (returns 401 when not logged in, 200 after login)
 * - POST /api/auth/login
 * - GET /api/workspace
 *
 * @param page - Playwright page instance
 */
export async function setupLoginMocks(page: Page): Promise<void> {
	let isLoggedIn = false;
	const csrfToken = `test-xsrf-token-${Date.now()}`;

	// Set XSRF cookie in browser context
	await page.context().addCookies([
		{
			name: "XSRF-TOKEN",
			value: csrfToken,
			domain: "localhost",
			path: "/",
			secure: true,
			sameSite: "Lax",
		},
	]);

	// Mock health-check
	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

	// Mock account endpoint - returns 401 if not logged in, 200 if logged in
	await page.route("**/api/account", async (route) => {
		if (isLoggedIn) {
			const response = MOCK_RESPONSES.accountAuthenticated(
				TEST_USERS.existingUser,
			);
			await route.fulfill({
				status: response.status,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify(response.body),
			});
		} else {
			const response = MOCK_RESPONSES.accountUnauthenticated;
			await route.fulfill({
				status: response.status,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify(response.body),
			});
		}
	});

	// Mock login endpoint
	await page.route("**/api/auth/login", async (route) => {
		if (route.request().method() === "POST") {
			isLoggedIn = true;
			const response = MOCK_RESPONSES.loginSuccess(TEST_USERS.existingUser);
			await route.fulfill({
				status: response.status,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify(response.body),
			});
		} else {
			await route.continue();
		}
	});

	// Mock workspace endpoint
	await page.route("**/api/workspace**", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.workspaces.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(MOCK_RESPONSES.workspaces.body),
		});
	});

	// Mock token refresh endpoint - returns 401 when not logged in (no valid refresh token)
	// This prevents the app from hanging when it tries to refresh after a 401 on /account
	await page.route("**/api/auth/token/refresh", async (route) => {
		if (isLoggedIn) {
			// If logged in, refresh should succeed with new tokens
			await route.fulfill({
				status: 200,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					accessToken: "refreshed-access-token-12345",
					expiresIn: 3600,
					tokenType: "Bearer",
				}),
			});
		} else {
			// If not logged in, refresh should fail with 401
			await route.fulfill({
				status: 401,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://cvix.com/problems/unauthorized",
					title: "Unauthorized",
					status: 401,
					detail: "No valid refresh token",
					instance: "/api/auth/token/refresh",
				}),
			});
		}
	});
}

/**
 * Sets up API mocking for registration flow tests.
 *
 * Mocks the following endpoints:
 * - GET /api/health-check
 * - GET /api/account (returns 401)
 * - POST /api/auth/register
 *
 * @param page - Playwright page instance
 */
export async function setupRegisterMocks(page: Page): Promise<void> {
	const csrfToken = `test-xsrf-token-${Date.now()}`;

	// Set XSRF cookie in browser context
	await page.context().addCookies([
		{
			name: "XSRF-TOKEN",
			value: csrfToken,
			domain: "localhost",
			path: "/",
			secure: true,
			sameSite: "Lax",
		},
	]);

	// Mock health-check
	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

	// Mock account endpoint - always returns 401 for registration flow
	await page.route("**/api/account", async (route) => {
		const response = MOCK_RESPONSES.accountUnauthenticated;
		await route.fulfill({
			status: response.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(response.body),
		});
	});

	// Mock register endpoint
	await page.route("**/api/auth/register", async (route) => {
		if (route.request().method() === "POST") {
			const response = MOCK_RESPONSES.registerSuccess(TEST_USERS.newUser);
			await route.fulfill({
				status: response.status,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify(response.body),
			});
		} else {
			await route.continue();
		}
	});
}

/**
 * Sets up custom API route handlers for error scenario testing.
 *
 * Use this when you need to simulate specific error conditions.
 *
 * IMPORTANT: These functions use `page.unroute()` to remove any existing
 * handlers for the same URL pattern before registering the error handler.
 * This ensures the error handler takes precedence even if success handlers
 * were registered earlier (e.g., in beforeEach hooks).
 */
export const mockApiErrors = {
	/**
	 * Mock invalid credentials response
	 */
	async invalidCredentials(page: Page): Promise<void> {
		// Remove any existing handlers for this route to ensure our error handler takes precedence
		await page.unroute("**/api/auth/login");
		await page.route("**/api/auth/login", async (route) => {
			await route.fulfill({
				status: 401,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://cvix.com/problems/invalid-credentials",
					title: "Invalid Credentials",
					status: 401,
					detail: "The email or password you entered is incorrect.",
					instance: "/api/auth/login",
				}),
			});
		});
	},

	/**
	 * Mock rate limit exceeded response
	 */
	async rateLimitExceeded(page: Page): Promise<void> {
		// Remove any existing handlers for this route to ensure our error handler takes precedence
		await page.unroute("**/api/auth/login");
		await page.route("**/api/auth/login", async (route) => {
			await route.fulfill({
				status: 429,
				contentType: API_CONTENT_TYPE,
				headers: {
					"Retry-After": "60",
					"X-RateLimit-Limit": "5",
					"X-RateLimit-Remaining": "0",
				},
				body: JSON.stringify({
					type: "https://cvix.com/problems/rate-limit-exceeded",
					title: "Too Many Requests",
					status: 429,
					detail:
						"You have exceeded the rate limit. Please try again in 60 seconds.",
					instance: "/api/auth/login",
				}),
			});
		});
	},

	/**
	 * Mock network error (connection refused)
	 * Note: Only aborts auth endpoints, not health-check (so page can load)
	 */
	async networkError(page: Page): Promise<void> {
		// Remove any existing handlers for auth routes
		await page.unroute("**/api/auth/**");
		await page.unroute("**/api/auth/login");
		await page.route("**/api/auth/**", (route) => route.abort("failed"));
	},

	/**
	 * Mock email already exists response (for registration)
	 */
	async emailAlreadyExists(page: Page): Promise<void> {
		// Remove any existing handlers for this route to ensure our error handler takes precedence
		await page.unroute("**/api/auth/register");
		await page.route("**/api/auth/register", async (route) => {
			await route.fulfill({
				status: 409,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://cvix.com/problems/email-already-exists",
					title: "Conflict",
					status: 409,
					detail:
						"An account with this email address already exists. Please use a different email or try logging in.",
					instance: "/api/auth/register",
				}),
			});
		});
	},
};

/**
 * Sets up basic API mocks (health-check and account) without login state.
 * Useful for tests that only need the page to load without full auth flow.
 */
export async function setupBasicMocks(page: Page): Promise<void> {
	const csrfToken = `test-xsrf-token-${Date.now()}`;

	// Set XSRF cookie in browser context
	await page.context().addCookies([
		{
			name: "XSRF-TOKEN",
			value: csrfToken,
			domain: "localhost",
			path: "/",
			secure: true,
			sameSite: "Lax",
		},
	]);

	// Mock health-check
	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

	// Mock account endpoint - returns 401
	await page.route("**/api/account", async (route) => {
		const response = MOCK_RESPONSES.accountUnauthenticated;
		await route.fulfill({
			status: response.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(response.body),
		});
	});
}

/**
 * Helper to perform login via UI
 *
 * @param page - Playwright page instance
 * @param credentials - User credentials (defaults to TEST_USERS.existingUser)
 */
export async function loginViaUI(
	page: Page,
	credentials = TEST_USERS.existingUser,
): Promise<void> {
	if (!page.url().includes("/login")) {
		await page.goto("/login");
	}

	await page.getByLabel(/email/i).fill(credentials.email);
	await page.getByLabel(/password/i).fill(credentials.password);
	await page.getByRole("button", { name: /sign in/i }).click();
}

/**
 * Helper to perform registration via UI
 *
 * @param page - Playwright page instance
 * @param userData - User data for registration (defaults to TEST_USERS.newUser)
 */
export async function registerViaUI(
	page: Page,
	userData = TEST_USERS.newUser,
): Promise<void> {
	if (!page.url().includes("/register")) {
		await page.goto("/register");
	}

	await page.getByLabel(/email/i).fill(userData.email);
	await page.getByLabel(/first name/i).fill(userData.firstName);
	await page.getByLabel(/last name/i).fill(userData.lastName);
	await page.getByLabel(/^password$/i).fill(userData.password);
	await page.getByLabel(/confirm password/i).fill(userData.password);
	await page.getByRole("button", { name: /create account/i }).click();
}
