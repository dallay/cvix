import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
	clearLastSelected,
	getLastSelected,
	STORAGE_KEY,
	saveLastSelected,
} from "../workspaceLocalStorage";

describe("workspaceLocalStorage", () => {
	beforeEach(() => {
		// Clear localStorage before each test
		localStorage.clear();
		vi.clearAllMocks();
	});

	afterEach(() => {
		localStorage.clear();
	});

	describe("saveLastSelected", () => {
		it("should save workspace selection preference to localStorage", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			saveLastSelected(userId, workspaceId);

			const stored = localStorage.getItem(STORAGE_KEY);
			expect(stored).not.toBeNull();

			const parsed = JSON.parse(stored as string);
			expect(parsed.userId).toBe(userId);
			expect(parsed.lastSelectedWorkspaceId).toBe(workspaceId);
			expect(typeof parsed.selectedAt).toBe("string");
			expect(new Date(parsed.selectedAt).toISOString()).toBe(
				new Date(parsed.selectedAt).toISOString(),
			);
		});

		it("should overwrite existing preference for same user", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";
			const workspace1 = "550e8400-e29b-41d4-a716-446655440000";
			const workspace2 = "660e8400-e29b-41d4-a716-446655440001";

			saveLastSelected(userId, workspace1);
			saveLastSelected(userId, workspace2);

			const stored = localStorage.getItem(STORAGE_KEY);
			expect(stored).not.toBeNull();

			const parsed = JSON.parse(stored as string) as {
				lastSelectedWorkspaceId: string | null;
			};

			expect(parsed.lastSelectedWorkspaceId).toBe(workspace2);
		});

		it("should throw error for invalid user ID", () => {
			const invalidUserId = "invalid-uuid";
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			expect(() => saveLastSelected(invalidUserId, workspaceId)).toThrow(
				"Invalid user ID",
			);
		});

		it("should throw error for invalid workspace ID", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";
			const invalidWorkspaceId = "invalid-uuid";

			expect(() => saveLastSelected(userId, invalidWorkspaceId)).toThrow(
				"Invalid workspace ID",
			);
		});
	});

	describe("getLastSelected", () => {
		it("should retrieve last selected workspace for user", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			saveLastSelected(userId, workspaceId);

			const result = getLastSelected(userId);

			expect(result).not.toBeNull();
			expect(result?.userId).toBe(userId);
			expect(result?.lastSelectedWorkspaceId).toBe(workspaceId);
			expect(result?.selectedAt).toBeInstanceOf(Date);
		});

		it("should return null when no preference exists", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";

			const result = getLastSelected(userId);

			expect(result).toBeNull();
		});

		it("should return null when user ID does not match", () => {
			const user1 = "123e4567-e89b-42d3-a456-426614174000";
			const user2 = "223e4567-e89b-42d3-a456-426614174001";
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			saveLastSelected(user1, workspaceId);

			const result = getLastSelected(user2);

			expect(result).toBeNull();
		});

		it("should return null for corrupted localStorage data", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";

			localStorage.setItem(STORAGE_KEY, "invalid-json");

			const result = getLastSelected(userId);

			expect(result).toBeNull();
		});

		it("should throw error for invalid user ID", () => {
			const invalidUserId = "invalid-uuid";

			expect(() => getLastSelected(invalidUserId)).toThrow("Invalid user ID");
		});
	});

	describe("clearLastSelected", () => {
		it("should remove workspace selection preference from localStorage", () => {
			const userId = "123e4567-e89b-42d3-a456-426614174000";
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			saveLastSelected(userId, workspaceId);
			expect(localStorage.getItem(STORAGE_KEY)).not.toBeNull();

			clearLastSelected();

			expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
		});

		it("should not throw error when nothing to clear", () => {
			expect(() => clearLastSelected()).not.toThrow();
		});
	});

	describe("STORAGE_KEY", () => {
		it("should use correct storage key", () => {
			expect(STORAGE_KEY).toBe("loomify:workspace:lastSelected");
		});
	});
});
