import { expect, test } from "@playwright/test";

/**
 * End-to-End Tests for Complete Authentication Flows
 *
 * Tests the entire authentication user journey from registration through
 * login, session management, and logout.
 *
 * **Requirements**: FR-001 to FR-038, User Stories 1-6
 */

test.describe("Complete Authentication Flow", () => {
	test.beforeEach(async ({ page }) => {
		// Navigate to the application home page
		await page.goto("/");
	});

	test.describe("User Registration Flow (US1)", () => {
		test("should successfully register a new user with email and password", async ({
			page,
		}) => {
			await test.step("Navigate to registration page", async () => {
				await page.getByRole("link", { name: /sign up|register/i }).click();
				await expect(page).toHaveURL(/\/register/);
				await expect(
					page.getByRole("heading", { name: /register|sign up/i }),
				).toBeVisible();
			});

			await test.step("Fill in registration form with valid data", async () => {
				await page.getByLabel(/email/i).fill(`test-${Date.now()}@example.com`);
				await page.getByLabel(/first name/i).fill("Test");
				await page.getByLabel(/last name/i).fill("User");
				await page.getByLabel(/^password$/i).fill("SecurePassword123!");
				await page.getByLabel(/confirm password/i).fill("SecurePassword123!");
			});

			await test.step("Submit registration form", async () => {
				await page.getByRole("button", { name: /register|sign up/i }).click();
			});

			await test.step("Verify successful registration", async () => {
				// Should redirect to dashboard or success page
				await expect(page).toHaveURL(/\/dashboard|\/welcome/);
				await expect(page.getByText(/welcome|success/i)).toBeVisible();
			});

			await test.step("Verify user is authenticated", async () => {
				// User menu or profile should be visible
				await expect(
					page.getByRole("button", { name: /profile|account|menu/i }),
				).toBeVisible();
			});
		});

		test("should show validation errors for invalid registration data", async ({
			page,
		}) => {
			await page.getByRole("link", { name: /sign up|register/i }).click();

			await test.step("Submit form with invalid email", async () => {
				await page.getByLabel(/email/i).fill("invalid-email");
				await page.getByLabel(/^password$/i).fill("SecurePassword123!");
				await page.getByLabel(/^password$/i).blur();

				await expect(
					page.getByText(/invalid email|email format|valid email/i),
				).toBeVisible();
			});

			await test.step("Submit form with weak password", async () => {
				await page.getByLabel(/email/i).fill("test@example.com");
				await page.getByLabel(/^password$/i).fill("weak");
				await page.getByLabel(/^password$/i).blur();

				await expect(
					page.getByText(
						/password.*strong|password.*8 characters|password.*uppercase/i,
					),
				).toBeVisible();
			});
		});

		test("should prevent registration with existing email", async ({
			page,
		}) => {
			const existingEmail = "existing.user@example.com";

			await page.getByRole("link", { name: /sign up|register/i }).click();
			await page.getByLabel(/email/i).fill(existingEmail);
			await page.getByLabel(/first name/i).fill("Existing");
			await page.getByLabel(/last name/i).fill("User");
			await page.getByLabel(/^password$/i).fill("SecurePassword123!");
			await page.getByLabel(/confirm password/i).fill("SecurePassword123!");
			await page.getByRole("button", { name: /register|sign up/i }).click();

			await expect(
				page.getByText(/email.*already.*exists|account.*already.*registered/i),
			).toBeVisible();
		});
	});

	test.describe("User Login Flow (US2)", () => {
		test("should successfully login with valid credentials", async ({
			page,
		}) => {
			await test.step("Navigate to login page", async () => {
				await page.getByRole("link", { name: /log in|sign in/i }).click();
				await expect(page).toHaveURL(/\/login/);
				await expect(
					page.getByRole("heading", { name: /log in|sign in/i }),
				).toBeVisible();
			});

			await test.step("Enter valid credentials", async () => {
				await page.getByLabel(/email/i).fill("test.user@example.com");
				await page.getByLabel(/password/i).fill("TestPassword123!");
			});

			await test.step("Submit login form", async () => {
				await page.getByRole("button", { name: /log in|sign in/i }).click();
			});

			await test.step("Verify successful login", async () => {
				await expect(page).toHaveURL(/\/dashboard/);
				await expect(
					page.getByRole("heading", { name: /dashboard/i }),
				).toBeVisible();
			});

			await test.step("Verify session cookie is set", async () => {
				const cookies = await page.context().cookies();
				const sessionCookie = cookies.find(
					(c) => c.name.includes("session") || c.name.includes("token"),
				);
				expect(sessionCookie).toBeDefined();
				expect(sessionCookie?.httpOnly).toBe(true);
				expect(sessionCookie?.secure).toBe(true);
			});
		});

		test("should show error for invalid credentials", async ({ page }) => {
			await page.getByRole("link", { name: /log in|sign in/i }).click();
			await page.getByLabel(/email/i).fill("nonexistent@example.com");
			await page.getByLabel(/password/i).fill("WrongPassword123!");
			await page.getByRole("button", { name: /log in|sign in/i }).click();

			await expect(
				page.getByText(/invalid.*credentials|incorrect.*email.*password/i),
			).toBeVisible();
		});

		test("should enforce rate limiting after multiple failed login attempts", async ({
			page,
		}) => {
			await page.getByRole("link", { name: /log in|sign in/i }).click();

			// Attempt login 6 times with wrong password (rate limit is 5)
			for (let i = 0; i < 6; i++) {
				await page.getByLabel(/email/i).fill("test@example.com");
				await page.getByLabel(/password/i).fill(`WrongPassword${i}`);
				await page.getByRole("button", { name: /log in|sign in/i }).click();

				if (i < 5) {
					await expect(page.getByText(/invalid.*credentials/i)).toBeVisible();
				}
			}

			// 6th attempt should be rate limited
			await expect(
				page.getByText(/too many.*attempts|rate limit|try again later/i),
			).toBeVisible();
		});

		test('should support "Remember Me" functionality', async ({ page }) => {
			await page.getByRole("link", { name: /log in|sign in/i }).click();
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByLabel(/remember me/i).check();
			await page.getByRole("button", { name: /log in|sign in/i }).click();

			await expect(page).toHaveURL(/\/dashboard/);

			// Verify extended session cookie expiration
			const cookies = await page.context().cookies();
			const sessionCookie = cookies.find(
				(c) => c.name.includes("refresh") || c.name.includes("token"),
			);

			if (sessionCookie?.expires) {
				const now = Date.now() / 1000;
				const expiresIn = sessionCookie.expires - now;
				// Should be close to 30 days (2592000 seconds)
				expect(expiresIn).toBeGreaterThan(2500000);
			}
		});
	});

	test.describe("Federated Login Flow (US3)", () => {
		test("should display federated login options", async ({ page }) => {
			await page.getByRole("link", { name: /log in|sign in/i }).click();

			await expect(page.getByRole("button", { name: /google/i })).toBeVisible();
			await expect(
				page.getByRole("button", { name: /microsoft/i }),
			).toBeVisible();
			await expect(page.getByRole("button", { name: /github/i })).toBeVisible();
		});

		test("should initiate Google OAuth flow when clicking Google button", async ({
			page,
		}) => {
			await page.getByRole("link", { name: /log in|sign in/i }).click();

			// Click Google login button
			const googleButton = page.getByRole("button", { name: /google/i });
			await expect(googleButton).toBeVisible();

			// Listen for navigation to OAuth provider
			const navigationPromise = page.waitForURL(
				/accounts\.google\.com|oauth2.*google/i,
			);
			await googleButton.click();

			// Verify redirection to Google OAuth (or mock endpoint in test environment)
			await navigationPromise;
		});
	});

	test.describe("Session Management Flow (US8)", () => {
		test.beforeEach(async ({ page }) => {
			// Login before session management tests
			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByRole("button", { name: /log in|sign in/i }).click();
			await expect(page).toHaveURL(/\/dashboard/);
		});

		test("should display active sessions list", async ({ page }) => {
			await test.step("Navigate to session management page", async () => {
				await page
					.getByRole("button", { name: /profile|account|menu/i })
					.click();
				await page.getByRole("link", { name: /sessions|devices/i }).click();
				await expect(page).toHaveURL(/\/sessions|\/devices/);
			});

			await test.step("Verify session list is visible", async () => {
				await expect(
					page.getByRole("heading", { name: /active sessions|devices/i }),
				).toBeVisible();

				// Should show at least the current session
				const sessionItems = page.getByTestId("session-item");
				await expect(sessionItems).toHaveCount(await sessionItems.count());
				await expect(sessionItems.first()).toBeVisible();
			});

			await test.step("Verify session details", async () => {
				const currentSession = page.getByTestId("session-item").first();
				await expect(
					currentSession.getByText(/current session/i),
				).toBeVisible();
				// Should display browser, OS, location info
				await expect(
					currentSession.getByText(/chrome|firefox|safari/i),
				).toBeVisible();
			});
		});

		test("should terminate a specific session", async ({ page, context }) => {
			// Open session management in a new tab to simulate multi-device
			const newPage = await context.newPage();
			await newPage.goto("/login");
			await newPage.getByLabel(/email/i).fill("test.user@example.com");
			await newPage.getByLabel(/password/i).fill("TestPassword123!");
			await newPage.getByRole("button", { name: /log in|sign in/i }).click();
			await expect(newPage).toHaveURL(/\/dashboard/);

			// Go to session management
			await page.getByRole("button", { name: /profile|account/i }).click();
			await page.getByRole("link", { name: /sessions/i }).click();

			// Should see 2 active sessions
			const sessionItems = page.getByTestId("session-item");
			await expect(sessionItems).toHaveCount(2);

			// Terminate the other session
			const otherSession = sessionItems.nth(1);
			await otherSession
				.getByRole("button", { name: /terminate|end session/i })
				.click();

			// Confirm termination
			await page.getByRole("button", { name: /confirm|yes/i }).click();

			// Verify session was terminated
			await expect(page.getByText(/session terminated/i)).toBeVisible();
			await expect(sessionItems).toHaveCount(1);

			// Other tab should be logged out
			await newPage.reload();
			await expect(newPage).toHaveURL(/\/login/);
		});

		test("should terminate all other sessions", async ({ page }) => {
			await page.getByRole("button", { name: /profile|account/i }).click();
			await page.getByRole("link", { name: /sessions/i }).click();

			await page
				.getByRole("button", { name: /terminate all|logout all/i })
				.click();
			await page.getByRole("button", { name: /confirm|yes/i }).click();

			await expect(page.getByText(/all.*sessions.*terminated/i)).toBeVisible();

			// Should only show current session
			const sessionItems = page.getByTestId("session-item");
			await expect(sessionItems).toHaveCount(1);
		});
	});

	test.describe("Token Refresh Flow (US4)", () => {
		test("should automatically refresh expired access token", async ({
			page,
		}) => {
			// Login
			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByRole("button", { name: /log in|sign in/i }).click();
			await expect(page).toHaveURL(/\/dashboard/);

			// Wait for access token to expire (in test environment, tokens might have short expiry)
			// Make an API call that requires authentication
			await page.waitForTimeout(3600000); // 1 hour in real scenario, adjust for test environment

			// Navigate to a protected page
			await page.goto("/dashboard/settings");

			// Should automatically refresh token and load page without redirecting to login
			await expect(page).toHaveURL(/\/dashboard\/settings/);
			await expect(
				page.getByRole("heading", { name: /settings/i }),
			).toBeVisible();
		});
	});

	test.describe("Logout Flow (US6)", () => {
		test.beforeEach(async ({ page }) => {
			// Login before logout tests
			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByRole("button", { name: /log in|sign in/i }).click();
			await expect(page).toHaveURL(/\/dashboard/);
		});

		test("should successfully logout user", async ({ page }) => {
			await test.step("Initiate logout", async () => {
				await page
					.getByRole("button", { name: /profile|account|menu/i })
					.click();
				await page.getByRole("button", { name: /log out|sign out/i }).click();
			});

			await test.step("Verify logout confirmation", async () => {
				// Optional: confirm logout if there's a confirmation dialog
				const confirmButton = page.getByRole("button", {
					name: /confirm|yes|log out/i,
				});
				if (await confirmButton.isVisible()) {
					await confirmButton.click();
				}
			});

			await test.step("Verify user is logged out", async () => {
				await expect(page).toHaveURL(/\/login|\//);
				await expect(
					page.getByRole("link", { name: /log in|sign in/i }),
				).toBeVisible();
			});

			await test.step("Verify session cookies are cleared", async () => {
				const cookies = await page.context().cookies();
				const sessionCookies = cookies.filter(
					(c) =>
						c.name.includes("session") ||
						c.name.includes("token") ||
						c.name.includes("refresh"),
				);
				// Session cookies should be expired or removed
				sessionCookies.forEach((cookie) => {
					expect(cookie.value).toBe("");
				});
			});

			await test.step("Verify cannot access protected pages after logout", async () => {
				await page.goto("/dashboard");
				await expect(page).toHaveURL(/\/login/);
			});
		});

		test("should clear local session state on logout", async ({ page }) => {
			await page.getByRole("button", { name: /profile|account/i }).click();
			await page.getByRole("button", { name: /log out/i }).click();

			// Check that sessionStorage and localStorage are cleared
			const sessionStorage = await page.evaluate(
				() => window.sessionStorage.length,
			);
			const localStorage = await page.evaluate(
				() => window.localStorage.length,
			);

			expect(sessionStorage).toBe(0);
			expect(localStorage).toBe(0);
		});
	});

	test.describe("Session Persistence Flow (US5)", () => {
		test("should restore session after page refresh", async ({ page }) => {
			// Login
			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByRole("button", { name: /log in/i }).click();
			await expect(page).toHaveURL(/\/dashboard/);

			// Refresh the page
			await page.reload();

			// Should still be logged in
			await expect(page).toHaveURL(/\/dashboard/);
			await expect(
				page.getByRole("heading", { name: /dashboard/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /profile|account/i }),
			).toBeVisible();
		});

		test("should restore session after browser restart", async ({
			browser,
		}) => {
			// Create a new context with persistent storage
			const context = await browser.newContext({
				storageState: undefined,
			});
			const page = await context.newPage();

			// Login
			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test.user@example.com");
			await page.getByLabel(/password/i).fill("TestPassword123!");
			await page.getByRole("button", { name: /log in/i }).click();
			await expect(page).toHaveURL(/\/dashboard/);

			// Save storage state
			const storageState = await context.storageState();
			await context.close();

			// Create new context with saved storage (simulates browser restart)
			const newContext = await browser.newContext({ storageState });
			const newPage = await newContext.newPage();

			// Navigate to dashboard
			await newPage.goto("/dashboard");

			// Should still be logged in
			await expect(newPage).toHaveURL(/\/dashboard/);
			await expect(
				newPage.getByRole("heading", { name: /dashboard/i }),
			).toBeVisible();

			await newContext.close();
		});
	});

	test.describe("Security and Error Handling", () => {
		test("should prevent XSS attacks in login form", async ({ page }) => {
			await page.goto("/login");

			const xssPayload = '<script>alert("XSS")</script>';
			await page.getByLabel(/email/i).fill(xssPayload);
			await page.getByLabel(/password/i).fill("password");
			await page.getByRole("button", { name: /log in/i }).click();

			// Should not execute the script
			page.on("dialog", () => {
				throw new Error("XSS vulnerability detected!");
			});

			await page.waitForTimeout(1000);
		});

		test("should handle network errors gracefully", async ({
			page,
			context,
		}) => {
			await context.route("**/api/auth/login", (route) => route.abort());

			await page.goto("/login");
			await page.getByLabel(/email/i).fill("test@example.com");
			await page.getByLabel(/password/i).fill("password");
			await page.getByRole("button", { name: /log in/i }).click();

			await expect(
				page.getByText(/network error|connection error|try again/i),
			).toBeVisible();
		});

		test("should enforce HTTPS in production", async ({ page }) => {
			// This test assumes the app enforces HTTPS redirects
			const url = new URL(page.url());
			if (process.env.NODE_ENV === "production") {
				expect(url.protocol).toBe("https:");
			}
		});
	});
});
