import type { Page } from "@playwright/test";

/**
 * E2E Test Helpers
 *
 * Shared utilities for E2E tests:
 * - Test data generation
 * - API mocking helpers
 * - Setup/teardown utilities
 */

// =============================================================================
// TEST DATA
// =============================================================================

const API_CONTENT_TYPE = "application/vnd.api.v1+json";
const FIXED_TIMESTAMP = "2024-01-01T00:00:00.000Z";

/**
 * Test user credentials for authentication tests
 */
export const TEST_USERS = {
	/** Existing user for login tests */
	existingUser: {
		email: "john.doe@profiletailors.com",
		password: "S3cr3tP@ssw0rd*123",
		firstName: "John",
		lastName: "Doe",
	},
	/** New user for registration tests */
	newUser: {
		email: "jane.doe@profiletailors.com",
		password: "S3cr3tP@ssw0rd*123",
		firstName: "Jane",
		lastName: "Doe",
	},
} as const;

// =============================================================================
// DATA GENERATION
// =============================================================================

/**
 * Generate a unique email for testing
 */
export function generateUniqueEmail(): string {
	return `test.${Date.now()}@example.com`;
}

/**
 * Test user data structure
 */
type TestUser = {
	email: string;
	password: string;
	firstName: string;
	lastName: string;
};

/**
 * Generate test user data with unique email
 */
export function generateTestUser(): TestUser {
	return {
		email: generateUniqueEmail(),
		password: "S3cr3tP@ssw0rd*123",
		firstName: "Test",
		lastName: "User",
	};
}

/**
 * Generate a strong password for testing
 */
export function generateStrongPassword(): string {
	return `Test${Date.now()}!@#`;
}

// =============================================================================
// MOCK RESPONSES
// =============================================================================

const MOCK_RESPONSES = {
	healthCheck: {
		status: 200,
		body: "OK",
	},
	accountUnauthenticated: {
		status: 401,
		body: {
			type: "https://profiletailors.com/problems/unauthorized",
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
					createdAt: FIXED_TIMESTAMP,
					updatedAt: FIXED_TIMESTAMP,
				},
			],
			meta: {
				total: 1,
				hasMore: false,
			},
		},
	},
};

// =============================================================================
// API MOCKING SETUP
// =============================================================================

/**
 * Set XSRF cookie in browser context
 */
async function setupCsrfToken(page: Page): Promise<void> {
	const csrfToken = `test-xsrf-token-${Date.now()}`;
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
}

/**
 * Sets up basic API mocks (health-check and account)
 * Useful for tests that only need the page to load
 */
export async function setupBasicMocks(page: Page): Promise<void> {
	await setupCsrfToken(page);

	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

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
 * Sets up API mocking for login flow tests
 *
 * Note: This mock maintains stateful behavior via the `isLoggedIn` flag.
 * After a successful POST to /api/auth/login, subsequent calls to /api/account
 * will return authenticated user data instead of 401 Unauthorized.
 */
export async function setupLoginMocks(page: Page): Promise<void> {
	let isLoggedIn = false; // Stateful: tracks authentication status across mocked API calls
	await setupCsrfToken(page);

	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

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

	await page.route("**/api/workspace**", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.workspaces.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(MOCK_RESPONSES.workspaces.body),
		});
	});

	await page.route("**/api/auth/token/refresh", async (route) => {
		if (isLoggedIn) {
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
			await route.fulfill({
				status: 401,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://profiletailors.com/problems/unauthorized",
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
 * Sets up API mocking for registration flow tests
 */
export async function setupRegisterMocks(page: Page): Promise<void> {
	await setupCsrfToken(page);

	await page.route("**/api/health-check", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.healthCheck.status,
			contentType: API_CONTENT_TYPE,
			body: MOCK_RESPONSES.healthCheck.body,
		});
	});

	await page.route("**/api/account", async (route) => {
		const response = MOCK_RESPONSES.accountUnauthenticated;
		await route.fulfill({
			status: response.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(response.body),
		});
	});

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

	// Registration also needs workspace mock for redirect after success
	await page.route("**/api/workspace**", async (route) => {
		await route.fulfill({
			status: MOCK_RESPONSES.workspaces.status,
			contentType: API_CONTENT_TYPE,
			body: JSON.stringify(MOCK_RESPONSES.workspaces.body),
		});
	});
}

// =============================================================================
// ERROR MOCKING
// =============================================================================

/**
 * Mock API error responses for testing error handling
 */
export const mockApiErrors = {
	/** Mock invalid credentials response (401) */
	async invalidCredentials(page: Page): Promise<void> {
		await page.unroute("**/api/auth/login");
		await page.route("**/api/auth/login", async (route) => {
			await route.fulfill({
				status: 401,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://profiletailors.com/problems/invalid-credentials",
					title: "Invalid Credentials",
					status: 401,
					detail: "The email or password you entered is incorrect.",
					instance: "/api/auth/login",
				}),
			});
		});
	},

	/** Mock rate limit exceeded response (429) */
	async rateLimitExceeded(page: Page): Promise<void> {
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
					type: "https://profiletailors.com/problems/rate-limit-exceeded",
					title: "Too Many Requests",
					status: 429,
					detail:
						"You have exceeded the rate limit. Please try again in 60 seconds.",
					instance: "/api/auth/login",
				}),
			});
		});
	},

	/** Mock network error (connection failed) */
	async networkError(page: Page): Promise<void> {
		await page.unroute("**/api/auth/**");
		await page.route("**/api/auth/**", (route) => route.abort("failed"));
	},

	/** Mock email already exists response (409) */
	async emailAlreadyExists(page: Page): Promise<void> {
		await page.unroute("**/api/auth/register");
		await page.route("**/api/auth/register", async (route) => {
			await route.fulfill({
				status: 409,
				contentType: API_CONTENT_TYPE,
				body: JSON.stringify({
					type: "https://profiletailors.com/problems/email-already-exists",
					title: "Conflict",
					status: 409,
					detail: "An account with this email address already exists.",
					instance: "/api/auth/register",
				}),
			});
		});
	},
};

// =============================================================================
// UI HELPERS
// =============================================================================

/**
 * Helper to perform login via UI (legacy - prefer LoginPage.login())
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
 * Helper to perform registration via UI (legacy - prefer RegisterPage.register())
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
	await page.getByRole("checkbox", { name: /terms|accept|agree/i }).check();
	await page.getByRole("button", { name: /create account/i }).click();
}

// =============================================================================
// RESUME TEST DATA
// =============================================================================

/**
 * Sample resume data for testing
 */
export const SAMPLE_RESUME = {
	basics: {
		name: "John Doe",
		label: "Senior Software Engineer",
		email: "john.doe@example.com",
		phone: "+1-555-0100",
		url: "https://johndoe.dev",
		summary:
			"Experienced software engineer with 10+ years building scalable web applications.",
		location: {
			address: "123 Tech Street",
			city: "San Francisco",
			region: "CA",
			postalCode: "94105",
			countryCode: "US",
		},
	},
} as const;

/**
 * Generate test resume data
 */
export function generateTestResume(overrides = {}): typeof SAMPLE_RESUME {
	return {
		...SAMPLE_RESUME,
		...overrides,
	};
}

// =============================================================================
// RESUME MOCKS
// =============================================================================

/**
 * Setup basic mocks for resume page (storage persistence)
 */
export async function setupResumeMocks(page: Page): Promise<void> {
	// Initialize localStorage with an empty resume ONLY if no data exists
	// This prevents overwriting saved data on page reloads
	await page.addInitScript(() => {
		const RESUME_KEY = "cvix:resume";

		// Only initialize if localStorage is empty
		if (!localStorage.getItem(RESUME_KEY)) {
			const emptyResume = {
				basics: {
					name: "",
					label: "",
					image: "",
					email: "",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "",
						region: "",
					},
					profiles: [],
				},
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};

			localStorage.setItem(RESUME_KEY, JSON.stringify(emptyResume));
		}
	});
}

/**
 * Setup authentication + resume page access
 */
export async function setupAuthenticatedResumeMocks(page: Page): Promise<void> {
	await setupBasicMocks(page);
	await setupLoginMocks(page);
	await setupResumeMocks(page);
}
