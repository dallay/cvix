/**
 * CSRF Token Management for Marketing Site
 *
 * This module handles CSRF token initialization for forms that interact with the backend API.
 * The token is loaded lazily (on-demand) to avoid unnecessary requests on pages without forms.
 */

/**
 * Initialize CSRF token by making a GET request to the backend
 * This sets the XSRF-TOKEN cookie which will be automatically sent with subsequent requests
 *
 * @param backendUrl - Base URL of the backend API
 * @returns Promise that resolves when CSRF token is initialized
 */
export async function initializeCsrfToken(backendUrl: string): Promise<void> {
	try {
		const response = await fetch(`${backendUrl}/api/health-check`, {
			method: "GET",
			credentials: "include", // Important: Include cookies in request
			headers: {
				Accept: "application/vnd.api.v1+json",
			},
		});

		if (!response.ok) {
			throw new Error(`CSRF initialization failed: ${response.status}`);
		}

		console.debug("✅ CSRF token initialized successfully");
	} catch (error) {
		console.warn("⚠️ Failed to initialize CSRF token:", error);
		// Don't throw - let the form submit handle CSRF errors
		// The backend will return a 403 if CSRF token is missing
	}
}

/**
 * Check if CSRF token cookie exists
 *
 * @returns true if XSRF-TOKEN cookie is present, false otherwise
 */
export function hasCsrfToken(): boolean {
	if (typeof document === "undefined") {
		return false;
	}

	const cookies = document.cookie.split(";");
	return cookies.some((cookie) => cookie.trim().startsWith("XSRF-TOKEN="));
}

/**
 * Get CSRF token from cookie
 *
 * @returns CSRF token value or null if not found
 */
export function getCsrfToken(): string | null {
	if (typeof document === "undefined") {
		return null;
	}

	const name = "XSRF-TOKEN=";
	const decodedCookie = decodeURIComponent(document.cookie);
	const cookies = decodedCookie.split(";");

	for (let cookie of cookies) {
		cookie = cookie.trim();
		if (cookie.startsWith(name)) {
			return cookie.substring(name.length);
		}
	}

	return null;
}

/**
 * Lazy CSRF token loader with caching
 * This class ensures CSRF token is loaded only once and only when needed
 */
export class CsrfTokenLoader {
	private loading = false;
	private loaded = false;

	/**
	 * Initialize CSRF token if not already loaded
	 * Multiple calls will only trigger one initialization
	 *
	 * @param backendUrl - Base URL of the backend API
	 */
	async ensureToken(backendUrl: string): Promise<void> {
		// Already loaded, skip
		if (this.loaded && hasCsrfToken()) {
			return;
		}

		// Currently loading, wait for it to complete
		if (this.loading) {
			// Wait a bit and check again
			await new Promise((resolve) => setTimeout(resolve, 100));
			return this.ensureToken(backendUrl);
		}

		// Start loading
		this.loading = true;

		try {
			await initializeCsrfToken(backendUrl);
			this.loaded = true;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * Reset the loader state (useful for testing or if token is invalidated)
	 */
	reset(): void {
		this.loading = false;
		this.loaded = false;
	}
}

// Export singleton instance for use across components
export const csrfLoader = new CsrfTokenLoader();
