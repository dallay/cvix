import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Volunteer } from "@/core/resume/domain/Resume";
import VolunteerSection from "./VolunteerSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						volunteer: "Add your volunteer experience",
					},
					labels: {
						volunteer: "Volunteer #{number}",
					},
					empty: {
						volunteer: "No volunteer experience added yet",
						highlights: "No highlights added yet",
					},
					addFirstVolunteer: "Add your first volunteer experience",
				},
				buttons: {
					addVolunteer: "Add Volunteer",
					addHighlight: "Add Highlight",
				},
				fields: {
					organization: "Organization",
					position: "Position",
					organizationUrl: "Organization URL",
					startDate: "Start Date",
					endDate: "End Date",
					currentVolunteer: "I currently volunteer here",
					summary: "Summary",
					highlights: "Highlights",
				},
				placeholders: {
					organization: "Red Cross",
					volunteerPosition: "Volunteer Coordinator",
					organizationUrl: "https://redcross.org",
					volunteerSummary: "Describe your volunteer work",
					volunteerHighlight: "Key achievement or responsibility",
				},
			},
		},
	},
});

describe("VolunteerSection.vue", () => {
	const mountComponent = (volunteers: Volunteer[] = []) => {
		return mount(VolunteerSection, {
			props: {
				modelValue: volunteers,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add volunteer button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Volunteer");
		});

		it("should show empty state when no volunteers", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("No volunteer experience added yet");
		});

		it("should render volunteer entry when provided", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-organization-0"]').exists(),
			).toBe(true);
		});

		it("should render multiple volunteer entries", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Coordinator",
					url: "",
					startDate: "2020-01-01",
					endDate: "2021-01-01",
					summary: "",
					highlights: [],
				},
				{
					organization: "Habitat",
					position: "Builder",
					url: "",
					startDate: "2021-02-01",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-organization-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="volunteer-organization-1"]').exists(),
			).toBe(true);
		});

		it("should display field labels", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(wrapper.text()).toContain("Organization");
			expect(wrapper.text()).toContain("Position");
			expect(wrapper.text()).toContain("Start Date");
			expect(wrapper.text()).toContain("End Date");
			expect(wrapper.text()).toContain("Summary");
		});
	});

	describe("v-model binding", () => {
		it("should bind organization field", async () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			const orgInput = wrapper.find('[data-testid="volunteer-organization-0"]');

			await orgInput.setValue("Red Cross");
			expect((orgInput.element as HTMLInputElement).value).toBe("Red Cross");
		});

		it("should bind position field", async () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			const posInput = wrapper.find('[data-testid="volunteer-position-0"]');

			await posInput.setValue("Coordinator");
			expect((posInput.element as HTMLInputElement).value).toBe("Coordinator");
		});

		it("should bind date fields", async () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			const startDate = wrapper.find('[data-testid="volunteer-start-date-0"]');
			const endDate = wrapper.find('[data-testid="volunteer-end-date-0"]');

			await startDate.setValue("2020-01-01");
			await endDate.setValue("2021-01-01");

			expect((startDate.element as HTMLInputElement).value).toBe("2020-01-01");
			expect((endDate.element as HTMLInputElement).value).toBe("2021-01-01");
		});

		it("should display pre-filled data", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Volunteer Coordinator",
					url: "https://redcross.org",
					startDate: "2020-01-01",
					endDate: "2021-01-01",
					summary: "Coordinated volunteers",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			const orgInput = wrapper.find('[data-testid="volunteer-organization-0"]');
			expect((orgInput.element as HTMLInputElement).value).toBe("Red Cross");
		});
	});

	describe("highlights management", () => {
		it("should render highlights when provided", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Coordinator",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["Led team of 20", "Organized 5 events"],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-0-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-0-1"]').exists(),
			).toBe(true);
		});

		it("should show empty state when no highlights", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Coordinator",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(wrapper.text()).toContain("No highlights added yet");
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper
					.find('[data-testid="volunteer-organization-0"]')
					.attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="volunteer-position-0"]')
					.attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="volunteer-start-date-0"]')
					.attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "",
					position: "",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-url-0"]').attributes("type"),
			).toBe("url");
			expect(
				wrapper
					.find('[data-testid="volunteer-start-date-0"]')
					.attributes("type"),
			).toBe("date");
			expect(
				wrapper.find('[data-testid="volunteer-end-date-0"]').attributes("type"),
			).toBe("date");
		});
	});

	describe("edge cases", () => {
		it("should handle empty highlights array", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Coordinator",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: [],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-0-0"]').exists(),
			).toBe(false);
		});

		it("should render multiple entries with different highlight counts", () => {
			const volunteers: Volunteer[] = [
				{
					organization: "Red Cross",
					position: "Coordinator",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["Highlight 1"],
				},
				{
					organization: "Habitat",
					position: "Builder",
					url: "",
					startDate: "",
					endDate: "",
					summary: "",
					highlights: ["H1", "H2", "H3"],
				},
			];

			const wrapper = mountComponent(volunteers);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-0-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-1-0"]').exists(),
			).toBe(true);
			expect(
				wrapper.find('[data-testid="volunteer-highlight-1-2"]').exists(),
			).toBe(true);
		});
	});
});
