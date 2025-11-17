import { flushPromises, mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createI18n } from "vue-i18n";
import { toast } from "vue-sonner";
import type { Resume } from "@/core/resume/domain/Resume";
import { useResumeStore } from "@/core/resume/infrastructure/store/resume.store.ts";
import ResumeForm from "./ResumeForm.vue";

// Mock vue-sonner
vi.mock("vue-sonner", () => ({
	toast: {
		error: vi.fn(),
		success: vi.fn(),
	},
}));

// Mock the ResumeHttpClient
vi.mock("@/core/resume/infrastructure/http/ResumeHttpClient", () => {
	const MockResumeHttpClient = class {
		async generatePdf() {
			return new Blob(["fake pdf"], { type: "application/pdf" });
		}
	};

	return {
		ResumeHttpClient: MockResumeHttpClient,
	};
});

// Mock window.confirm
globalThis.confirm = vi.fn(() => true);

// Mock URL.createObjectURL and revokeObjectURL
globalThis.URL.createObjectURL = vi.fn(() => "blob:mock-url");
globalThis.URL.revokeObjectURL = vi.fn();

// Create a minimal i18n instance for testing
const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				form: {
					submit: "Submit",
					saving: "Saving...",
					cancel: "Cancel",
					generatePdf: "Generate PDF",
					generating: "Generating...",
					confirmClear: "Are you sure you want to clear all form data?",
				},
				toast: {
					validationError: {
						title: "Validation Error",
						description: "Please check all required fields.",
					},
					saveSuccess: {
						title: "Success",
						description: "Resume saved successfully!",
					},
					saveError: {
						title: "Error",
						description: "Failed to save resume.",
					},
					pdfSuccess: {
						title: "PDF Generated",
						description: "Your resume has been generated successfully!",
					},
					pdfError: {
						title: "Generation Failed",
						description: "Failed to generate PDF.",
					},
					formCleared: {
						title: "Form Cleared",
						description: "All form data has been reset.",
					},
				},
			},
		},
		es: {
			resume: {
				form: {
					submit: "Enviar",
					saving: "Guardando...",
					cancel: "Cancelar",
					generatePdf: "Generar PDF",
					generating: "Generando...",
					confirmClear: "¿Estás seguro de que quieres borrar todos los datos?",
				},
				toast: {
					validationError: {
						title: "Error de Validación",
						description: "Por favor, verifica todos los campos requeridos.",
					},
					saveSuccess: {
						title: "Éxito",
						description: "¡Currículum guardado exitosamente!",
					},
					saveError: {
						title: "Error",
						description: "Error al guardar el currículum.",
					},
					pdfSuccess: {
						title: "PDF Generado",
						description: "¡Tu currículum ha sido generado exitosamente!",
					},
					pdfError: {
						title: "Generación Fallida",
						description: "Error al generar el PDF.",
					},
					formCleared: {
						title: "Formulario Limpiado",
						description: "Todos los datos han sido reiniciados.",
					},
				},
			},
		},
	},
});

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

	const mountComponent = () => {
		return mount(ResumeForm, {
			global: {
				plugins: [i18n],
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
				},
			},
		});
	};

	describe("rendering", () => {
		it("should render the form with submit, generate PDF, and cancel buttons", () => {
			const wrapper = mountComponent();

			const buttons = wrapper.findAll("button");
			expect(buttons.length).toBeGreaterThanOrEqual(3);

			const submitButton = wrapper.find('button[type="submit"]');
			expect(submitButton.exists()).toBe(true);
			expect(submitButton.text()).toBe("Submit");
		});
	});

	describe("form submission", () => {
		it("should show validation error when submitting invalid data", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Mock validation to return false
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(false);

			const form = wrapper.find("form");
			await form.trigger("submit");
			await flushPromises();

			expect(toast.error).toHaveBeenCalledWith(
				"Validation Error",
				expect.objectContaining({
					description: "Please check all required fields.",
				}),
			);
		});

		it("should save to storage and show success message on valid submission", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Set valid resume data
			resumeStore.setResume(createMockResume());

			// Mock validation to return true
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(true);

			// Mock saveToStorage
			const saveToStorageSpy = vi
				.spyOn(resumeStore, "saveToStorage")
				.mockResolvedValue();

			const form = wrapper.find("form");
			await form.trigger("submit");
			await flushPromises();

			expect(saveToStorageSpy).toHaveBeenCalled();
			expect(toast.success).toHaveBeenCalledWith(
				"Success",
				expect.objectContaining({
					description: "Resume saved successfully!",
				}),
			);
		});

		it("should show error message when storage fails", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Set valid resume data
			resumeStore.setResume(createMockResume());

			// Mock validation to return true
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(true);

			// Mock saveToStorage to fail
			vi.spyOn(resumeStore, "saveToStorage").mockRejectedValue(
				new Error("Storage error"),
			);
			const form = wrapper.find("form");
			await form.trigger("submit");
			await flushPromises();

			expect(toast.error).toHaveBeenCalledWith(
				"Error",
				expect.objectContaining({
					description: "Failed to save resume.",
				}),
			);
		});
	});

	describe("PDF generation", () => {
		it("should generate PDF and show success message", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Set valid resume data
			resumeStore.setResume(createMockResume());

			// Find and click the generate PDF button
			const generateButton = wrapper
				.findAll("button")
				.find((btn) => btn.text().includes("Generate PDF"));
			expect(generateButton).toBeTruthy();

			await generateButton?.trigger("click");
			await flushPromises();

			expect(toast.success).toHaveBeenCalledWith(
				"PDF Generated",
				expect.objectContaining({
					description: "Your resume has been generated successfully!",
				}),
			);
		});

		it("should show error message when PDF generation fails", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Mock generatePdf to fail
			vi.spyOn(resumeStore, "generatePdf").mockRejectedValue(
				new Error("Generation error"),
			);

			const generateButton = wrapper
				.findAll("button")
				.find((btn) => btn.text().includes("Generate PDF"));

			await generateButton?.trigger("click");
			await flushPromises();

			expect(toast.error).toHaveBeenCalledWith(
				"Generation Failed",
				expect.objectContaining({
					description: "Failed to generate PDF.",
				}),
			);
		});
	});

	describe("form cancellation", () => {
		it("should clear form when cancel is confirmed", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Set some data
			resumeStore.setResume(createMockResume());

			const cancelButton = wrapper
				.findAll("button")
				.find((btn) => btn.text() === "Cancel");
			expect(cancelButton).toBeTruthy();

			await cancelButton?.trigger("click");
			await flushPromises();

			expect(globalThis.confirm).toHaveBeenCalledWith(
				"Are you sure you want to clear all form data?",
			);
			expect(toast.success).toHaveBeenCalledWith(
				"Form Cleared",
				expect.objectContaining({
					description: "All form data has been reset.",
				}),
			);
		});

		it("should not clear form when cancel is not confirmed", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Mock confirm to return false
			vi.mocked(globalThis.confirm).mockReturnValue(false);

			// Set some data
			resumeStore.setResume(createMockResume());

			const cancelButton = wrapper
				.findAll("button")
				.find((btn) => btn.text() === "Cancel");

			await cancelButton?.trigger("click");
			await flushPromises();

			// Should not show success toast
			expect(toast.success).not.toHaveBeenCalled();
		});
	});

	describe("internationalization", () => {
		it("should render buttons in English by default", () => {
			const wrapper = mountComponent();

			const submitButton = wrapper.find('button[type="submit"]');
			expect(submitButton.text()).toBe("Submit");

			const cancelButton = wrapper
				.findAll("button")
				.find((btn) => btn.text() === "Cancel");
			expect(cancelButton?.text()).toBe("Cancel");
		});

		it("should render buttons in Spanish when locale is changed", async () => {
			i18n.global.locale.value = "es";
			const wrapper = mountComponent();

			const submitButton = wrapper.find('button[type="submit"]');
			expect(submitButton.text()).toBe("Enviar");

			const cancelButton = wrapper
				.findAll("button")
				.find((btn) => btn.text() === "Cancelar");
			expect(cancelButton?.text()).toBe("Cancelar");

			// Reset locale
			i18n.global.locale.value = "en";
		});

		it("should show Spanish error messages when locale is es", async () => {
			i18n.global.locale.value = "es";
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			// Mock validation to return false
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(false);

			const form = wrapper.find("form");
			await form.trigger("submit");
			await flushPromises();

			expect(toast.error).toHaveBeenCalledWith(
				"Error de Validación",
				expect.objectContaining({
					description: "Por favor, verifica todos los campos requeridos.",
				}),
			);

			// Reset locale
			i18n.global.locale.value = "en";
		});
	});

	describe("button states", () => {
		it("should disable submit button when submitting", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			resumeStore.setResume(createMockResume());

			// Mock validation to return true
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(true);

			// Create a promise that we control
			let resolveStorage: (() => void) | undefined;
			const storagePromise = new Promise<void>((resolve) => {
				resolveStorage = resolve;
			});

			vi.spyOn(resumeStore, "saveToStorage").mockReturnValue(storagePromise);

			const form = wrapper.find("form");
			await form.trigger("submit");

			// Wait for the component to update
			await wrapper.vm.$nextTick();
			await wrapper.vm.$nextTick();

			const submitButton = wrapper.find('button[type="submit"]');
			// Button should be disabled while submitting
			expect(submitButton.attributes("disabled")).toBeDefined();

			// Resolve the storage operation
			if (resolveStorage) {
				resolveStorage();
			}
			await flushPromises();
		});

		it("should show 'Saving...' text while submitting", async () => {
			const wrapper = mountComponent();
			const resumeStore = useResumeStore();

			resumeStore.setResume(createMockResume());

			// Mock validation to return true
			vi.spyOn(resumeStore, "validateResume").mockReturnValue(true);

			let resolveStorage: (() => void) | undefined;
			const storagePromise = new Promise<void>((resolve) => {
				resolveStorage = resolve;
			});

			vi.spyOn(resumeStore, "saveToStorage").mockReturnValue(storagePromise);

			const form = wrapper.find("form");
			await form.trigger("submit");

			// Wait for the component to update
			await wrapper.vm.$nextTick();
			await wrapper.vm.$nextTick();

			const submitButton = wrapper.find('button[type="submit"]');
			// Button text should change to "Saving..."
			expect(submitButton.text()).toBe("Saving...");

			if (resolveStorage) {
				resolveStorage();
			}
			await flushPromises();
		});
	});
});
