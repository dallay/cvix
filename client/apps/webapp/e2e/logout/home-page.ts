import type { Locator, Page } from "@playwright/test";
import { expect } from "@playwright/test";

import { BasePage } from "../base-page";

/**
 * HomePage / DashboardPage - Page Object for authenticated state
 *
 * Used after login to interact with authenticated pages.
 * Contains logout functionality and common authenticated actions.
 */
export class HomePage extends BasePage {
	// User menu elements
	readonly userMenuButton: Locator;
	readonly logoutButton: Locator;

	constructor(page: Page) {
		super(page);

		// User menu / profile button
		this.userMenuButton = page.getByRole("button", {
			name: /profile|account|menu|user/i,
		});

		// Logout button - use first() since there may be multiple (sidebar + menu)
		this.logoutButton = page
			.getByRole("button", { name: /log out|sign out|logout/i })
			.first();
	}

	/**
	 * Navigate to dashboard/home
	 */
	async goto(): Promise<void> {
		await super.goto("/dashboard");
	}

	/**
	 * Verify user is on authenticated page
	 */
	async verifyPageLoaded(): Promise<void> {
		await expect(this.page).toHaveURL(/\/dashboard|\/workspace/);
	}

	/**
	 * Open user menu if it exists
	 */
	async openUserMenu(): Promise<boolean> {
		const isVisible = await this.userMenuButton.isVisible();
		if (isVisible) {
			await this.userMenuButton.click();
			return true;
		}
		return false;
	}

	/**
	 * Perform logout action
	 */
	async logout(): Promise<void> {
		// Try to open user menu first (if it exists)
		await this.openUserMenu();

		// Check logout button is visible
		const isLogoutVisible = await this.logoutButton.isVisible();
		if (!isLogoutVisible) {
			throw new Error(
				"Logout button not found. Expected either a user menu with logout option or a sidebar logout button.",
			);
		}

		await this.logoutButton.click();
	}

	/**
	 * Verify logout was successful (redirected to login or home)
	 */
	async verifyLogoutSuccess(): Promise<void> {
		await expect(this.page).toHaveURL(/\/login|\/$/);
	}

	/**
	 * Verify session storage is cleared after logout
	 */
	async verifySessionCleared(): Promise<void> {
		const sessionStorageLength = await this.page.evaluate(
			() => window.sessionStorage.length,
		);
		expect(sessionStorageLength).toBe(0);
	}
}
