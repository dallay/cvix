import { afterEach, describe, expect, it } from "vitest";
import {
	clearCurrentWorkspaceId,
	getCurrentWorkspaceId,
	setCurrentWorkspaceId,
} from "./WorkspaceContext";

describe("WorkspaceContext", () => {
	afterEach(() => {
		// Reset state after each test
		clearCurrentWorkspaceId();
	});

	describe("setCurrentWorkspaceId", () => {
		it("should set the workspace ID", () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			setCurrentWorkspaceId(workspaceId);

			expect(getCurrentWorkspaceId()).toBe(workspaceId);
		});

		it("should allow setting null to clear the workspace ID", () => {
			setCurrentWorkspaceId("550e8400-e29b-41d4-a716-446655440000");

			setCurrentWorkspaceId(null);

			expect(getCurrentWorkspaceId()).toBeNull();
		});

		it("should overwrite previous workspace ID", () => {
			const firstId = "550e8400-e29b-41d4-a716-446655440000";
			const secondId = "660e8400-e29b-41d4-a716-446655440001";

			setCurrentWorkspaceId(firstId);
			expect(getCurrentWorkspaceId()).toBe(firstId);

			setCurrentWorkspaceId(secondId);
			expect(getCurrentWorkspaceId()).toBe(secondId);
		});
	});

	describe("getCurrentWorkspaceId", () => {
		it("should return null when no workspace is set", () => {
			expect(getCurrentWorkspaceId()).toBeNull();
		});

		it("should return the current workspace ID when set", () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			setCurrentWorkspaceId(workspaceId);

			expect(getCurrentWorkspaceId()).toBe(workspaceId);
		});
	});

	describe("clearCurrentWorkspaceId", () => {
		it("should clear the workspace ID", () => {
			setCurrentWorkspaceId("550e8400-e29b-41d4-a716-446655440000");
			expect(getCurrentWorkspaceId()).not.toBeNull();

			clearCurrentWorkspaceId();

			expect(getCurrentWorkspaceId()).toBeNull();
		});

		it("should be safe to call when already null", () => {
			expect(getCurrentWorkspaceId()).toBeNull();

			clearCurrentWorkspaceId();

			expect(getCurrentWorkspaceId()).toBeNull();
		});
	});
});
