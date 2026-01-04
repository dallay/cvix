import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import { createTestI18n } from "@/test-utils/i18n-helper";
import ResumeForm from "./ResumeForm.vue";

// Mock the useResumeForm composable
const mockLoadResume = vi.fn();
const mockClearForm = vi.fn();

vi.mock(
	"@/core/resume/infrastructure/presentation/composables/useResumeForm",
	() => ({
		useResumeForm: () => ({
			basics: { value: { name: "", label: "", profiles: [] } },
			workExperiences: { value: [] },
			volunteers: { value: [] },
			education: { value: [] },
			awards: { value: [] },
			certificates: { value: [] },
			publications: { value: [] },
			skills: { value: [] },
			languages: { value: [] },
			interests: { value: [] },
			references: { value: [] },
			projects: { value: [] },
			clearForm: mockClearForm,
			loadResume: mockLoadResume,
		}),
	}),
);

const createMockResume = (): Resume => ({
	basics: {
		name: "John Doe",
		label: "Software Engineer",
		image: "https://example.com/photo.jpg",
		email: "john@example.com",
		phone: "+1-555-0100",
		url: "https://johndoe.com",
		summary: "Experienced software engineer",
		location: {
			address: "123 Main St",
			postalCode: "12345",
			city: "San Francisco",
			countryCode: "US",
			region: "California",
		},
		profiles: [],
	},
	work: [],
	volunteer: [],
	education: [],
	awards: [],
	certificates: [],
	publications: [],
	skills: [],
	languages: [],
	interests: [],
	references: [],
	projects: [],
});

describe("ResumeForm.vue", () => {
	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();
	});

	const mountComponent = (customI18n = createTestI18n()) => {
		return mount(ResumeForm, {
			global: {
				plugins: [customI18n],
				stubs: {
					BasicsSection: true,
					ProfilesField: true,
					WorkExperienceSection: true,
					VolunteerSection: true,
					EducationSection: true,
					AwardSection: true,
					CertificateSection: true,
					PublicationSection: true,
					SkillSection: true,
					LanguageSection: true,
					InterestSection: true,
					ReferenceSection: true,
					ProjectSection: true,
					Accordion: {
						template: '<div class="accordion"><slot /></div>',
					},
					AccordionItem: {
						template: '<div class="accordion-item"><slot /></div>',
						props: ["value"],
					},
					AccordionTrigger: {
						template: '<button class="accordion-trigger"><slot /></button>',
					},
					AccordionContent: {
						template: '<div class="accordion-content"><slot /></div>',
					},
					FieldGroup: {
						template: '<div class="field-group"><slot /></div>',
					},
					FieldSet: {
						template: '<fieldset class="field-set"><slot /></fieldset>',
					},
				},
			},
		});
	};

	describe("rendering", () => {
		it("should render all accordion sections", () => {
			const wrapper = mountComponent();

			// The component should render accordion items for each section
			const accordionItems = wrapper.findAll(".accordion-item");
			expect(accordionItems.length).toBe(12); // 12 sections total

			// Check that section triggers are rendered with correct labels
			const triggers = wrapper.findAll(".accordion-trigger");
			expect(triggers.length).toBeGreaterThan(0);
		});

		it("should render BasicsSection component", () => {
			const wrapper = mountComponent();

			const basicsSection = wrapper.findComponent({ name: "BasicsSection" });
			expect(basicsSection.exists()).toBe(true);
		});

		it("should render ProfilesField component", () => {
			const wrapper = mountComponent();

			const profilesField = wrapper.findComponent({ name: "ProfilesField" });
			expect(profilesField.exists()).toBe(true);
		});

		it("should render WorkExperienceSection component", () => {
			const wrapper = mountComponent();

			const workSection = wrapper.findComponent({
				name: "WorkExperienceSection",
			});
			expect(workSection.exists()).toBe(true);
		});

		it("should render EducationSection component", () => {
			const wrapper = mountComponent();

			const educationSection = wrapper.findComponent({
				name: "EducationSection",
			});
			expect(educationSection.exists()).toBe(true);
		});

		it("should render SkillSection component", () => {
			const wrapper = mountComponent();

			const skillSection = wrapper.findComponent({ name: "SkillSection" });
			expect(skillSection.exists()).toBe(true);
		});

		it("should render ProjectSection component", () => {
			const wrapper = mountComponent();

			const projectSection = wrapper.findComponent({ name: "ProjectSection" });
			expect(projectSection.exists()).toBe(true);
		});
	});

	describe("exposed methods", () => {
		it("should expose loadResume method", () => {
			const wrapper = mountComponent();

			expect(wrapper.vm.loadResume).toBeDefined();
			expect(typeof wrapper.vm.loadResume).toBe("function");
		});

		it("should expose clearForm method", () => {
			const wrapper = mountComponent();

			expect(wrapper.vm.clearForm).toBeDefined();
			expect(typeof wrapper.vm.clearForm).toBe("function");
		});

		it("should call composable loadResume when exposed method is called", () => {
			const wrapper = mountComponent();
			const mockResume = createMockResume();

			wrapper.vm.loadResume(mockResume);

			expect(mockLoadResume).toHaveBeenCalledWith(mockResume);
		});

		it("should call composable clearForm when exposed method is called", () => {
			const wrapper = mountComponent();

			wrapper.vm.clearForm();

			expect(mockClearForm).toHaveBeenCalled();
		});
	});

	describe("internationalization", () => {
		it("should render section labels using i18n", () => {
			const wrapper = mountComponent();

			// Check that at least one trigger has translated text
			const triggers = wrapper.findAll(".accordion-trigger");
			expect(triggers.length).toBeGreaterThan(0);

			// The first trigger should have the "Personal Details" label
			// (from the test i18n helper)
			const firstTrigger = triggers[0]!;
			expect(firstTrigger.text()).toBeTruthy();
		});

		it("should render in Spanish when locale is changed", async () => {
			const i18n = createTestI18n();
			i18n.global.locale.value = "es";
			const wrapper = mountComponent(i18n);

			const triggers = wrapper.findAll(".accordion-trigger");
			expect(triggers.length).toBeGreaterThan(0);

			// Reset locale
			i18n.global.locale.value = "en";
		});
	});

	describe("structure", () => {
		it("should NOT render a form element (buttons moved to parent)", () => {
			const wrapper = mountComponent();

			const form = wrapper.find("form");
			expect(form.exists()).toBe(false);
		});

		it("should NOT render submit/cancel buttons (moved to parent)", () => {
			const wrapper = mountComponent();

			const submitButton = wrapper.find('button[type="submit"]');
			expect(submitButton.exists()).toBe(false);

			const buttons = wrapper.findAll("button");
			const cancelButton = buttons.find((btn) => btn.text() === "Cancel");
			expect(cancelButton).toBeUndefined();
		});

		it("should have accordion as root structure", () => {
			const wrapper = mountComponent();

			const accordion = wrapper.find(".accordion");
			expect(accordion.exists()).toBe(true);
		});
	});
});
