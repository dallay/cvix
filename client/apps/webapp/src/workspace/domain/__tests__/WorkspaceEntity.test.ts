import { describe, expect, it } from "vitest";
import type { Workspace } from "../WorkspaceEntity";
import { createWorkspace, isValidWorkspace } from "../WorkspaceEntity";

describe("WorkspaceEntity", () => {
	const validWorkspaceData: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Test Workspace",
		description: "A test workspace",
		isDefault: false,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	describe("createWorkspace", () => {
		it("should create a valid workspace", () => {
			const workspace = createWorkspace(validWorkspaceData);

			expect(workspace).toEqual(validWorkspaceData);
		});

		it("should create workspace with null description", () => {
			const workspaceData: Workspace = {
				...validWorkspaceData,
				description: null,
			};

			const workspace = createWorkspace(workspaceData);

			expect(workspace.description).toBeNull();
		});

		it("should throw error for invalid workspace ID", () => {
			const invalidData = {
				...validWorkspaceData,
				id: "invalid-uuid",
			};

			expect(() => createWorkspace(invalidData)).toThrow(
				"Invalid workspace ID",
			);
		});

		it("should throw error for empty workspace name", () => {
			const invalidData = {
				...validWorkspaceData,
				name: "",
			};

			expect(() => createWorkspace(invalidData)).toThrow(
				"Workspace name must be 1-100 characters",
			);
		});

		it("should throw error for workspace name exceeding 100 characters", () => {
			const invalidData = {
				...validWorkspaceData,
				name: "A".repeat(101),
			};

			expect(() => createWorkspace(invalidData)).toThrow(
				"Workspace name must be 1-100 characters",
			);
		});

		it("should throw error for invalid owner ID", () => {
			const invalidData = {
				...validWorkspaceData,
				ownerId: "invalid-uuid",
			};

			expect(() => createWorkspace(invalidData)).toThrow("Invalid owner ID");
		});

		it("should throw error for description exceeding 500 characters", () => {
			const invalidData = {
				...validWorkspaceData,
				description: "A".repeat(501),
			};

			expect(() => createWorkspace(invalidData)).toThrow(
				"Workspace description must not exceed 500 characters",
			);
		});

		it("should throw error when updatedAt is before createdAt", () => {
			const invalidData = {
				...validWorkspaceData,
				createdAt: new Date("2025-10-01T10:00:00Z"),
				updatedAt: new Date("2025-09-01T10:00:00Z"), // Before createdAt
			};

			expect(() => createWorkspace(invalidData)).toThrow(
				"updatedAt must be after or equal to createdAt",
			);
		});
	});

	describe("isValidWorkspace", () => {
		it("should return true for valid workspace", () => {
			expect(isValidWorkspace(validWorkspaceData)).toBe(true);
		});

		it("should return false for invalid workspace ID", () => {
			const invalidData = {
				...validWorkspaceData,
				id: "invalid-uuid",
			};

			expect(isValidWorkspace(invalidData)).toBe(false);
		});

		it("should return false for invalid workspace name", () => {
			const invalidData = {
				...validWorkspaceData,
				name: "",
			};

			expect(isValidWorkspace(invalidData)).toBe(false);
		});

		it("should return false for invalid owner ID", () => {
			const invalidData = {
				...validWorkspaceData,
				ownerId: "invalid-uuid",
			};

			expect(isValidWorkspace(invalidData)).toBe(false);
		});

		it("should return true for workspace with null description", () => {
			const workspaceData = {
				...validWorkspaceData,
				description: null,
			};

			expect(isValidWorkspace(workspaceData)).toBe(true);
		});

		it("should return false for description exceeding 500 characters", () => {
			const invalidData = {
				...validWorkspaceData,
				description: "A".repeat(501),
			};

			expect(isValidWorkspace(invalidData)).toBe(false);
		});

		it("should return false when updatedAt is before createdAt", () => {
			const invalidData = {
				...validWorkspaceData,
				createdAt: new Date("2025-10-01T10:00:00Z"),
				updatedAt: new Date("2025-09-01T10:00:00Z"),
			};

			expect(isValidWorkspace(invalidData)).toBe(false);
		});
	});
});
