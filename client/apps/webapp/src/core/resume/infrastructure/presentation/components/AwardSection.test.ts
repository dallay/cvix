import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Award } from "@/core/resume/domain/Resume";
import AwardSection from "./AwardSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						awards: "Add your awards and honors",
					},
					labels: {
						award: "Award #{number}",
					},
					empty: {
						awards: "No awards added yet",
					},
					addFirstAward: "Add your first award",
				},
				buttons: {
					addAward: "Add Award",
				},
				fields: {
					awardTitle: "Award Title",
					date: "Date",
					awarder: "Awarder",
					summary: "Summary",
				},
				placeholders: {
					awardTitle: "Best Developer Award",
					awarder: "Company Name",
					awardSummary: "Description of the award",
				},
			},
		},
	},
});

describe("AwardSection.vue", () => {
	const mountComponent = (awards: Award[] = []) => {
		return mount(AwardSection, {
			props: {
				modelValue: awards,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add award button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Award");
		});

		it("should show empty state when no awards", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("No awards added yet");
		});

		it("should render award entry when provided", () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(wrapper.find('[data-testid="award-title-0"]').exists()).toBe(true);
		});

		it("should render multiple award entries", () => {
			const awards: Award[] = [
				{
					title: "Award 1",
					date: "2020-01-01",
					awarder: "Organization 1",
					summary: "",
				},
				{
					title: "Award 2",
					date: "2021-01-01",
					awarder: "Organization 2",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(wrapper.find('[data-testid="award-title-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="award-title-1"]').exists()).toBe(true);
		});

		it("should display field labels", () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(wrapper.text()).toContain("Award Title");
			expect(wrapper.text()).toContain("Date");
			expect(wrapper.text()).toContain("Awarder");
			expect(wrapper.text()).toContain("Summary");
		});
	});

	describe("v-model binding", () => {
		it("should bind title field", async () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const titleInput = wrapper.find('[data-testid="award-title-0"]');

			await titleInput.setValue("Employee of the Year");
			expect((titleInput.element as HTMLInputElement).value).toBe(
				"Employee of the Year",
			);
		});

		it("should bind date field", async () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const dateInput = wrapper.find('[data-testid="award-date-0"]');

			await dateInput.setValue("2021-12-31");
			expect((dateInput.element as HTMLInputElement).value).toBe("2021-12-31");
		});

		it("should bind awarder field", async () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const awarderInput = wrapper.find('[data-testid="award-awarder-0"]');

			await awarderInput.setValue("Tech Company Inc.");
			expect((awarderInput.element as HTMLInputElement).value).toBe(
				"Tech Company Inc.",
			);
		});

		it("should bind summary field", async () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const summaryInput = wrapper.find('[data-testid="award-summary-0"]');

			await summaryInput.setValue("Awarded for outstanding performance");
			expect((summaryInput.element as HTMLTextAreaElement).value).toBe(
				"Awarded for outstanding performance",
			);
		});

		it("should display pre-filled data", () => {
			const awards: Award[] = [
				{
					title: "Best Developer",
					date: "2021-06-15",
					awarder: "Tech Corp",
					summary: "Recognized for exceptional coding skills",
				},
			];

			const wrapper = mountComponent(awards);
			const titleInput = wrapper.find('[data-testid="award-title-0"]');
			const dateInput = wrapper.find('[data-testid="award-date-0"]');
			const awarderInput = wrapper.find('[data-testid="award-awarder-0"]');

			expect((titleInput.element as HTMLInputElement).value).toBe(
				"Best Developer",
			);
			expect((dateInput.element as HTMLInputElement).value).toBe("2021-06-15");
			expect((awarderInput.element as HTMLInputElement).value).toBe(
				"Tech Corp",
			);
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(
				wrapper.find('[data-testid="award-title-0"]').attributes("required"),
			).toBeDefined();
			expect(
				wrapper.find('[data-testid="award-date-0"]').attributes("required"),
			).toBeDefined();
			expect(
				wrapper.find('[data-testid="award-awarder-0"]').attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const awards: Award[] = [
				{
					title: "",
					date: "",
					awarder: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(
				wrapper.find('[data-testid="award-title-0"]').attributes("type"),
			).toBe("text");
			expect(
				wrapper.find('[data-testid="award-date-0"]').attributes("type"),
			).toBe("date");
			expect(
				wrapper.find('[data-testid="award-awarder-0"]').attributes("type"),
			).toBe("text");
		});
	});

	describe("edge cases", () => {
		it("should handle single award", () => {
			const awards: Award[] = [
				{
					title: "Excellence Award",
					date: "2021-01-01",
					awarder: "Organization",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(wrapper.find('[data-testid="award-title-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="award-title-1"]').exists()).toBe(
				false,
			);
		});

		it("should handle award without summary", () => {
			const awards: Award[] = [
				{
					title: "Award Title",
					date: "2021-01-01",
					awarder: "Awarder",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const summaryInput = wrapper.find('[data-testid="award-summary-0"]');
			expect((summaryInput.element as HTMLTextAreaElement).value).toBe("");
		});

		it("should handle multiple awards from different awarders", () => {
			const awards: Award[] = [
				{
					title: "Innovation Award",
					date: "2020-03-15",
					awarder: "Tech Conference",
					summary: "For innovative product design",
				},
				{
					title: "Community Award",
					date: "2021-09-01",
					awarder: "Local Community",
					summary: "For community service",
				},
				{
					title: "Performance Award",
					date: "2021-12-31",
					awarder: "Company",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			expect(wrapper.find('[data-testid="award-title-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="award-title-1"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="award-title-2"]').exists()).toBe(true);
		});

		it("should handle awards with long summaries", () => {
			const awards: Award[] = [
				{
					title: "Lifetime Achievement",
					date: "2021-01-01",
					awarder: "Industry Association",
					summary:
						"This award recognizes an individual who has made significant and lasting contributions to the field over many years. The recipient has demonstrated exceptional dedication, innovation, and leadership throughout their career.",
				},
			];

			const wrapper = mountComponent(awards);
			const summaryInput = wrapper.find('[data-testid="award-summary-0"]');
			expect(
				(summaryInput.element as HTMLTextAreaElement).value.length,
			).toBeGreaterThan(100);
		});

		it("should handle chronological ordering of awards", () => {
			const awards: Award[] = [
				{
					title: "Recent Award",
					date: "2023-01-01",
					awarder: "Org A",
					summary: "",
				},
				{
					title: "Old Award",
					date: "2015-01-01",
					awarder: "Org B",
					summary: "",
				},
				{
					title: "Middle Award",
					date: "2020-01-01",
					awarder: "Org C",
					summary: "",
				},
			];

			const wrapper = mountComponent(awards);
			const date0 = wrapper.find('[data-testid="award-date-0"]');
			const date1 = wrapper.find('[data-testid="award-date-1"]');
			const date2 = wrapper.find('[data-testid="award-date-2"]');

			expect((date0.element as HTMLInputElement).value).toBe("2023-01-01");
			expect((date1.element as HTMLInputElement).value).toBe("2015-01-01");
			expect((date2.element as HTMLInputElement).value).toBe("2020-01-01");
		});
	});
});
