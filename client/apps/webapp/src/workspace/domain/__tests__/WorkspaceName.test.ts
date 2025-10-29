import { describe, expect, it } from "vitest";
import { WorkspaceName } from "../WorkspaceName";

describe("WorkspaceName", () => {
	describe("constructor", () => {
		it("should create WorkspaceName with valid name", () => {
			const name = "My Workspace";
			const workspaceName = new WorkspaceName(name);

			expect(workspaceName.toString()).toBe(name);
		});

		it("should trim whitespace from name", () => {
			const nameWithWhitespace = "  My Workspace  ";
			const workspaceName = new WorkspaceName(nameWithWhitespace);

			expect(workspaceName.toString()).toBe("My Workspace");
		});

		it("should accept name with exactly 1 character", () => {
			const name = "A";
			const workspaceName = new WorkspaceName(name);

			expect(workspaceName.toString()).toBe(name);
		});

		it("should accept name with exactly 100 characters", () => {
			const name = "A".repeat(100);
			const workspaceName = new WorkspaceName(name);

			expect(workspaceName.toString()).toBe(name);
		});

		it("should throw error for empty string", () => {
			expect(() => new WorkspaceName("")).toThrow(
				"Workspace name must be 1-100 characters",
			);
		});

		it("should throw error for whitespace-only string", () => {
			expect(() => new WorkspaceName("   ")).toThrow(
				"Workspace name must be 1-100 characters",
			);
		});

		it("should throw error for name exceeding 100 characters", () => {
			const tooLongName = "A".repeat(101);

			expect(() => new WorkspaceName(tooLongName)).toThrow(
				"Workspace name must be 1-100 characters",
			);
		});

		it("should handle name with special characters", () => {
			const nameWithSpecialChars = "Project #1 - [Draft] & Test!";
			const workspaceName = new WorkspaceName(nameWithSpecialChars);

			expect(workspaceName.toString()).toBe(nameWithSpecialChars);
		});

		it("should handle name with unicode characters", () => {
			const nameWithUnicode = "Proyecto Español 🚀";
			const workspaceName = new WorkspaceName(nameWithUnicode);

			expect(workspaceName.toString()).toBe(nameWithUnicode);
		});
	});

	describe("toString", () => {
		it("should return the workspace name string value", () => {
			const name = "Test Workspace";
			const workspaceName = new WorkspaceName(name);

			expect(workspaceName.toString()).toBe(name);
		});
	});
});
