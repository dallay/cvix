import { expect, test } from "@playwright/test";

import { setupLoginMocks, TEST_USERS } from "../helpers";
import { LoginPage } from "../login/login-page";
import { HomePage } from "./home-page";

/**
 * E2E Tests: User Logout Flow (US6)
 *
 * Tests user logout functionality.
 * Uses manual API mocking - no real backend required.
 *
 * @see ./logout.md for test documentation
 */
test.describe("Logout", () => {
	test.beforeEach(async ({ page }) => {
		// Set up login mocks and perform login
		await setupLoginMocks(page);
		const loginPage = new LoginPage(page);
		await loginPage.goto();
		await loginPage.login(TEST_USERS.existingUser);

		// Verify we're authenticated
		await expect(page).toHaveURL(/\/dashboard|\/workspace/);
	});

	test(
		"User can logout and session is cleared",
		{ tag: ["@critical", "@e2e", "@logout", "@LOGOUT-E2E-001"] },
		async ({ page }) => {
			const homePage = new HomePage(page);

			await test.step("Locate and click logout button", async () => {
				await homePage.logout();
			});

			await test.step("Verify redirect to login or home", async () => {
				await homePage.verifyLogoutSuccess();
			});

			await test.step("Verify session storage is cleared", async () => {
				await homePage.verifySessionCleared();
			});
		},
	);
});
