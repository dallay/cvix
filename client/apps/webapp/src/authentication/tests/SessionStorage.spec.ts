import { beforeEach, describe, expect, it, vi } from "vitest";
import { authSessionStorage } from "../infrastructure/storage/SessionStorage";

describe("AuthSessionStorage", () => {
	beforeEach(() => {
		// Clear sessionStorage before each test
		sessionStorage.clear();
		vi.clearAllMocks();
	});

	describe("setSessionExpiration", () => {
		it("should store session expiration timestamp correctly", () => {
			const expiresIn = 3600; // 1 hour in seconds
			const beforeTimestamp = Date.now();

			authSessionStorage.setSessionExpiration(expiresIn);

			const storedValue = sessionStorage.getItem("auth_session_expires");
			expect(storedValue).toBeTruthy();

			const storedTimestamp = Number.parseInt(storedValue ?? "0", 10);
			const expectedTimestamp = beforeTimestamp + expiresIn * 1000;

			// Allow 100ms tolerance for test execution time
			expect(storedTimestamp).toBeGreaterThanOrEqual(expectedTimestamp - 100);
			expect(storedTimestamp).toBeLessThanOrEqual(expectedTimestamp + 100);
		});

		it("should overwrite existing session expiration", () => {
			authSessionStorage.setSessionExpiration(1800); // 30 minutes
			const firstValue = sessionStorage.getItem("auth_session_expires");

			authSessionStorage.setSessionExpiration(3600); // 1 hour
			const secondValue = sessionStorage.getItem("auth_session_expires");

			expect(secondValue).not.toEqual(firstValue);
		});
	});

	describe("getSessionExpiration", () => {
		it("should return null when no session expiration is stored", () => {
			const result = authSessionStorage.getSessionExpiration();
			expect(result).toBeNull();
		});

		it("should return the stored expiration timestamp", () => {
			const expiresIn = 3600;
			authSessionStorage.setSessionExpiration(expiresIn);

			const result = authSessionStorage.getSessionExpiration();
			expect(result).toBeTruthy();
			expect(typeof result).toBe("number");
		});

		it("should handle invalid stored values gracefully", () => {
			sessionStorage.setItem("auth_session_expires", "invalid");
			const result = authSessionStorage.getSessionExpiration();
			expect(result).toBeNull();
		});
	});

	describe("isSessionValid", () => {
		it("should return false when no session expiration is stored", () => {
			const result = authSessionStorage.isSessionValid();
			expect(result).toBe(false);
		});

		it("should return true when session is not expired", () => {
			const expiresIn = 3600; // 1 hour from now
			authSessionStorage.setSessionExpiration(expiresIn);

			const result = authSessionStorage.isSessionValid();
			expect(result).toBe(true);
		});

		it("should return false when session is expired", () => {
			// Set expiration to past time (negative expiresIn)
			const expiredTime = -1;
			authSessionStorage.setSessionExpiration(expiredTime);

			const result = authSessionStorage.isSessionValid();
			expect(result).toBe(false);
		});

		it("should return false when session expiration is exactly now", () => {
			// Set expiration to 0 seconds (expires immediately)
			authSessionStorage.setSessionExpiration(0);

			// Wait a tiny bit to ensure time has passed
			vi.useFakeTimers();
			vi.advanceTimersByTime(1);

			const result = authSessionStorage.isSessionValid();
			expect(result).toBe(false);

			vi.useRealTimers();
		});
	});

	describe("clearSession", () => {
		it("should remove session expiration from storage", () => {
			authSessionStorage.setSessionExpiration(3600);
			expect(sessionStorage.getItem("auth_session_expires")).toBeTruthy();

			authSessionStorage.clearSession();
			expect(sessionStorage.getItem("auth_session_expires")).toBeNull();
		});

		it("should not throw error when clearing non-existent session", () => {
			expect(() => authSessionStorage.clearSession()).not.toThrow();
		});

		it("should be idempotent (safe to call multiple times)", () => {
			authSessionStorage.setSessionExpiration(3600);
			authSessionStorage.clearSession();
			authSessionStorage.clearSession();
			expect(sessionStorage.getItem("auth_session_expires")).toBeNull();
		});
	});

	describe("integration scenarios", () => {
		it("should handle complete session lifecycle", () => {
			// 1. No session initially
			expect(authSessionStorage.isSessionValid()).toBe(false);

			// 2. Set session
			authSessionStorage.setSessionExpiration(3600);
			expect(authSessionStorage.isSessionValid()).toBe(true);

			// 3. Clear session
			authSessionStorage.clearSession();
			expect(authSessionStorage.isSessionValid()).toBe(false);
		});

		it("should persist data across multiple reads", () => {
			authSessionStorage.setSessionExpiration(3600);

			// Multiple reads should return the same value
			const firstRead = authSessionStorage.getSessionExpiration();
			const secondRead = authSessionStorage.getSessionExpiration();
			const thirdRead = authSessionStorage.getSessionExpiration();

			expect(firstRead).toEqual(secondRead);
			expect(secondRead).toEqual(thirdRead);
		});

		it("should handle session refresh scenario", () => {
			// Initial session
			authSessionStorage.setSessionExpiration(1800);
			const initialExpiration = authSessionStorage.getSessionExpiration();

			// Simulate token refresh after some time
			vi.useFakeTimers();
			vi.advanceTimersByTime(1000); // Advance 1 second

			// New session with extended expiration
			authSessionStorage.setSessionExpiration(3600);
			const newExpiration = authSessionStorage.getSessionExpiration();

			expect(newExpiration).toBeGreaterThan(initialExpiration ?? 0);
			expect(authSessionStorage.isSessionValid()).toBe(true);

			vi.useRealTimers();
		});
	});
});
