import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Publication } from "@/core/resume/domain/Resume";
import PublicationSection from "./PublicationSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						publications: "Add your publications",
					},
					labels: {
						publication: "Publication #{number}",
					},
					empty: {
						publications: "No publications added yet",
					},
					addFirstPublication: "Add your first publication",
				},
				buttons: {
					addPublication: "Add Publication",
				},
				fields: {
					publicationName: "Publication Name",
					publisher: "Publisher",
					releaseDate: "Release Date",
					url: "URL",
					summary: "Summary",
				},
				placeholders: {
					publicationName: "Research Paper Title",
					publisher: "IEEE",
					publicationUrl: "https://doi.org/10.1234/example",
					publicationSummary: "Brief summary of the publication",
				},
			},
		},
	},
});

describe("PublicationSection.vue", () => {
	const mountComponent = (publications: Publication[] = []) => {
		return mount(PublicationSection, {
			props: {
				modelValue: publications,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add publication button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Publication");
		});

		it("should show empty state when no publications", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("No publications added yet");
		});

		it("should render publication entry when provided", () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(wrapper.find('[data-testid="publication-name-0"]').exists()).toBe(
				true,
			);
		});

		it("should render multiple publication entries", () => {
			const publications: Publication[] = [
				{
					name: "Paper 1",
					publisher: "IEEE",
					releaseDate: "2020-01-01",
					url: "",
					summary: "",
				},
				{
					name: "Paper 2",
					publisher: "ACM",
					releaseDate: "2021-01-01",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(wrapper.find('[data-testid="publication-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="publication-name-1"]').exists()).toBe(
				true,
			);
		});

		it("should display field labels", () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(wrapper.text()).toContain("Publication Name");
			expect(wrapper.text()).toContain("Publisher");
			expect(wrapper.text()).toContain("Release Date");
			expect(wrapper.text()).toContain("URL");
			expect(wrapper.text()).toContain("Summary");
		});
	});

	describe("v-model binding", () => {
		it("should bind publication name field", async () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const nameInput = wrapper.find('[data-testid="publication-name-0"]');

			await nameInput.setValue("Machine Learning in Healthcare");
			expect((nameInput.element as HTMLInputElement).value).toBe(
				"Machine Learning in Healthcare",
			);
		});

		it("should bind publisher field", async () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const publisherInput = wrapper.find(
				'[data-testid="publication-publisher-0"]',
			);

			await publisherInput.setValue("IEEE");
			expect((publisherInput.element as HTMLInputElement).value).toBe("IEEE");
		});

		it("should bind release date field", async () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const dateInput = wrapper.find(
				'[data-testid="publication-release-date-0"]',
			);

			await dateInput.setValue("2021-06-15");
			expect((dateInput.element as HTMLInputElement).value).toBe("2021-06-15");
		});

		it("should bind URL field", async () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const urlInput = wrapper.find('[data-testid="publication-url-0"]');

			await urlInput.setValue("https://doi.org/10.1234/example");
			expect((urlInput.element as HTMLInputElement).value).toBe(
				"https://doi.org/10.1234/example",
			);
		});

		it("should bind summary field", async () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const summaryInput = wrapper.find(
				'[data-testid="publication-summary-0"]',
			);

			await summaryInput.setValue("This paper explores...");
			expect((summaryInput.element as HTMLTextAreaElement).value).toBe(
				"This paper explores...",
			);
		});

		it("should display pre-filled data", () => {
			const publications: Publication[] = [
				{
					name: "Deep Learning Applications",
					publisher: "Nature",
					releaseDate: "2021-03-15",
					url: "https://example.com/paper",
					summary: "A comprehensive study on deep learning",
				},
			];

			const wrapper = mountComponent(publications);
			const nameInput = wrapper.find('[data-testid="publication-name-0"]');
			const publisherInput = wrapper.find(
				'[data-testid="publication-publisher-0"]',
			);

			expect((nameInput.element as HTMLInputElement).value).toBe(
				"Deep Learning Applications",
			);
			expect((publisherInput.element as HTMLInputElement).value).toBe("Nature");
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(
				wrapper
					.find('[data-testid="publication-name-0"]')
					.attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="publication-publisher-0"]')
					.attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="publication-release-date-0"]')
					.attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const publications: Publication[] = [
				{
					name: "",
					publisher: "",
					releaseDate: "",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(
				wrapper.find('[data-testid="publication-url-0"]').attributes("type"),
			).toBe("url");
			expect(
				wrapper
					.find('[data-testid="publication-release-date-0"]')
					.attributes("type"),
			).toBe("date");
		});
	});

	describe("edge cases", () => {
		it("should handle single publication", () => {
			const publications: Publication[] = [
				{
					name: "My Research Paper",
					publisher: "IEEE",
					releaseDate: "2021-01-01",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			expect(wrapper.find('[data-testid="publication-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="publication-name-1"]').exists()).toBe(
				false,
			);
		});

		it("should handle publication without URL", () => {
			const publications: Publication[] = [
				{
					name: "Unpublished Work",
					publisher: "Internal",
					releaseDate: "2021-01-01",
					url: "",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const urlInput = wrapper.find('[data-testid="publication-url-0"]');
			expect((urlInput.element as HTMLInputElement).value).toBe("");
		});

		it("should handle publication without summary", () => {
			const publications: Publication[] = [
				{
					name: "Paper Title",
					publisher: "Publisher",
					releaseDate: "2021-01-01",
					url: "https://example.com",
					summary: "",
				},
			];

			const wrapper = mountComponent(publications);
			const summaryInput = wrapper.find(
				'[data-testid="publication-summary-0"]',
			);
			expect((summaryInput.element as HTMLTextAreaElement).value).toBe("");
		});

		it("should handle multiple publications with varying data", () => {
			const publications: Publication[] = [
				{
					name: "Paper 1",
					publisher: "IEEE",
					releaseDate: "2020-01-01",
					url: "https://doi.org/1",
					summary: "Short summary",
				},
				{
					name: "Paper 2",
					publisher: "ACM",
					releaseDate: "2021-06-15",
					url: "",
					summary:
						"This is a much longer summary that provides detailed information about the research conducted and the findings.",
				},
			];

			const wrapper = mountComponent(publications);
			const summary1 = wrapper.find('[data-testid="publication-summary-0"]');
			const summary2 = wrapper.find('[data-testid="publication-summary-1"]');

			expect((summary1.element as HTMLTextAreaElement).value).toBe(
				"Short summary",
			);
			expect(
				(summary2.element as HTMLTextAreaElement).value.length,
			).toBeGreaterThan(50);
		});
	});
});
