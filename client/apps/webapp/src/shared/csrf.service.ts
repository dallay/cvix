import { BaseHttpClient } from "./BaseHttpClient";

/**
 * Global CSRF service for application-wide CSRF token initialization
 * This should be called once when the application starts
 */
class CsrfService {
	private client: BaseHttpClient;
	private initialized = false;

	constructor() {
		this.client = new BaseHttpClient();
	}

	/**
	 * Initialize CSRF token for the application
	 * This method is idempotent - it will only initialize once
	 */
	async initialize(): Promise<void> {
		if (this.initialized) {
			return;
		}

		try {
			await this.client.initializeCsrf();
			this.initialized = true;
		} catch (error) {
			console.error("Failed to initialize CSRF token on app startup:", error);
			// Don't throw - let individual requests handle CSRF errors
		}
	}

	/**
	 * Check if CSRF has been initialized
	 */
	isInitialized(): boolean {
		return this.initialized;
	}
}

// Export singleton instance
export const csrfService = new CsrfService();
