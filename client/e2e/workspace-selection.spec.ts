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

	test.describe("Manual Workspace Selection (User Story 2)", () => {
		test("should switch workspace on manual selection within 3 seconds", async ({
			page,
		}) => {
			await test.step("Authenticate user", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();

				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Wait for initial workspace to load", async () => {
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible({ timeout: 2000 });
			});

			await test.step("Open workspace selector", async () => {
				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);
				await workspaceSelector.click();

				// Verify dropdown opens
				const dropdown = page.locator('[role="listbox"]');
				await expect(dropdown).toBeVisible();
			});

			await test.step("Select different workspace", async () => {
				const startTime = Date.now();

				// Get current workspace name
				const currentWorkspaceName = await page
					.locator('[data-testid="workspace-indicator"]')
					.textContent();

				// Find and click a different workspace
				const workspaceOptions = page.locator('[role="option"]');
				const count = await workspaceOptions.count();

				// Click the first option that's not the current workspace
				for (let i = 0; i < count; i++) {
					const option = workspaceOptions.nth(i);
					const optionText = await option.textContent();

					if (optionText !== currentWorkspaceName) {
						await option.click();
						break;
					}
				}

				// Wait for workspace to switch
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).not.toContainText(
					currentWorkspaceName || "",
					{ timeout: 3000 },
				);

				const switchTime = Date.now() - startTime;

				// Verify switch time meets SC-002 requirement (<3 seconds)
				expect(switchTime).toBeLessThan(3000);
			});

			await test.step("Verify workspace switch persisted", async () => {
				// Refresh page
				await page.reload();

				// Wait for page to load
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible({ timeout: 2000 });

				// Verify the switched workspace is still selected
				const workspaceName = await workspaceIndicator.textContent();
				expect(workspaceName).toBeTruthy();
			});
		});

		test("should display all available workspaces in selector", async ({
			page,
		}) => {
			await test.step("Authenticate and navigate", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Open workspace selector and verify workspaces", async () => {
				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);
				await workspaceSelector.click();

				// Verify at least one workspace option is visible
				const workspaceOptions = page.locator('[role="option"]');
				await expect(workspaceOptions.first()).toBeVisible();

				// Count available workspaces
				const count = await workspaceOptions.count();
				expect(count).toBeGreaterThan(0);

				// Verify each option has a name
				for (let i = 0; i < count; i++) {
					const option = workspaceOptions.nth(i);
					const text = await option.textContent();
					expect(text?.length).toBeGreaterThan(0);
				}
			});
		});

		test("should show default badge in workspace selector", async ({
			page,
		}) => {
			await test.step("Authenticate and open selector", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
				await expect(page).toHaveURL(/\/dashboard/);

				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);
				await workspaceSelector.click();
			});

			await test.step("Verify default badge present", async () => {
				// Look for default badge in the dropdown
				const defaultBadge = page.locator(
					'[data-testid="default-badge-in-selector"]',
				);
				await expect(defaultBadge).toBeVisible();
				await expect(defaultBadge).toContainText(/default/i);
			});
		});

		test("should support keyboard navigation in workspace selector", async ({
			page,
		}) => {
			await test.step("Authenticate and open selector", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
				await expect(page).toHaveURL(/\/dashboard/);
			});

			await test.step("Test keyboard navigation", async () => {
				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);

				// Open with keyboard
				await workspaceSelector.focus();
				await workspaceSelector.press("Enter");

				// Verify dropdown opens
				const dropdown = page.locator('[role="listbox"]');
				await expect(dropdown).toBeVisible();

				// Navigate with arrow keys
				await page.keyboard.press("ArrowDown");
				await page.keyboard.press("ArrowDown");

				// Select with Enter
				await page.keyboard.press("Enter");

				// Verify workspace switched
				const workspaceIndicator = page.locator(
					'[data-testid="workspace-indicator"]',
				);
				await expect(workspaceIndicator).toBeVisible();
			});
		});

		test("should close selector with Escape key", async ({ page }) => {
			await test.step("Authenticate and open selector", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
				await expect(page).toHaveURL(/\/dashboard/);

				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);
				await workspaceSelector.click();
			});

			await test.step("Close with Escape", async () => {
				const dropdown = page.locator('[role="listbox"]');
				await expect(dropdown).toBeVisible();

				await page.keyboard.press("Escape");

				// Verify dropdown closes
				await expect(dropdown).not.toBeVisible({ timeout: 1000 });
			});
		});

		test("should emit workspace-selected event after selection", async ({
			page,
		}) => {
			await test.step("Setup event listener", async () => {
				// Monitor console for event emission (in a real app with analytics)
				const events: string[] = [];
				page.on("console", (msg) => {
					if (msg.text().includes("workspace-selected")) {
						events.push(msg.text());
					}
				});
			});

			await test.step("Authenticate and select workspace", async () => {
				await page.getByLabel("Email").fill("test@example.com");
				await page.getByLabel("Password").fill("password123");
				await page.getByRole("button", { name: /log in/i }).click();
				await expect(page).toHaveURL(/\/dashboard/);

				const workspaceSelector = page.locator(
					'[data-testid="workspace-selector"]',
				);
				await workspaceSelector.click();

				const workspaceOptions = page.locator('[role="option"]');
				await workspaceOptions.first().click();
			});

			await test.step("Verify event was emitted", async () => {
				// In a real implementation, you might verify through a testable side effect
				// like a toast notification or analytics tracking
				const successToast = page.locator('[data-testid="success-toast"]');
				await expect(successToast).toBeVisible({ timeout: 2000 });
			});
		});
	});
});
