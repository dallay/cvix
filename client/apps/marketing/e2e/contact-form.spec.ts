import { expect, test } from "@playwright/test";

/**
 * E2E Tests for Contact Form
 *
 * Tests the contact form functionality on the marketing site:
 * - Form rendering and visibility
 * - Form validation (client-side)
 * - Form submission (happy path)
 * - Success/error message display
 * - Form reset after successful submission
 *
 * Note: These tests use live backend integration. For CI/CD, consider
 * recording HAR files or mocking API responses.
 */

test.describe("Contact Form", () => {
	test.beforeEach(async ({ page }) => {
		// Navigate to English contact page
		await page.goto("/en/contact", { waitUntil: "domcontentloaded" });
		await expect(page.getByRole("button", { name: /send/i })).toBeVisible();
	});

	test("should render all form fields and submit button", async ({ page }) => {
		await test.step("Verify form structure", async () => {
			// Check all required form fields are visible
			await expect(page.getByLabel(/name/i)).toBeVisible();
			await expect(page.getByLabel(/email/i)).toBeVisible();
			await expect(page.getByLabel(/subject/i)).toBeVisible();
			await expect(page.getByLabel(/message/i)).toBeVisible();

			// Check submit button
			const submitButton = page.getByRole("button", { name: /send/i });
			await expect(submitButton).toBeVisible();
			await expect(submitButton).toBeEnabled();
		});
	});

	test("should validate required fields with browser validation", async ({
		page,
	}) => {
		await test.step("Submit empty form", async () => {
			const submitButton = page.getByRole("button", { name: /send/i });
			await submitButton.click();

			// Browser should prevent submission and show validation message
			// Check that we're still on the contact page (not navigated away)
			await expect(page).toHaveURL(/\/en\/contact/);
		});

		await test.step("Verify required attribute on fields", async () => {
			// Check that fields have required attribute (causes browser validation)
			const nameInput = page.getByLabel(/name/i);
			await expect(nameInput).toHaveAttribute("required", "");
		});
	});

	test("should lazy-load CSRF token on first interaction", async ({ page }) => {
		await test.step("No CSRF token initially", async () => {
			// Hidden CSRF input should not exist before interaction
			const csrfInput = page.locator('input[name="csrf_token"]');
			await expect(csrfInput).toHaveCount(0);
		});

		await test.step("CSRF token loaded on field focus", async () => {
			// Focus on any input field
			const nameInput = page.getByLabel(/name/i);
			await nameInput.focus();

			// Now CSRF input should exist (expect will auto-retry)
			const csrfInput = page.locator('input[name="csrf_token"]');
			await expect(csrfInput).toHaveCount(1);
			await expect(csrfInput).toHaveAttribute("value", /.+/); // Non-empty value
		});
	});

	test("should display error message with invalid email format", async ({
		page,
	}) => {
		await test.step("Fill form with invalid email", async () => {
			await page.getByLabel(/name/i).fill("Test User");
			await page.getByLabel(/email/i).fill("invalid-email"); // Invalid format
			await page.getByLabel(/subject/i).fill("Test Subject");
			await page.getByLabel(/message/i).fill("Test message content");
		});

		await test.step("Submit and verify browser validation", async () => {
			const submitButton = page.getByRole("button", { name: /send/i });
			await submitButton.click();

			// Browser should prevent submission due to invalid email
			await expect(page).toHaveURL(/\/en\/contact/);
		});
	});

	test("should submit form successfully with valid data (requires backend)", async ({
		page,
	}) => {
		// Skip this test if USE_HAR is enabled and no HAR file exists
		const useHar = process.env.USE_HAR === "true" || process.env.CI === "true";
		if (useHar) {
			test.skip(
				true,
				"Backend integration test - skipped in HAR mode. Run with USE_HAR=false to test live backend.",
			);
		}

		await test.step("Fill form with valid data", async () => {
			await page.getByLabel(/name/i).fill("Test User");
			await page.getByLabel(/email/i).fill("test@example.com");
			await page.getByLabel(/subject/i).fill("E2E Test Subject");
			await page
				.getByLabel(/message/i)
				.fill("This is a test message from E2E tests.");
		});

		await test.step("Submit form", async () => {
			// Trigger CSRF token load
			const nameInput = page.getByLabel(/name/i);
			await nameInput.focus();

			const submitButton = page.getByRole("button", { name: /send/i });
			await submitButton.click();

			// Button should show loading state
			await expect(submitButton).toBeDisabled();
			await expect(submitButton).toHaveText(/sending/i);
		});

		await test.step("Verify success message", async () => {
			// Wait for success message to appear
			const messageDiv = page.locator("#form-message");
			await expect(messageDiv).toBeVisible({ timeout: 10_000 });

			// Check for success text (depends on i18n, checking for common patterns)
			await expect(messageDiv).toContainText(/success|sent|thank/i);

			// Success message should have appropriate styling (green/success color)
			await expect(messageDiv).toHaveClass(/green|success/i);
		});

		await test.step("Verify form reset", async () => {
			// Form fields should be cleared after successful submission
			await expect(page.getByLabel(/name/i)).toHaveValue("");
			await expect(page.getByLabel(/email/i)).toHaveValue("");
			await expect(page.getByLabel(/subject/i)).toHaveValue("");
			await expect(page.getByLabel(/message/i)).toHaveValue("");
		});
	});

	test("should display form in Spanish locale", async ({ page }) => {
		await test.step("Navigate to Spanish contact page", async () => {
			await page.goto("/es/contact", { waitUntil: "domcontentloaded" });
			await expect(page.getByRole("button", { name: /enviar/i })).toBeVisible();
		});

		await test.step("Verify Spanish labels", async () => {
			// Check for Spanish form labels
			await expect(page.getByLabel(/nombre/i)).toBeVisible();
			await expect(page.getByLabel(/correo|email/i)).toBeVisible();
			await expect(page.getByLabel(/asunto/i)).toBeVisible();
			await expect(page.getByLabel(/mensaje/i)).toBeVisible();

			// Check Spanish submit button
			const submitButton = page.getByRole("button", { name: /enviar/i });
			await expect(submitButton).toBeVisible();
		});
	});

	test("should take screenshot of initial form state", async ({ page }) => {
		await test.step("Capture form screenshot", async () => {
			// Take screenshot for visual regression
			const form = page.locator("form");
			await expect(form).toBeVisible();

			// Screenshot the form area using Playwright snapshots (parallel-safe)
			await expect(form).toHaveScreenshot("contact-form-initial.png");
		});
	});

	test("should handle network errors gracefully", async ({ page }) => {
		await test.step("Fill and submit form", async () => {
			await page.getByLabel(/name/i).fill("Test User");
			await page.getByLabel(/email/i).fill("test@example.com");
			await page.getByLabel(/subject/i).fill("Test");
			await page.getByLabel(/message/i).fill("Test message");

			// Trigger CSRF load
			await page.getByLabel(/name/i).focus();
		});

		await test.step("Simulate network failure", async () => {
			// Intercept the contact API call and return network error
			await page.route("**/api/contact", async (route) => {
				await route.abort("failed");
			});

			const submitButton = page.getByRole("button", { name: /send/i });
			await submitButton.click();
		});

		await test.step("Verify error message displayed", async () => {
			const messageDiv = page.locator("#form-message");
			await expect(messageDiv).toBeVisible({ timeout: 10_000 });

			// Should show error message
			await expect(messageDiv).toContainText(/error|failed/i);

			// Error message should have appropriate styling (red/error color)
			await expect(messageDiv).toHaveClass(/red|error/i);
		});
	});
});
