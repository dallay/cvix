import { expect, test } from "@playwright/test";
import {
	loginViaUI,
	mockApiErrors,
	setupBasicMocks,
	setupLoginMocks,
	setupRegisterMocks,
	TEST_USERS,
} from "./helpers/auth.helper";

/**
 * End-to-End Tests for Authentication Flows
 *
 * These tests use manual API mocking (via Playwright route API) to run
 * WITHOUT a real backend. This approach is more reliable than HAR-based
 * mocking because it doesn't depend on matching exact request headers/cookies.
 *
 * **Test Coverage**:
 * - US1: User Registration Flow
 * - US2: User Login Flow
 * - US6: User Logout Flow (partial - UI interaction only)
 * - Form Validation (client-side)
 * - Error Handling (network errors, invalid credentials)
 */

test.describe("User Login Flow (US2)", () => {
	test.beforeEach(async ({ page }) => {
		await setupLoginMocks(page);
	});

	test("should successfully login with valid credentials", async ({ page }) => {
		await test.step("Navigate to login page", async () => {
			await page.goto("/login");
			await expect(page).toHaveURL(/\/login/);
			await expect(
				page.getByRole("heading", { name: /welcome back/i }),
			).toBeVisible();
		});

		await test.step("Enter valid credentials", async () => {
			await page.getByLabel(/email/i).fill(TEST_USERS.existingUser.email);
			await page.getByLabel(/password/i).fill(TEST_USERS.existingUser.password);
		});

		await test.step("Submit login form", async () => {
			await page.getByRole("button", { name: /sign in/i }).click();
		});

		await test.step("Verify successful login", async () => {
			// Should redirect to dashboard or workspace selection
			await expect(page).toHaveURL(/\/dashboard|\/workspace/);
		});
	});

	test("should show error for invalid credentials", async ({ page }) => {
		// Set up basic mocks first, then override with error response
		await setupBasicMocks(page);
		await mockApiErrors.invalidCredentials(page);

		await page.goto("/login");
		await page.getByLabel(/email/i).fill("wrong@example.com");
		await page.getByLabel(/password/i).fill("WrongPassword123!");
		await page.getByRole("button", { name: /sign in/i }).click();

		// The app converts 401 to "Invalid email or password" via InvalidCredentialsError
		await expect(
			page.getByText(/invalid email or password|login failed/i),
		).toBeVisible();
	});

	test("should handle network errors gracefully", async ({ page }) => {
		// Set up basic mocks first (so page can load), then mock network error for auth
		await setupBasicMocks(page);
		await mockApiErrors.networkError(page);

		await page.goto("/login");
		await page.getByLabel(/email/i).fill("test@example.com");
		await page.getByLabel(/password/i).fill("password123");
		await page.getByRole("button", { name: /sign in/i }).click();

		await expect(
			page.getByText(/network error|connection|try again|failed/i),
		).toBeVisible();
	});

	test("should show rate limit message after too many attempts", async ({
		page,
	}) => {
		// Set up basic mocks first, then override with rate limit error
		await setupBasicMocks(page);
		await mockApiErrors.rateLimitExceeded(page);

		await page.goto("/login");
		await page.getByLabel(/email/i).fill("test@example.com");
		await page.getByLabel(/password/i).fill("password123");
		await page.getByRole("button", { name: /sign in/i }).click();

		// The app converts 429 to "Too many requests. Please try again later."
		await expect(
			page.getByText(/too many requests|try again later|login failed/i),
		).toBeVisible();
	});
});

test.describe("User Registration Flow (US1)", () => {
	test.beforeEach(async ({ page }) => {
		await setupRegisterMocks(page);
	});

	test("should successfully register a new user", async ({ page }) => {
		await test.step("Navigate to registration page", async () => {
			await page.goto("/register");
			await expect(page).toHaveURL(/\/register/);
			await expect(
				page.getByRole("heading", { name: /create your account/i }),
			).toBeVisible();
		});

		await test.step("Fill registration form with valid data", async () => {
			await page.getByLabel(/email/i).fill(TEST_USERS.newUser.email);
			await page.getByLabel(/first name/i).fill(TEST_USERS.newUser.firstName);
			await page.getByLabel(/last name/i).fill(TEST_USERS.newUser.lastName);
			await page.getByLabel(/^password$/i).fill(TEST_USERS.newUser.password);
			await page
				.getByLabel(/confirm password/i)
				.fill(TEST_USERS.newUser.password);
			// Accept terms and conditions
			await page.getByLabel(/terms/i).check();
		});

		await test.step("Submit registration form", async () => {
			await page.getByRole("button", { name: /create account/i }).click();
		});

		await test.step("Verify successful registration", async () => {
			// Should redirect to dashboard or login page
			await expect(page).toHaveURL(/\/dashboard|\/login|\/workspace/);
		});
	});

	test("should prevent registration with existing email", async ({ page }) => {
		// Set up basic mocks first, then override with conflict error
		await setupBasicMocks(page);
		await mockApiErrors.emailAlreadyExists(page);

		await page.goto("/register");
		await page.getByLabel(/email/i).fill(TEST_USERS.existingUser.email);
		await page.getByLabel(/first name/i).fill("Test");
		await page.getByLabel(/last name/i).fill("User");
		await page.getByLabel(/^password$/i).fill("SecurePassword123!");
		await page.getByLabel(/confirm password/i).fill("SecurePassword123!");
		// Also need to accept terms
		await page.getByLabel(/terms/i).check();
		await page.getByRole("button", { name: /create account/i }).click();

		// The app converts 409 to "An account with this email already exists"
		await expect(
			page.getByText(/already exists|registration failed/i),
		).toBeVisible();
	});
});

test.describe("Form Validation (Client-Side)", () => {
	test.beforeEach(async ({ page }) => {
		await setupBasicMocks(page);
	});

	test("should show validation errors for invalid email format", async ({
		page,
	}) => {
		await page.goto("/login");

		await page.getByLabel(/email/i).fill("invalid-email");
		await page.getByLabel(/password/i).fill("password123");
		await page.getByLabel(/password/i).blur();

		await expect(
			page.getByText(/invalid email|email format|valid email/i),
		).toBeVisible();
	});

	test("should show validation errors for weak password on registration", async ({
		page,
	}) => {
		await page.goto("/register");

		await page.getByLabel(/email/i).fill("test@example.com");
		await page.getByLabel(/first name/i).fill("Test");
		await page.getByLabel(/last name/i).fill("User");
		await page.getByLabel(/^password$/i).fill("weak");
		await page.getByLabel(/^password$/i).blur();

		await expect(
			page.getByText(
				/password.*characters|password.*strong|password.*uppercase|password.*number/i,
			),
		).toBeVisible();
	});

	test("should show error when passwords do not match", async ({ page }) => {
		await page.goto("/register");

		await page.getByLabel(/^password$/i).fill("SecurePassword123!");
		await page.getByLabel(/confirm password/i).fill("DifferentPassword123!");
		await page.getByLabel(/confirm password/i).blur();

		await expect(page.getByText(/passwords.*match|don't match/i)).toBeVisible();
	});

	test("should require all registration fields", async ({ page }) => {
		await page.goto("/register");

		// Try to submit empty form
		await page.getByRole("button", { name: /create account/i }).click();

		// Should show required field errors
		await expect(page.getByText(/email.*required|enter.*email/i)).toBeVisible();
	});
});

test.describe("Logout Flow (US6)", () => {
	test.beforeEach(async ({ page }) => {
		// Set up login mocks and perform login
		await setupLoginMocks(page);
		await loginViaUI(page);
		await expect(page).toHaveURL(/\/dashboard|\/workspace/);
	});

	test("should clear storage on logout", async ({ page }) => {
		await test.step("Locate and click logout button", async () => {
			// Try to find user menu or logout button
			const userMenu = page.getByRole("button", {
				name: /profile|account|menu|user/i,
			});
			if (await userMenu.isVisible().catch(() => false)) {
				await userMenu.click();
			}

			// Use first() since there may be multiple logout buttons (sidebar + menu)
			const logoutButton = page
				.getByRole("button", {
					name: /log out|sign out|logout/i,
				})
				.first();
			if (await logoutButton.isVisible().catch(() => false)) {
				await logoutButton.click();
			}
		});

		await test.step("Verify redirect to login or home", async () => {
			await expect(page).toHaveURL(/\/login|\/$/);
		});

		await test.step("Verify session storage is cleared", async () => {
			const sessionStorageLength = await page.evaluate(
				() => window.sessionStorage.length,
			);
			expect(sessionStorageLength).toBe(0);
		});
	});
});

test.describe("Navigation", () => {
	test.beforeEach(async ({ page }) => {
		await setupBasicMocks(page);
	});

	test("should navigate between login and register pages", async ({ page }) => {
		await test.step("Navigate to login page", async () => {
			await page.goto("/login");
			await expect(page).toHaveURL(/\/login/);
		});

		await test.step("Click on Sign up link to go to registration", async () => {
			const registerLink = page.getByRole("link", {
				name: /sign up|register/i,
			});
			await expect(registerLink).toBeVisible();
			await registerLink.click();
			await expect(page).toHaveURL(/\/register/);
		});

		await test.step("Click on Sign in link to return to login", async () => {
			const loginLink = page.getByRole("link", { name: /log in|sign in/i });
			await expect(loginLink).toBeVisible();
			await loginLink.click();
			await expect(page).toHaveURL(/\/login/);
		});
	});
});

test.describe("Security", () => {
	test.beforeEach(async ({ page }) => {
		await setupBasicMocks(page);
	});

	test("should prevent XSS attacks in login form", async ({ page }) => {
		await page.goto("/login");

		const xssPayload = '<script>alert("XSS")</script>';
		await page.getByLabel(/email/i).fill(xssPayload);
		await page.getByLabel(/password/i).fill("password");

		// Set up listener for any dialogs (alerts) - should NOT appear for XSS attempts
		await page.getByRole("button", { name: /sign in/i }).click();

		// Wait for dialog event with explicit timeout - test fails if dialog appears
		try {
			await page.waitForEvent("dialog", { timeout: 100 });
			// If we reach here, a dialog was shown (XSS vulnerability detected)
			throw new Error(
				"Dialog was unexpectedly opened - potential XSS vulnerability",
			);
		} catch (error) {
			// TimeoutError is expected (no dialog = good)
			if (error instanceof Error && error.message.includes("Timeout")) {
				// Success: no dialog appeared within timeout window
				expect(true).toBe(true);
			} else {
				// Re-throw if it's our custom XSS error
				throw error;
			}
		}
	});

	test("should use HTTPS in URLs", async ({ page }) => {
		await page.goto("/login");
		const url = new URL(page.url());
		expect(url.protocol).toBe("https:");
	});
});
