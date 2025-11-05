import { config } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { expect } from "vitest";
import * as matchers from "vitest-axe/matchers";
import { createI18n } from "vue-i18n";

// Extend Vitest expect with axe accessibility matchers
expect.extend(matchers);

// Mock i18n setup
const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				sections: {
					personalInfo: "Personal Information",
					workExperience: "Work Experience",
					education: "Education",
					skills: "Skills",
					projects: "Projects",
					languages: "Languages",
				},
				placeholders: {
					fullName: "Full Name",
					summary: "Summary",
				},
				fields: {
					fullName: "Full Name",
					email: "Email",
					phone: "Phone",
					location: "Location",
					linkedin: "LinkedIn",
					github: "GitHub",
					website: "Website",
					summary: "Summary",
					company: "Company",
					position: "Position",
					startDate: "Start Date",
					endDate: "End Date",
				},
				actions: {
					add: "Add",
					removeWorkExperience: "Remove Work Experience",
				},
				form: {
					generate: "Generate Resume",
				},
			},
		},
	},
});

// Initialize Pinia
const pinia = createPinia();
setActivePinia(pinia);

// Add Pinia to global plugins
config.global.plugins = [i18n, pinia];
