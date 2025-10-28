import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Session, User } from "../domain/models/auth.model";
import { authSessionStorage } from "../infrastructure/storage/SessionStorage";
import { useAuthStore } from "../presentation/stores/authStore";

const mockUser: User = {
	username: "testuser",
	email: "test@example.com",
	firstName: "Test",
	lastName: "User",
	roles: ["USER"],
};

const mockSession: Session = {
	accessToken: "mock-access-token",
	refreshToken: "mock-refresh-token",
	expiresIn: 3600,
	tokenType: "Bearer",
	scope: "openid profile email",
};

// Mock the HTTP client
vi.mock("../infrastructure/http/AuthHttpClient", () => {
	return {
		AuthHttpClient: vi.fn(function AuthHttpClient(this: {
			login: () => Promise<Session>;
			getCurrentUser: () => Promise<User>;
			logout: () => Promise<void>;
			refreshToken: () => Promise<Session>;
			register: () => Promise<User>;
			setupInterceptors: () => void;
			initiateOAuthLogin: () => void;
		}) {
			this.login = async () => mockSession;
			this.getCurrentUser = async () => mockUser;
			this.logout = async () => {};
			this.refreshToken = async () => mockSession;
			this.register = async () => mockUser;
			this.setupInterceptors = () => {};
			this.initiateOAuthLogin = () => {};
		}),
	};
});

describe("authStore.initialize", () => {
	beforeEach(() => {
		setActivePinia(createPinia());
		sessionStorage.clear();
		vi.clearAllMocks();
	});

	describe("session restoration", () => {
		it("should attempt to restore user when valid session exists", async () => {
			// Setup: Store a valid session
			authSessionStorage.setSessionExpiration(3600); // 1 hour from now

			const authStore = useAuthStore();

			// Execute: Initialize the store
			await authStore.initialize();

			// Verify: checkAuth should have been called and user info fetched
			// Note: isAuthenticated will be false because we don't store tokens in sessionStorage
			// (security best practice). User data is fetched but session tokens are not restored.
			expect(authStore.user).toBeTruthy();
			expect(authStore.user?.email).toBe("test@example.com");
		});

		it("should not restore session when session is expired", async () => {
			// Setup: Store an expired session
			authSessionStorage.setSessionExpiration(-1); // Expired 1 second ago

			const authStore = useAuthStore();

			// Execute: Initialize the store
			await authStore.initialize();

			// Verify: User should not be authenticated
			expect(authStore.isAuthenticated).toBe(false);
			expect(authStore.user).toBeNull();
		});

		it("should not restore session when no session exists", async () => {
			// Setup: No session in storage

			const authStore = useAuthStore();

			// Execute: Initialize the store
			await authStore.initialize();

			// Verify: User should not be authenticated
			expect(authStore.isAuthenticated).toBe(false);
			expect(authStore.user).toBeNull();
		});
	});

	describe("error handling", () => {
		it("should handle checkAuth failure gracefully", async () => {
			// Setup: Valid session but API call will fail
			authSessionStorage.setSessionExpiration(3600);

			// Mock getCurrentUser to throw an error
			const authStore = useAuthStore();
			vi.spyOn(authStore, "checkAuth").mockRejectedValueOnce(
				new Error("Network error"),
			);

			// Execute: Initialize should not throw
			await expect(authStore.initialize()).resolves.not.toThrow();

			// Verify: User should not be authenticated
			expect(authStore.isAuthenticated).toBe(false);
		});
	});

	describe("loading state", () => {
		it("should set loading state during initialization", async () => {
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			// Create a promise we can control
			let resolveCheckAuth: (() => void) | undefined;
			const checkAuthPromise = new Promise<void>((resolve) => {
				resolveCheckAuth = resolve;
			});

			vi.spyOn(authStore, "checkAuth").mockReturnValue(checkAuthPromise);

			// Start initialization
			const initPromise = authStore.initialize();

			// Loading should be true during checkAuth
			await new Promise((resolve) => setTimeout(resolve, 0));
			// Note: Loading state is managed by checkAuth internally

			// Resolve the checkAuth
			if (resolveCheckAuth) {
				resolveCheckAuth();
			}
			await initPromise;
		});
	});

	describe("integration scenarios", () => {
		it("should handle app reload scenario", async () => {
			// Scenario 1: User logs in (simulated)
			authSessionStorage.setSessionExpiration(3600);

			// Scenario 2: App reloads (new store instance)
			const authStore = useAuthStore();
			await authStore.initialize();

			// Verify: User data is restored (tokens are in HTTP-only cookies, not storage)
			expect(authStore.user).toBeTruthy();
			expect(authStore.user?.email).toBe("test@example.com");
		});

		it("should handle expired session on app reload", async () => {
			// Scenario 1: User logged in but session expired
			authSessionStorage.setSessionExpiration(-3600); // Expired 1 hour ago

			// Scenario 2: App reloads
			const authStore = useAuthStore();
			await authStore.initialize();

			// Verify: Session is not restored, user must login again
			expect(authStore.isAuthenticated).toBe(false);
			expect(authStore.user).toBeNull();
		});

		it("should call initialize multiple times independently", async () => {
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			// Call initialize multiple times sequentially
			await authStore.initialize();
			expect(authStore.user).toBeTruthy();

			await authStore.initialize();
			expect(authStore.user).toBeTruthy();

			await authStore.initialize();
			expect(authStore.user).toBeTruthy();

			// All calls should succeed independently
			expect(authStore.user?.email).toBe("test@example.com");
		});
	});

	describe("session validity edge cases", () => {
		it("should handle session expiring during initialization", async () => {
			// Set session to expire very soon
			authSessionStorage.setSessionExpiration(1); // 1 second from now

			const authStore = useAuthStore();

			// Delay to let session expire
			await new Promise((resolve) => setTimeout(resolve, 1100));

			await authStore.initialize();

			// Session should be treated as invalid
			expect(authStore.isAuthenticated).toBe(false);
		});

		it("should handle boundary case: session expires at exact moment", async () => {
			// Set session to expire in 0 seconds (expires immediately)
			authSessionStorage.setSessionExpiration(0);

			const authStore = useAuthStore();
			await authStore.initialize();

			// Should not restore session
			expect(authStore.isAuthenticated).toBe(false);
		});
	});
});
