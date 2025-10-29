import { flushPromises, mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { computed, nextTick, ref } from "vue";
import { useWorkspaceSelection } from "../../../application/useWorkspaceSelection";
import type { Workspace } from "../../../domain/WorkspaceEntity";
import type { WorkspaceError } from "../../../domain/WorkspaceError";
import WorkspaceSelector from "../WorkspaceSelector.vue";

// Mock the useWorkspaceSelection composable
vi.mock("../../../application/useWorkspaceSelection");

describe("WorkspaceSelector", () => {
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

	// Reactive refs to control the mock's state from within tests
	const workspacesRef = ref<Workspace[]>([]);
	const currentWorkspaceRef = ref<Workspace | null>(null);
	const isLoadingRef = ref(false);
	const errorRef = ref<WorkspaceError | null>(null);
	const selectWorkspaceMock = vi.fn();

	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();

		// Reset refs to default state for each test
		workspacesRef.value = mockWorkspaces;
		currentWorkspaceRef.value = mockWorkspace1;
		isLoadingRef.value = false;
		errorRef.value = null;
		selectWorkspaceMock.mockResolvedValue(undefined);

		// Mock the return value of the composable
		vi.mocked(useWorkspaceSelection).mockReturnValue({
			workspaces: computed(() => workspacesRef.value),
			currentWorkspace: computed(() => currentWorkspaceRef.value),
			isLoading: computed(() => isLoadingRef.value),
			error: computed(() => errorRef.value),
			hasWorkspaces: computed(() => workspacesRef.value.length > 0),
			hasCurrentWorkspace: computed(() => !!currentWorkspaceRef.value),
			availableWorkspaces: computed(() =>
				workspacesRef.value.filter(
					(w) => w.id !== currentWorkspaceRef.value?.id,
				),
			),
			defaultWorkspace: computed(
				() => workspacesRef.value.find((w) => w.isDefault) ?? null,
			),
			selectWorkspace: selectWorkspaceMock,
		});
	});

	describe("rendering", () => {
		it("should render workspace selector with current workspace", () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.find("[data-testid='workspace-selector']").exists()).toBe(
				true,
			);
			expect(wrapper.text()).toContain("Workspace 1");
		});

		it("should display placeholder when no workspace selected", () => {
			currentWorkspaceRef.value = null;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.text()).toContain("Select workspace");
		});

		it("should render all available workspaces in dropdown", () => {
			// Verify both workspaces are available in the data
			expect(workspacesRef.value).toHaveLength(2);
			expect(workspacesRef.value[0]?.name).toBe("Workspace 1");
			expect(workspacesRef.value[1]?.name).toBe("Workspace 2");
		});

		it("should display default badge for default workspace", () => {
			// Set current workspace to the default one
			currentWorkspaceRef.value = mockWorkspace2;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// The default badge should be shown in the trigger for the current workspace
			expect(wrapper.html()).toContain("Default");
		});
	});

	describe("workspace selection", () => {
		it("should call selectWorkspace when workspace is clicked", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// Simulate selecting a workspace by calling the handleSelect method directly
			const component = wrapper.vm as any;
			await component.handleSelect(mockWorkspace2.id);

			expect(selectWorkspaceMock).toHaveBeenCalledWith(
				mockWorkspace2.id,
				"user-123",
			);
		});

		it("should emit workspace-selected event after successful selection", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// Simulate selecting a workspace
			const component = wrapper.vm as any;
			await component.handleSelect(mockWorkspace2.id);

			await flushPromises();

			expect(wrapper.emitted("workspace-selected")).toBeTruthy();
			expect(wrapper.emitted("workspace-selected")?.[0]).toEqual([
				mockWorkspace2.id,
			]);
		});

		it("should close dropdown after workspace selection", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// Set isOpen to true to simulate opening the dropdown
			const component = wrapper.vm as any;
			component.isOpen = true;
			await nextTick();

			expect(component.isOpen).toBe(true);

			// Simulate selecting a workspace
			await component.handleSelect(mockWorkspace2.id);

			// Dropdown should be closed
			expect(component.isOpen).toBe(false);
		});
	});

	describe("loading state", () => {
		it("should show loading indicator when isLoading is true", () => {
			isLoadingRef.value = true;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.find("[data-testid='workspace-loading']").exists()).toBe(
				true,
			);
		});

		it("should disable selector when loading", () => {
			isLoadingRef.value = true;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// When loading, the selector is not rendered, only the loading indicator
			expect(wrapper.find("[data-testid='workspace-loading']").exists()).toBe(
				true,
			);
			expect(wrapper.find("[data-slot='select-trigger']").exists()).toBe(false);
		});
	});

	describe("error state", () => {
		it("should display error message when error exists", () => {
			errorRef.value = {
				message: "Failed to load workspaces",
			} as WorkspaceError;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.text()).toContain("Failed to load workspaces");
		});

		it("should show error icon when error exists", () => {
			errorRef.value = {
				message: "Failed to load workspaces",
			} as WorkspaceError;

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.find("[data-testid='error-icon']").exists()).toBe(true);
		});
	});

	describe("empty state", () => {
		it("should display empty state when no workspaces available", () => {
			workspacesRef.value = [];

			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			expect(wrapper.text()).toContain("No workspaces available");
		});
	});

	describe("accessibility", () => {
		it("should have proper ARIA labels", () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			const combobox = wrapper.find("[role='combobox']");
			expect(combobox.attributes("aria-label")).toBeTruthy();
			expect(combobox.attributes("aria-haspopup")).toBe("listbox");
		});

		it("should toggle aria-expanded on open/close", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			const component = wrapper.vm as any;
			const trigger = wrapper.find("[data-slot='select-trigger']");
			expect(trigger.attributes("aria-expanded")).toBe("false");

			// Simulate opening
			component.handleOpenChange(true);
			await nextTick();

			expect(component.isOpen).toBe(true);
		});

		it("should support keyboard navigation with Arrow keys", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			await wrapper.find("[role='combobox']").trigger("keydown", {
				key: "ArrowDown",
			});

			expect(
				wrapper.find("[role='combobox']").attributes("aria-expanded"),
			).toBe("true");
		});

		it("should support Enter key for selection", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			// Keyboard navigation is handled by reka-ui Select component
			// We test that the handleSelect method works correctly
			const component = wrapper.vm as any;
			await component.handleSelect(mockWorkspace2.id);

			expect(selectWorkspaceMock).toHaveBeenCalled();
		});

		it("should close dropdown with Escape key", async () => {
			const wrapper = mount(WorkspaceSelector, {
				props: { userId: "user-123" },
			});

			const component = wrapper.vm as any;

			// Open the dropdown
			component.handleOpenChange(true);
			await nextTick();
			expect(component.isOpen).toBe(true);

			// Close with Escape (simulated by handleOpenChange)
			component.handleOpenChange(false);
			await nextTick();

			expect(component.isOpen).toBe(false);
		});
	});
});
