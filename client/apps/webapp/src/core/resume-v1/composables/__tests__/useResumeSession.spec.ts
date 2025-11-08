import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { defineComponent, ref } from "vue";
import type { Resume } from "../../types/resume.ts";
import { useResumeSession } from "../useResumeSession.ts";

// Mock sessionStorage
const mockSessionStorage = (() => {
	let store: Record<string, string> = {};

	return {
		getItem: (key: string) => store[key] || null,
		setItem: (key: string, value: string) => {
			store[key] = value;
		},
		removeItem: (key: string) => {
			delete store[key];
		},
		clear: () => {
			store = {};
		},
	};
})();

Object.defineProperty(window, "sessionStorage", {
	value: mockSessionStorage,
});

describe("useResumeSession", () => {
	beforeEach(() => {
		mockSessionStorage.clear();
		vi.clearAllTimers();
	});

	it("should save resume data to sessionStorage", async () => {
		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "John Doe",
						email: "john@example.com",
					},
					work: [],
					education: [],
					skills: [],
				});

				const { saveToSession } = useResumeSession(resume);

				return { resume, saveToSession };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		// Manually trigger save
		component.saveToSession();

		const saved = mockSessionStorage.getItem("resume_form_data");
		expect(saved).toBeTruthy();

		if (saved) {
			const parsed = JSON.parse(saved);
			expect(parsed.basics.name).toBe("John Doe");
			expect(parsed.basics.email).toBe("john@example.com");
		}
	});

	it("should load resume data from sessionStorage on mount", async () => {
		const savedData: Resume = {
			basics: {
				name: "Jane Smith",
				email: "jane@example.com",
			},
			work: [
				{
					company: "Test Corp",
					position: "Developer",
					startDate: "2020-01-01",
				},
			],
			education: [],
			skills: [],
		};

		mockSessionStorage.setItem("resume_form_data", JSON.stringify(savedData));

		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "",
						email: "",
					},
					work: [],
					education: [],
					skills: [],
				});

				useResumeSession(resume);

				return { resume };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		// Wait for onMounted to complete
		await wrapper.vm.$nextTick();

		expect(component.resume.basics.name).toBe("Jane Smith");
		expect(component.resume.basics.email).toBe("jane@example.com");
		expect(component.resume.work?.[0]?.company).toBe("Test Corp");
	});

	it("should auto-save on resume data changes with debounce", async () => {
		vi.useFakeTimers();

		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "",
						email: "",
					},
					work: [],
					education: [],
					skills: [],
				});

				useResumeSession(resume);

				return { resume };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		// Change resume data
		component.resume.basics.name = "Test User";

		// Wait for Vue to process the change
		await wrapper.vm.$nextTick();

		// Should not save immediately
		expect(mockSessionStorage.getItem("resume_form_data")).toBeNull();

		// Fast-forward debounce time
		vi.advanceTimersByTime(300);

		// Now it should be saved
		const saved = mockSessionStorage.getItem("resume_form_data");
		expect(saved).toBeTruthy();

		if (saved) {
			const parsed = JSON.parse(saved);
			expect(parsed.basics.name).toBe("Test User");
		}

		vi.useRealTimers();
	});

	it("should clear session data", () => {
		const savedData: Resume = {
			basics: {
				name: "Test",
				email: "test@example.com",
			},
			work: [],
			education: [],
			skills: [],
		};

		mockSessionStorage.setItem("resume_form_data", JSON.stringify(savedData));

		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "",
						email: "",
					},
					work: [],
					education: [],
					skills: [],
				});

				const { clearSession } = useResumeSession(resume);

				return { clearSession };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		// Clear the session
		component.clearSession();

		expect(mockSessionStorage.getItem("resume_form_data")).toBeNull();
	});

	it("should handle invalid session data gracefully", async () => {
		// Set invalid JSON
		mockSessionStorage.setItem("resume_form_data", "invalid json");

		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "Default",
						email: "default@example.com",
					},
					work: [],
					education: [],
					skills: [],
				});

				useResumeSession(resume);

				return { resume };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		await wrapper.vm.$nextTick();

		// Should keep default values when session data is invalid
		expect(component.resume.basics.name).toBe("Default");
		expect(component.resume.basics.email).toBe("default@example.com");
	});

	it("should handle missing session data", async () => {
		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "Default",
						email: "default@example.com",
					},
					work: [],
					education: [],
					skills: [],
				});

				useResumeSession(resume);

				return { resume };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		await wrapper.vm.$nextTick();

		// Should keep default values when no session data exists
		expect(component.resume.basics.name).toBe("Default");
		expect(component.resume.basics.email).toBe("default@example.com");
	});

	it("should map skills with 'name' to 'category' for compatibility", async () => {
		// Old format with 'name' instead of 'category'
		const savedData = {
			basics: {
				name: "Test",
				email: "test@example.com",
			},
			work: [],
			education: [],
			skills: [
				{
					name: "Programming",
					keywords: ["JavaScript", "TypeScript"],
				},
			],
		};

		mockSessionStorage.setItem("resume_form_data", JSON.stringify(savedData));

		const TestComponent = defineComponent({
			setup() {
				const resume = ref<Resume>({
					basics: {
						name: "",
						email: "",
					},
					work: [],
					education: [],
					skills: [],
				});

				useResumeSession(resume);

				return { resume };
			},
			template: "<div></div>",
		});

		const wrapper = mount(TestComponent);
		const component = wrapper.vm;

		await wrapper.vm.$nextTick();

		// Should map 'name' to 'category'
		expect(component.resume.skills?.[0]?.category).toBe("Programming");
		expect(component.resume.skills?.[0]?.keywords).toEqual([
			"JavaScript",
			"TypeScript",
		]);
	});
});
