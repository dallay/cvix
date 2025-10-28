const SESSION_KEY = "auth_session_expires";

/**
 * Session storage utility for managing authentication session persistence
 */
export class AuthSessionStorage {
	/**
	 * Store the session expiration time
	 * @param expiresIn Time in seconds until the session expires
	 */
	setSessionExpiration(expiresIn: number): void {
		const expirationTime = Date.now() + expiresIn * 1000;
		window.sessionStorage.setItem(SESSION_KEY, expirationTime.toString());
	}

	/**
	 * Get the stored session expiration time
	 * @returns The expiration timestamp, or null if not found or invalid
	 */
	getSessionExpiration(): number | null {
		const expiration = window.sessionStorage.getItem(SESSION_KEY);
		if (!expiration) {
			return null;
		}
		const parsed = Number.parseInt(expiration, 10);
		return Number.isNaN(parsed) ? null : parsed;
	}

	/**
	 * Clear the stored session data
	 */
	clearSession(): void {
		window.sessionStorage.removeItem(SESSION_KEY);
	}

	/**
	 * Check if the current session is still valid
	 * @returns True if the session is valid, false otherwise
	 */
	isSessionValid(): boolean {
		const expiration = this.getSessionExpiration();
		return expiration ? Date.now() < expiration : false;
	}
}

export const authSessionStorage = new AuthSessionStorage();
