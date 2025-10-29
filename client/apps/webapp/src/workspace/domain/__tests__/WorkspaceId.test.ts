import { describe, expect, it } from "vitest";
import { WorkspaceId } from "../WorkspaceId";

describe("WorkspaceId", () => {
	describe("constructor", () => {
		it("should create WorkspaceId with valid UUID v4", () => {
			const validUuid = "550e8400-e29b-41d4-a716-446655440000";
			const workspaceId = new WorkspaceId(validUuid);

			expect(workspaceId.toString()).toBe(validUuid);
		});

		it("should throw error for invalid UUID format", () => {
			const invalidUuid = "not-a-uuid";

			expect(() => new WorkspaceId(invalidUuid)).toThrow(
				"Invalid workspace ID",
			);
		});

		it("should throw error for empty string", () => {
			expect(() => new WorkspaceId("")).toThrow("Invalid workspace ID");
		});

		it("should throw error for non-v4 UUID", () => {
			// UUID v1 format
			const uuidV1 = "550e8400-e29b-11d4-a716-446655440000";

			expect(() => new WorkspaceId(uuidV1)).toThrow("Invalid workspace ID");
		});
	});

	describe("isValid", () => {
		it("should return true for valid UUID v4", () => {
			const validUuid = "550e8400-e29b-41d4-a716-446655440000";

			expect(WorkspaceId.isValid(validUuid)).toBe(true);
		});

		it("should return false for invalid UUID format", () => {
			expect(WorkspaceId.isValid("not-a-uuid")).toBe(false);
			expect(WorkspaceId.isValid("")).toBe(false);
			expect(WorkspaceId.isValid("550e8400-e29b-11d4-a716-446655440000")).toBe(
				false,
			); // UUID v1
		});

		it("should handle uppercase UUID", () => {
			const uppercaseUuid = "550E8400-E29B-41D4-A716-446655440000";

			expect(WorkspaceId.isValid(uppercaseUuid)).toBe(true);
		});
	});

	describe("toString", () => {
		it("should return the UUID string value", () => {
			const validUuid = "550e8400-e29b-41d4-a716-446655440000";
			const workspaceId = new WorkspaceId(validUuid);

			expect(workspaceId.toString()).toBe(validUuid);
		});
	});

	describe("equals", () => {
		it("should return true for WorkspaceIds with same UUID", () => {
			const uuid = "550e8400-e29b-41d4-a716-446655440000";
			const workspaceId1 = new WorkspaceId(uuid);
			const workspaceId2 = new WorkspaceId(uuid);

			expect(workspaceId1.equals(workspaceId2)).toBe(true);
		});

		it("should return false for WorkspaceIds with different UUIDs", () => {
			const workspaceId1 = new WorkspaceId(
				"550e8400-e29b-41d4-a716-446655440000",
			);
			const workspaceId2 = new WorkspaceId(
				"660e8400-e29b-41d4-a716-446655440001",
			);

			expect(workspaceId1.equals(workspaceId2)).toBe(false);
		});
	});
});
