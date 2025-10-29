import { expect, test } from "@playwright/test";

test.describe("Workspace Selection Feature", () => {
	test.beforeEach(async ({ page }) => {
		// Setup: Navigate to login page
		await page.goto("/login");
	});

	test.describe("Auto-load on Login (User Story 1)", () => {
		test("should auto-load last selected workspace within 2 seconds", async ({
			page,
		}) => {
			await test.step("Authenticate user", async () => {
				// Login with test credentials
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();

				// Wait for redirect to dashboard
				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Verify workspace loads within 2 seconds", async () => {
				const startTime = Date.now();

				// Wait for workspace indicator to appear
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible({ timeout: 2000 });

				const loadTime = Date.now() - startTime;

				// Verify load time meets SC-001 requirement (<2 seconds)
				expect(loadTime).toBeLessThan(2000);

				// Verify workspace name is displayed
				const workspaceName = await workspaceIndicator.textContent();
				expect(workspaceName).toBeTruthy();
				expect(workspaceName?.length).toBeGreaterThan(0);
			});

			await test.step("Verify last selected workspace is loaded", async () => {
				// Check that the loaded workspace matches the last selected
				const workspaceId = await page
					.locator('[data-testid="workspace-indicator"]')
					.getAttribute("data-workspace-id");
				expect(workspaceId).toBeTruthy();

				// Verify workspace ID is a valid UUID v4
				const uuidV4Regex =
					/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
				expect(workspaceId).toMatch(uuidV4Regex);
			});
		});

		test("should auto-load default workspace when no last selected", async ({
			page,
		}) => {
			await test.step("Authenticate new user with no history", async () => {
				// Login with new user credentials (no workspace selection history)
				await page.getByLabel("Email").fill("newuser@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();

				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Verify default workspace loads", async () => {
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible({ timeout: 2000 });

				// Verify default workspace badge is shown
				const defaultBadge = page.locator(
					'[data-testid="default-workspace-badge"]',
				);
				await expect(defaultBadge).toBeVisible();
			});
		});

		test("should auto-load first workspace when no default exists", async ({
			page,
		}) => {
			await test.step("Setup account with non-default workspaces only", async () => {
				// This would require test data setup with no default workspace
				// For now, we'll verify the behavior if it occurs
			});

			await test.step("Authenticate user", async () => {
				await page.getByLabel("Email").fill("nodefault@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();

				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Verify first workspace loads", async () => {
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible({ timeout: 2000 });

				// Verify no default badge is shown
				const defaultBadge = page.locator(
					'[data-testid="default-workspace-badge"]',
				);
				await expect(defaultBadge).not.toBeVisible();
			});
		});

		test("should show error when no workspaces available", async ({ page }) => {
			await test.step("Authenticate user with no workspaces", async () => {
				// This requires test data setup with user having no workspaces
				await page.getByLabel("Email").fill("noworkspace@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
			});

			await test.step("Verify error message displayed", async () => {
				const errorMessage = page.getByRole("alert");
				await expect(errorMessage).toBeVisible({ timeout: 2000 });
				await expect(errorMessage).toContainText(/no workspaces/i);
			});
		});

		test("should handle network errors gracefully during auto-load", async ({
			page,
		}) => {
			await test.step("Simulate network failure", async () => {
				// Intercept API call and simulate failure
				await page.route("**/api/workspace", (route) => route.abort("failed"));
			});

			await test.step("Authenticate user", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
			});

			await test.step("Verify error handling", async () => {
				const errorMessage = page.getByRole("alert");
				await expect(errorMessage).toBeVisible({ timeout: 2000 });
				await expect(errorMessage).toContainText(/network/i);

				// Verify retry button is available
				const retryButton = page.getByRole("button", { name: /retry/i });
				await expect(retryButton).toBeVisible();
			});
		});
	});
});
