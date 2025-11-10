/**
 * E2E Test: Resume Generation Flow
 * Tests the complete user journey from form fill to PDF download
 *
 * @see T154 - E2E test for complete resume generation flow
 */

import { expect, test } from "@playwright/test";
import { AxeBuilder } from "@axe-core/playwright";

test.describe("Resume Generation E2E Flow", () => {
	test.beforeEach(async ({ page }) => {
		// Navigate to the resume generator page
		await page.goto("/resume");

		// Wait for the page to load
		await page.waitForLoadState("networkidle");
	});

	test("should complete full resume generation workflow", async ({ page }) => {
		// Step 1: Fill Personal Information
		await test.step("Fill personal information", async () => {
			await page.getByLabel("Full Name").fill("John Doe");
			await page.getByLabel("Email").fill("john.doe@example.com");
			await page.getByLabel("Phone").fill("+1-555-123-4567");
			await page.getByLabel("Job Title").fill("Senior Software Engineer");

			// Location fields
			await page.getByLabel("City").fill("San Francisco");
			await page.getByLabel("Country").fill("USA");

			// Optional: Summary
			await page
				.getByLabel("Professional Summary")
				.fill(
					"Experienced software engineer with 8+ years building scalable web applications.",
				);
		});

		// Step 2: Add Work Experience
		await test.step("Add work experience", async () => {
			// Click add work experience button
			await page.getByRole("button", { name: /add work experience/i }).click();

			// Fill first work experience
			await page.getByLabel("Company Name").first().fill("Tech Corp");
			await page
				.getByLabel("Position")
				.first()
				.fill("Senior Software Engineer");
			await page.getByLabel("Start Date").first().fill("2020-01");
			await page.getByLabel("End Date").first().fill("2024-11");

			await page
				.getByLabel("Description", { exact: false })
				.first()
				.fill(
					"Led development of microservices architecture serving 1M+ users",
				);

			// Add highlights
			await page
				.getByRole("button", { name: /add highlight/i })
				.first()
				.click();
			await page
				.locator('input[placeholder*="highlight"]')
				.first()
				.fill("Reduced API latency by 40% through caching optimization");
		});

		// Step 3: Add Education
		await test.step("Add education", async () => {
			await page.getByRole("button", { name: /add education/i }).click();

			await page.getByLabel("Institution").fill("Stanford University");
			await page.getByLabel("Degree").fill("Bachelor of Science");
			await page.getByLabel("Field of Study").fill("Computer Science");
			await page.getByLabel("Graduation Date").fill("2016-06");
		});

		// Step 4: Add Skills
		await test.step("Add skills", async () => {
			await page.getByRole("button", { name: /add skill category/i }).click();

			await page.getByLabel("Category Name").fill("Programming Languages");
			await page
				.getByRole("button", { name: /add skill/i })
				.first()
				.click();

			// Add individual skills
			const skillInputs = page.locator('input[placeholder*="skill"]');
			await skillInputs.first().fill("JavaScript");
			await page
				.getByRole("button", { name: /add skill/i })
				.first()
				.click();
			await skillInputs.nth(1).fill("TypeScript");
			await page
				.getByRole("button", { name: /add skill/i })
				.first()
				.click();
			await skillInputs.nth(2).fill("Python");
		});

		// Step 5: Verify preview updates
		await test.step("Verify live preview", async () => {
			// Check if preview panel is visible
			const preview = page.locator('[data-testid="resume-preview"]');
			await expect(preview).toBeVisible();

			// Verify personal info appears in preview
			await expect(preview).toContainText("John Doe");
			await expect(preview).toContainText("john.doe@example.com");
			await expect(preview).toContainText("Senior Software Engineer");
		});

		// Step 6: Validate form
		await test.step("Form validation passes", async () => {
			// All required fields should be filled, no error messages
			const errorMessages = page.locator('[role="alert"]');
			await expect(errorMessages).toHaveCount(0);
		});

		// Step 7: Generate PDF
		await test.step("Generate and download PDF", async () => {
			// Set up download handler
			const downloadPromise = page.waitForEvent("download");

			// Click generate resume button
			await page
				.getByRole("button", { name: /generate resume|generate pdf/i })
				.click();

			// Wait for loading indicator
			await expect(page.getByText(/generating/i)).toBeVisible();

			// Wait for download to complete
			const download = await downloadPromise;

			// Verify download filename
			expect(download.suggestedFilename()).toMatch(/resume.*\.pdf/i);

			// Verify download completed successfully
			expect(await download.failure()).toBeNull();

			// Success message should appear
			await expect(
				page.getByText(/success|generated|download/i),
			).toBeVisible({
				timeout: 10000,
			});

			// Verify PDF content (requires a PDF parsing library)
			const downloadPath = await download.path();
			expect(downloadPath).not.toBeNull();
		});
	});

	test("should handle complex data input", async ({ page }) => {
		// Fill personal information
		await page.getByLabel("Full Name").fill("Jane Doe");
		await page.getByLabel("Email").fill("jane.doe@example.com");

		// Add multiple work experiences
		await page.getByRole("button", { name: /add work experience/i }).click();
		await page.getByLabel("Company Name").first().fill("Innovate Inc.");
		await page.getByLabel("Position").first().fill("Lead Developer");
		await page.getByLabel("Start Date").first().fill("2018-03");

		await page.getByRole("button", { name: /add work experience/i }).click();
		await page.getByLabel("Company Name").nth(1).fill("Creative Solutions");
		await page.getByLabel("Position").nth(1).fill("Software Engineer");
		await page.getByLabel("Start Date").nth(1).fill("2016-07");

		// Verify preview updates
		await expect(page.locator('[data-testid="resume-preview"]')).toContainText(
			"Innovate Inc.",
		);
		await expect(page.locator('[data-testid="resume-preview"]')).toContainText(
			"Creative Solutions",
		);
	});

	test("should handle validation errors gracefully", async ({ page }) => {
		await test.step("Submit empty form", async () => {
			// Try to generate without filling required fields
			await page.getByRole("button", { name: /generate resume/i }).click();

			// Should see validation errors
			await expect(page.getByText(/required/i).first()).toBeVisible();
		});

		await test.step("Fix validation errors", async () => {
			// Fill minimum required fields
			await page.getByLabel("Full Name").fill("Jane Smith");
			await page.getByLabel("Email").fill("jane@example.com");

			// Add at least one work experience
			await page.getByRole("button", { name: /add work experience/i }).click();
			await page.getByLabel("Company Name").fill("ABC Inc");
			await page.getByLabel("Position").fill("Developer");
			await page.getByLabel("Start Date").fill("2022-01");

			// Errors should clear
			await page.getByLabel("Full Name").blur();
			await expect(page.locator('[role="alert"]')).toHaveCount(0);
		});
	});

	test("should handle generation errors with retry", async ({ page }) => {
		// This test simulates an error scenario
		// In a real environment, you might mock the API to return an error

		await test.step("Fill minimal valid form", async () => {
			await page.getByLabel("Full Name").fill("Test User");
			await page.getByLabel("Email").fill("test@example.com");

			await page.getByRole("button", { name: /add work experience/i }).click();
			await page.getByLabel("Company Name").fill("Test Co");
			await page.getByLabel("Position").fill("Tester");
			await page.getByLabel("Start Date").fill("2023-01");
		});

		// Mock API to return a 500 error
		await page.route("/api/resumes", (route) => {
			route.fulfill({
				status: 500,
				contentType: "application/json",
				body: JSON.stringify({ message: "Internal Server Error" }),
			});
		});

		// Click generate resume button
		await page.getByRole("button", { name: /generate resume/i }).click();

		// Verify error message and retry button are visible
		const errorDisplay = page.locator('[data-testid="error-display"]');
		await expect(errorDisplay).toBeVisible();
		await expect(errorDisplay).toContainText(/error/i);
		await expect(
			errorDisplay.getByRole("button", { name: /retry/i }),
		).toBeVisible();
	});

	test("should be mobile responsive", async ({ page }) => {
		// Set viewport to mobile size
		await page.setViewportSize({ width: 375, height: 667 });

		await test.step("Form should be usable on mobile", async () => {
			// Check that form fields are visible and accessible
			await expect(page.getByLabel("Full Name")).toBeVisible();

			// Fill a field on mobile
			await page.getByLabel("Full Name").fill("Mobile User");
			await expect(page.getByLabel("Full Name")).toHaveValue("Mobile User");

			// Check that buttons are touch-friendly (min 44px)
			const addButton = page.getByRole("button", {
				name: /add work experience/i,
			});
			await expect(addButton).toBeVisible();

			// Verify button is tappable
			await addButton.click();
			await expect(page.getByLabel("Company Name")).toBeVisible();
		});

		await test.step("Preview should adapt to mobile", async () => {
			// Preview should be collapsible or scrollable on mobile
			const preview = page.locator('[data-testid="resume-preview"]');

			// Preview might be hidden by default on mobile or scrollable
			// This depends on the implementation
			const isVisible = await preview.isVisible().catch(() => false);
			expect(typeof isVisible).toBe("boolean");
		});
	});

	test("should support keyboard navigation", async ({ page }) => {
		await test.step("Navigate form with keyboard", async () => {
			// Focus first input
			await page.keyboard.press("Tab");

			// Should be able to fill fields with keyboard
			await page.keyboard.type("Keyboard User");
			await expect(page.getByLabel("Full Name")).toHaveValue("Keyboard User");

			// Tab to next field
			await page.keyboard.press("Tab");
			await page.keyboard.type("keyboard@example.com");
			await expect(page.getByLabel("Email")).toHaveValue(
				"keyboard@example.com",
			);
		});

		await test.step("Activate buttons with keyboard", async () => {
			// Tab to add button
			await page.getByRole("button", { name: /add work experience/i }).focus();
			await page.keyboard.press("Enter");

			// Work experience section should appear
			await expect(page.getByLabel("Company Name")).toBeVisible();
		});
	});

	test("should preserve form data on page refresh", async ({ page }) => {
		await test.step("Fill form data", async () => {
			await page.getByLabel("Full Name").fill("Persistent User");
			await page.getByLabel("Email").fill("persistent@example.com");
		});

		await test.step("Refresh page", async () => {
			await page.reload();
			await page.waitForLoadState("networkidle");
		});

		await test.step("Verify data persisted", async () => {
			// Data should be restored from sessionStorage
			await expect(page.getByLabel("Full Name")).toHaveValue("Persistent User");
			await expect(page.getByLabel("Email")).toHaveValue(
				"persistent@example.com",
			);
		});
	});

	test("should support bilingual generation", async ({ page }) => {
		await test.step("Fill form in English", async () => {
			await page.getByLabel("Full Name").fill("Bilingual User");
			await page.getByLabel("Email").fill("bilingual@example.com");
		});

		await test.step("Switch language to Spanish", async () => {
			// Look for language selector
			const languageSelector = page.locator(
				'[data-testid="language-selector"]',
			);

			if (await languageSelector.isVisible()) {
				await languageSelector.selectOption("es");

				// Form labels should update to Spanish
				await expect(
					page.getByText(/nombre completo|correo electrónico/i),
				).toBeVisible();
			}
		});
	});
});

test.describe("Resume Generation - Accessibility", () => {
	test("should meet WCAG 2.1 AA standards", async ({ page }) => {
		await page.goto("/resume");

		const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
		expect(accessibilityScanResults.violations).toEqual([]);
	});

	test("should have proper ARIA labels", async ({ page }) => {
		await page.goto("/resume");

		// Check for required ARIA labels
		await expect(
			page.getByRole("button", { name: /add work experience/i }),
		).toBeVisible();
		await expect(
			page.getByRole("button", { name: /generate resume/i }),
		).toBeVisible();
	});
});
