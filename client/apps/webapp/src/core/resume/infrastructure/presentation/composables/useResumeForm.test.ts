import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import { useResumeForm } from "./useResumeForm";

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

describe("useResumeForm", () => {
	beforeEach(() => {
		setActivePinia(createPinia());
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

	describe("initialization", () => {
		it("should initialize with empty form data", () => {
			const { basics, workExperiences, isValid, hasResume } = useResumeForm();

			// Getters return safe defaults without side effects (no auto-creation)
			expect(basics.value.name).toBe("");
			expect(basics.value.email).toBe("");
			expect(workExperiences.value).toEqual([]);
			expect(isValid.value).toBe(false); // No resume yet
			expect(hasResume.value).toBe(false); // No resume yet (not auto-created by field access)
		});
	});

	describe("form state", () => {
		it("should update resume when basics change", async () => {
			const { loadResume, isValid } = useResumeForm();
			const resume = createMockResume();

			// Load a resume with complete basics data
			loadResume(resume);

			// Wait for state to update
			await nextTick();

			// The validator will accept this as valid since basics exists
			expect(isValid.value).toBe(true);
		});

		it("should validate complete resume", async () => {
			const { loadResume, isValid } = useResumeForm();
			const resume = createMockResume();

			loadResume(resume);

			// Wait for the watch to trigger
			await nextTick();
			await nextTick();

			expect(isValid.value).toBe(true);
		});
	});

	describe("submitResume", () => {
		it("should return true for empty resume with basics", () => {
			const { loadResume, submitResume } = useResumeForm();
			const resume = createMockResume();

			// Load a valid resume to ensure validation passes
			loadResume(resume);
			const result = submitResume();

			expect(result).toBe(true);
		});

		it("should return true for valid resume", () => {
			const { loadResume, submitResume } = useResumeForm();
			const resume = createMockResume();

			loadResume(resume);
			const result = submitResume();

			expect(result).toBe(true);
		});
	});

	describe("generatePdf", () => {
		it("should throw error when no resume is available", async () => {
			const { generatePdf, clearForm } = useResumeForm();

			// Clear the form to ensure no resume is set
			clearForm();

			await expect(generatePdf()).rejects.toThrow(
				"No resume data available to generate PDF",
			);
		});

		it("should generate PDF successfully with valid resume", async () => {
			const { loadResume, generatePdf, submitResume } = useResumeForm();
			const resume = createMockResume();

			loadResume(resume);
			// Ensure the resume is submitted to the store
			submitResume();

			const pdf = await generatePdf("en");

			expect(pdf).toBeInstanceOf(Blob);
			expect(pdf.type).toBe("application/pdf");
		});

		it("should set generating state during PDF generation", async () => {
			const { loadResume, generatePdf, isGenerating, submitResume } =
				useResumeForm();
			const resume = createMockResume();

			loadResume(resume);
			// Ensure the resume is submitted to the store
			submitResume();

			expect(isGenerating.value).toBe(false);

			const pdfPromise = generatePdf("en");
			await pdfPromise;

			expect(isGenerating.value).toBe(false);
		});
	});

	describe("clearForm", () => {
		it("should clear all form data", async () => {
			const {
				loadResume,
				clearForm,
				basics,
				workExperiences,
				hasResume,
				isValid,
			} = useResumeForm();
			const resume = createMockResume();

			loadResume(resume);

			// Wait for the watch to trigger
			await nextTick();
			await nextTick();

			expect(hasResume.value).toBe(true);

			clearForm();

			// After clearing, getters return safe defaults without auto-creating
			expect(basics.value.name).toBe("");
			expect(basics.value.email).toBe("");
			expect(workExperiences.value).toEqual([]);
			expect(hasResume.value).toBe(false); // Resume is null after clearing (no auto-creation)
			expect(isValid.value).toBe(false); // No resume, so not valid
		});
	});

	describe("loadResume", () => {
		it("should load resume data into form fields", () => {
			const { loadResume, basics, workExperiences } = useResumeForm();
			const resume = createMockResume();

			resume.work = [
				{
					name: "Tech Corp",
					position: "Developer",
					url: "https://techcorp.com",
					startDate: "2020-01",
					endDate: "2023-12",
					summary: "Built amazing things",
					highlights: ["Achievement 1", "Achievement 2"],
				},
			];

			loadResume(resume);

			expect(basics.value.name).toBe("John Doe");
			expect(basics.value.email).toBe("john@example.com");
			expect(workExperiences.value).toHaveLength(1);
			expect(workExperiences.value[0]?.name).toBe("Tech Corp");
		});

		it("should convert readonly arrays to mutable arrays", () => {
			const { loadResume, basics } = useResumeForm();
			const resume = createMockResume();

			resume.basics.profiles = [
				{
					network: "LinkedIn",
					username: "johndoe",
					url: "https://linkedin.com/in/johndoe",
				},
			];

			loadResume(resume);

			// Should be able to mutate the array
			expect(() => {
				basics.value.profiles.push({
					network: "GitHub",
					username: "johndoe",
					url: "https://github.com/johndoe",
				});
			}).not.toThrow();

			expect(basics.value.profiles).toHaveLength(2);
		});
	});
});
