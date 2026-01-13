import type { Locator, Page } from "@playwright/test";
import { expect } from "@playwright/test";

import { BasePage } from "../base-page";

/**
 * Login credentials interface
 */
export interface LoginCredentials {
	email: string;
	password: string;
}

/**
 * LoginPage - Page Object for /login
 *
 * Encapsulates all login-related interactions and assertions.
 * Extends BasePage for common functionality.
 */
export class LoginPage extends BasePage {
	// Form elements
	readonly emailInput: Locator;
	readonly passwordInput: Locator;
	readonly submitButton: Locator;

	// Navigation elements
	readonly registerLink: Locator;
	readonly forgotPasswordLink: Locator;

	// Error elements
	readonly errorMessage: Locator;

	constructor(page: Page) {
		super(page);

		// Form elements - using getByLabel for accessibility
		this.emailInput = page.getByLabel(/email/i);
		this.passwordInput = page.getByLabel(/password/i);
		this.submitButton = page.getByRole("button", { name: /sign in/i });

		// Navigation links
		this.registerLink = page.getByRole("link", { name: /sign up|register/i });
		this.forgotPasswordLink = page.getByRole("link", {
			name: /forgot|reset/i,
		});

		// Error messages
		this.errorMessage = page.getByText(
			/invalid|error|failed|too many requests/i,
		);
	}

	/**
	 * Navigate to login page
	 */
	async goto(): Promise<void> {
		await super.goto("/login");
	}

	/**
	 * Verify login page is loaded
	 */
	async verifyPageLoaded(): Promise<void> {
		await expect(this.page).toHaveURL(/\/login/);
		await this.verifyHeading(/welcome back/i);
	}

	/**
	 * Fill login form with credentials
	 */
	async fillForm(credentials: LoginCredentials): Promise<void> {
		await this.emailInput.fill(credentials.email);
		await this.passwordInput.fill(credentials.password);
	}

	/**
	 * Submit login form
	 */
	async submit(): Promise<void> {
		await this.submitButton.click();
	}

	/**
	 * Complete login flow (fill + submit)
	 */
	async login(credentials: LoginCredentials): Promise<void> {
		await this.fillForm(credentials);
		await this.submit();
	}

	/**
	 * Verify successful login (redirects to dashboard or workspace)
	 */
	async verifyLoginSuccess(): Promise<void> {
		await expect(this.page).toHaveURL(/\/dashboard|\/workspace/);
	}

	/**
	 * Verify error message is displayed
	 */
	async verifyErrorMessage(message?: string | RegExp): Promise<void> {
		if (message) {
			await expect(this.page.getByText(message)).toBeVisible();
		} else {
			await expect(this.errorMessage).toBeVisible();
		}
	}

	/**
	 * Navigate to registration page via link
	 */
	async goToRegister(): Promise<void> {
		await expect(this.registerLink).toBeVisible();
		await this.registerLink.click();
		await expect(this.page).toHaveURL(/\/register/);
	}

	/**
	 * Verify form validation error for email
	 */
	async verifyEmailValidationError(): Promise<void> {
		await expect(
			this.page.getByText(/invalid email|email format|valid email/i),
		).toBeVisible();
	}

	/**
	 * Trigger field validation by blurring
	 */
	async triggerValidation(): Promise<void> {
		await this.passwordInput.blur();
	}
}
