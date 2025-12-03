import { config } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { expect } from "vitest";
import * as matchers from "vitest-axe/matchers";
import { createI18n } from "vue-i18n";

// Extend Vitest expect with axe accessibility matchers
expect.extend(matchers);

// Mock i18n setup
export const i18n = createI18n({
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
					publicationName: "Research Paper Title",
					publisher: "IEEE",
					publicationUrl: "https://doi.org/10.1234/example",
					publicationSummary: "Brief summary of the publication",
					releaseDate: "YYYY-MM-DD",
					certificateName: "AWS Certified Solutions Architect",
					issuer: "Amazon Web Services",
					certificateUrl: "https://aws.amazon.com/certification/",
					date: "YYYY-MM-DD",
					projectName: "E-commerce Platform",
					projectUrl: "https://github.com/user/project",
					projectDescription: "Built a full-stack e-commerce solution",
					projectHighlight: "Key feature or achievement",
					startDate: "YYYY-MM-DD",
					endDate: "YYYY-MM-DD",
					organization: "Red Cross",
					volunteerPosition: "Volunteer Coordinator",
					organizationUrl: "https://redcross.org",
					volunteerSummary: "Describe your volunteer work",
					volunteerHighlight: "Key achievement or responsibility",
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
					publicationName: "Publication Name",
					publisher: "Publisher",
					releaseDate: "Release Date",
					url: "URL",
					certificateName: "Certificate Name",
					date: "Date",
					issuer: "Issuer",
					institution: "Institution",
					area: "Field of Study",
					studyType: "Degree",
					projectName: "Project Name",
					currentProject: "This is an ongoing project",
					description: "Description",
					highlights: "Highlights",
					organization: "Organization",
					organizationUrl: "Organization URL",
					currentVolunteer: "I currently volunteer here",
				},
				actions: {
					add: "Add",
					removeWorkExperience: "Remove Work Experience",
					descriptions: {
						publications: "Add your publications",
						certificates: "Add your certifications",
						projects: "Add your notable projects",
						volunteer: "Add your volunteer experience",
						education: "Add your education",
						awards: "Add your awards",
					},
					labels: {
						publication: "Publication #{number}",
						certificate: "Certificate #{number}",
						project: "Project #{number}",
						volunteer: "Volunteer #{number}",
						education: "Education #{number}",
						award: "Award #{number}",
					},
					empty: {
						publications: "No publications added yet",
						certificates: "No certificates added yet",
						projects: "No projects added yet",
						volunteer: "No volunteer experience added yet",
						highlights: "No highlights added yet",
						education: "No education added yet",
						awards: "No awards added yet",
					},
					addFirstPublication: "Add your first publication",
					addFirstCertificate: "Add your first certificate",
					addFirstProject: "Add your first project",
					addFirstVolunteer: "Add your first volunteer experience",
					addFirstEducation: "Add your first education",
					addFirstAward: "Add your first award",
				},
				buttons: {
					addPublication: "Add Publication",
					addCertificate: "Add Certificate",
					addProject: "Add Project",
					addVolunteer: "Add Volunteer",
					addHighlight: "Add Highlight",
					addEducation: "Add Education",
					addAward: "Add Award",
				},
				form: {
					generate: "Generate Resume",
				},
			},
		},
		es: {
			resume: {
				sections: {
					personalInfo: "Información Personal",
					workExperience: "Experiencia Laboral",
					education: "Educación",
					skills: "Habilidades",
					projects: "Proyectos",
					languages: "Idiomas",
				},
				placeholders: {
					fullName: "Nombre Completo",
					summary: "Resumen",
				},
				fields: {
					fullName: "Nombre Completo",
					email: "Correo Electrónico",
					phone: "Teléfono",
					location: "Ubicación",
					linkedin: "LinkedIn",
					github: "GitHub",
					website: "Sitio Web",
					summary: "Resumen",
					company: "Empresa",
					position: "Puesto",
					startDate: "Fecha de Inicio",
					endDate: "Fecha de Fin",
				},
				actions: {
					add: "Agregar",
					removeWorkExperience: "Eliminar Experiencia Laboral",
				},
				form: {
					generate: "Generar Currículum",
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
