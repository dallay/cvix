import type { Locator, Page } from "@playwright/test";
import { expect } from "@playwright/test";

/**
 * BasePage - Parent class for ALL page objects
 *
 * Contains common methods shared across all pages:
 * - Navigation helpers
 * - Notification handling
 * - Common UI interactions
 * - Screenshot utilities
 */
export class BasePage {
	protected readonly notification: Locator;

	constructor(protected readonly page: Page) {
		this.notification = page.getByRole("status");
	}

	/**
	 * Navigate to a path and wait for network idle
	 */
	async goto(path: string): Promise<void> {
		await this.page.goto(path);
		await this.page.waitForLoadState("networkidle");
	}

	/**
	 * Get current page URL
	 */
	getCurrentUrl(): string {
		return this.page.url();
	}

	/**
	 * Wait for URL to match a pattern
	 */
	async waitForUrl(pattern: RegExp | string): Promise<void> {
		await expect(this.page).toHaveURL(pattern);
	}

	/**
	 * Wait for page to finish loading
	 */
	async waitForPageLoad(): Promise<void> {
		await this.page.waitForLoadState("networkidle");
	}

	/**
	 * Wait for notification to appear
	 */
	async waitForNotification(): Promise<void> {
		await this.notification.waitFor({ state: "visible" });
	}

	/**
	 * Verify notification contains expected message
	 */
	async verifyNotificationMessage(message: string | RegExp): Promise<void> {
		await this.waitForNotification();
		await expect(this.notification).toContainText(message);
	}

	/**
	 * Check if an element is visible
	 */
	async isVisible(locator: Locator): Promise<boolean> {
		return locator.isVisible();
	}

	/**
	 * Wait for an element to be visible
	 */
	async waitForVisible(locator: Locator): Promise<void> {
		await locator.waitFor({ state: "visible" });
	}

	/**
	 * Take a screenshot for debugging
	 * @param name - Screenshot name (will be sanitized to prevent path traversal)
	 */
	async takeScreenshot(name: string): Promise<void> {
		// Sanitize filename: remove path separators and special chars
		const sanitizedName = name.replace(/[^a-zA-Z0-9_-]/g, "_");
		await this.page.screenshot({
			path: `playwright-report/screenshots/${sanitizedName}.png`,
			fullPage: true,
		});
	}

	/**
	 * Wait for text to appear anywhere on the page
	 */
	async waitForText(text: string | RegExp): Promise<void> {
		await expect(this.page.getByText(text).first()).toBeVisible();
	}

	/**
	 * Verify page has expected heading
	 */
	async verifyHeading(text: string | RegExp): Promise<void> {
		await expect(
			this.page.getByRole("heading", { name: text }).first(),
		).toBeVisible();
	}
}
