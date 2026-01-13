import { expect, test } from "@playwright/test";

import {
	mockApiErrors,
	setupBasicMocks,
	setupRegisterMocks,
	TEST_USERS,
} from "../helpers";
import { RegisterPage } from "./register-page";

/**
 * E2E Tests: User Registration Flow (US1)
 *
 * Tests user account creation via the registration form.
 * Uses manual API mocking - no real backend required.
 *
 * @see ./register.md for test documentation
 */
test.describe("Registration", () => {
	test(
		"User can register with valid data",
		{ tag: ["@critical", "@e2e", "@register", "@REGISTER-E2E-001"] },
		async ({ page }) => {
			await setupRegisterMocks(page);
			const registerPage = new RegisterPage(page);

			await test.step("Navigate to registration page", async () => {
				await registerPage.goto();
				await registerPage.verifyPageLoaded();
			});

			await test.step("Fill registration form with valid data", async () => {
				await registerPage.register(TEST_USERS.newUser);
			});

			await test.step("Verify successful registration", async () => {
				await registerPage.verifyRegistrationSuccess();
			});
		},
	);

	test(
		"User sees error for existing email",
		{ tag: ["@high", "@e2e", "@register", "@REGISTER-E2E-002"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			await mockApiErrors.emailAlreadyExists(page);
			const registerPage = new RegisterPage(page);

			await registerPage.goto();
			await registerPage.register(TEST_USERS.existingUser);

			await registerPage.verifyErrorMessage(
				/already exists|registration failed/i,
			);
		},
	);

	test(
		"User sees error for weak password",
		{ tag: ["@medium", "@e2e", "@register", "@REGISTER-E2E-003"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const registerPage = new RegisterPage(page);

			await registerPage.goto();
			await registerPage.emailInput.fill("test@example.com");
			await registerPage.firstNameInput.fill("Test");
			await registerPage.lastNameInput.fill("User");
			await registerPage.passwordInput.fill("weak");
			await registerPage.triggerPasswordValidation();

			await registerPage.verifyWeakPasswordError();
		},
	);

	test(
		"User sees error for mismatched passwords",
		{ tag: ["@medium", "@e2e", "@register", "@REGISTER-E2E-004"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const registerPage = new RegisterPage(page);

			await registerPage.goto();
			await registerPage.fillMismatchedPasswords(
				"SecurePassword123!",
				"DifferentPassword123!",
			);
			await registerPage.triggerConfirmPasswordValidation();

			await registerPage.verifyPasswordMismatchError();
		},
	);

	test(
		"User sees required field errors for empty form",
		{ tag: ["@medium", "@e2e", "@register", "@REGISTER-E2E-005"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const registerPage = new RegisterPage(page);

			await registerPage.goto();
			await registerPage.submit();

			await registerPage.verifyRequiredFieldError();
		},
	);

	test(
		"User can navigate to login page",
		{ tag: ["@low", "@e2e", "@register", "@REGISTER-E2E-006"] },
		async ({ page }) => {
			await setupBasicMocks(page);
			const registerPage = new RegisterPage(page);

			await registerPage.goto();
			await registerPage.goToLogin();

			await expect(page).toHaveURL(/\/login/);
		},
	);
});
