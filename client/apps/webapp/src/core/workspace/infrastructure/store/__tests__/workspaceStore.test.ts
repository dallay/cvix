import { createPinia, setActivePinia } from "pinia";
import type { Mock } from "vitest";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../../domain/WorkspaceEntity.ts";
import { useWorkspaceStore } from "../workspaceStore.ts";

// Mock the HTTP client module
vi.mock("../../http/workspaceHttpClient", () => ({
	workspaceHttpClient: {
		getAllWorkspaces: vi.fn(),
		getWorkspace: vi.fn(),
	},
}));

vi.mock("../../storage/workspaceLocalStorage", () => ({
	saveLastSelected: vi.fn(),
	getLastSelected: vi.fn(),
	clearLastSelected: vi.fn(),
}));

vi.mock("@/shared/WorkspaceContext", () => ({
	setCurrentWorkspaceId: vi.fn(),
	clearCurrentWorkspaceId: vi.fn(),
}));

import {
	clearCurrentWorkspaceId,
	setCurrentWorkspaceId,
} from "@/shared/WorkspaceContext";
import { WorkspaceErrorCode } from "../../../domain/WorkspaceError.ts";
import { workspaceHttpClient } from "../../http/workspaceHttpClient.ts";
import { saveLastSelected } from "../../storage/workspaceLocalStorage.ts";

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
		vi.resetAllMocks();
		vi.useFakeTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	describe("selectWorkspace", () => {
		const userId = "123e4567-e89b-42d3-a456-426614174000";

		it("should select an existing workspace and persist the selection", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [...mockWorkspaces];
			store.currentWorkspace = null;
			vi.mocked(workspaceHttpClient.getWorkspace).mockResolvedValue(null);

			await store.selectWorkspace(mockWorkspace1.id, userId);

			expect(store.currentWorkspace).toEqual(mockWorkspace1);
			expect(saveLastSelected).toHaveBeenCalledWith(userId, mockWorkspace1.id);
			expect(store.error).toBeNull();
		});

		it("should fetch workspace when not present in store", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [mockWorkspace1];
			const remoteWorkspace = {
				...mockWorkspace2,
				id: "770e8400-e29b-41d4-a716-446655440002",
			};
			vi.mocked(workspaceHttpClient.getWorkspace).mockResolvedValue(
				remoteWorkspace,
			);

			await store.selectWorkspace(remoteWorkspace.id, userId);

			expect(workspaceHttpClient.getWorkspace).toHaveBeenCalledWith(
				remoteWorkspace.id,
			);
			expect(store.workspaces).toContainEqual(remoteWorkspace);
			expect(store.currentWorkspace).toEqual(remoteWorkspace);
			expect(saveLastSelected).toHaveBeenCalledWith(userId, remoteWorkspace.id);
		});

		it("should set error when workspace cannot be found", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [];
			vi.mocked(workspaceHttpClient.getWorkspace).mockResolvedValue(null);

			await expect(
				store.selectWorkspace("999e8400-e29b-41d4-a716-446655440999", userId),
			).rejects.toThrow();

			expect(store.error).not.toBeNull();
			expect(store.error?.code).toBe(WorkspaceErrorCode.WORKSPACE_NOT_FOUND);
			expect(saveLastSelected).not.toHaveBeenCalled();
		});

		it("should propagate validation errors", async () => {
			const store = useWorkspaceStore();

			await expect(store.selectWorkspace("invalid", userId)).rejects.toThrow(
				"Invalid workspace ID",
			);

			expect(store.error?.code).toBe(WorkspaceErrorCode.VALIDATION_ERROR);
		});

		it("should surface storage errors without preventing selection", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [...mockWorkspaces];
			const storageError = new Error("Storage unavailable");
			vi.mocked(saveLastSelected).mockImplementation(() => {
				throw storageError;
			});

			await expect(
				store.selectWorkspace(mockWorkspace2.id, userId),
			).rejects.toThrow("Storage unavailable");

			expect(store.currentWorkspace).toEqual(mockWorkspace2);
			expect(store.error?.code).toBe(WorkspaceErrorCode.SELECTION_FAILED);
		});
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

			await expect(store.loadWorkspaces()).rejects.toMatchObject({
				code: WorkspaceErrorCode.NETWORK_ERROR,
			});

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
			await expect(store.loadWorkspaces()).rejects.toMatchObject({
				code: WorkspaceErrorCode.NETWORK_ERROR,
			});
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

	describe("resetSession", () => {
		it("should reset all session state", () => {
			const store = useWorkspaceStore();
			// Set some state
			store.currentWorkspace = mockWorkspace1;
			store.lastSelectedId = mockWorkspace1.id;
			store.lastSelectedAt = new Date();
			store.loadedInSession = true;
			store.error = {
				code: WorkspaceErrorCode.NETWORK_ERROR,
				message: "Test error",
				timestamp: new Date(),
			};

			store.resetSession();

			expect(store.currentWorkspace).toBeNull();
			expect(store.lastSelectedId).toBeNull();
			expect(store.lastSelectedAt).toBeNull();
			expect(store.loadedInSession).toBe(false);
			expect(store.error).toBeNull();
		});

		it("should clear workspace context for HTTP clients", () => {
			const store = useWorkspaceStore();
			store.currentWorkspace = mockWorkspace1;

			store.resetSession();

			expect(clearCurrentWorkspaceId).toHaveBeenCalled();
		});
	});

	describe("setCurrentWorkspace context sync", () => {
		it("should sync workspace ID with global context", () => {
			const store = useWorkspaceStore();

			store.setCurrentWorkspace(mockWorkspace1);

			expect(setCurrentWorkspaceId).toHaveBeenCalledWith(mockWorkspace1.id);
		});
	});

	describe("setCurrentWorkspace", () => {
		it("should sync workspace ID with global context", () => {
			const store = useWorkspaceStore();

			store.setCurrentWorkspace(mockWorkspace1);

			expect(setCurrentWorkspaceId).toHaveBeenCalledWith(mockWorkspace1.id);
		});

		it("should clear context when setting null workspace", () => {
			const store = useWorkspaceStore();
			store.currentWorkspace = mockWorkspace1;

			store.setCurrentWorkspace(null);

			expect(store.currentWorkspace).toBeNull();
			expect(clearCurrentWorkspaceId).toHaveBeenCalled();
		});
	});

	describe("selectWorkspace context sync", () => {
		const userId = "123e4567-e89b-42d3-a456-426614174000";

		it("should sync workspace ID with global context on selection", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [...mockWorkspaces];
			(workspaceHttpClient.getWorkspace as Mock).mockResolvedValue(null);

			await store.selectWorkspace(mockWorkspace1.id, userId);

			expect(setCurrentWorkspaceId).toHaveBeenCalledWith(mockWorkspace1.id);
		});

		it("should not sync context when workspace selection fails", async () => {
			const store = useWorkspaceStore();
			store.workspaces = [];
			(workspaceHttpClient.getWorkspace as Mock).mockResolvedValue(null);
			(setCurrentWorkspaceId as Mock).mockClear();

			await expect(
				store.selectWorkspace("999e8400-e29b-41d4-a716-446655440999", userId),
			).rejects.toThrow();

			expect(setCurrentWorkspaceId).not.toHaveBeenCalled();
		});
	});
});
