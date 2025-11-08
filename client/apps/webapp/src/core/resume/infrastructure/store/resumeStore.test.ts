import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import { useResumeStore } from "./resumeStore.ts";

describe("useResumeStore", () => {
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

	describe("with default validator", () => {
		it("should initialize with null resume", () => {
			const store = useResumeStore();

			expect(store.resume).toBeNull();
			expect(store.hasResume).toBe(false);
			expect(store.isValid).toBe(false);
		});

		it("should set and validate a valid resume", () => {
			const store = useResumeStore();
			const validResume = createMockResume();

			store.setResume(validResume);

			expect(store.resume).toEqual(validResume);
			expect(store.hasResume).toBe(true);
			expect(store.isValid).toBe(true);
		});

		it("should invalidate resume with invalid email", () => {
			const store = useResumeStore();
			const invalidResume = createMockResume();
			invalidResume.basics.email = "invalid-email";

			store.setResume(invalidResume);

			expect(store.hasResume).toBe(true);
			expect(store.isValid).toBe(false);
		});

		it("should clear resume", () => {
			const store = useResumeStore();
			const validResume = createMockResume();

			store.setResume(validResume);
			expect(store.hasResume).toBe(true);

			store.clearResume();
			expect(store.resume).toBeNull();
			expect(store.hasResume).toBe(false);
			expect(store.isValid).toBe(false);
		});

		it("should validate resume explicitly", () => {
			const store = useResumeStore();
			const validResume = createMockResume();

			store.setResume(validResume);

			const isValid = store.validateResume();
			expect(isValid).toBe(true);
		});

		it("should return false when validating null resume", () => {
			const store = useResumeStore();

			const isValid = store.validateResume();
			expect(isValid).toBe(false);
		});
	});

	describe("with custom validator (dependency injection)", () => {
		it("should use default validator when no injection is provided", () => {
			const store = useResumeStore();
			const resume = createMockResume();

			store.setResume(resume);

			// The default JsonResumeValidator should validate this correctly
			expect(store.isValid).toBe(true);
		});

		it("should validate complex resume with default validator", () => {
			const store = useResumeStore();
			const resume = createMockResume();

			// Add more complex data
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

			store.setResume(resume);

			expect(store.isValid).toBe(true);
		});
	});

	describe("generation state", () => {
		it("should manage generating state", () => {
			const store = useResumeStore();

			expect(store.isGenerating).toBe(false);

			store.setGenerating(true);
			expect(store.isGenerating).toBe(true);

			store.setGenerating(false);
			expect(store.isGenerating).toBe(false);
		});

		it("should manage generation errors", () => {
			const store = useResumeStore();

			expect(store.generationError).toBeNull();

			const error = {
				type: "validation_error",
				title: "Validation Failed",
				status: 400,
				detail: "Resume validation failed",
			};

			store.setGenerationError(error);
			expect(store.generationError).toEqual(error);

			store.setGenerationError(null);
			expect(store.generationError).toBeNull();
		});

		it("should clear error when clearing resume", () => {
			const store = useResumeStore();
			const resume = createMockResume();

			const error = {
				type: "validation_error",
				title: "Validation Failed",
				status: 400,
				detail: "Resume validation failed",
			};

			store.setResume(resume);
			store.setGenerationError(error);

			expect(store.generationError).toEqual(error);

			store.clearResume();
			expect(store.generationError).toBeNull();
		});
	});
});
