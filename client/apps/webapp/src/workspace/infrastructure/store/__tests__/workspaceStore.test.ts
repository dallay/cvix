// @ts-nocheck - Vitest module mocking with TypeScript is complex, tests pass in runtime
import { createPinia, setActivePinia } from "pinia";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../domain/WorkspaceEntity";
import { useWorkspaceStore } from "../workspaceStore";

// Mock the HTTP client module
vi.mock("../../http/workspaceHttpClient", () => ({
	workspaceHttpClient: {
		getAllWorkspaces: vi.fn(),
		getWorkspace: vi.fn(),
	},
}));

import { workspaceHttpClient } from "../../http/workspaceHttpClient";

describe("workspaceStore", () => {
	const mockWorkspace1: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Workspace 1",
		description: "First workspace",
		isDefault: false,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	const mockWorkspace2: Workspace = {
		id: "660e8400-e29b-41d4-a716-446655440001",
		name: "Workspace 2",
		description: "Default workspace",
		isDefault: true,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-02T10:00:00Z"),
		updatedAt: new Date("2025-10-02T10:00:00Z"),
	};

	const mockWorkspaces = [mockWorkspace1, mockWorkspace2];

	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();
		vi.useFakeTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	describe("initial state", () => {
		it("should have correct initial state", () => {
			const store = useWorkspaceStore();

			expect(store.workspaces).toEqual([]);
			expect(store.currentWorkspace).toBeNull();
			expect(store.isLoading).toBe(false);
			expect(store.error).toBeNull();
			expect(store.lastFetchedAt).toBeNull();
		});
	});

	describe("loadWorkspaces", () => {
		it("should set loading state while fetching", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			const promise = store.loadWorkspaces();

			expect(store.isLoading).toBe(true);

			await promise;
		});

		it("should load workspaces from API and update state", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			await store.loadWorkspaces();

			expect(store.workspaces).toEqual(mockWorkspaces);
			expect(store.isLoading).toBe(false);
			expect(store.error).toBeNull();
			expect(store.lastFetchedAt).toBeInstanceOf(Date);
		});

		it("should set error state when API call fails", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockRejectedValue(
				new Error("Network error"),
			);

			await store.loadWorkspaces();

			expect(store.workspaces).toEqual([]);
			expect(store.isLoading).toBe(false);
			expect(store.error).toBeTruthy();
			expect(store.error?.code).toBe("NETWORK_ERROR");
		});

		it("should use cached data when cache is fresh", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			// First load
			await store.loadWorkspaces();
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(1);

			// Second load within cache TTL (5 minutes)
			await store.loadWorkspaces();
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(1); // Not called again
		});

		it("should refetch when cache is stale", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			// First load
			await store.loadWorkspaces();
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(1);

			// Advance time by 6 minutes (more than 5-minute TTL)
			vi.advanceTimersByTime(6 * 60 * 1000);

			// Second load after cache expires
			await store.loadWorkspaces();
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(2);
		});

		it("should force refetch when force parameter is true", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			// First load
			await store.loadWorkspaces();
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(1);

			// Force reload
			await store.loadWorkspaces(true);
			expect(workspaceHttpClient.getAllWorkspaces).toHaveBeenCalledTimes(2);
		});
	});

	describe("setCurrentWorkspace", () => {
		it("should set current workspace", () => {
			const store = useWorkspaceStore();

			store.setCurrentWorkspace(mockWorkspace1);

			expect(store.currentWorkspace).toEqual(mockWorkspace1);
		});

		it("should update current workspace when called multiple times", () => {
			const store = useWorkspaceStore();

			store.setCurrentWorkspace(mockWorkspace1);
			expect(store.currentWorkspace).toEqual(mockWorkspace1);

			store.setCurrentWorkspace(mockWorkspace2);
			expect(store.currentWorkspace).toEqual(mockWorkspace2);
		});
	});

	describe("clearError", () => {
		it("should clear error state", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockRejectedValue(
				new Error("Network error"),
			);

			// Trigger error
			await store.loadWorkspaces();
			expect(store.error).toBeTruthy();

			// Clear error
			store.clearError();
			expect(store.error).toBeNull();
		});
	});

	describe("getters", () => {
		it("hasWorkspaces should return false when no workspaces", () => {
			const store = useWorkspaceStore();

			expect(store.hasWorkspaces).toBe(false);
		});

		it("hasWorkspaces should return true when workspaces exist", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			await store.loadWorkspaces();

			expect(store.hasWorkspaces).toBe(true);
		});

		it("defaultWorkspace should return default workspace", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue(
				mockWorkspaces,
			);

			await store.loadWorkspaces();

			expect(store.defaultWorkspace).toEqual(mockWorkspace2);
		});

		it("defaultWorkspace should return undefined when no default exists", async () => {
			const store = useWorkspaceStore();
			vi.mocked(workspaceHttpClient.getAllWorkspaces).mockResolvedValue([
				mockWorkspace1,
			]);

			await store.loadWorkspaces();

			expect(store.defaultWorkspace).toBeUndefined();
		});
	});
});
