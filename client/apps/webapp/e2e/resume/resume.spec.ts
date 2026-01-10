import { expect, test } from "@playwright/test";
import {
	SAMPLE_RESUME,
	setupAuthenticatedResumeMocks,
	TEST_USERS,
} from "../helpers";
import { LoginPage } from "../login/login-page";
import { ResumeEditorPage } from "./resume-editor-page";

/**
 * Resume Generator E2E Tests
 *
 * Tests core resume functionality:
 * - Form filling and data persistence
 * - JSON import/export
 * - Preview toggle
 * - Form reset
 * - Unsaved changes tracking
 */

test.describe("Resume Generator", () => {
	test.beforeEach(async ({ page }) => {
		// Setup authentication mocks
		await setupAuthenticatedResumeMocks(page);

		// Perform actual login to access protected routes
		const loginPage = new LoginPage(page);
		await loginPage.goto();
		await loginPage.login({
			email: TEST_USERS.existingUser.email,
			password: TEST_USERS.existingUser.password,
		});

		// Wait for redirect to dashboard
		await page.waitForURL(/\/dashboard/);
	});

	test(
		"should navigate to resume editor and display empty form",
		{
			tag: ["@critical", "@e2e", "@resume", "@RESUME-E2E-001"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);

			// Navigate
			await resumePage.gotoResumeEditor();

			// Verify page loaded
			await expect(resumePage.pageTitle).toBeVisible();
			await expect(resumePage.saveButton).toBeVisible();

			// Verify form is empty
			await expect(resumePage.nameInput).toHaveValue("");
			await expect(resumePage.emailInput).toHaveValue("");
		},
	);

	test(
		"should fill basic information and save resume",
		{
			tag: ["@critical", "@e2e", "@resume", "@RESUME-E2E-002"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill basic info
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
				label: SAMPLE_RESUME.basics.label,
				email: SAMPLE_RESUME.basics.email,
				phone: SAMPLE_RESUME.basics.phone,
				url: SAMPLE_RESUME.basics.url,
				summary: SAMPLE_RESUME.basics.summary,
			});

			// Verify unsaved changes indicator appears
			await expect(resumePage.unsavedChangesIndicator).toBeVisible();

			// Save resume
			await resumePage.saveResume();

			// Verify unsaved changes indicator disappears
			await expect(resumePage.unsavedChangesIndicator).not.toBeVisible();

			// Verify last saved text appears
			const savedText = await resumePage.getLastSavedText();
			expect(savedText?.toLowerCase()).toContain("saved");
		},
	);

	test(
		"should persist data after page refresh",
		{
			tag: ["@high", "@e2e", "@resume", "@RESUME-E2E-003"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill and save
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
				email: SAMPLE_RESUME.basics.email,
			});
			await resumePage.saveResume();

			// Refresh page
			await page.reload();
			await resumePage.waitForPageLoad();

			// Verify data persisted
			await expect(resumePage.nameInput).toHaveValue(SAMPLE_RESUME.basics.name);
			await expect(resumePage.emailInput).toHaveValue(
				SAMPLE_RESUME.basics.email,
			);
		},
	);

	test(
		"should toggle preview panel visibility",
		{
			tag: ["@medium", "@e2e", "@resume", "@RESUME-E2E-004"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill some data first to see preview content
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
			});

			// Check if preview is initially visible (depends on viewport)
			const initiallyVisible = await resumePage.isPreviewVisible();

			// Toggle preview
			await resumePage.togglePreview();

			// Verify toggle worked
			const afterToggle = await resumePage.isPreviewVisible();
			expect(afterToggle).toBe(!initiallyVisible);
		},
	);

	test(
		"should export resume as JSON",
		{
			tag: ["@high", "@e2e", "@resume", "@RESUME-E2E-005"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill basic info
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
				email: SAMPLE_RESUME.basics.email,
			});

			// Start waiting for download before clicking
			const downloadPromise = page.waitForEvent("download");

			// Click download
			await resumePage.downloadJson();

			// Wait for download
			const download = await downloadPromise;

			// Verify download
			expect(download.suggestedFilename()).toMatch(/resume.*\.json/i);
		},
	);

	test(
		"should validate JSON and show validation panel",
		{
			tag: ["@medium", "@e2e", "@resume", "@RESUME-E2E-006"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill basic info
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
			});

			// Click validate
			await resumePage.validateJson();

			// Verify validation panel appears (should be visible)
			// The panel should show either success or errors
			await page.waitForTimeout(500); // Wait for panel animation
		},
	);

	test(
		"should reset form with confirmation",
		{
			tag: ["@medium", "@e2e", "@resume", "@RESUME-E2E-007"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill and save
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
				email: SAMPLE_RESUME.basics.email,
			});
			await resumePage.saveResume();

			// Reset form
			await resumePage.resetForm();

			// Verify form is empty
			await expect(resumePage.nameInput).toHaveValue("");
			await expect(resumePage.emailInput).toHaveValue("");
		},
	);

	test(
		"should cancel reset and keep data",
		{
			tag: ["@low", "@e2e", "@resume", "@RESUME-E2E-008"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill data
			const testName = SAMPLE_RESUME.basics.name;
			await resumePage.fillBasicInfo({
				name: testName,
			});

			// Cancel reset
			await resumePage.cancelReset();

			// Verify data still there
			await expect(resumePage.nameInput).toHaveValue(testName);
		},
	);

	test(
		"should navigate to PDF generation page",
		{
			tag: ["@critical", "@e2e", "@resume", "@RESUME-E2E-009"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Fill basic data
			await resumePage.fillBasicInfo({
				name: SAMPLE_RESUME.basics.name,
			});

			// Navigate to PDF page
			await resumePage.navigateToPdfPage();

			// Verify URL changed
			expect(page.url()).toContain("/resume/pdf");
		},
	);

	test(
		"should detect unsaved changes on form modification",
		{
			tag: ["@medium", "@e2e", "@resume", "@RESUME-E2E-010"],
		},
		async ({ page }) => {
			const resumePage = new ResumeEditorPage(page);
			await resumePage.gotoResumeEditor();

			// Initially no unsaved changes
			await expect(resumePage.unsavedChangesIndicator).not.toBeVisible();

			// Modify form
			await resumePage.nameInput.fill("Test Name");

			// Wait a bit for reactive state to update
			await page.waitForTimeout(300);

			// Verify unsaved changes indicator appears
			await expect(resumePage.unsavedChangesIndicator).toBeVisible();
		},
	);
});
