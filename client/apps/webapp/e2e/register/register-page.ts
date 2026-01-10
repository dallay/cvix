import type { Locator, Page } from "@playwright/test";
import { expect } from "@playwright/test";

import { BasePage } from "../base-page";

/**
 * Registration data interface
 */
export interface RegistrationData {
	email: string;
	firstName: string;
	lastName: string;
	password: string;
}

/**
 * RegisterPage - Page Object for /register
 *
 * Encapsulates all registration-related interactions and assertions.
 * Extends BasePage for common functionality.
 */
export class RegisterPage extends BasePage {
	// Form elements
	readonly emailInput: Locator;
	readonly firstNameInput: Locator;
	readonly lastNameInput: Locator;
	readonly passwordInput: Locator;
	readonly confirmPasswordInput: Locator;
	readonly termsCheckbox: Locator;
	readonly submitButton: Locator;

	// Navigation elements
	readonly loginLink: Locator;

	// Error elements
	readonly errorMessage: Locator;

	constructor(page: Page) {
		super(page);

		// Form elements - using getByLabel for accessibility
		this.emailInput = page.getByLabel(/email/i);
		this.firstNameInput = page.getByLabel(/first name/i);
		this.lastNameInput = page.getByLabel(/last name/i);
		this.passwordInput = page.getByLabel(/^password$/i);
		this.confirmPasswordInput = page.getByLabel(/confirm password/i);
		this.termsCheckbox = page.getByLabel(/terms/i);
		this.submitButton = page.getByRole("button", { name: /create account/i });

		// Navigation links
		this.loginLink = page.getByRole("link", { name: /log in|sign in/i });

		// Error messages
		this.errorMessage = page.getByText(
			/already exists|error|failed|password.*characters|don't match/i,
		);
	}

	/**
	 * Navigate to registration page
	 */
	async goto(): Promise<void> {
		await super.goto("/register");
	}

	/**
	 * Verify registration page is loaded
	 */
	async verifyPageLoaded(): Promise<void> {
		await expect(this.page).toHaveURL(/\/register/);
		await this.verifyHeading(/create your account/i);
	}

	/**
	 * Fill registration form with user data
	 */
	async fillForm(data: RegistrationData): Promise<void> {
		await this.emailInput.fill(data.email);
		await this.firstNameInput.fill(data.firstName);
		await this.lastNameInput.fill(data.lastName);
		await this.passwordInput.fill(data.password);
		await this.confirmPasswordInput.fill(data.password);
	}

	/**
	 * Accept terms and conditions
	 */
	async acceptTerms(): Promise<void> {
		await this.termsCheckbox.check();
	}

	/**
	 * Submit registration form
	 */
	async submit(): Promise<void> {
		await this.submitButton.click();
	}

	/**
	 * Complete registration flow (fill + accept terms + submit)
	 */
	async register(data: RegistrationData): Promise<void> {
		await this.fillForm(data);
		await this.acceptTerms();
		await this.submit();
	}

	/**
	 * Verify successful registration (redirects to dashboard, login, or workspace)
	 */
	async verifyRegistrationSuccess(): Promise<void> {
		await expect(this.page).toHaveURL(/\/dashboard|\/login|\/workspace/);
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
	 * Navigate to login page via link
	 */
	async goToLogin(): Promise<void> {
		await expect(this.loginLink).toBeVisible();
		await this.loginLink.click();
		await expect(this.page).toHaveURL(/\/login/);
	}

	/**
	 * Fill password fields with mismatched values
	 */
	async fillMismatchedPasswords(
		password: string,
		confirmPassword: string,
	): Promise<void> {
		await this.passwordInput.fill(password);
		await this.confirmPasswordInput.fill(confirmPassword);
	}

	/**
	 * Trigger password validation by blurring
	 */
	async triggerPasswordValidation(): Promise<void> {
		await this.passwordInput.blur();
	}

	/**
	 * Trigger confirm password validation by blurring
	 */
	async triggerConfirmPasswordValidation(): Promise<void> {
		await this.confirmPasswordInput.blur();
	}

	/**
	 * Verify password strength validation error
	 */
	async verifyWeakPasswordError(): Promise<void> {
		await expect(
			this.page.getByText(
				/password.*characters|password.*strong|password.*uppercase|password.*number/i,
			),
		).toBeVisible();
	}

	/**
	 * Verify password mismatch error
	 */
	async verifyPasswordMismatchError(): Promise<void> {
		await expect(
			this.page.getByText(/passwords.*match|don't match/i),
		).toBeVisible();
	}

	/**
	 * Verify required field error
	 */
	async verifyRequiredFieldError(): Promise<void> {
		await expect(
			this.page.getByText(/email.*required|enter.*email/i),
		).toBeVisible();
	}
}
