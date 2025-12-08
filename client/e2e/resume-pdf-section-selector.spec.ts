/**
 * E2E Test: Resume PDF Section Selector
 * Tests the section visibility toggle functionality for PDF generation
 *
 * Feature: 005-pdf-section-selector
 * Tests: T031-T034 from tasks.md
 *
 * @see specs/005-pdf-section-selector/spec.md for user stories
 *
 * Prerequisites:
 * 1. Backend services must be running (make start)
 * 2. Frontend dev server will be started automatically by playwright webServer config
 * 3. User must be authenticated (these tests assume authentication is handled)
 *
 * To run:
 *   pnpm playwright test e2e/resume-pdf-section-selector.spec.ts
 *
 * Note: These tests create a test resume in the beforeEach hook.
 * In production, you may want to use fixtures or auth.setup.ts for authentication.
 */

import { expect, test } from "@playwright/test";
import type { Page } from "@playwright/test";

// Helper to create a minimal resume with test data
async function createTestResume(page: Page) {
	await page.goto("/resume/editor");
	await page.waitForLoadState("networkidle");

	// Fill Personal Information (required for all tests)
	await page.getByLabel("Full Name").fill("Alex Morgan");
	await page.getByLabel("Email").fill("alex@example.com");
	await page.getByLabel("Phone").fill("(555) 123-4567");
	await page.getByLabel("Job Title").fill("Senior Software Engineer");
	await page.getByLabel("City").fill("San Francisco");
	await page.getByLabel("Country Code").fill("US");

	// Add Work Experience (at least 2 entries for item toggle tests)
	await page.getByRole("button", { name: /add work experience/i }).click();
	await page.getByLabel("Company Name").first().fill("TechFlow Inc.");
	await page.getByLabel("Position").first().fill("Senior Frontend Engineer");
	await page.getByLabel("Start Date").first().fill("2021-01");
	// Leave end date empty (current position)
	await page
		.getByLabel("Description", { exact: false })
		.first()
		.fill(
			"Led the migration of dashboard to React. Mentored 3 junior developers.",
		);

	// Add second work experience
	await page.getByRole("button", { name: /add work experience/i }).click();
	await page.getByLabel("Company Name").nth(1).fill("Creative Solutions");
	await page.getByLabel("Position").nth(1).fill("Software Developer");
	await page.getByLabel("Start Date").nth(1).fill("2018-01");
	await page.getByLabel("End Date").nth(1).fill("2021-01");
	await page
		.getByLabel("Description", { exact: false })
		.nth(1)
		.fill("Developed and maintained client-facing web applications.");

	// Add Education
	await page.getByRole("button", { name: /add education/i }).click();
	await page.getByLabel("Institution").fill("University of California");
	await page.getByLabel("Degree").fill("Bachelor of Science");
	await page.getByLabel("Field of Study").fill("Computer Science");
	await page.getByLabel("Graduation Date").fill("2018-05");

	// Add Skills
	await page.getByRole("button", { name: /add skill category/i }).click();
	await page.getByLabel("Category Name").fill("Programming Languages");

	// Wait for form to be saved
	await page.waitForTimeout(1000);
}

test.describe("Resume PDF Section Selector - Section Toggle", () => {
	test.beforeEach(async ({ page }) => {
		// Create a test resume with multiple sections
		await createTestResume(page);

		// Navigate to PDF page
		await page.goto("/resume/pdf");
		await page.waitForLoadState("networkidle");

		// Wait for section toggle panel to be visible
		await expect(
			page.getByText("Visible Sections", { exact: false }),
		).toBeVisible();
	});

	// T031: Toggle section visibility, verify live preview updates
	test("should toggle section visibility and update live preview", async ({
		page,
	}) => {
		await test.step("Verify all sections are enabled by default", async () => {
			// Check that key sections are visible as pills
			await expect(
				page.getByRole("button", { name: /Personal Details.*enabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Work Experience.*enabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Education.*enabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Skills.*enabled/i }),
			).toBeVisible();
		});

		await test.step("Disable Work Experience section", async () => {
			// Click on Work Experience pill to disable it
			await page
				.getByRole("button", { name: /Work Experience.*enabled/i })
				.click();

			// Verify pill state changed to disabled
			await expect(
				page.getByRole("button", { name: /Work Experience.*disabled/i }),
			).toBeVisible();

			// Verify the section is no longer in preview (if preview iframe is accessible)
			// Note: PDF preview might be in iframe/object, so we test by checking pill state
			// and later in PDF download test we verify actual content
		});

		await test.step("Re-enable Work Experience section", async () => {
			// Click again to re-enable
			await page
				.getByRole("button", { name: /Work Experience.*disabled/i })
				.click();

			// Verify pill state changed back to enabled
			await expect(
				page.getByRole("button", { name: /Work Experience.*enabled/i }),
			).toBeVisible();
		});

		await test.step("Disable multiple sections", async () => {
			// Disable Education
			await page
				.getByRole("button", { name: /Education.*enabled/i })
				.click();
			await expect(
				page.getByRole("button", { name: /Education.*disabled/i }),
			).toBeVisible();

			// Disable Skills
			await page.getByRole("button", { name: /Skills.*enabled/i }).click();
			await expect(
				page.getByRole("button", { name: /Skills.*disabled/i }),
			).toBeVisible();

			// Personal Details and Work Experience should still be enabled
			await expect(
				page.getByRole("button", { name: /Personal Details.*enabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Work Experience.*enabled/i }),
			).toBeVisible();
		});
	});

	// T032: Toggle sections/items, download PDF, verify content matches selections
	test("should download PDF with only selected sections", async ({ page }) => {
		await test.step("Disable Work Experience section", async () => {
			await page
				.getByRole("button", { name: /Work Experience.*enabled/i })
				.click();
			await expect(
				page.getByRole("button", { name: /Work Experience.*disabled/i }),
			).toBeVisible();
		});

		await test.step("Wait for PDF preview to regenerate", async () => {
			// Wait for the debounced PDF generation to complete
			await page.waitForTimeout(1000);
		});

		await test.step("Download PDF and verify", async () => {
			// Start waiting for download before clicking
			const downloadPromise = page.waitForEvent("download");

			// Click download button
			await page.getByRole("button", { name: /Download PDF/i }).click();

			// Wait for download to complete
			const download = await downloadPromise;
			expect(download.suggestedFilename()).toMatch(/\.pdf$/);

			// Note: Full PDF content verification would require a PDF parser
			// For now, we verify the download occurs successfully
			// In a production test, you could use a library like pdf-parse to verify content
		});
	});

	// T033: Toggle preferences, refresh page, verify persistence
	test("should persist section visibility preferences after refresh", async ({
		page,
	}) => {
		await test.step("Configure custom section visibility", async () => {
			// Disable Work Experience
			await page
				.getByRole("button", { name: /Work Experience.*enabled/i })
				.click();
			await expect(
				page.getByRole("button", { name: /Work Experience.*disabled/i }),
			).toBeVisible();

			// Disable Education
			await page
				.getByRole("button", { name: /Education.*enabled/i })
				.click();
			await expect(
				page.getByRole("button", { name: /Education.*disabled/i }),
			).toBeVisible();

			// Wait for persistence to complete (storage is synchronous but give it a moment)
			await page.waitForTimeout(500);
		});

		await test.step("Refresh the page", async () => {
			await page.reload();
			await page.waitForLoadState("networkidle");

			// Wait for section toggle panel to be visible
			await expect(
				page.getByText("Visible Sections", { exact: false }),
			).toBeVisible();
		});

		await test.step("Verify preferences were restored", async () => {
			// Personal Details and Skills should be enabled
			await expect(
				page.getByRole("button", { name: /Personal Details.*enabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Skills.*enabled/i }),
			).toBeVisible();

			// Work Experience and Education should be disabled
			await expect(
				page.getByRole("button", { name: /Work Experience.*disabled/i }),
			).toBeVisible();
			await expect(
				page.getByRole("button", { name: /Education.*disabled/i }),
			).toBeVisible();
		});
	});

	test("should enforce that Personal Details cannot be disabled", async ({
		page,
	}) => {
		await test.step("Attempt to disable Personal Details", async () => {
			// Personal Details pill should be visible and enabled
			const personalDetailsPill = page.getByRole("button", {
				name: /Personal Details.*enabled/i,
			});
			await expect(personalDetailsPill).toBeVisible();

			// Click on it
			await personalDetailsPill.click();

			// It should remain enabled (or show a tooltip/message)
			// Based on implementation, Personal Details should not toggle off
			await expect(
				page.getByRole("button", { name: /Personal Details.*enabled/i }),
			).toBeVisible();

			// Should NOT see a disabled state
			await expect(
				page.getByRole("button", { name: /Personal Details.*disabled/i }),
			).not.toBeVisible();
		});
	});
});

test.describe("Resume PDF Section Selector - Item Toggle", () => {
	test.beforeEach(async ({ page }) => {
		// Create a test resume with multiple sections
		await createTestResume(page);

		// Navigate to PDF page
		await page.goto("/resume/pdf");
		await page.waitForLoadState("networkidle");

		// Wait for section toggle panel to be visible
		await expect(
			page.getByText("Visible Sections", { exact: false }),
		).toBeVisible();
	});

	test("should expand section to show individual items", async ({ page }) => {
		await test.step("Click Work Experience pill to expand", async () => {
			// Work Experience should be enabled
			const workExpPill = page.getByRole("button", {
				name: /Work Experience.*enabled/i,
			});
			await expect(workExpPill).toBeVisible();

			// Click to expand (not toggle - we need to verify expand behavior)
			await workExpPill.click();

			// Wait for item list to appear
			// Look for individual work items (company names or checkboxes)
			await expect(page.getByText("TechFlow Inc.")).toBeVisible();
			await expect(page.getByText("Creative Solutions")).toBeVisible();
		});

		await test.step("Click again to collapse", async () => {
			const workExpPill = page.getByRole("button", {
				name: /Work Experience/i,
			});
			await workExpPill.click();

			// Items should no longer be visible
			await expect(page.getByText("TechFlow Inc.")).not.toBeVisible();
		});
	});

	test("should toggle individual items within a section", async ({ page }) => {
		await test.step("Expand Work Experience section", async () => {
			await page
				.getByRole("button", { name: /Work Experience.*enabled/i })
				.click();
			await expect(page.getByText("TechFlow Inc.")).toBeVisible();
		});

		await test.step("Disable first work experience item", async () => {
			// Find checkbox for TechFlow Inc. and uncheck it
			const techFlowCheckbox = page
				.locator('label:has-text("TechFlow Inc.")')
				.locator('input[type="checkbox"]');
			await expect(techFlowCheckbox).toBeChecked();

			await techFlowCheckbox.uncheck();
			await expect(techFlowCheckbox).not.toBeChecked();

			// Pill should show count: 1/2 visible
			await expect(
				page.getByRole("button", { name: /Work Experience.*1\/2/i }),
			).toBeVisible();
		});

		await test.step("Re-enable the item", async () => {
			const techFlowCheckbox = page
				.locator('label:has-text("TechFlow Inc.")')
				.locator('input[type="checkbox"]');
			await techFlowCheckbox.check();
			await expect(techFlowCheckbox).toBeChecked();

			// Pill should show 2/2 visible
			await expect(
				page.getByRole("button", { name: /Work Experience.*2\/2/i }),
			).toBeVisible();
		});
	});

	test("should disable section when all items are disabled", async ({
		page,
	}) => {
		await test.step("Expand Work Experience section", async () => {
			await page
				.getByRole("button", { name: /Work Experience.*enabled/i })
				.click();
			await expect(page.getByText("TechFlow Inc.")).toBeVisible();
		});

		await test.step("Disable all work experience items", async () => {
			// Uncheck first item
			const techFlowCheckbox = page
				.locator('label:has-text("TechFlow Inc.")')
				.locator('input[type="checkbox"]');
			await techFlowCheckbox.uncheck();

			// Uncheck second item
			const creativeSolutionsCheckbox = page
				.locator('label:has-text("Creative Solutions")')
				.locator('input[type="checkbox"]');
			await creativeSolutionsCheckbox.uncheck();

			// Wait a moment for auto-disable logic
			await page.waitForTimeout(300);
		});

		await test.step("Verify section is auto-disabled", async () => {
			// Section pill should be disabled or show 0/2
			// Based on implementation: section auto-disables when all items are off
			await expect(
				page.getByRole("button", { name: /Work Experience.*disabled/i }),
			).toBeVisible();
		});
	});

	test("should toggle Personal Details fields individually", async ({
		page,
	}) => {
		await test.step("Expand Personal Details section", async () => {
			await page
				.getByRole("button", { name: /Personal Details.*enabled/i })
				.click();

			// Should see individual fields: email, phone, location, image
			await expect(page.getByText("Email")).toBeVisible();
			await expect(page.getByText("Phone")).toBeVisible();
			await expect(page.getByText("Location")).toBeVisible();
		});

		await test.step("Disable email field", async () => {
			const emailCheckbox = page
				.locator('label:has-text("Email")')
				.locator('input[type="checkbox"]');
			await expect(emailCheckbox).toBeChecked();

			await emailCheckbox.uncheck();
			await expect(emailCheckbox).not.toBeChecked();
		});

		await test.step("Disable phone field", async () => {
			const phoneCheckbox = page
				.locator('label:has-text("Phone")')
				.locator('input[type="checkbox"]');
			await phoneCheckbox.uncheck();
			await expect(phoneCheckbox).not.toBeChecked();
		});

		await test.step("Verify Personal Details section remains enabled", async () => {
			// Even with some fields disabled, Personal Details should remain enabled
			// because name is always required
			await expect(
				page.getByRole("button", { name: /Personal Details.*enabled/i }),
			).toBeVisible();
		});
	});
});

// T034: Verify responsive behavior at 768px, 1024px, 1440px breakpoints
test.describe("Resume PDF Section Selector - Responsive Layout", () => {
	test.beforeEach(async ({ page }) => {
		// Create a test resume
		await createTestResume(page);
	});

	const breakpoints = [
		{ width: 768, height: 1024, name: "Tablet (768px)" },
		{ width: 1024, height: 768, name: "Small Desktop (1024px)" },
		{ width: 1440, height: 900, name: "Desktop (1440px)" },
		{ width: 2560, height: 1440, name: "Large Desktop (2560px)" },
	];

	for (const { width, height, name } of breakpoints) {
		test(`should maintain layout stability at ${name}`, async ({ page }) => {
			await test.step(`Set viewport to ${name}`, async () => {
				await page.setViewportSize({ width, height });
				await page.goto("/resume/pdf");
				await page.waitForLoadState("networkidle");

				// Wait for section toggle panel to be visible
				await expect(
					page.getByText("Visible Sections", { exact: false }),
				).toBeVisible();
			});

			await test.step("Verify all sections are visible", async () => {
				// All key sections should be visible as pills
				await expect(
					page.getByRole("button", { name: /Personal Details/i }),
				).toBeVisible();
				await expect(
					page.getByRole("button", { name: /Work Experience/i }),
				).toBeVisible();
				await expect(
					page.getByRole("button", { name: /Education/i }),
				).toBeVisible();
				await expect(
					page.getByRole("button", { name: /Skills/i }),
				).toBeVisible();
			});

			await test.step("Toggle all sections", async () => {
				// Toggle Work Experience
				await page
					.getByRole("button", { name: /Work Experience.*enabled/i })
					.click();
				await expect(
					page.getByRole("button", { name: /Work Experience.*disabled/i }),
				).toBeVisible();

				// Toggle Education
				await page
					.getByRole("button", { name: /Education.*enabled/i })
					.click();
				await expect(
					page.getByRole("button", { name: /Education.*disabled/i }),
				).toBeVisible();

				// Re-enable them
				await page
					.getByRole("button", { name: /Work Experience.*disabled/i })
					.click();
				await page
					.getByRole("button", { name: /Education.*disabled/i })
					.click();
			});

			await test.step("Expand section and verify items", async () => {
				// Expand Work Experience
				await page
					.getByRole("button", { name: /Work Experience.*enabled/i })
					.click();

				// Items should be visible
				await expect(page.getByText("TechFlow Inc.")).toBeVisible();
				await expect(page.getByText("Creative Solutions")).toBeVisible();

				// Toggle an item
				const checkbox = page
					.locator('label:has-text("TechFlow Inc.")')
					.locator('input[type="checkbox"]');
				await checkbox.uncheck();
				await expect(checkbox).not.toBeChecked();

				// Re-check
				await checkbox.check();
				await expect(checkbox).toBeChecked();
			});

			await test.step("Verify pill wrapping behavior", async () => {
				// Pills should wrap to multiple rows on narrow screens
				// We can verify by checking that all pills are still visible
				// and not overflowing the container
				const pills = page.getByRole("button", {
					name: /Personal Details|Work Experience|Education|Skills/,
				});
				const pillCount = await pills.count();
				expect(pillCount).toBeGreaterThanOrEqual(4);

				// All pills should be in viewport
				for (let i = 0; i < pillCount; i++) {
					await expect(pills.nth(i)).toBeInViewport();
				}
			});
		});
	}
});

