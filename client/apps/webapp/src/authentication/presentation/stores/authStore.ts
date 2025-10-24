import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Session, User } from "../../domain/models/auth.model.ts";
import type {
	LoginFormData,
	RegisterFormData,
} from "../../domain/validators/auth.schema.ts";
import { AuthHttpClient } from "../../infrastructure/http/AuthHttpClient.ts";
import { authSessionStorage } from "../../infrastructure/storage/SessionStorage.ts";

/**
 * Authentication store using Pinia
 */
export const useAuthStore = defineStore("auth", () => {
	// State
	const user = ref<User | null>(null);
	const session = ref<Session | null>(null);
	const isLoading = ref(false);
	const error = ref<string | null>(null);

	// Getters
	const isAuthenticated = computed(() => !!user.value && !!session.value);
	const userRoles = computed(() => user.value?.roles ?? []);
	const hasRole = computed(
		() => (role: string) => userRoles.value.includes(role),
	);

	// Create HTTP client instance
	const httpClient = new AuthHttpClient();

	/**
	 * Register a new user
	 * Per registration flow diagram: After successful registration, redirect to login page
	 * User must verify email (if Keycloak is configured) before being able to login
	 */
	async function register(data: RegisterFormData): Promise<void> {
		isLoading.value = true;
		error.value = null;

		try {
			await httpClient.register(data);
		} catch (err) {
			error.value = err instanceof Error ? err.message : "Registration failed";
			throw err;
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Login with email and password
	 */
	async function login(data: LoginFormData): Promise<void> {
		isLoading.value = true;
		error.value = null;

		try {
			const newSession = await httpClient.login(data);
			session.value = newSession;

			// Store session expiration
			authSessionStorage.setSessionExpiration(newSession.expiresIn);

			// Fetch user details after successful login
			user.value = await httpClient.getCurrentUser();
		} catch (err) {
			error.value = err instanceof Error ? err.message : "Login failed";
			throw err;
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Logout the current user
	 */
	async function logout(): Promise<void> {
		isLoading.value = true;
		error.value = null;

		try {
			await httpClient.logout();
		} catch (err) {
			error.value = err instanceof Error ? err.message : "Logout failed";
			// Even if logout fails on the server, clear local state
		} finally {
			user.value = null;
			session.value = null;
			authSessionStorage.clearSession();
			isLoading.value = false;
		}
	}

	/**
	 * Refresh the access token
	 */
	async function refreshToken(): Promise<void> {
		try {
			const newSession = await httpClient.refreshToken();
			session.value = newSession;
			authSessionStorage.setSessionExpiration(newSession.expiresIn);
		} catch (err) {
			// If token refresh fails, logout the user
			await logout();
			throw err;
		}
	}

	/**
	 * Check if user is authenticated and fetch current user
	 * This is used to restore session on page reload
	 */
	async function checkAuth(): Promise<void> {
		isLoading.value = true;
		error.value = null;

		try {
			const currentUser = await httpClient.getCurrentUser();
			user.value = currentUser;

			// If we successfully got the user, it means we have a valid session
			// Create a minimal session object if we don't have one
			if (!session.value) {
				session.value = {
					accessToken: "", // Token is in HTTP-only cookie
					refreshToken: "",
					expiresIn: 0,
					tokenType: "Bearer",
					scope: "",
				};
			}
		} catch {
			user.value = null;
			session.value = null;
		} finally {
			isLoading.value = false;
		}
	}

	/**
	 * Initialize the auth store and restore session if valid
	 * Should be called on app startup
	 */
	async function initialize(): Promise<void> {
		if (authSessionStorage.isSessionValid()) {
			await checkAuth();
		}
	}

	/**
	 * Clear error state
	 */
	function clearError(): void {
		error.value = null;
	}

	/**
	 * Initiate OAuth login with a federated identity provider
	 */
	function loginWithOAuth(provider: string, redirectUri?: string): void {
		httpClient.initiateOAuthLogin(provider, redirectUri);
	}

	return {
		// State
		user,
		session,
		isLoading,
		error,

		// Getters
		isAuthenticated,
		userRoles,
		hasRole,

		// Actions
		register,
		login,
		logout,
		refreshToken,
		checkAuth,
		initialize,
		clearError,
		loginWithOAuth,
	};
});
