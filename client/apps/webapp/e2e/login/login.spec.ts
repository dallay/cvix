import { expect, test } from "@playwright/test";
import {
	mockApiErrors,
	setupBasicMocks,
	setupLoginMocks,
	TEST_USERS,
} from "../helpers";
import { LoginPage } from "./login-page";

/**
 * E2E Tests: User Login Flow (US2)
 *
 * Tests user authentication via the login form.
 * Uses manual API mocking - no real backend required.
 *
 * @see ./login.md for test documentation
 */
test.describe("Login", () => {
	test(
		"User can login with valid credentials",
		{ tag: ["@critical", "@e2e", "@login", "@LOGIN-E2E-001"] },
		async ({ page }) => {
			await setupLoginMocks(page);
			const loginPage = new LoginPage(page);

			await test.step("Navigate to login page", async () => {
				await loginPage.goto();
				await loginPage.verifyPageLoaded();
			});

			await test.step("Enter valid credentials and submit", async () => {
				await loginPage.login(TEST_USERS.existingUser);
			});

			await test.step("Verify successful login", async () => {
				await loginPage.verifyLoginSuccess();
			});
		},
	);

	test(
		"User sees error for invalid credentials",
		{ tag: ["@high", "@e2e", "@login", "@LOGIN-E2E-002"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			await mockApiErrors.invalidCredentials(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();
			await loginPage.login({
				email: "wrong@example.com",
				password: "WrongPassword123!",
			});

			await loginPage.verifyErrorMessage(
				/invalid email or password|login failed/i,
			);
		},
	);

	test(
		"User sees rate limit message after too many attempts",
		{ tag: ["@medium", "@e2e", "@login", "@LOGIN-E2E-003"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			await mockApiErrors.rateLimitExceeded(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();
			await loginPage.login({
				email: "test@example.com",
				password: "password123",
			});

			await loginPage.verifyErrorMessage(
				/too many requests|try again later|login failed/i,
			);
		},
	);

	test(
		"User sees error on network failure",
		{ tag: ["@medium", "@e2e", "@login", "@LOGIN-E2E-004"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			await mockApiErrors.networkError(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();
			await loginPage.login({
				email: "test@example.com",
				password: "password123",
			});

			await loginPage.verifyErrorMessage(
				/network error|connection|try again|failed/i,
			);
		},
	);

	test(
		"User sees validation error for invalid email format",
		{ tag: ["@medium", "@e2e", "@login", "@LOGIN-E2E-005"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();
			await loginPage.fillForm({
				email: "invalid-email",
				password: "password123",
			});
			await loginPage.triggerValidation();

			await loginPage.verifyEmailValidationError();
		},
	);

	test(
		"User can navigate to registration page",
		{ tag: ["@low", "@e2e", "@login", "@LOGIN-E2E-006"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();
			await loginPage.goToRegister();

			await expect(page).toHaveURL(/\/register/);
		},
	);
});

test.describe("Login Security", () => {
	test(
		"Login form prevents XSS attacks",
		{ tag: ["@critical", "@e2e", "@login", "@security", "@LOGIN-E2E-007"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();

			const xssPayload = '<script>alert("XSS")</script>';
			await loginPage.fillForm({
				email: xssPayload,
				password: "password",
			});
			await loginPage.submit();

			// Test fails if a dialog (alert) appears - indicates XSS vulnerability
			try {
				await page.waitForEvent("dialog", { timeout: 100 });
				throw new Error("XSS vulnerability detected - dialog was opened");
			} catch (error) {
				if (error instanceof Error && error.name === "TimeoutError") {
					// Success: no dialog appeared
					return;
				}
				throw error;
			}
		},
	);

	test(
		"Login page uses HTTPS",
		{ tag: ["@critical", "@e2e", "@login", "@security", "@LOGIN-E2E-008"] },
		async ({ page }) => {
			// Skip in HTTP mode (CI or FORCE_HTTP)
			if (process.env.CI || process.env.FORCE_HTTP === "true") {
				test.skip();
			}

			await setupBasicMocks(page);
			const loginPage = new LoginPage(page);

			await loginPage.goto();

			const url = new URL(page.url());
			expect(url.protocol).toBe("https:");
		},
	);
});
