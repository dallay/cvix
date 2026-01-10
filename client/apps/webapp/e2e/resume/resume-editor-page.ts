import type { Locator, Page } from "@playwright/test";
import { BasePage } from "../base-page";

/**
 * Page Object for Resume Editor Page
 * Handles resume creation, editing, import/export, and preview functionality
 */
export class ResumeEditorPage extends BasePage {
	// Header elements
	readonly pageTitle: Locator;
	readonly pageSubtitle: Locator;

	// Utility bar buttons
	readonly saveButton: Locator;
	readonly uploadJsonButton: Locator;
	readonly downloadJsonButton: Locator;
	readonly validateJsonButton: Locator;
	readonly resetFormButton: Locator;
	readonly generatePdfButton: Locator;
	readonly togglePreviewButton: Locator;

	// Save state indicators
	readonly unsavedChangesIndicator: Locator;
	readonly lastSavedText: Locator;

	// Form sections - Basic Info
	readonly nameInput: Locator;
	readonly labelInput: Locator;
	readonly emailInput: Locator;
	readonly phoneInput: Locator;
	readonly urlInput: Locator;
	readonly summaryTextarea: Locator;

	// Form sections - Location
	readonly locationAddressInput: Locator;
	readonly locationCityInput: Locator;
	readonly locationRegionInput: Locator;
	readonly locationPostalCodeInput: Locator;
	readonly locationCountryCodeInput: Locator;

	// Preview panel
	readonly previewCard: Locator;
	readonly previewContent: Locator;

	// Dialogs
	readonly uploadConfirmDialog: Locator;
	readonly uploadConfirmButton: Locator;
	readonly uploadCancelButton: Locator;

	readonly resetConfirmDialog: Locator;
	readonly resetConfirmButton: Locator;
	readonly resetCancelButton: Locator;

	// File input (hidden)
	readonly fileInput: Locator;

	constructor(page: Page) {
		super(page);

		// Header
		this.pageTitle = page.getByRole("heading", { name: /resume/i, level: 1 });
		this.pageSubtitle = page.getByText(/create and manage/i);

		// Utility bar buttons
		this.saveButton = page.getByRole("button", { name: /save/i });
		this.uploadJsonButton = page.getByRole("button", { name: /upload.*json/i });
		this.downloadJsonButton = page.getByRole("button", {
			name: /download.*json/i,
		});
		this.validateJsonButton = page.getByRole("button", {
			name: /validate.*json/i,
		});
		this.resetFormButton = page.getByRole("button", { name: /reset.*form/i });
		this.generatePdfButton = page.getByRole("button", {
			name: /generate.*pdf/i,
		});
		this.togglePreviewButton = page.getByRole("button", {
			name: /hide|show|preview/i,
		});

		// Save state
		this.unsavedChangesIndicator = page.getByText(/unsaved changes/i);
		this.lastSavedText = page.getByText(/saved/i);

		// Form inputs - Basic Info
		this.nameInput = page.locator("#full-name");
		this.labelInput = page.locator("#label-short-description");
		this.emailInput = page.locator("#email");
		this.phoneInput = page.locator("#phone");
		this.urlInput = page.locator("#url");
		this.summaryTextarea = page.locator("#summary");

		// Form inputs - Location
		this.locationAddressInput = page.locator("#street");
		this.locationCityInput = page.locator("#city");
		this.locationRegionInput = page.locator("#region");
		this.locationPostalCodeInput = page.locator("#zip");
		this.locationCountryCodeInput = page.locator("#countryCode");

		// Preview
		this.previewCard = page.locator('[data-slot="card"]').filter({
			hasText: /preview/i,
		});
		this.previewContent = this.previewCard.locator(".overflow-y-auto");

		// Dialogs
		this.uploadConfirmDialog = page.getByRole("alertdialog").filter({
			hasText: /upload/i,
		});
		this.uploadConfirmButton = this.uploadConfirmDialog.getByRole("button", {
			name: /confirm|yes|replace/i,
		});
		this.uploadCancelButton = this.uploadConfirmDialog.getByRole("button", {
			name: /cancel/i,
		});

		this.resetConfirmDialog = page.getByRole("alertdialog").filter({
			hasText: /reset/i,
		});
		this.resetConfirmButton = this.resetConfirmDialog.getByRole("button", {
			name: /reset|confirm/i,
		});
		this.resetCancelButton = this.resetConfirmDialog.getByRole("button", {
			name: /cancel/i,
		});

		// File input
		this.fileInput = page.locator("input[type='file']");
	}

	/**
	 * Navigate to resume editor page
	 */
	async gotoResumeEditor(): Promise<void> {
		await super.goto("/resume");
		await this.waitForPageLoad();
	}

	/**
	 * Wait for page to be fully loaded
	 */
	override async waitForPageLoad(): Promise<void> {
		await this.pageTitle.waitFor({ state: "visible" });
		await this.saveButton.waitFor({ state: "visible" });
	}

	/**
	 * Fill basic information section
	 */
	async fillBasicInfo(data: {
		name?: string;
		label?: string;
		email?: string;
		phone?: string;
		url?: string;
		summary?: string;
	}): Promise<void> {
		if (data.name) await this.nameInput.fill(data.name);
		if (data.label) await this.labelInput.fill(data.label);
		if (data.email) await this.emailInput.fill(data.email);
		if (data.phone) await this.phoneInput.fill(data.phone);
		if (data.url) await this.urlInput.fill(data.url);
		if (data.summary) await this.summaryTextarea.fill(data.summary);
	}

	/**
	 * Fill location section
	 */
	async fillLocation(data: {
		address?: string;
		city?: string;
		region?: string;
		postalCode?: string;
		countryCode?: string;
	}): Promise<void> {
		if (data.address) await this.locationAddressInput.fill(data.address);
		if (data.city) await this.locationCityInput.fill(data.city);
		if (data.region) await this.locationRegionInput.fill(data.region);
		if (data.postalCode)
			await this.locationPostalCodeInput.fill(data.postalCode);
		if (data.countryCode)
			await this.locationCountryCodeInput.fill(data.countryCode);
	}

	/**
	 * Click save button
	 */
	async clickSave(): Promise<void> {
		await this.saveButton.click();
	}

	/**
	 * Save resume (click save button and wait for save to complete)
	 */
	async saveResume(): Promise<void> {
		await this.clickSave();
		// Wait for save to complete (localStorage save is near-instant)
		// Just ensure the button isn't in loading state
		await this.page.waitForTimeout(500);
	}

	/**
	 * Upload JSON file
	 * @param filePath - Absolute path to JSON file
	 * @param confirmReplace - If true, confirms replacement of existing data
	 */
	async uploadJson(filePath: string, confirmReplace = false): Promise<void> {
		// Set file to input (triggers change event)
		await this.fileInput.setInputFiles(filePath);

		// Handle confirmation dialog if existing data
		if (confirmReplace) {
			await this.uploadConfirmDialog.waitFor({ state: "visible" });
			await this.uploadConfirmButton.click();
		}

		// Wait for upload to complete
		await this.page.waitForTimeout(500);
	}

	/**
	 * Download JSON export (triggers download)
	 */
	async downloadJson(): Promise<void> {
		await this.downloadJsonButton.click();
		// Download is instant, no need to wait for notification
		await this.page.waitForTimeout(300);
	}

	/**
	 * Validate JSON schema
	 */
	async validateJson(): Promise<void> {
		await this.validateJsonButton.click();
	}

	/**
	 * Reset form with confirmation
	 */
	async resetForm(): Promise<void> {
		await this.resetFormButton.click();
		await this.resetConfirmDialog.waitFor({ state: "visible" });
		await this.resetConfirmButton.click();
		// Wait for reset to complete
		await this.page.waitForTimeout(500);
	}

	/**
	 * Cancel reset dialog
	 */
	async cancelReset(): Promise<void> {
		await this.resetFormButton.click();
		await this.resetConfirmDialog.waitFor({ state: "visible" });
		await this.resetCancelButton.click();
	}

	/**
	 * Navigate to PDF generation page
	 */
	async navigateToPdfPage(): Promise<void> {
		await this.generatePdfButton.click();
		await this.page.waitForURL(/\/resume\/pdf/);
	}

	/**
	 * Toggle preview panel visibility
	 */
	async togglePreview(): Promise<void> {
		await this.togglePreviewButton.click();
	}

	/**
	 * Check if preview is visible
	 */
	async isPreviewVisible(): Promise<boolean> {
		return await this.previewCard.isVisible();
	}

	/**
	 * Check if unsaved changes indicator is visible
	 */
	async hasUnsavedChanges(): Promise<boolean> {
		return await this.unsavedChangesIndicator.isVisible();
	}

	/**
	 * Get last saved timestamp text
	 */
	async getLastSavedText(): Promise<string | null> {
		if (await this.lastSavedText.isVisible()) {
			return await this.lastSavedText.textContent();
		}
		return null;
	}
}
