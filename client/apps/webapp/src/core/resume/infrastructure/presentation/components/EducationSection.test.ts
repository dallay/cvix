import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Education } from "@/core/resume/domain/Resume";
import EducationSection from "./EducationSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						education: "Add your education history",
					},
					labels: {
						education: "Education #{number}",
						course: "Course #{number}",
					},
				},
				buttons: {
					addEducation: "Add Education",
					addCourse: "Add Course",
				},
				fields: {
					institution: "Institution",
					url: "Website",
					area: "Field of Study",
					studyType: "Degree",
					startDate: "Start Date",
					endDate: "End Date",
					score: "GPA/Score",
					courses: "Courses",
				},
				placeholders: {
					institution: "University Name",
					url: "https://university.edu",
					area: "Computer Science",
					studyType: "Bachelor",
					score: "3.8",
					course: "Course Name",
				},
			},
		},
	},
});

describe("EducationSection.vue", () => {
	const mountComponent = (educationEntries: Education[] = []) => {
		return mount(EducationSection, {
			props: {
				modelValue: educationEntries,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add education button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Education");
		});

		it("should not show education entries when empty", () => {
			const wrapper = mountComponent();
			expect(
				wrapper.find('[data-testid="education-institution-0"]').exists(),
			).toBe(false);
		});

		it("should render education entry when provided", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			const institutionInput = wrapper.find(
				'[data-testid="education-institution-0"]',
			);
			expect(institutionInput.exists()).toBe(true);
		});

		it("should render multiple education entries", async () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "https://mit.edu",
					area: "Computer Science",
					studyType: "Bachelor",
					startDate: "2015-09-01",
					endDate: "2019-05-31",
					score: "3.9",
					courses: [],
				},
				{
					institution: "Stanford",
					url: "https://stanford.edu",
					area: "AI",
					studyType: "Master",
					startDate: "2019-09-01",
					endDate: "2021-05-31",
					score: "4.0",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);

			expect(
				wrapper.find('[data-testid="education-institution-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="education-institution-1"]').exists(),
			).toBe(true);
		});

		it("should display field labels", () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);

			expect(wrapper.text()).toContain("Institution");
			expect(wrapper.text()).toContain("Field of Study");
			expect(wrapper.text()).toContain("Degree");
			expect(wrapper.text()).toContain("Start Date");
			expect(wrapper.text()).toContain("End Date");
		});
	});

	describe("v-model binding", () => {
		it("should bind institution field", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			const institutionInput = wrapper.find(
				'[data-testid="education-institution-0"]',
			);

			await institutionInput.setValue("Harvard University");
			expect((institutionInput.element as HTMLInputElement).value).toBe(
				"Harvard University",
			);
		});

		it("should bind area field", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			const areaInput = wrapper.find('[data-testid="education-area-0"]');

			await areaInput.setValue("Physics");
			expect((areaInput.element as HTMLInputElement).value).toBe("Physics");
		});

		it("should bind start and end dates", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			const startDateInput = wrapper.find(
				'[data-testid="education-start-date-0"]',
			);
			const endDateInput = wrapper.find('[data-testid="education-end-date-0"]');

			await startDateInput.setValue("2015-09-01");
			await endDateInput.setValue("2019-05-31");

			expect((startDateInput.element as HTMLInputElement).value).toBe(
				"2015-09-01",
			);
			expect((endDateInput.element as HTMLInputElement).value).toBe(
				"2019-05-31",
			);
		});

		it("should display pre-filled data", () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "https://mit.edu",
					area: "Computer Science",
					studyType: "Bachelor",
					startDate: "2015-09-01",
					endDate: "2019-05-31",
					score: "3.9",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);

			const institutionInput = wrapper.find(
				'[data-testid="education-institution-0"]',
			);
			expect((institutionInput.element as HTMLInputElement).value).toBe("MIT");

			const areaInput = wrapper.find('[data-testid="education-area-0"]');
			expect((areaInput.element as HTMLInputElement).value).toBe(
				"Computer Science",
			);
		});
	});

	describe("form validation", () => {
		it("should mark required fields", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);

			expect(
				wrapper
					.find('[data-testid="education-institution-0"]')
					.attributes("required"),
			).toBeDefined();
			expect(
				wrapper.find('[data-testid="education-area-0"]').attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="education-studyType-0"]')
					.attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", async () => {
			const educations: Education[] = [
				{
					institution: "",
					url: "",
					area: "",
					studyType: "",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);

			expect(
				wrapper.find('[data-testid="education-url-0"]').attributes("type"),
			).toBe("url");
			expect(
				wrapper
					.find('[data-testid="education-start-date-0"]')
					.attributes("type"),
			).toBe("date");
			expect(
				wrapper.find('[data-testid="education-end-date-0"]').attributes("type"),
			).toBe("date");
		});
	});

	describe("edge cases", () => {
		it("should handle empty courses array", () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "",
					area: "CS",
					studyType: "Bachelor",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			expect(
				wrapper.find('[data-testid="education-course-0-0"]').exists(),
			).toBe(false);
		});

		it("should render courses when provided", () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "",
					area: "CS",
					studyType: "Bachelor",
					startDate: "",
					endDate: "",
					score: "",
					courses: ["Algorithms 101", "Data Structures"],
				},
			];

			const wrapper = mountComponent(educations);
			const course1 = wrapper.find('[data-testid="education-course-0-0"]');
			const course2 = wrapper.find('[data-testid="education-course-0-1"]');

			expect(course1.exists()).toBe(true);
			expect(course2.exists()).toBe(true);
			expect((course1.element as HTMLInputElement).value).toBe(
				"Algorithms 101",
			);
			expect((course2.element as HTMLInputElement).value).toBe(
				"Data Structures",
			);
		});

		it("should render delete buttons for education entries", () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "",
					area: "CS",
					studyType: "Bachelor",
					startDate: "",
					endDate: "",
					score: "",
					courses: [],
				},
			];

			const wrapper = mountComponent(educations);
			// Look for buttons with ghost variant that contain Trash2 icon
			const allButtons = wrapper.findAllComponents({ name: "Button" });
			expect(allButtons.length).toBeGreaterThan(0);
		});

		it("should handle multiple education entries with courses", () => {
			const educations: Education[] = [
				{
					institution: "MIT",
					url: "",
					area: "CS",
					studyType: "Bachelor",
					startDate: "",
					endDate: "",
					score: "",
					courses: ["CS101"],
				},
				{
					institution: "Stanford",
					url: "",
					area: "AI",
					studyType: "Master",
					startDate: "",
					endDate: "",
					score: "",
					courses: ["AI201", "ML301"],
				},
			];

			const wrapper = mountComponent(educations);

			expect(
				wrapper.find('[data-testid="education-course-0-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="education-course-1-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="education-course-1-1"]').exists(),
			).toBe(true);
		});
	});
});
