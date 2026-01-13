import { expect, test } from "@playwright/test";
import { setupAuthenticatedResumeMocks, TEST_USERS } from "../helpers";
import { LoginPage } from "../login/login-page";
import { ContentSelectionPage } from "./content-selection-page";

/**
 * Checkbox State Synchronization E2E Tests
 *
 * Verifies that the checkbox UI correctly responds to user interactions:
 * - Section checkboxes toggle when clicked
 * - Parent-child state synchronization works
 * - Indeterminate state displays correctly
 *
 * Bug Context (006-checkbox-state-sync):
 * The checkbox wrapper component had an API mismatch with Reka UI.
 * Consumers used @update:checked but the wrapper only emitted update:modelValue.
 * This caused checkboxes to be completely unresponsive to clicks.
 */

/**
 * Sample resume with multiple work experiences for testing
 */
const RESUME_WITH_WORK = {
	basics: {
		name: "Test User",
		label: "Software Engineer",
		email: "test@example.com",
		phone: "+1-555-0100",
		url: "https://example.com",
		summary: "Experienced engineer",
		location: {
			address: "123 Main St",
			postalCode: "12345",
			city: "Test City",
			countryCode: "US",
			region: "CA",
		},
		profiles: [],
	},
	work: [
		{
			name: "Company A",
			position: "Senior Engineer",
			startDate: "2020-01-01",
			endDate: "2023-12-31",
			summary: "Did engineering things",
			highlights: ["Built stuff"],
		},
		{
			name: "Company B",
			position: "Junior Engineer",
			startDate: "2018-01-01",
			endDate: "2019-12-31",
			summary: "Learned engineering",
			highlights: ["Learned stuff"],
		},
		{
			name: "Company C",
			position: "Intern",
			startDate: "2017-06-01",
			endDate: "2017-08-31",
			summary: "Summer internship",
			highlights: ["Started stuff"],
		},
	],
	education: [
		{
			institution: "Test University",
			studyType: "Bachelor",
			area: "Computer Science",
			startDate: "2014-09-01",
			endDate: "2018-05-31",
		},
	],
	skills: [],
	languages: [],
	projects: [],
	volunteer: [],
	awards: [],
	certificates: [],
	publications: [],
	interests: [],
	references: [],
};

/**
 * Setup resume data in localStorage before navigating.
 * Uses a session marker to only clear visibility on the first load,
 * allowing persistence tests to verify state survives reloads.
 */
async function setupResumeData(
	page: import("@playwright/test").Page,
): Promise<void> {
	await page.addInitScript((resumeData) => {
		// Always set resume data
		localStorage.setItem("cvix:resume", JSON.stringify(resumeData));

		// Use sessionStorage marker to track first load
		// Only clear visibility state on the FIRST load of this test session
		const SESSION_MARKER = "cvix:e2e-session-initialized";
		if (!sessionStorage.getItem(SESSION_MARKER)) {
			localStorage.removeItem("cvix:section-visibility");
			sessionStorage.setItem(SESSION_MARKER, "true");
		}
	}, RESUME_WITH_WORK);
}

test.describe("Checkbox State Synchronization", () => {
	test.beforeEach(async ({ page }) => {
		// Setup mocks and resume data
		await setupAuthenticatedResumeMocks(page);
		await setupResumeData(page);

		// Login
		const loginPage = new LoginPage(page);
		await loginPage.goto();
		await loginPage.login({
			email: TEST_USERS.existingUser.email,
			password: TEST_USERS.existingUser.password,
		});

		// Wait for dashboard redirect
		await page.waitForURL(/\/dashboard/);
	});

	test.describe("US1: Section Toggle Synchronization", () => {
		test(
			"should toggle section checkbox from checked to unchecked",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-001"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Work section should be checked initially (all items visible)
				await contentPage.expectSectionState(/work/i, "checked");

				// Toggle the section off
				await contentPage.toggleSectionCheckbox(/work/i);

				// Section should now be unchecked
				await contentPage.expectSectionState(/work/i, "unchecked");
			},
		);

		test(
			"should toggle section checkbox from unchecked to checked",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-002"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// First, uncheck the section
				await contentPage.toggleSectionCheckbox(/work/i);
				await contentPage.expectSectionState(/work/i, "unchecked");

				// Toggle it back on
				await contentPage.toggleSectionCheckbox(/work/i);

				// Section should now be checked
				await contentPage.expectSectionState(/work/i, "checked");
			},
		);

		test(
			"should enable all children when clicking indeterminate section",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-003"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Expand the work section to access items
				await contentPage.expandSection(/work/i);

				// Uncheck one item to create indeterminate state
				await contentPage.toggleItemCheckbox(/work/i, 0);

				// Section should now be indeterminate
				await contentPage.expectSectionState(/work/i, "indeterminate");

				// Click the section checkbox (should select all)
				await contentPage.toggleSectionCheckbox(/work/i);

				// Section should now be checked (all items selected)
				await contentPage.expectSectionState(/work/i, "checked");
			},
		);
	});

	test.describe("US2: Item Toggle Updates Parent", () => {
		test(
			"should show indeterminate state when one item is unchecked",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-004"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Section should start as checked (all items visible)
				await contentPage.expectSectionState(/work/i, "checked");

				// Expand section to see items
				await contentPage.expandSection(/work/i);

				// Uncheck one item
				await contentPage.toggleItemCheckbox(/work/i, 0);

				// Section should now show indeterminate state
				await contentPage.expectSectionState(/work/i, "indeterminate");
			},
		);

		test(
			"should show unchecked state when all items are unchecked",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-005"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Expand section
				await contentPage.expandSection(/work/i);

				// Uncheck all 3 items
				await contentPage.toggleItemCheckbox(/work/i, 0);
				await contentPage.toggleItemCheckbox(/work/i, 1);
				await contentPage.toggleItemCheckbox(/work/i, 2);

				// Section should now be unchecked
				await contentPage.expectSectionState(/work/i, "unchecked");
			},
		);

		test(
			"should show checked state when all items are re-enabled via section toggle",
			{
				tag: ["@critical", "@e2e", "@checkbox", "@CHECKBOX-E2E-006"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Expand and uncheck all items first
				await contentPage.expandSection(/work/i);
				await contentPage.toggleItemCheckbox(/work/i, 0);
				await contentPage.toggleItemCheckbox(/work/i, 1);
				await contentPage.toggleItemCheckbox(/work/i, 2);

				// Verify unchecked (section collapses when all items are unchecked)
				await contentPage.expectSectionState(/work/i, "unchecked");

				// NOTE: Disabled sections cannot be expanded (business rule).
				// To re-enable items, click the section checkbox to enable all items.
				await contentPage.toggleSectionCheckbox(/work/i);

				// Section should now be checked (all items enabled)
				await contentPage.expectSectionState(/work/i, "checked");
			},
		);
	});

	test.describe("US2: Counter Badge Updates", () => {
		test(
			"should update counter badge when toggling items",
			{
				tag: ["@high", "@e2e", "@checkbox", "@CHECKBOX-E2E-007"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Initial counter should be 3/3
				let counter = await contentPage.getSectionCounter(/work/i);
				expect(counter).toBe("3/3");

				// Expand and uncheck one item
				await contentPage.expandSection(/work/i);
				await contentPage.toggleItemCheckbox(/work/i, 0);

				// Counter should now be 2/3
				counter = await contentPage.getSectionCounter(/work/i);
				expect(counter).toBe("2/3");

				// Uncheck another item
				await contentPage.toggleItemCheckbox(/work/i, 1);

				// Counter should now be 1/3
				counter = await contentPage.getSectionCounter(/work/i);
				expect(counter).toBe("1/3");
			},
		);
	});

	test.describe("Persistence", () => {
		test(
			"should persist checkbox state after page refresh",
			{
				tag: ["@high", "@e2e", "@checkbox", "@CHECKBOX-E2E-008"],
			},
			async ({ page }) => {
				const contentPage = new ContentSelectionPage(page);
				await contentPage.gotoPdfPage();

				// Toggle work section off
				await contentPage.toggleSectionCheckbox(/work/i);
				await contentPage.expectSectionState(/work/i, "unchecked");

				// Wait for debounced save to complete (store uses 300ms debounce)
				// Adding extra buffer to ensure localStorage is written
				await page.waitForTimeout(500);

				// Refresh the page
				await page.reload();
				await contentPage.waitForPageLoad();

				// State should be preserved
				await contentPage.expectSectionState(/work/i, "unchecked");
			},
		);
	});
});
