import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createI18n } from "vue-i18n";
import type { Skill } from "@/core/resume/domain/Resume";
import SkillSection from "./SkillSection.vue";

const createTestI18n = () =>
	createI18n({
		legacy: false,
		locale: "en",
		messages: {
			en: {
				resume: {
					actions: {
						descriptions: {
							skills: "Add your skills",
						},
						labels: {
							skill: "Skill #{number}",
							keyword: "Keyword #{number}",
						},
					},
					buttons: {
						addSkill: "Add Skill",
						addKeyword: "Add Keyword",
					},
					fields: {
						skillName: "Skill Name",
						level: "Proficiency Level",
						keywords: "Keywords",
					},
					placeholders: {
						skillName: "Programming",
						level: "Expert",
						keyword: "JavaScript",
					},
				},
			},
		},
	});

describe("SkillSection.vue", () => {
	const mountComponent = (skills: Skill[] = []) => {
		return mount(SkillSection, {
			props: {
				modelValue: skills,
			},
			global: {
				plugins: [createTestI18n()],
			},
		});
	};

	describe("rendering", () => {
		it("should render when no skills exist", () => {
			const wrapper = mountComponent();
			expect(wrapper.find('[data-testid="skill-name-0"]').exists()).toBe(false);
		});

		it("should render skill entry when provided", () => {
			const skills: Skill[] = [
				{
					name: "",
					level: "",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			expect(wrapper.find('[data-testid="skill-name-0"]').exists()).toBe(true);
		});

		it("should render multiple skill entries", () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: ["JavaScript", "TypeScript"],
				},
				{
					name: "Design",
					level: "Intermediate",
					keywords: ["Figma", "Sketch"],
				},
			];

			const wrapper = mountComponent(skills);
			expect(wrapper.find('[data-testid="skill-name-0"]').exists()).toBe(true);
			expect(wrapper.find('[data-testid="skill-name-1"]').exists()).toBe(true);
		});
	});

	describe("v-model binding", () => {
		it("should bind skill name field", async () => {
			const skills: Skill[] = [
				{
					name: "",
					level: "",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			const nameInput = wrapper.find('[data-testid="skill-name-0"]');

			await nameInput.setValue("Web Development");
			expect((nameInput.element as HTMLInputElement).value).toBe(
				"Web Development",
			);
		});

		it("should bind level field", async () => {
			const skills: Skill[] = [
				{
					name: "",
					level: "",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			const levelInput = wrapper.find('[data-testid="skill-level-0"]');

			await levelInput.setValue("Advanced");
			expect((levelInput.element as HTMLInputElement).value).toBe("Advanced");
		});

		it("should display pre-filled data", () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			const nameInput = wrapper.find('[data-testid="skill-name-0"]');
			expect((nameInput.element as HTMLInputElement).value).toBe("Programming");

			const levelInput = wrapper.find('[data-testid="skill-level-0"]');
			expect((levelInput.element as HTMLInputElement).value).toBe("Expert");
		});
	});

	describe("keywords management", () => {
		it("should render keywords when provided", () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: ["JavaScript", "TypeScript", "Python"],
				},
			];

			const wrapper = mountComponent(skills);
			const keyword1 = wrapper.find('[data-testid="skill-keyword-0-0"]');
			const keyword2 = wrapper.find('[data-testid="skill-keyword-0-1"]');
			const keyword3 = wrapper.find('[data-testid="skill-keyword-0-2"]');

			expect(keyword1.exists()).toBe(true);
			expect(keyword2.exists()).toBe(true);
			expect(keyword3.exists()).toBe(true);
			expect((keyword1.element as HTMLInputElement).value).toBe("JavaScript");
			expect((keyword2.element as HTMLInputElement).value).toBe("TypeScript");
			expect((keyword3.element as HTMLInputElement).value).toBe("Python");
		});

		it("should handle empty keywords array", () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			expect(wrapper.find('[data-testid="skill-keyword-0-0"]').exists()).toBe(
				false,
			);
		});
	});

	describe("form validation", () => {
		it("should mark required fields", () => {
			const skills: Skill[] = [
				{
					name: "",
					level: "",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			expect(
				wrapper.find('[data-testid="skill-name-0"]').attributes("required"),
			).toBeDefined();
		});

		it("should have correct input types", () => {
			const skills: Skill[] = [
				{
					name: "",
					level: "",
					keywords: [],
				},
			];

			const wrapper = mountComponent(skills);
			expect(
				wrapper.find('[data-testid="skill-name-0"]').attributes("type"),
			).toBe("text");
		});
	});

	describe("edge cases", () => {
		it("should handle multiple skills with keywords", () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: ["JavaScript"],
				},
				{
					name: "Design",
					level: "Intermediate",
					keywords: ["Figma", "Sketch"],
				},
			];

			const wrapper = mountComponent(skills);

			expect(wrapper.find('[data-testid="skill-keyword-0-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="skill-keyword-1-0"]').exists()).toBe(
				true,
			);
			expect(wrapper.find('[data-testid="skill-keyword-1-1"]').exists()).toBe(
				true,
			);
		});

		it("should handle updating keyword values", async () => {
			const skills: Skill[] = [
				{
					name: "Programming",
					level: "Expert",
					keywords: [""],
				},
			];

			const wrapper = mountComponent(skills);
			const keywordInput = wrapper.find('[data-testid="skill-keyword-0-0"]');

			await keywordInput.setValue("React");
			expect((keywordInput.element as HTMLInputElement).value).toBe("React");
		});
	});
});
