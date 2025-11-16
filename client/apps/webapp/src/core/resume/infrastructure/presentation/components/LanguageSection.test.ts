import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Language } from "@/core/resume/domain/Resume";
import LanguageSection from "./LanguageSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						languages: "Add languages you speak",
					},
					labels: {
						language: "Language #{number}",
					},
					empty: {
						languages: "No languages added yet",
					},
					addFirstLanguage: "Add your first language",
				},
				buttons: {
					addLanguage: "Add Language",
				},
				fields: {
					language: "Language",
					fluency: "Fluency",
				},
				placeholders: {
					language: "English",
					fluency: "Native speaker",
				},
			},
		},
	},
});

describe("LanguageSection.vue", () => {
	const mountComponent = (languages: Language[] = []) => {
		return mount(LanguageSection, {
			props: {
				modelValue: languages,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add language button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Language");
		});

		it("should show empty state when no languages", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("No languages added yet");
		});

		it("should render language entry when provided", () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			expect(wrapper.find('[data-testid="language-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-fluency-0"]').exists()).toBe(
				true,
			);
		});

		it("should render multiple language entries", () => {
			const languages: Language[] = [
				{
					language: "English",
					fluency: "Native",
				},
				{
					language: "Spanish",
					fluency: "Fluent",
				},
			];

			const wrapper = mountComponent(languages);
			expect(wrapper.find('[data-testid="language-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-name-1"]').exists()).toBe(
				true,
			);
		});

		it("should display field labels", () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			expect(wrapper.text()).toContain("Language");
			expect(wrapper.text()).toContain("Fluency");
		});
	});

	describe("v-model binding", () => {
		it("should bind language field", async () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			const langInput = wrapper.find('[data-testid="language-name-0"]');

			await langInput.setValue("English");
			expect((langInput.element as HTMLInputElement).value).toBe("English");
		});

		it("should bind fluency field", async () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			const fluencyInput = wrapper.find('[data-testid="language-fluency-0"]');

			await fluencyInput.setValue("Native speaker");
			expect((fluencyInput.element as HTMLInputElement).value).toBe(
				"Native speaker",
			);
		});

		it("should display pre-filled data", () => {
			const languages: Language[] = [
				{
					language: "English",
					fluency: "Native speaker",
				},
				{
					language: "Spanish",
					fluency: "Professional working proficiency",
				},
			];

			const wrapper = mountComponent(languages);
			const lang1 = wrapper.find('[data-testid="language-name-0"]');
			const fluency1 = wrapper.find('[data-testid="language-fluency-0"]');
			const lang2 = wrapper.find('[data-testid="language-name-1"]');

			expect((lang1.element as HTMLInputElement).value).toBe("English");
			expect((fluency1.element as HTMLInputElement).value).toBe(
				"Native speaker",
			);
			expect((lang2.element as HTMLInputElement).value).toBe("Spanish");
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			expect(
				wrapper.find('[data-testid="language-name-0"]').attributes("required"),
			).toBeDefined();
			expect(
				wrapper
					.find('[data-testid="language-fluency-0"]')
					.attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const languages: Language[] = [
				{
					language: "",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			expect(
				wrapper.find('[data-testid="language-name-0"]').attributes("type"),
			).toBe("text");
			expect(
				wrapper.find('[data-testid="language-fluency-0"]').attributes("type"),
			).toBe("text");
		});
	});

	describe("edge cases", () => {
		it("should handle single language", () => {
			const languages: Language[] = [
				{
					language: "English",
					fluency: "Native",
				},
			];

			const wrapper = mountComponent(languages);
			expect(wrapper.find('[data-testid="language-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-name-1"]').exists()).toBe(
				false,
			);
		});

		it("should handle multiple languages with different fluency levels", () => {
			const languages: Language[] = [
				{
					language: "English",
					fluency: "Native",
				},
				{
					language: "Spanish",
					fluency: "Fluent",
				},
				{
					language: "French",
					fluency: "Intermediate",
				},
				{
					language: "German",
					fluency: "Beginner",
				},
			];

			const wrapper = mountComponent(languages);
			expect(wrapper.find('[data-testid="language-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-name-1"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-name-2"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="language-name-3"]').exists()).toBe(
				true,
			);
		});

		it("should handle empty fluency", () => {
			const languages: Language[] = [
				{
					language: "English",
					fluency: "",
				},
			];

			const wrapper = mountComponent(languages);
			const fluencyInput = wrapper.find('[data-testid="language-fluency-0"]');
			expect((fluencyInput.element as HTMLInputElement).value).toBe("");
		});

		it("should handle special characters in language names", () => {
			const languages: Language[] = [
				{
					language: "Français",
					fluency: "Natif",
				},
				{
					language: "中文",
					fluency: "Fluent",
				},
			];

			const wrapper = mountComponent(languages);
			const lang1 = wrapper.find('[data-testid="language-name-0"]');
			const lang2 = wrapper.find('[data-testid="language-name-1"]');

			expect((lang1.element as HTMLInputElement).value).toBe("Français");
			expect((lang2.element as HTMLInputElement).value).toBe("中文");
		});
	});
});
