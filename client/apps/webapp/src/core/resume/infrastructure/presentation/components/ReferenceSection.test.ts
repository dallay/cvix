import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Reference } from "@/core/resume/domain/Resume";
import ReferenceSection from "./ReferenceSection.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				actions: {
					descriptions: {
						references: "Add professional references",
					},
					labels: {
						reference: "Reference #{number}",
					},
					empty: {
						references: "No references added yet",
					},
					addFirstReference: "Add your first reference",
				},
				buttons: {
					addReference: "Add Reference",
				},
				fields: {
					referenceName: "Reference Name",
					referenceText: "Reference",
				},
				placeholders: {
					referenceName: "Jane Doe",
					referenceText: "Jane was a pleasure to work with...",
				},
			},
		},
	},
});

describe("ReferenceSection.vue", () => {
	const mountComponent = (references: Reference[] = []) => {
		return mount(ReferenceSection, {
			props: {
				modelValue: references,
			},
			global: {
				plugins: [i18n],
			},
		});
	};

	describe("rendering", () => {
		it("should render add reference button", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("Add Reference");
		});

		it("should show empty state when no references", () => {
			const wrapper = mountComponent();
			expect(wrapper.text()).toContain("No references added yet");
		});

		it("should render reference entry when provided", () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			expect(wrapper.find('[data-testid="reference-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="reference-text-0"]').exists()).toBe(
				true,
			);
		});

		it("should render multiple reference entries", () => {
			const references: Reference[] = [
				{
					name: "Jane Doe",
					reference: "Excellent worker",
				},
				{
					name: "John Smith",
					reference: "Great team player",
				},
			];

			const wrapper = mountComponent(references);
			expect(wrapper.find('[data-testid="reference-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="reference-name-1"]').exists()).toBe(
				true,
			);
		});

		it("should display field labels", () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			expect(wrapper.text()).toContain("Reference Name");
			expect(wrapper.text()).toContain("Reference");
		});
	});

	describe("v-model binding", () => {
		it("should bind name field", async () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			const nameInput = wrapper.find('[data-testid="reference-name-0"]');

			await nameInput.setValue("Jane Doe");
			expect((nameInput.element as HTMLInputElement).value).toBe("Jane Doe");
		});

		it("should bind reference text field", async () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			const textArea = wrapper.find('[data-testid="reference-text-0"]');

			await textArea.setValue("Excellent professional");
			expect((textArea.element as HTMLTextAreaElement).value).toBe(
				"Excellent professional",
			);
		});

		it("should display pre-filled data", () => {
			const references: Reference[] = [
				{
					name: "Jane Doe",
					reference:
						"Jane was an outstanding colleague who consistently delivered high-quality work.",
				},
			];

			const wrapper = mountComponent(references);
			const nameInput = wrapper.find('[data-testid="reference-name-0"]');
			const textArea = wrapper.find('[data-testid="reference-text-0"]');

			expect((nameInput.element as HTMLInputElement).value).toBe("Jane Doe");
			expect((textArea.element as HTMLTextAreaElement).value).toContain(
				"outstanding colleague",
			);
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			expect(
				wrapper.find('[data-testid="reference-name-0"]').attributes("required"),
			).toBeDefined();
			expect(
				wrapper.find('[data-testid="reference-text-0"]').attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			expect(
				wrapper.find('[data-testid="reference-name-0"]').attributes("type"),
			).toBe("text");

			// Verify that the reference text field is a textarea
			const textArea = wrapper.find('[data-testid="reference-text-0"]');
			expect(textArea.element.tagName).toBe("TEXTAREA");
			expect(textArea.attributes("type")).toBeUndefined();
		});
	});

	describe("edge cases", () => {
		it("should handle single reference", () => {
			const references: Reference[] = [
				{
					name: "Jane Doe",
					reference: "Great colleague",
				},
			];

			const wrapper = mountComponent(references);
			expect(wrapper.find('[data-testid="reference-name-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="reference-name-1"]').exists()).toBe(
				false,
			);
		});

		it("should handle multiple references with varying text lengths", () => {
			const references: Reference[] = [
				{
					name: "Jane Doe",
					reference: "Short reference",
				},
				{
					name: "John Smith",
					reference:
						"This is a much longer reference that contains multiple sentences and provides detailed information about the candidate's performance and character.",
				},
			];

			const wrapper = mountComponent(references);
			const ref1 = wrapper.find('[data-testid="reference-text-0"]');
			const ref2 = wrapper.find('[data-testid="reference-text-1"]');

			expect((ref1.element as HTMLTextAreaElement).value).toBe(
				"Short reference",
			);
			expect(
				(ref2.element as HTMLTextAreaElement).value.length,
			).toBeGreaterThan(50);
		});

		it("should handle empty name or reference", () => {
			const references: Reference[] = [
				{
					name: "",
					reference: "Anonymous reference",
				},
				{
					name: "Jane Doe",
					reference: "",
				},
			];

			const wrapper = mountComponent(references);
			const name0 = wrapper.find('[data-testid="reference-name-0"]');
			const text1 = wrapper.find('[data-testid="reference-text-1"]');

			expect((name0.element as HTMLInputElement).value).toBe("");
			expect((text1.element as HTMLTextAreaElement).value).toBe("");
		});
	});
});
