import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Work } from "@/core/resume/domain/Resume";
import WorkExperienceSection from "./WorkExperienceSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						workExperience: "Add your work experience",
					},
					labels: {
						work: "Work #{number}",
						highlight: "Highlight #{number}",
					},
				},
				buttons: {
					addWorkExperience: "Add Work Experience",
					addHighlight: "Add Highlight",
				},
				fields: {
					company: "Company",
					position: "Position",
					url: "Company Website",
					startDate: "Start Date",
					endDate: "End Date",
					summary: "Summary",
					highlights: "Highlights",
				},
				placeholders: {
					company: "Company Name",
					position: "Job Title",
					url: "https://company.com",
					summary: "Brief description...",
					highlight: "Achievement or responsibility",
				},
			},
		},
	},
});

describe("WorkExperienceSection.vue", () => {
	const mountComponent = (workExperiences: Work[] = []) => {
		return mount(WorkExperienceSection, {
			props: {
				modelValue: workExperiences,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render when no work entries exist", () => {
			const wrapper = mountComponent();
			expect(wrapper.find('[data-testid="work-name-0"]').exists()).toBe(false);
		});

		it("should render work entry when provided", () => {
			const work: Work[] = [
				{
					name: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			expect(wrapper.find('[data-testid="work-name-0"]').exists()).toBe(true);
		});

		it("should render multiple work entries", () => {
			const work: Work[] = [
				{
					name: "Google",
					position: "Software Engineer",
					url: "https://google.com",
					startDate: "2020-01-01",
					endDate: "2022-01-01",
					summary: "Worked on search",
					highlights: [],
				},
				{
					name: "Meta",
					position: "Senior Engineer",
					url: "https://meta.com",
					startDate: "2022-02-01",
					endDate: "",
					summary: "Working on React",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			expect(wrapper.find('[data-testid="work-name-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="work-name-1"]').exists()).toBe(true);
		});
	});

	describe("v-model binding", () => {
		it("should bind company name field", async () => {
			const work: Work[] = [
				{
					name: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			const nameInput = wrapper.find('[data-testid="work-name-0"]');

			await nameInput.setValue("Apple Inc.");
			expect((nameInput.element as HTMLInputElement).value).toBe("Apple Inc.");
		});

		it("should bind position field", async () => {
			const work: Work[] = [
				{
					name: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			const positionInput = wrapper.find('[data-testid="work-position-0"]');

			await positionInput.setValue("Tech Lead");
			expect((positionInput.element as HTMLInputElement).value).toBe(
				"Tech Lead",
			);
		});

		it("should bind start and end dates", async () => {
			const work: Work[] = [
				{
					name: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			const startDateInput = wrapper.find('[data-testid="work-start-date-0"]');
			const endDateInput = wrapper.find('[data-testid="work-end-date-0"]');

			await startDateInput.setValue("2020-01-01");
			await endDateInput.setValue("2022-12-31");

			expect((startDateInput.element as HTMLInputElement).value).toBe(
				"2020-01-01",
			);
			expect((endDateInput.element as HTMLInputElement).value).toBe(
				"2022-12-31",
			);
		});

		it("should display pre-filled data", () => {
			const work: Work[] = [
				{
					name: "Google",
					position: "Software Engineer",
					url: "https://google.com",
					startDate: "2020-01-01",
					endDate: "2022-01-01",
					summary: "Worked on search",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			const nameInput = wrapper.find('[data-testid="work-name-0"]');
			expect((nameInput.element as HTMLInputElement).value).toBe("Google");
		});
	});

	describe("highlights management", () => {
		it("should render highlights when provided", () => {
			const work: Work[] = [
				{
					name: "Google",
					position: "Engineer",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["Improved performance by 50%", "Led team of 5"],
				},
			];

			const wrapper = mountComponent(work);
			const highlight1 = wrapper.find('[data-testid="work-highlight-0-0"]');
			const highlight2 = wrapper.find('[data-testid="work-highlight-0-1"]');

			expect(highlight1.exists()).toBe(true);
			expect(highlight2.exists()).toBe(true);
			expect((highlight1.element as HTMLInputElement).value).toBe(
				"Improved performance by 50%",
			);
			expect((highlight2.element as HTMLInputElement).value).toBe(
				"Led team of 5",
			);
		});

		it("should handle empty highlights array", () => {
			const work: Work[] = [
				{
					name: "Google",
					position: "Engineer",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);
			expect(wrapper.find('[data-testid="work-highlight-0-0"]').exists()).toBe(
				false,
			);
		});
	});

	describe("form validation", () => {
		it("should have form fields for work entry", () => {
			const work: Work[] = [
				{
					name: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(work);

			expect(wrapper.find('[data-testid="work-name-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="work-position-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="work-start-date-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="work-end-date-0"]').exists()).toBe(
				true,
			);
		});
	});

	describe("edge cases", () => {
		it("should handle multiple work entries with highlights", () => {
			const work: Work[] = [
				{
					name: "Google",
					position: "Engineer",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["Achievement 1"],
				},
				{
					name: "Meta",
					position: "Senior Engineer",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["Achievement 2", "Achievement 3"],
				},
			];

			const wrapper = mountComponent(work);

			expect(wrapper.find('[data-testid="work-highlight-0-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="work-highlight-1-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="work-highlight-1-1"]').exists()).toBe(
				true,
			);
		});
	});
});
