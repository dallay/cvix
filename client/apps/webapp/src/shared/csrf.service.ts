import { BaseHttpClient } from "./BaseHttpClient";

/**
 * Global CSRF service for application-wide CSRF token initialization
 * This should be called once when the application starts
 */
export class CsrfService {
	private client: BaseHttpClient;
	private isInitializedState = false;
	private initializationPromise: Promise<void> | null = null;

	constructor() {
		this.client = new BaseHttpClient(this);
	}

	/**
	 * Initializes the CSRF token. This method is idempotent; it ensures the
	 * initialization logic runs only once and returns the same promise to all callers.
	 * This prevents multiple token requests during application startup.
	 * @returns {Promise<void>} A promise that resolves when initialization is complete.
	 */
	initialize(): Promise<void> {
		if (this.initializationPromise) {
			return this.initializationPromise;
		}

		this.initializationPromise = this.client
			.initializeCsrf()
			.then(() => {
				this.isInitializedState = true;
			})
			.catch((error) => {
				// On failure, reset the promise to allow the initialization to be retried.
				this.initializationPromise = null;
				// Re-throw the error to allow callers (like the HTTP client interceptor) to handle it.
				throw error;
			});

		return this.initializationPromise;
	}

	/**
	 * Check if CSRF has been initialized
	 */
	isInitialized(): boolean {
		return this.isInitializedState;
	}
}

// Export singleton instance
export const csrfService = new CsrfService();
