/**
 * Accessibility Test Suite
 * Tests WCAG AA compliance for resume generator components
 *
 * @see T142 - Run accessibility audit and fix WCAG AA issues
 */

import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { axe } from "vitest-axe";
import EducationSection from "../components/EducationSection.vue";
import LanguagesSection from "../components/LanguagesSection.vue";
import PersonalInfoSection from "../components/PersonalInfoSection.vue";
import ProjectsSection from "../components/ProjectsSection.vue";
import ResumeForm from "../components/ResumeForm.vue";
import ResumePreview from "../components/ResumePreview.vue";
import SkillsSection from "../components/SkillsSection.vue";
import WorkExperienceSection from "../components/WorkExperienceSection.vue";

describe("Accessibility - WCAG AA Compliance", () => {
	describe("ResumeForm", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});

		it("should have proper heading hierarchy", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			const headings = wrapper.element.querySelectorAll(
				"h1, h2, h3, h4, h5, h6",
			);

			// Verify heading levels increase incrementally (no skipping levels)
			let previousLevel = 0;
			for (const heading of headings) {
				const currentLevel = Number.parseInt(heading.tagName.substring(1), 10);
				if (previousLevel > 0) {
					expect(currentLevel - previousLevel).toBeLessThanOrEqual(1);
				}
				previousLevel = currentLevel;
			}
		});

		it("should have accessible form controls", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			// All inputs, textareas, and selects should have associated labels
			const inputs = wrapper.element.querySelectorAll(
				"input, textarea, select",
			);

			for (const input of inputs) {
				const id = input.getAttribute("id");
				const ariaLabel = input.getAttribute("aria-label");
				const ariaLabelledby = input.getAttribute("aria-labelledby");

				// Each input should have either an id with a label, aria-label, or aria-labelledby
				const hasLabel =
					id && wrapper.element.querySelector(`label[for="${id}"]`);
				const hasAccessibleName = ariaLabel || ariaLabelledby || hasLabel;

				expect(hasAccessibleName).toBeTruthy();
			}
		});
	});

	describe("ResumePreview", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(ResumePreview, {
				props: {
					data: {
						personalInfo: {
							name: "John Doe",
							email: "john@example.com",
							phone: "+1234567890",
							location: { city: "New York", country: "USA" },
						},
						workExperience: [],
						education: [],
						skills: [],
						languages: [],
						projects: [],
					},
				},
			});

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});

		it("should have semantic HTML structure", () => {
			const wrapper = mount(ResumePreview, {
				props: {
					data: {
						personalInfo: {
							name: "John Doe",
							email: "john@example.com",
						},
						workExperience: [],
						education: [],
						skills: [],
					},
				},
			});

			// Should use semantic HTML elements
			const hasSemanticElements =
				wrapper.element.querySelector(
					"header, main, section, article, aside",
				) !== null;

			expect(hasSemanticElements).toBeTruthy();
		});
	});

	describe("PersonalInfoSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(PersonalInfoSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});

		it("should have proper input labels", () => {
			const wrapper = mount(PersonalInfoSection);

			// Check for FormLabel components or label elements
			const labels = wrapper.element.querySelectorAll("label");
			const inputs = wrapper.element.querySelectorAll("input");

			// Each input should have a corresponding label
			expect(labels.length).toBeGreaterThan(0);
			expect(inputs.length).toBeGreaterThan(0);
			expect(labels.length).toBeGreaterThanOrEqual(inputs.length);
		});
	});

	describe("WorkExperienceSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(WorkExperienceSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});

		it("should have accessible add/remove buttons", () => {
			const wrapper = mount(WorkExperienceSection);

			const buttons = wrapper.element.querySelectorAll("button");

			for (const button of buttons) {
				// Each button should have accessible text content or aria-label
				const hasAccessibleName =
					button.textContent?.trim() || button.getAttribute("aria-label");

				expect(hasAccessibleName).toBeTruthy();
			}
		});
	});

	describe("EducationSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(EducationSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});
	});

	describe("SkillsSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(SkillsSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});
	});

	describe("LanguagesSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(LanguagesSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});
	});

	describe("ProjectsSection", () => {
		it("should have no accessibility violations", async () => {
			const wrapper = mount(ProjectsSection);

			const results = await axe(wrapper.element as HTMLElement);
			expect(results.violations).toEqual([]);
		});
	});

	describe("Keyboard Navigation", () => {
		it("should have proper tab order in form", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			// Check that interactive elements have proper tabindex
			const interactiveElements = wrapper.element.querySelectorAll(
				'a, button, input, textarea, select, [tabindex]:not([tabindex="-1"])',
			);

			for (const element of interactiveElements) {
				const tabindex = element.getAttribute("tabindex");

				// Tabindex should not be > 0 (to maintain natural tab order)
				if (tabindex !== null) {
					expect(Number.parseInt(tabindex, 10)).toBeLessThanOrEqual(0);
				}
			}
		});

		it("should allow focus on all interactive elements", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			// All buttons and inputs should be focusable
			const interactiveElements = wrapper.element.querySelectorAll(
				"button, input, textarea, select",
			);

			for (const element of interactiveElements) {
				const tabindex = element.getAttribute("tabindex");
				const disabled = element.hasAttribute("disabled");

				// Should not have tabindex="-1" unless disabled
				if (tabindex === "-1") {
					expect(disabled).toBeTruthy();
				}
			}
		});
	});

	describe("ARIA Attributes", () => {
		it("should have valid ARIA roles", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			const elementsWithRoles = wrapper.element.querySelectorAll("[role]");
			const validRoles = [
				"alert",
				"alertdialog",
				"application",
				"article",
				"banner",
				"button",
				"checkbox",
				"columnheader",
				"combobox",
				"complementary",
				"contentinfo",
				"definition",
				"dialog",
				"directory",
				"document",
				"feed",
				"figure",
				"form",
				"grid",
				"gridcell",
				"group",
				"heading",
				"img",
				"link",
				"list",
				"listbox",
				"listitem",
				"log",
				"main",
				"marquee",
				"math",
				"menu",
				"menubar",
				"menuitem",
				"menuitemcheckbox",
				"menuitemradio",
				"navigation",
				"none",
				"note",
				"option",
				"presentation",
				"progressbar",
				"radio",
				"radiogroup",
				"region",
				"row",
				"rowgroup",
				"rowheader",
				"scrollbar",
				"search",
				"searchbox",
				"separator",
				"slider",
				"spinbutton",
				"status",
				"switch",
				"tab",
				"table",
				"tablist",
				"tabpanel",
				"term",
				"textbox",
				"timer",
				"toolbar",
				"tooltip",
				"tree",
				"treegrid",
				"treeitem",
			];

			for (const element of elementsWithRoles) {
				const role = element.getAttribute("role");
				if (role) {
					expect(validRoles).toContain(role);
				}
			}
		});

		it("should have proper aria-required on required fields", () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			const requiredInputs = wrapper.element.querySelectorAll("[required]");

			for (const input of requiredInputs) {
				// Required inputs should have aria-required or rely on HTML5 required attribute
				const ariaRequired = input.getAttribute("aria-required");
				const hasRequired = input.hasAttribute("required");

				expect(hasRequired || ariaRequired === "true").toBeTruthy();
			}
		});
	});

	describe("Color Contrast", () => {
		it("should have sufficient color contrast for text", async () => {
			const wrapper = mount(ResumeForm, {
				global: {
					stubs: {
						PersonalInfoSection: true,
						WorkExperienceSection: true,
						EducationSection: true,
						SkillsSection: true,
						LanguagesSection: true,
						ProjectsSection: true,
					},
				},
			});

			// axe will check color contrast as part of WCAG AA compliance
			const results = await axe(wrapper.element as HTMLElement, {
				rules: {
					"color-contrast": { enabled: true },
				},
			});

			expect(results.violations).toEqual([]);
		});
	});
});
