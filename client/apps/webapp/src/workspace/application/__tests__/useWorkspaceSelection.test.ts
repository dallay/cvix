// @ts-nocheck - Mock types for Pinia store are complex, tests pass in runtime
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../domain/WorkspaceEntity";
import { useWorkspaceSelection } from "../useWorkspaceSelection";

// Mock the workspace store
vi.mock("../../infrastructure/store/workspaceStore", () => ({
	useWorkspaceStore: vi.fn(),
}));

// Mock the workspace storage
vi.mock("../../infrastructure/storage/workspaceLocalStorage", () => ({
	saveLastSelected: vi.fn(),
	getLastSelected: vi.fn(),
	clearLastSelected: vi.fn(),
}));

import { saveLastSelected } from "../../infrastructure/storage/workspaceLocalStorage";
import { useWorkspaceStore } from "../../infrastructure/store/workspaceStore";

describe("useWorkspaceSelection", () => {
	const mockWorkspace1: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Workspace 1",
		description: "First workspace",
		isDefault: false,
		ownerId: "user-123",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	const mockWorkspace2: Workspace = {
		id: "660e8400-e29b-41d4-a716-446655440001",
		name: "Workspace 2",
		description: "Default workspace",
		isDefault: true,
		ownerId: "user-123",
		createdAt: new Date("2025-10-02T10:00:00Z"),
		updatedAt: new Date("2025-10-02T10:00:00Z"),
	};

	const mockWorkspaces = [mockWorkspace1, mockWorkspace2];

	type MockStore = {
		workspaces: Workspace[];
		currentWorkspace: Workspace | null;
		isLoading: boolean;
		error: string | null;
		selectWorkspace: ReturnType<typeof vi.fn>;
	};

	let mockStore: MockStore;

	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();

		// Create a mock store
		mockStore = {
			workspaces: mockWorkspaces,
			currentWorkspace: null,
			isLoading: false,
			error: null,
			selectWorkspace: vi.fn(),
		};

		vi.mocked(useWorkspaceStore).mockReturnValue(mockStore);
	});

	describe("composable initialization", () => {
		it("should expose workspaces from store", () => {
			const { workspaces } = useWorkspaceSelection();

			expect(workspaces.value).toEqual(mockWorkspaces);
		});

		it("should expose currentWorkspace from store", () => {
			mockStore.currentWorkspace = mockWorkspace1;

			const { currentWorkspace } = useWorkspaceSelection();

			expect(currentWorkspace.value).toEqual(mockWorkspace1);
		});

		it("should expose isLoading from store", () => {
			mockStore.isLoading = true;

			const { isLoading } = useWorkspaceSelection();

			expect(isLoading.value).toBe(true);
		});

		it("should expose error from store", () => {
			const mockError = "Failed to load";
			mockStore.error = mockError;

			const { error } = useWorkspaceSelection();

			expect(error.value).toBe(mockError);
		});
	});

	describe("selectWorkspace", () => {
		it("should call store.selectWorkspace with workspace ID", async () => {
			mockStore.selectWorkspace.mockResolvedValue(undefined);

			const { selectWorkspace } = useWorkspaceSelection();

			await selectWorkspace(mockWorkspace1.id, "user-123");

			expect(mockStore.selectWorkspace).toHaveBeenCalledWith(
				mockWorkspace1.id,
				"user-123",
			);
		});

		it("should handle successful workspace selection", async () => {
			mockStore.selectWorkspace.mockResolvedValue(undefined);
			mockStore.currentWorkspace = mockWorkspace1;

			const { selectWorkspace, currentWorkspace } = useWorkspaceSelection();

			await selectWorkspace(mockWorkspace1.id, "user-123");

			expect(currentWorkspace.value).toEqual(mockWorkspace1);
		});

		it("should handle errors during workspace selection", async () => {
			const error = new Error("Selection failed");
			mockStore.selectWorkspace.mockRejectedValue(error);

			const { selectWorkspace } = useWorkspaceSelection();

			await expect(
				selectWorkspace(mockWorkspace1.id, "user-123"),
			).rejects.toThrow("Selection failed");
		});

		it("should save last selected workspace to storage", async () => {
			mockStore.selectWorkspace.mockResolvedValue(undefined);

			const { selectWorkspace } = useWorkspaceSelection();

			await selectWorkspace(mockWorkspace1.id, "user-123");

			expect(saveLastSelected).toHaveBeenCalledWith(
				"user-123",
				mockWorkspace1.id,
			);
		});
	});

	describe("computed properties", () => {
		it("should compute hasWorkspaces correctly when workspaces exist", () => {
			const { hasWorkspaces } = useWorkspaceSelection();

			expect(hasWorkspaces.value).toBe(true);
		});

		it("should compute hasWorkspaces correctly when no workspaces exist", () => {
			mockStore.workspaces = [];

			const { hasWorkspaces } = useWorkspaceSelection();

			expect(hasWorkspaces.value).toBe(false);
		});

		it("should compute hasCurrentWorkspace correctly when workspace is selected", () => {
			mockStore.currentWorkspace = mockWorkspace1;

			const { hasCurrentWorkspace } = useWorkspaceSelection();

			expect(hasCurrentWorkspace.value).toBe(true);
		});

		it("should compute hasCurrentWorkspace correctly when no workspace is selected", () => {
			mockStore.currentWorkspace = null;

			const { hasCurrentWorkspace } = useWorkspaceSelection();

			expect(hasCurrentWorkspace.value).toBe(false);
		});
	});

	describe("workspace filtering", () => {
		it("should provide available workspaces excluding current", () => {
			mockStore.currentWorkspace = mockWorkspace1;

			const { availableWorkspaces } = useWorkspaceSelection();

			expect(availableWorkspaces.value).toEqual([mockWorkspace2]);
			expect(availableWorkspaces.value).toHaveLength(1);
		});

		it("should return all workspaces when none is selected", () => {
			mockStore.currentWorkspace = null;

			const { availableWorkspaces } = useWorkspaceSelection();

			expect(availableWorkspaces.value).toEqual(mockWorkspaces);
			expect(availableWorkspaces.value).toHaveLength(2);
		});
	});

	describe("default workspace", () => {
		it("should identify the default workspace", () => {
			const { defaultWorkspace } = useWorkspaceSelection();

			expect(defaultWorkspace.value).toEqual(mockWorkspace2);
		});

		it("should return null when no default workspace exists", () => {
			mockStore.workspaces = [
				{ ...mockWorkspace1, isDefault: false },
				{ ...mockWorkspace2, isDefault: false },
			];

			const { defaultWorkspace } = useWorkspaceSelection();

			expect(defaultWorkspace.value).toBeNull();
		});
	});
});
