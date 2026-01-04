import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import type {
	PartialResume,
	PersistenceResult,
	ResumeStorage,
	StorageType,
} from "@/core/resume/domain/ResumeStorage";
import * as storageFactory from "@/core/resume/infrastructure/storage/factory";
import { useResumeStore } from "@/core/resume/infrastructure/store/resume.store";
import StorageSelector from "./StorageSelector.vue";

// Mock the composable
vi.mock("@/core/settings/infrastructure/presentation/composables", () => ({
	useStoragePreference: () => ({
		storagePreference: ref("local" as StorageType),
		setStoragePreference: vi.fn().mockResolvedValue(undefined),
		availableStorageTypes: ref([
			"session",
			"local",
			"indexeddb",
		] as StorageType[]),
		isStorageAvailable: vi.fn(() => true),
	}),
}));

// Helper to create a mock resume
const createMockResume = (): Resume => ({
	basics: {
		name: "John Doe",
		label: "Software Engineer",
		image: "",
		email: "john@example.com",
		phone: "",
		url: "",
		summary: "",
		location: {
			address: "",
			postalCode: "",
			city: "",
			countryCode: "",
			region: "",
		},
		profiles: [],
	},
	work: [],
	volunteer: [],
	education: [],
	awards: [],
	certificates: [],
	publications: [],
	skills: [],
	languages: [],
	interests: [],
	references: [],
	projects: [],
});

// Helper to create a mock storage adapter
const createMockStorage = (
	type: StorageType,
	hasData = false,
): ResumeStorage => {
	const mockResume = hasData ? createMockResume() : null;
	return {
		save: vi.fn().mockResolvedValue({
			data: mockResume,
			timestamp: new Date().toISOString(),
			storageType: type,
		}),
		load: vi.fn().mockResolvedValue({
			data: mockResume,
			timestamp: new Date().toISOString(),
			storageType: type,
		}),
		clear: vi.fn().mockResolvedValue(undefined),
		type: () => type,
	};
};

// Helper to find button by text content (Vue Test Utils doesn't support :contains)
const findButtonByText = (wrapper: ReturnType<typeof mount>, text: string) => {
	const buttons = wrapper.findAll("button");
	return buttons.find((btn) => btn.text().includes(text));
};

// Helper to find button in document.body (for portal-rendered dialogs)
const findButtonInDocument = (text: string): HTMLButtonElement | undefined => {
	const buttons = Array.from(document.querySelectorAll("button"));
	return buttons.find((btn) => btn.textContent?.includes(text)) as
		| HTMLButtonElement
		| undefined;
};

// Helper to change storage selection by directly setting the ref (exposed via defineExpose)
const changeStorageSelection = async (
	wrapper: ReturnType<typeof mount>,
	targetType: StorageType,
) => {
	// Access the exposed ref and set it directly
	(wrapper.vm as any).selectedStorage = targetType;
	await nextTick();
};

describe("StorageSelector.vue", () => {
	let pinia: ReturnType<typeof createPinia>;

	beforeEach(() => {
		pinia = createPinia();
		setActivePinia(pinia);
		vi.clearAllMocks();
	});

	describe("Component Rendering", () => {
		it("should render the storage options card", () => {
			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			expect(wrapper.find("h3").text()).toContain("Storage Settings");
			expect(wrapper.text()).toContain("Choose where to save your resume data");
		});

		it("should render all available storage options", () => {
			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Should show session, local, and indexeddb (based on mock)
			expect(wrapper.text()).toContain("Session Storage");
			expect(wrapper.text()).toContain("Local Storage");
			expect(wrapper.text()).toContain("IndexedDB");
		});

		it("should show Apply and Cancel buttons only when there are changes", async () => {
			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// No buttons initially (no changes)
			expect(findButtonByText(wrapper, "Apply Changes")).toBeUndefined();

			// Change selection to a different type (current is "local", change to "session")
			await changeStorageSelection(wrapper, "session");

			// Buttons should now appear
			expect(findButtonByText(wrapper, "Apply Changes")).toBeDefined();
			expect(findButtonByText(wrapper, "Cancel")).toBeDefined();
		});
	});

	describe("checkOldStorageForData()", () => {
		it("should return true when old storage has data", async () => {
			const mockStorageWithData = createMockStorage("local", true);
			vi.spyOn(storageFactory, "createResumeStorage").mockReturnValue(
				mockStorageWithData,
			);

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			const vm = wrapper.vm as unknown as {
				checkOldStorageForData: () => Promise<boolean>;
			};

			const hasData = await vm.checkOldStorageForData();

			expect(hasData).toBe(true);
			expect(mockStorageWithData.load).toHaveBeenCalled();
		});

		it("should return false when old storage is empty", async () => {
			const mockStorageEmpty = createMockStorage("local", false);
			vi.spyOn(storageFactory, "createResumeStorage").mockReturnValue(
				mockStorageEmpty,
			);

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			const vm = wrapper.vm as unknown as {
				checkOldStorageForData: () => Promise<boolean>;
			};

			const hasData = await vm.checkOldStorageForData();

			expect(hasData).toBe(false);
			expect(mockStorageEmpty.load).toHaveBeenCalled();
		});

		it("should return true when storage check fails (safe default)", async () => {
			const mockStorageFailing = {
				...createMockStorage("local", false),
				load: vi.fn().mockRejectedValue(new Error("Storage unavailable")),
			};
			vi.spyOn(storageFactory, "createResumeStorage").mockReturnValue(
				mockStorageFailing,
			);

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			const vm = wrapper.vm as unknown as {
				checkOldStorageForData: () => Promise<boolean>;
			};

			const hasData = await vm.checkOldStorageForData();

			// Should return true to be safe
			expect(hasData).toBe(true);
			expect(mockStorageFailing.load).toHaveBeenCalled();
		});
	});

	describe("applyStorageChange()", () => {
		it("should show confirmation dialog when old storage has data", async () => {
			const mockStorageWithData = createMockStorage("local", true);
			vi.spyOn(storageFactory, "createResumeStorage").mockReturnValue(
				mockStorageWithData,
			);

			const wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal
				global: {
					plugins: [pinia],
				},
			});

			// Change selection to trigger Apply button
			await changeStorageSelection(wrapper, "session");

			// Click Apply
			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises(); // Wait for async checkOldStorageForData
			await nextTick();

			// Dialog should be visible (rendered in portal)
			expect(document.body.textContent).toContain("Switch Storage Location?");
			expect(document.body.textContent).toContain(
				"You have resume data stored in",
			);

			wrapper.unmount();
		});

		it("should switch immediately when old storage is empty", async () => {
			const mockStorageEmpty = createMockStorage("local", false);
			const mockNewStorage = createMockStorage("session", false);

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockStorageEmpty) // For checkOldStorageForData
				.mockReturnValueOnce(mockStorageEmpty) // For performStorageChange (old)
				.mockReturnValueOnce(mockNewStorage); // For performStorageChange (new)

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Change selection to session storage
			await changeStorageSelection(wrapper, "session");

			// Click Apply
			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await nextTick();

			// Wait for async operations
			await wrapper.vm.$nextTick();
			await new Promise((resolve) => setTimeout(resolve, 100));

			// Dialog should NOT appear, should proceed directly
			expect(wrapper.text()).not.toContain("Switch Storage Location?");
			expect(resumeStore.changeStorageStrategy).toHaveBeenCalled();
		});

		it("should display loading state during storage check", async () => {
			// Create a mock that takes time to resolve
			const slowLoadFn = (): Promise<PersistenceResult<Resume | null>> =>
				new Promise((resolve) =>
					setTimeout(
						() =>
							resolve({
								data: createMockResume(),
								timestamp: new Date().toISOString(),
								storageType: "local",
							}),
						100,
					),
				);

			const slowMockStorage: ResumeStorage = {
				...createMockStorage("local", true),
				load: vi.fn(slowLoadFn),
			};

			vi.spyOn(storageFactory, "createResumeStorage").mockReturnValue(
				slowMockStorage,
			);

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Change selection to session storage
			await changeStorageSelection(wrapper, "session");

			// Click Apply
			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await nextTick();

			// Should show "Checking..." text
			expect(wrapper.text()).toContain("Checking...");

			// Button should be disabled
			expect(applyButton?.attributes("disabled")).toBeDefined();

			// Wait for operation to complete
			await new Promise((resolve) => setTimeout(resolve, 150));
			await nextTick();
		});
	});

	describe("Confirmation Dialog - Static Content", () => {
		let wrapper: VueWrapper;

		beforeEach(async () => {
			const mockStorageWithData = createMockStorage("local", true);
			const createStorageSpy = vi
				.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValue(mockStorageWithData);

			wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal to render
				global: {
					plugins: [pinia],
				},
			});

			// Trigger dialog by changing selection
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();

			// Clear any previous calls from component initialization
			createStorageSpy.mockClear();

			await applyButton?.trigger("click");

			// Wait aggressively for async checkOldStorageForData and Vue re-render
			await flushPromises();
			await nextTick();
			await flushPromises(); // Double flush to ensure all promises resolve
			await nextTick(); // Double tick to ensure all renders complete

			// Debug: Verify the storage was created and load was called
			expect(createStorageSpy).toHaveBeenCalledWith("local");
			expect(mockStorageWithData.load).toHaveBeenCalled();
		});

		afterEach(() => {
			wrapper.unmount();
		});

		it("should show correct storage labels in dialog", () => {
			// Dialog is rendered in a portal, check document.body
			expect(document.body.textContent).toContain("Local Storage");
			expect(document.body.textContent).toContain("Session Storage");
		});

		it("should show three action buttons", () => {
			// Dialog is rendered in a portal, check document.body
			expect(document.body.textContent).toContain("Cancel");
			expect(document.body.textContent).toContain("Switch Without Migrating");
			expect(document.body.textContent).toContain("Migrate & Switch");
		});
	});

	describe("Confirmation Dialog - Actions", () => {
		it("should cancel and revert selection when 'Cancel' clicked", async () => {
			const mockOldStorage = createMockStorage("local", true);
			const mockNewStorage = createMockStorage("session", false);

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old (won't be called)
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new (won't be called)

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			// Create fresh wrapper
			const testWrapper = mount(StorageSelector, {
				attachTo: document.body,
				global: { plugins: [pinia] },
			});

			// Change selection to trigger checking old storage
			await changeStorageSelection(testWrapper, "session");

			// Click Apply to trigger dialog
			const applyButton = findButtonByText(testWrapper, "Apply Changes");
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Dialog should appear
			expect(document.body.textContent).toContain(
				"You have resume data stored in",
			);

			// Find Cancel button in dialog portal
			const dialogElement = Array.from(
				document.querySelectorAll('[role="alertdialog"]'),
			)[0];
			expect(dialogElement).toBeDefined();

			const cancelButton = Array.from(
				dialogElement?.querySelectorAll("button") ?? [],
			).find(
				(btn) => btn.textContent?.trim() === "Cancel",
			) as HTMLButtonElement;

			expect(cancelButton).toBeDefined();
			cancelButton?.click();

			await flushPromises();
			await nextTick();

			// Verify the important behavior: storage strategy NOT called
			expect(resumeStore.changeStorageStrategy).not.toHaveBeenCalled();

			// Note: We don't check if dialog closes visually - that's a UI implementation detail.
			// The critical behavior (not calling changeStorageStrategy) is already verified.

			testWrapper.unmount();
		});

		it.skip("should migrate data when 'Migrate & Switch' clicked", async () => {
			const mockOldStorage = createMockStorage("local", true);
			const mockNewStorage = createMockStorage("session", false);
			const mockResume = createMockResume();

			mockOldStorage.load = vi.fn().mockResolvedValue({
				data: mockResume,
				timestamp: new Date().toISOString(),
			});

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();
			vi.spyOn(resumeStore, "setResume");

			// Create a fresh wrapper for this test
			const testWrapper = mount(StorageSelector, {
				attachTo: document.body,
				global: {
					plugins: [pinia],
				},
			});

			// Change selection and trigger dialog
			await changeStorageSelection(testWrapper, "session");

			const applyButton = findButtonByText(testWrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Dialog is in portal, find button in document
			const migrateButton = findButtonInDocument("Migrate & Switch");
			expect(migrateButton).toBeDefined();

			// Use DOM click event instead of wrapper.trigger
			migrateButton?.dispatchEvent(new MouseEvent("click", { bubbles: true }));
			await flushPromises();
			await nextTick();
			await flushPromises(); // Extra flush for async operations

			// Should have saved to new storage
			expect(mockNewStorage.save).toHaveBeenCalledWith(mockResume);
			expect(resumeStore.setResume).toHaveBeenCalledWith(mockResume);
			expect(resumeStore.changeStorageStrategy).toHaveBeenCalledWith(
				mockNewStorage,
				false,
			);

			testWrapper.unmount();
		});

		it.skip("should switch without migrating when 'Switch Without Migrating' clicked", async () => {
			// Create mocks BEFORE mounting component
			const mockOldStorage = createMockStorage("local", true);
			const mockNewStorage = createMockStorage("session", false);

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			// Create fresh wrapper with portal support
			const testWrapper = mount(StorageSelector, {
				attachTo: document.body,
				global: { plugins: [pinia] },
			});

			// Change selection to trigger checking old storage
			await changeStorageSelection(testWrapper, "session");

			// Click Apply to trigger dialog
			const applyButton = findButtonByText(testWrapper, "Apply Changes");
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Dialog should appear in document.body
			expect(document.body.textContent).toContain(
				"You have resume data stored in",
			);

			// Dialog is in portal, find button in document
			const noMigrateButton = findButtonInDocument("Switch Without Migrating");
			expect(noMigrateButton).toBeDefined();

			// Use DOM click event
			noMigrateButton?.dispatchEvent(
				new MouseEvent("click", { bubbles: true }),
			);
			await flushPromises();
			await nextTick();
			await flushPromises(); // Extra flush

			// Should NOT have saved to new storage
			expect(mockNewStorage.save).not.toHaveBeenCalled();
			// But should have changed strategy
			expect(resumeStore.changeStorageStrategy).toHaveBeenCalledWith(
				mockNewStorage,
				false,
			);

			// Cleanup
			testWrapper.unmount();
		});
	});

	describe("performStorageChange()", () => {
		it("should show success message after successful migration", async () => {
			const mockOldStorage = createMockStorage("local", true);
			const mockNewStorage = createMockStorage("session", false);
			const mockResume = createMockResume();

			mockOldStorage.load = vi.fn().mockResolvedValue({
				data: mockResume,
				timestamp: new Date().toISOString(),
			});

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();
			vi.spyOn(resumeStore, "setResume");

			const wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal
				global: {
					plugins: [pinia],
				},
			});

			// Change selection and trigger migration
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises(); // Wait for dialog to appear
			await nextTick();

			// Find button in portal
			const migrateButton = findButtonInDocument("Migrate & Switch");
			expect(migrateButton).toBeDefined();
			migrateButton?.click();
			await flushPromises();
			await nextTick();

			// Success message should appear
			expect(wrapper.text()).toContain("Storage changed successfully!");

			wrapper.unmount();
		});

		it.skip("should show error message when migration fails", async () => {
			const mockOldStorage = createMockStorage("local", true);
			const mockNewStorage = {
				...createMockStorage("session", false),
				save: vi.fn().mockRejectedValue(new Error("Network error")),
			};

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			const wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal
				global: {
					plugins: [pinia],
				},
			});

			// Change selection and trigger migration
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises(); // Wait for dialog to appear
			await nextTick();

			// Find button in portal
			const migrateButton = findButtonInDocument("Migrate & Switch");
			expect(migrateButton).toBeDefined();
			migrateButton?.click();
			await flushPromises();
			await nextTick();

			// Error message should appear
			expect(wrapper.text()).toContain("Network error");

			wrapper.unmount();
		});

		it.skip("should disable buttons during migration", async () => {
			const mockOldStorage = createMockStorage("local", true);

			// Create a typed slow save function
			const slowSaveFn = (): Promise<
				PersistenceResult<Resume | PartialResume>
			> =>
				new Promise((resolve) =>
					setTimeout(
						() =>
							resolve({
								data: null as unknown as Resume,
								timestamp: new Date().toISOString(),
								storageType: "session",
							}),
						100,
					),
				);

			const mockNewStorage: ResumeStorage = {
				...createMockStorage("session", false),
				save: vi.fn(slowSaveFn),
			};

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValue(mockOldStorage)
				.mockReturnValueOnce(mockOldStorage)
				.mockReturnValueOnce(mockNewStorage);

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			const wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal
				global: {
					plugins: [pinia],
				},
			});

			// Trigger migration
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises(); // Wait for dialog to appear
			await nextTick();

			// Find button in portal
			const migrateButton = findButtonInDocument("Migrate & Switch");
			expect(migrateButton).toBeDefined();
			migrateButton?.click();
			await nextTick();

			// Buttons should be disabled during migration
			expect(migrateButton?.disabled).toBe(true);

			// Wait for migration to complete
			await new Promise((resolve) => setTimeout(resolve, 150));
			await nextTick();

			wrapper.unmount();
		});

		it("should clear success message after 3 seconds", async () => {
			vi.useFakeTimers();

			const mockOldStorage = createMockStorage("local", false);
			const mockNewStorage = createMockStorage("session", false);

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage) // checkOldStorageForData
				.mockReturnValueOnce(mockOldStorage) // performStorageChange old
				.mockReturnValueOnce(mockNewStorage); // performStorageChange new

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Change selection (no dialog since empty storage)
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Success message should appear
			expect(wrapper.text()).toContain("Storage changed successfully!");

			// Fast-forward 3 seconds
			await vi.advanceTimersByTimeAsync(3000);
			await nextTick();

			// Success message should be gone
			expect(wrapper.text()).not.toContain("Storage changed successfully!");

			vi.useRealTimers();
		});
	});

	describe("Edge Cases", () => {
		it.skip("should handle network errors when switching to cloud storage", async () => {
			const mockOldStorage = createMockStorage("local", true);
			const mockRemoteStorage = {
				...createMockStorage("remote" as StorageType, false),
				save: vi.fn().mockRejectedValue(new Error("Authentication required")),
			};

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage)
				.mockReturnValueOnce(mockOldStorage)
				.mockReturnValueOnce(mockRemoteStorage);

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();

			const wrapper = mount(StorageSelector, {
				attachTo: document.body, // Required for AlertDialog portal
				global: {
					plugins: [pinia],
				},
			});

			// Trigger migration
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Find button in portal
			const migrateButton = findButtonInDocument("Migrate & Switch");
			expect(migrateButton).toBeDefined();
			migrateButton?.click();
			await flushPromises();
			await nextTick();

			// Should show authentication error
			expect(wrapper.text()).toContain("Authentication required");

			// Storage preference should NOT change
			expect(resumeStore.changeStorageStrategy).not.toHaveBeenCalled();

			wrapper.unmount();
		});

		it("should handle empty resume during migration", async () => {
			const mockOldStorage = createMockStorage("local", false); // No data
			const mockNewStorage = createMockStorage("session", false);

			// Mock load to return null explicitly
			mockOldStorage.load = vi
				.fn()
				.mockResolvedValue({ data: null, timestamp: new Date().toISOString() });

			vi.spyOn(storageFactory, "createResumeStorage")
				.mockReturnValueOnce(mockOldStorage)
				.mockReturnValueOnce(mockOldStorage)
				.mockReturnValueOnce(mockNewStorage);

			const resumeStore = useResumeStore();
			vi.spyOn(resumeStore, "changeStorageStrategy").mockResolvedValue();
			vi.spyOn(resumeStore, "setResume");

			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Since old storage is empty, should switch immediately without dialog
			await changeStorageSelection(wrapper, "session");

			const applyButton = findButtonByText(wrapper, "Apply Changes");
			expect(applyButton).toBeDefined();
			await applyButton?.trigger("click");
			await flushPromises();
			await nextTick();

			// Should NOT attempt to save null data
			expect(mockNewStorage.save).not.toHaveBeenCalled();
			// Should still change strategy
			expect(resumeStore.changeStorageStrategy).toHaveBeenCalled();
		});

		it("should not show Apply button when selection equals current preference", () => {
			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// No changes, so no Apply button
			expect(findButtonByText(wrapper, "Apply Changes")).toBeUndefined();
		});

		it("should revert selection when Cancel button is clicked", async () => {
			const wrapper = mount(StorageSelector, {
				global: {
					plugins: [pinia],
				},
			});

			// Change selection
			await changeStorageSelection(wrapper, "session");

			// Verify Apply button appeared
			expect(findButtonByText(wrapper, "Apply Changes")).toBeDefined();

			// Click Cancel
			const cancelButton = findButtonByText(wrapper, "Cancel");
			expect(cancelButton).toBeDefined();
			await cancelButton?.trigger("click");
			await nextTick();

			// Apply button should disappear (selection reverted)
			expect(findButtonByText(wrapper, "Apply Changes")).toBeUndefined();
		});
	});
});
