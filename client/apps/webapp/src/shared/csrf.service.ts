import axios, { type AxiosInstance } from "axios";

/**
 * Global CSRF service for application-wide CSRF token initialization.
 * This should be called once when the application starts.
 */
class CsrfService {
	private client: AxiosInstance;
	private isInitializedState = false;
	private initializationPromise: Promise<void> | null = null;

	constructor() {
		const envRecord = import.meta.env as unknown as Record<string, unknown>;
		const backend = envRecord.BACKEND_URL as string | undefined;
		const baseURL = backend ?? "/api";
		// Use a private, minimal axios instance to avoid circular dependencies.
		this.client = axios.create({ baseURL, withCredentials: true });
	}

	/**
	 * Initializes the CSRF token. This method is idempotent, ensuring the
	 * initialization logic runs only once and returns the same promise to all callers.
	 * @returns {Promise<void>} A promise that resolves when initialization is complete.
	 */
	initialize(): Promise<void> {
		if (this.initializationPromise) {
			return this.initializationPromise;
		}

		this.initializationPromise = this.client
			.get<void>("/health-check")
			.then(() => {
				this.isInitializedState = true;
				console.debug("CSRF token initialized successfully");
			})
			.catch((error) => {
				this.initializationPromise = null;
				console.warn(
					"Failed to initialize CSRF token, will retry on first request:",
					error,
				);
				throw error;
			});

		return this.initializationPromise;
	}

	/**
	 * Checks if CSRF has been initialized.
	 */
	isInitialized(): boolean {
		return this.isInitializedState;
	}
}

// Export singleton instance
export const csrfService = new CsrfService();
