import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Session, User } from "../domain/models/auth.model.ts";
import { authSessionStorage } from "../infrastructure/storage/SessionStorage.ts";
import { useAuthStore } from "../presentation/stores/authStore.ts";

const mockUser: User = {
	id: "123e4567-e89b-12d3-a456-426614174000",
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

			// Verify: User should be authenticated with restored session
			expect(authStore.user).toBeTruthy();
			expect(authStore.user?.email).toBe("test@example.com");
			expect(authStore.isAuthenticated).toBe(true);
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
		it("should call checkAuth during initialization when session is valid", async () => {
			// Setup: Store a valid session
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			// Create a promise we can control
			let resolveCheckAuth: (() => void) | undefined;
			const checkAuthPromise = new Promise<void>((resolve) => {
				resolveCheckAuth = resolve;
			});

			const checkAuthSpy = vi
				.spyOn(authStore, "checkAuth")
				.mockImplementation(async () => {
					// Mimic the real checkAuth behavior with loading state
					authStore.isLoading = true;
					await checkAuthPromise;
					authStore.isLoading = false;
				});

			// Start initialization
			const initPromise = authStore.initialize();

			// Verify: checkAuth should be called during initialization
			expect(checkAuthSpy).toHaveBeenCalledTimes(1);

			// Loading should be true while checkAuth is pending
			await new Promise((resolve) => setTimeout(resolve, 0));
			expect(authStore.isLoading).toBe(true);

			// Resolve the checkAuth
			if (resolveCheckAuth) {
				resolveCheckAuth();
			}
			await initPromise;

			// Loading should be false after initialization completes
			expect(authStore.isLoading).toBe(false);
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

	describe("isAuthenticated validation", () => {
		it("should return false when user has no id", async () => {
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			// Manually set invalid user state (simulates corrupted/malformed API response)
			authStore.user = {
				id: "", // Empty id should not be valid
				username: "test",
				email: "test@example.com",
				firstName: null,
				lastName: null,
				roles: [],
			};
			authStore.session = mockSession;

			// Even with session present, empty id should not be authenticated
			expect(authStore.isAuthenticated).toBe(false);
		});

		it("should return false when user id is undefined", async () => {
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			// Manually set invalid user state (simulates HTML response parsed as object)
			authStore.user = {
				id: undefined as unknown as string, // Type coercion to simulate runtime bug
				username: undefined as unknown as string,
				email: undefined as unknown as string,
				firstName: null,
				lastName: null,
				roles: [],
			};
			authStore.session = mockSession;

			// Should not be authenticated with undefined id
			expect(authStore.isAuthenticated).toBe(false);
		});

		it("should return true when user has valid id", async () => {
			authSessionStorage.setSessionExpiration(3600);

			const authStore = useAuthStore();

			authStore.user = mockUser;
			authStore.session = mockSession;

			expect(authStore.isAuthenticated).toBe(true);
		});
	});
});
