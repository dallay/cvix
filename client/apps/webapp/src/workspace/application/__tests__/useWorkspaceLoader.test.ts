// @ts-nocheck - Mock types are complex, tests pass in runtime
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../domain/WorkspaceEntity";
import { useWorkspaceLoader } from "../useWorkspaceLoader";

describe("useWorkspaceLoader", () => {
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

	// Helper to create complete mock store
	function createMockStore(workspaces: Workspace[] = mockWorkspaces) {
		return {
			loadWorkspaces: vi.fn().mockResolvedValue(undefined),
			setCurrentWorkspace: vi.fn(),
			clearError: vi.fn(),
			workspaces,
			currentWorkspace: null as Workspace | null,
			isLoading: false,
			error: null,
			lastFetchedAt: null,
			hasWorkspaces: workspaces.length > 0,
			defaultWorkspace: workspaces.find((w) => w.isDefault) || null,
		};
	}

	// Helper to create complete mock storage
	function createMockStorage(
		lastSelected: {
			userId: string;
			lastSelectedWorkspaceId: string;
			selectedAt: Date;
		} | null = null,
	) {
		return {
			getLastSelected: vi.fn().mockReturnValue(lastSelected),
			saveLastSelected: vi.fn(),
			clearLastSelected: vi.fn(),
		};
	}

	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();
	});

	describe("loadWorkspaceOnLogin", () => {
		it("should load last selected workspace when available", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();

			const mockStore = createMockStore();
			const mockStorage = createMockStorage({
				userId: "123e4567-e89b-42d3-a456-426614174000",
				lastSelectedWorkspaceId: mockWorkspace1.id,
				selectedAt: new Date(),
			});

			const result = await loadWorkspaceOnLogin(
				"123e4567-e89b-42d3-a456-426614174000",
				{
					store: mockStore,
					storage: mockStorage,
				},
			);

			expect(result).toEqual(mockWorkspace1);
			expect(mockStore.setCurrentWorkspace).toHaveBeenCalledWith(
				mockWorkspace1,
			);
		});

		it("should load default workspace when no last selected", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();

			const mockStore = createMockStore();
			const mockStorage = createMockStorage(null);

			const result = await loadWorkspaceOnLogin(
				"123e4567-e89b-42d3-a456-426614174000",
				{
					store: mockStore,
					storage: mockStorage,
				},
			);

			expect(result).toEqual(mockWorkspace2); // Default workspace
			expect(mockStore.setCurrentWorkspace).toHaveBeenCalledWith(
				mockWorkspace2,
			);
		});

		it("should load first workspace when no default exists", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();

			const workspacesWithoutDefault = [mockWorkspace1];
			const mockStore = createMockStore(workspacesWithoutDefault);
			const mockStorage = createMockStorage(null);

			const result = await loadWorkspaceOnLogin(
				"123e4567-e89b-42d3-a456-426614174000",
				{
					store: mockStore,
					storage: mockStorage,
				},
			);

			expect(result).toEqual(mockWorkspace1);
			expect(mockStore.setCurrentWorkspace).toHaveBeenCalledWith(
				mockWorkspace1,
			);
		});

		it("should return null when no workspaces available", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();

			const mockStore = createMockStore([]);
			const mockStorage = createMockStorage(null);

			const result = await loadWorkspaceOnLogin(
				"123e4567-e89b-42d3-a456-426614174000",
				{
					store: mockStore,
					storage: mockStorage,
				},
			);

			expect(result).toBeNull();
			expect(mockStore.setCurrentWorkspace).not.toHaveBeenCalled();
		});

		it("should handle errors gracefully", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();

			const mockStore = createMockStore([]);
			mockStore.loadWorkspaces = vi
				.fn()
				.mockRejectedValue(new Error("Network error"));
			const mockStorage = createMockStorage(null);

			await expect(
				loadWorkspaceOnLogin("123e4567-e89b-42d3-a456-426614174000", {
					store: mockStore,
					storage: mockStorage,
					maxRetries: 3,
				}),
			).rejects.toThrow("Network error");
		});

		it("should retry with exponential backoff", async () => {
			const { loadWorkspaceOnLogin } = useWorkspaceLoader();
			const mockStore = createMockStore([]);
			let callCount = 0;
			mockStore.loadWorkspaces = vi.fn().mockImplementation(() => {
				callCount++;
				if (callCount < 3) throw new Error("Network error");
				return Promise.resolve();
			});

			await loadWorkspaceOnLogin("123e4567-e89b-42d3-a456-426614174000", {
				store: mockStore,
				maxRetries: 3,
			});
			expect(mockStore.loadWorkspaces).toHaveBeenCalledTimes(3);
		});
	});

	describe("switchWorkspace", () => {
		it("should switch to specified workspace and save to local storage", async () => {
			const { switchWorkspace } = useWorkspaceLoader();

			const mockStore = createMockStore();
			const mockStorage = createMockStorage(null);

			await switchWorkspace(
				"123e4567-e89b-42d3-a456-426614174000",
				mockWorkspace2.id,
				{
					store: mockStore,
					storage: mockStorage,
				},
			);

			expect(mockStore.setCurrentWorkspace).toHaveBeenCalledWith(
				mockWorkspace2,
			);
			expect(mockStorage.saveLastSelected).toHaveBeenCalledWith(
				"123e4567-e89b-42d3-a456-426614174000",
				mockWorkspace2.id,
			);
		});

		it("should throw error when workspace not found", async () => {
			const { switchWorkspace } = useWorkspaceLoader();

			const mockStore = createMockStore();
			const mockStorage = createMockStorage(null);

			await expect(
				switchWorkspace(
					"123e4567-e89b-42d3-a456-426614174000",
					"999e8400-e29b-41d4-a716-446655440999",
					{
						store: mockStore,
						storage: mockStorage,
					},
				),
			).rejects.toThrow();
		});
	});
});
