import { mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick } from "vue";
import ResumeForm from "../components/ResumeForm.vue";
import { useResumeStore } from "../stores/resumeStore";

// Mock the i18n plugin
vi.mock("vue-i18n", () => ({
	useI18n: () => ({
		t: (key: string) => key,
		locale: { value: "en" },
	}),
}));

describe("ResumeForm", () => {
	let pinia: ReturnType<typeof createPinia>;

	beforeEach(() => {
		pinia = createPinia();
		vi.clearAllMocks();
	});

	it("should render the form with all sections", () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		expect(wrapper.find('[data-testid="personal-info-section"]').exists()).toBe(
			true,
		);
		expect(
			wrapper.find('[data-testid="work-experience-section"]').exists(),
		).toBe(true);
		expect(wrapper.find('[data-testid="education-section"]').exists()).toBe(
			true,
		);
		expect(wrapper.find('[data-testid="skills-section"]').exists()).toBe(true);
	});

	it("should show validation errors when submitting empty form", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Find and click submit button
		const submitButton = wrapper.find('[data-testid="submit-button"]');
		await submitButton.trigger("click");
		await nextTick();

		// Should show validation errors for required fields
		expect(wrapper.text()).toContain("resume.validation.required");
	});

	it("should validate email format", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Fill in invalid email
		const emailInput = wrapper.find('[data-testid="email-input"]');
		await emailInput.setValue("invalid-email");
		await emailInput.trigger("blur");
		await nextTick();

		// Should show email validation error
		expect(wrapper.text()).toContain("resume.validation.email");
	});

	it("should validate field length limits", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Fill in name exceeding 100 characters
		const nameInput = wrapper.find('[data-testid="fullname-input"]');
		const longName = "A".repeat(101);
		await nameInput.setValue(longName);
		await nameInput.trigger("blur");
		await nextTick();

		// Should show max length validation error
		expect(wrapper.text()).toContain("resume.validation.max_length");
	});

	it("should add and remove work experience entries", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Initially should have one work experience entry
		expect(
			wrapper.findAll('[data-testid="work-experience-entry"]'),
		).toHaveLength(1);

		// Click add work experience button
		const addButton = wrapper.find('[data-testid="add-work-experience"]');
		await addButton.trigger("click");
		await nextTick();

		// Should now have two entries
		expect(
			wrapper.findAll('[data-testid="work-experience-entry"]'),
		).toHaveLength(2);

		// Click remove button on first entry
		const removeButtons = wrapper.findAll(
			'[data-testid="remove-work-experience"]',
		);
		if (removeButtons.length > 0) {
			await removeButtons[0]?.trigger("click");
			await nextTick();
		}

		// Should be back to one entry
		expect(
			wrapper.findAll('[data-testid="work-experience-entry"]'),
		).toHaveLength(1);
	});

	it("should add and remove education entries", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Click add education button
		const addButton = wrapper.find('[data-testid="add-education"]');
		await addButton.trigger("click");
		await nextTick();

		// Should have one education entry
		expect(wrapper.findAll('[data-testid="education-entry"]')).toHaveLength(1);

		// Click remove button
		const removeButton = wrapper.find('[data-testid="remove-education"]');
		await removeButton.trigger("click");
		await nextTick();

		// Should have no entries
		expect(wrapper.findAll('[data-testid="education-entry"]')).toHaveLength(0);
	});

	it("should add and remove skill categories", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Click add skill category button
		const addButton = wrapper.find('[data-testid="add-skill-category"]');
		await addButton.trigger("click");
		await nextTick();

		// Should have one skill category
		expect(
			wrapper.findAll('[data-testid="skill-category-entry"]'),
		).toHaveLength(1);
	});

	it("should validate that resume has at least one of work/education/skills", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Fill only personal info
		await wrapper.find('[data-testid="fullname-input"]').setValue("John Doe");
		await wrapper
			.find('[data-testid="email-input"]')
			.setValue("john@example.com");
		await wrapper.find('[data-testid="phone-input"]').setValue("+1234567890");

		// Try to submit without work/education/skills
		await wrapper.find('[data-testid="submit-button"]').trigger("click");
		await nextTick();

		// Should show content requirement validation error
		expect(wrapper.text()).toContain("resume.validation.min_content");
	});

	it.skip("should call generateResume when form is valid and submitted", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// TODO: Update this test to use useResumeGeneration composable
		// const store = useResumeStore();
		// const generateSpy = vi.spyOn(store, "generateResume");

		// Fill in valid data
		await wrapper.find('[data-testid="fullname-input"]').setValue("John Doe");
		await wrapper
			.find('[data-testid="email-input"]')
			.setValue("john@example.com");
		await wrapper.find('[data-testid="phone-input"]').setValue("+1234567890");

		// Add work experience
		await wrapper.find('[data-testid="add-work-experience"]').trigger("click");
		await nextTick();
		await wrapper.find('[data-testid="company-input"]').setValue("Tech Corp");
		await wrapper.find('[data-testid="position-input"]').setValue("Developer");
		await wrapper
			.find('[data-testid="start-date-input"]')
			.setValue("2020-01-01");

		// Submit form
		await wrapper.find('[data-testid="submit-button"]').trigger("click");
		await nextTick();

		// Should call generateResume
		// expect(generateSpy).toHaveBeenCalled();
	});

	it("should disable submit button while generating", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		const store = useResumeStore();
		store.isGenerating = true;

		await nextTick();

		const submitButton = wrapper.find('[data-testid="submit-button"]');
		expect(submitButton.attributes("disabled")).toBeDefined();
	});

	it("should show loading state while generating", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		const store = useResumeStore();
		store.isGenerating = true;

		await nextTick();

		expect(wrapper.text()).toContain("resume.loading.generating");
	});

	it.skip("should show error message when generation fails", async () => {
		const _wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// TODO: Update this test to use useResumeGeneration composable
		// const store = useResumeStore();
		// store.error = {
		// 	code: "pdf_generation_failed",
		// 	message: "Failed to generate PDF",
		// };

		await nextTick();

		// expect(wrapper.text()).toContain("pdf_generation_failed");
	});

	it.skip("should show success message when generation succeeds", async () => {
		const _wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// TODO: Update this test to use useResumeGeneration composable
		// const store = useResumeStore();
		// store.isGenerating = false;
		// store.error = null;
		// Simulate successful generation by setting a success flag
		// store.$patch({ generatedSuccessfully: true });

		await nextTick();

		// expect(wrapper.text()).toContain("resume.success");
	});

	it("should validate date ranges for work experience", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Add work experience
		await wrapper.find('[data-testid="add-work-experience"]').trigger("click");
		await nextTick();

		// Set end date before start date
		await wrapper
			.find('[data-testid="start-date-input"]')
			.setValue("2020-01-01");
		await wrapper.find('[data-testid="end-date-input"]').setValue("2019-01-01");
		await wrapper.find('[data-testid="end-date-input"]').trigger("blur");
		await nextTick();

		// Should show date range validation error
		expect(wrapper.text()).toContain("resume.validation.date_range");
	});

	it("should validate date ranges for education", async () => {
		const wrapper = mount(ResumeForm, {
			global: {
				plugins: [pinia],
			},
		});

		// Add education
		await wrapper.find('[data-testid="add-education"]').trigger("click");
		await nextTick();

		// Set end date before start date
		await wrapper
			.find('[data-testid="edu-start-date-input"]')
			.setValue("2020-01-01");
		await wrapper
			.find('[data-testid="edu-end-date-input"]')
			.setValue("2019-01-01");
		await wrapper.find('[data-testid="edu-end-date-input"]').trigger("blur");
		await nextTick();

		// Should show date range validation error
		expect(wrapper.text()).toContain("resume.validation.date_range");
	});
});
