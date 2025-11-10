import { describe, expect, it, vi } from "vitest";
import type { Workspace } from "../WorkspaceEntity.ts";
import { WorkspaceErrorCode } from "../WorkspaceError.ts";
import {
	determineWorkspaceToLoad,
	findDefaultWorkspace,
	handleNoWorkspaces,
	isWorkspaceValid,
	WorkspaceSelectionService,
} from "../WorkspaceSelectionService.ts";

describe("WorkspaceSelectionService", () => {
	const workspace1: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Workspace 1",
		description: "First workspace",
		isDefault: false,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	const workspace2: Workspace = {
		id: "660e8400-e29b-41d4-a716-446655440001",
		name: "Workspace 2",
		description: "Default workspace",
		isDefault: true,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-02T10:00:00Z"),
		updatedAt: new Date("2025-10-02T10:00:00Z"),
	};

	const workspace3: Workspace = {
		id: "770e8400-e29b-41d4-a716-446655440002",
		name: "Workspace 3",
		description: "Third workspace",
		isDefault: false,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-03T10:00:00Z"),
		updatedAt: new Date("2025-10-03T10:00:00Z"),
	};

	const workspaces = [workspace1, workspace2, workspace3];

	describe("determineWorkspaceToLoad", () => {
		it("should return last selected workspace when valid", () => {
			const lastSelectedId = workspace1.id;

			const result = determineWorkspaceToLoad(workspaces, lastSelectedId);

			expect(result).toEqual(workspace1);
		});

		it("should return default workspace when last selected is invalid", () => {
			const invalidLastSelectedId = "999e8400-e29b-41d4-a716-446655440999";

			const result = determineWorkspaceToLoad(
				workspaces,
				invalidLastSelectedId,
			);

			expect(result).toEqual(workspace2);
		});

		it("should return default workspace when last selected is null", () => {
			const result = determineWorkspaceToLoad(workspaces, null);

			expect(result).toEqual(workspace2);
		});

		it("should return first workspace when no default exists", () => {
			const workspacesWithoutDefault = [workspace1, workspace3];

			const result = determineWorkspaceToLoad(workspacesWithoutDefault, null);

			expect(result).toEqual(workspace1);
		});

		it("should return null when workspaces array is empty", () => {
			const result = determineWorkspaceToLoad([], null);

			expect(result).toBeNull();
		});

		it("should prioritize last selected over default", () => {
			const lastSelectedId = workspace3.id;

			const result = determineWorkspaceToLoad(workspaces, lastSelectedId);

			expect(result).toEqual(workspace3);
			expect(result).not.toEqual(workspace2); // Not the default
		});
	});

	describe("isWorkspaceValid", () => {
		it("should return true when workspace ID exists in list", () => {
			const result = isWorkspaceValid(workspace1.id, workspaces);

			expect(result).toBe(true);
		});

		it("should return false when workspace ID does not exist in list", () => {
			const invalidId = "999e8400-e29b-41d4-a716-446655440999";

			const result = isWorkspaceValid(invalidId, workspaces);

			expect(result).toBe(false);
		});

		it("should return false when workspaces array is empty", () => {
			const result = isWorkspaceValid(workspace1.id, []);

			expect(result).toBe(false);
		});
	});

	describe("findDefaultWorkspace", () => {
		it("should return the default workspace when one exists", () => {
			const result = findDefaultWorkspace(workspaces);

			expect(result).toEqual(workspace2);
		});

		it("should return undefined when no default workspace exists", () => {
			const workspacesWithoutDefault = [workspace1, workspace3];

			const result = findDefaultWorkspace(workspacesWithoutDefault);

			expect(result).toBeUndefined();
		});

		it("should return undefined when workspaces array is empty", () => {
			const result = findDefaultWorkspace([]);

			expect(result).toBeUndefined();
		});

		it("should return first default when multiple defaults exist", () => {
			// Mock development environment and spy on console.warn
			const originalEnv = import.meta.env.DEV;
			const consoleWarnSpy = vi
				.spyOn(console, "warn")
				.mockImplementation(() => {});

			// Set DEV to true to enable warnings
			import.meta.env.DEV = true;

			const workspace4: Workspace = {
				...workspace3,
				id: "880e8400-e29b-41d4-a716-446655440003",
				isDefault: true,
			};

			const workspacesWithMultipleDefaults = [
				workspace1,
				workspace2,
				workspace4,
			];

			const result = findDefaultWorkspace(workspacesWithMultipleDefaults);

			expect(result).toEqual(workspace2);
			expect(consoleWarnSpy).toHaveBeenCalledWith(
				"[Workspace] Multiple default workspaces found (2): 660e8400-e29b-41d4-a716-446655440001, 880e8400-e29b-41d4-a716-446655440003. Using first.",
			);

			// Restore original environment and spy
			import.meta.env.DEV = originalEnv;
			consoleWarnSpy.mockRestore();
		});

		it("should not log warning when multiple defaults exist in production", () => {
			// Mock production environment and spy on console.warn
			const originalEnv = import.meta.env.DEV;
			const consoleWarnSpy = vi
				.spyOn(console, "warn")
				.mockImplementation(() => {});

			// Set DEV to false to disable warnings
			import.meta.env.DEV = false;

			const workspace4: Workspace = {
				...workspace3,
				id: "880e8400-e29b-41d4-a716-446655440003",
				isDefault: true,
			};

			const workspacesWithMultipleDefaults = [
				workspace1,
				workspace2,
				workspace4,
			];

			const result = findDefaultWorkspace(workspacesWithMultipleDefaults);

			expect(result).toEqual(workspace2);
			expect(consoleWarnSpy).not.toHaveBeenCalled();

			// Restore original environment and spy
			import.meta.env.DEV = originalEnv;
			consoleWarnSpy.mockRestore();
		});
	});

	describe("handleNoWorkspaces", () => {
		it("should return error with NO_WORKSPACES code", () => {
			const result = handleNoWorkspaces();

			expect(result.code).toBe(WorkspaceErrorCode.NO_WORKSPACES);
			expect(result.message).toBeTruthy();
			expect(result.timestamp).toBeInstanceOf(Date);
		});
	});

	describe("WorkspaceSelectionService class", () => {
		it("should provide all methods via service instance", () => {
			const service = new WorkspaceSelectionService();

			expect(service.determineWorkspaceToLoad).toBeDefined();
			expect(service.isWorkspaceValid).toBeDefined();
			expect(service.findDefaultWorkspace).toBeDefined();
			expect(service.handleNoWorkspaces).toBeDefined();
		});

		it("should determine workspace using service instance", () => {
			const service = new WorkspaceSelectionService();
			const lastSelectedId = workspace1.id;

			const result = service.determineWorkspaceToLoad(
				workspaces,
				lastSelectedId,
			);

			expect(result).toEqual(workspace1);
		});
	});
});
