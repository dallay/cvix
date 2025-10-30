import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Workspace } from "../../../domain/WorkspaceEntity";
import WorkspaceSelectorItem from "../WorkspaceSelectorItem.vue";

describe("WorkspaceSelectorItem", () => {
	const mockWorkspace: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Test Workspace",
		description: "A test workspace",
		isDefault: false,
		ownerId: "user-123",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	const defaultWorkspace: Workspace = {
		...mockWorkspace,
		id: "660e8400-e29b-41d4-a716-446655440001",
		name: "Default Workspace",
		isDefault: true,
	};

	describe("rendering", () => {
		it("should render workspace name", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			expect(wrapper.text()).toContain("Test Workspace");
		});

		it("should render workspace description", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			expect(wrapper.text()).toContain("A test workspace");
		});

		it("should render without description", () => {
			const workspaceWithoutDesc = { ...mockWorkspace, description: null };
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: workspaceWithoutDesc },
			});

			expect(wrapper.find("[data-testid='workspace-name']").exists()).toBe(
				true,
			);
			expect(
				wrapper.find("[data-testid='workspace-description']").exists(),
			).toBe(false);
		});
	});

	describe("default badge", () => {
		it("should display default badge when workspace is default", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: defaultWorkspace },
			});

			expect(wrapper.find("[data-testid='default-badge']").exists()).toBe(true);
			expect(wrapper.text()).toContain("Default");
		});

		it("should not display default badge for non-default workspace", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			expect(wrapper.find("[data-testid='default-badge']").exists()).toBe(
				false,
			);
		});
	});

	describe("selected state", () => {
		it("should apply selected styling when isSelected is true", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace, isSelected: true },
			});

			// Check for selected background styling
			expect(wrapper.classes()).toContain("bg-accent/30");
		});

		it("should not apply selected styling when isSelected is false", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace, isSelected: false },
			});

			expect(wrapper.classes()).not.toContain("bg-accent/30");
		});

		it("should show check icon when selected", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace, isSelected: true },
			});

			expect(wrapper.find("[data-testid='check-icon']").exists()).toBe(true);
		});
	});

	describe("interaction", () => {
		it("should emit click event when clicked", async () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			await wrapper.trigger("click");

			expect(wrapper.emitted("click")).toBeTruthy();
		});

		it("should have proper role for accessibility", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			expect(wrapper.attributes("role")).toBe("option");
		});

		it("should have aria-selected attribute when selected", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace, isSelected: true },
			});

			expect(wrapper.attributes("aria-selected")).toBe("true");
		});

		it("should have aria-selected false when not selected", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace, isSelected: false },
			});

			expect(wrapper.attributes("aria-selected")).toBe("false");
		});
	});

	describe("styling", () => {
		it("should apply hover styling", async () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			await wrapper.trigger("mouseenter");

			// Check for hover styling class
			expect(wrapper.classes()).toContain("hover:bg-accent/50");
		});

		it("should have proper visual hierarchy for name and description", () => {
			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace: mockWorkspace },
			});

			const name = wrapper.find("[data-testid='workspace-name']");
			const description = wrapper.find("[data-testid='workspace-description']");

			expect(name.exists()).toBe(true);
			expect(description.exists()).toBe(true);

			// Name should have larger/bolder styling than description
			const nameClasses = name.classes().join(" ");
			const descClasses = description.classes().join(" ");

			expect(nameClasses).not.toBe(descClasses);
		});
	});

	describe("edge cases", () => {
		it("should handle very long workspace names", () => {
			const longName = "A".repeat(100);
			const workspace = { ...mockWorkspace, name: longName };

			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace },
			});

			expect(wrapper.text()).toContain(longName);
		});

		it("should handle very long descriptions", () => {
			const longDesc = "B".repeat(200);
			const workspace = { ...mockWorkspace, description: longDesc };

			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace },
			});

			expect(wrapper.text()).toContain(longDesc);
		});

		it("should handle empty description string", () => {
			const workspace = { ...mockWorkspace, description: "" };

			const wrapper = mount(WorkspaceSelectorItem, {
				props: { workspace },
			});

			expect(
				wrapper.find("[data-testid='workspace-description']").exists(),
			).toBe(false);
		});
	});
});
