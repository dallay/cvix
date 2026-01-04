import { describe, expect, it } from "vitest";
import { createTestResume } from "@/core/resume/test-resume-factory.ts";
import type { Resume } from "../domain/Resume";
import type {
	ArraySectionVisibility,
	SectionVisibility,
} from "../domain/SectionVisibility";
import { createDefaultVisibility as createDomainVisibility } from "../domain/SectionVisibility";
import { ResumeSectionFilterService } from "./ResumeSectionFilterService";

describe("ResumeSectionFilterService", () => {
	const service = new ResumeSectionFilterService();

	describe("filterResume", () => {
		const resumeId = "b25582a9-cfe5-4d63-8d64-83b90b24d777";
		it("should return unmodified resume when all sections and items are visible", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);

			const filtered = service.filterResume(resume, visibility);

			expect(filtered).toEqual(resume);
		});

		it("should preserve name (always visible)", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.name).toBe("John Doe");
		});

		it("should filter out hidden personal details fields", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.personalDetails.fields.email = false;
			visibility.personalDetails.fields.phone = false;
			visibility.personalDetails.fields.image = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.name).toBe("John Doe"); // Always visible
			expect(filtered.basics.email).toBe("");
			expect(filtered.basics.phone).toBe("");
			expect(filtered.basics.image).toBe("");
			expect(filtered.basics.url).toBe("https://johndoe.com"); // Still visible
		});

		it("should filter out hidden location fields", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.personalDetails.fields.location.city = false;
			visibility.personalDetails.fields.location.postalCode = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.location).not.toBeNull();
			expect(filtered.basics.location?.address).toBe("123 Main St");
			expect(filtered.basics.location?.city).toBe("");
			expect(filtered.basics.location?.postalCode).toBe("");
			expect(filtered.basics.location?.countryCode).toBe("US");
		});

		it("should filter out hidden profiles", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.personalDetails.fields.profiles.LinkedIn = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.profiles).toHaveLength(1);
			expect(filtered.basics.profiles[0]?.network).toBe("GitHub");
		});

		it("should return empty array when section is disabled", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.work.enabled = false;
			visibility.education.enabled = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toEqual([]);
			expect(filtered.education).toEqual([]);
		});

		it("should filter out hidden items in work section", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.work.items[1] = false; // Hide second work item

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toHaveLength(1);
			expect(filtered.work[0]?.name).toBe("Company A");
		});

		it("should filter out hidden items in education section", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.education.items[0] = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.education).toEqual([]);
		});

		it("should filter out hidden items in skills section", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.skills.items[0] = false; // Hide JavaScript
			visibility.skills.items[2] = false; // Hide Python

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.skills).toHaveLength(1);
			expect(filtered.skills[0]?.name).toBe("TypeScript");
		});

		it("should handle multiple sections disabled", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.projects.enabled = false;
			visibility.certificates.enabled = false;
			visibility.volunteer.enabled = false;
			visibility.awards.enabled = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.projects).toEqual([]);
			expect(filtered.certificates).toEqual([]);
			expect(filtered.volunteer).toEqual([]);
			expect(filtered.awards).toEqual([]);
		});

		it("should preserve enabled sections when others are disabled", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.work.enabled = false;
			visibility.skills.enabled = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toEqual([]);
			expect(filtered.skills).toEqual([]);
			expect(filtered.education).toHaveLength(1);
			expect(filtered.projects).toHaveLength(1);
		});

		it("should handle mixed visibility states", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.work.items[0] = false; // Hide first work item
			visibility.skills.enabled = false; // Disable entire skills section
			visibility.personalDetails.fields.email = false; // Hide email

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toHaveLength(1);
			expect(filtered.work[0]?.name).toBe("Company B");
			expect(filtered.skills).toEqual([]);
			expect(filtered.basics.email).toBe("");
		});

		it("should treat undefined visibility items as visible (default behavior)", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			// Remove some items from visibility to test undefined handling
			visibility.work.items = [true]; // Only first item defined

			const filtered = service.filterResume(resume, visibility);

			// Both work items should be visible (undefined !== false)
			expect(filtered.work).toHaveLength(2);
		});

		it("should filter all array sections independently", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility(resumeId, resume);
			visibility.publications.items[0] = false;
			visibility.languages.items[0] = false;
			visibility.interests.items[0] = false;
			visibility.references.items[0] = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.publications).toEqual([]);
			expect(filtered.languages).toEqual([]);
			expect(filtered.interests).toEqual([]);
			expect(filtered.references).toEqual([]);
		});
	});

	describe("countVisibleItems", () => {
		it("should return 0 when section is disabled", () => {
			const visibility: ArraySectionVisibility = {
				enabled: false,
				expanded: false,
				items: [true, true, true],
			};

			expect(service.countVisibleItems(visibility)).toBe(0);
		});

		it("should count visible items correctly", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [true, false, true, false, true],
			};

			expect(service.countVisibleItems(visibility)).toBe(3);
		});

		it("should return total count when all items are visible", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [true, true, true],
			};

			expect(service.countVisibleItems(visibility)).toBe(3);
		});

		it("should return 0 when all items are hidden", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [false, false, false],
			};

			expect(service.countVisibleItems(visibility)).toBe(0);
		});

		it("should handle empty items array", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [],
			};

			expect(service.countVisibleItems(visibility)).toBe(0);
		});
	});

	describe("edge cases", () => {
		it("should handle resume with empty arrays", () => {
			const resume: Resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "",
						region: "",
					},
					profiles: [],
				},
				work: [],
				education: [],
				skills: [],
				projects: [],
				certificates: [],
				volunteer: [],
				awards: [],
				publications: [],
				languages: [],
				interests: [],
				references: [],
			};

			const visibility: SectionVisibility = {
				resumeId: "empty-resume",
				personalDetails: {
					enabled: true,
					expanded: false,
					fields: {
						image: true,
						email: true,
						phone: true,
						location: {
							address: true,
							postalCode: true,
							city: true,
							countryCode: true,
							region: true,
						},
						summary: true,
						url: true,
						profiles: {},
					},
				},
				work: { enabled: true, expanded: false, items: [] },
				education: { enabled: true, expanded: false, items: [] },
				skills: { enabled: true, expanded: false, items: [] },
				projects: { enabled: true, expanded: false, items: [] },
				certificates: { enabled: true, expanded: false, items: [] },
				volunteer: { enabled: true, expanded: false, items: [] },
				awards: { enabled: true, expanded: false, items: [] },
				publications: { enabled: true, expanded: false, items: [] },
				languages: { enabled: true, expanded: false, items: [] },
				interests: { enabled: true, expanded: false, items: [] },
				references: { enabled: true, expanded: false, items: [] },
			};

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.name).toBe("Test User");
			expect(filtered.work).toEqual([]);
			expect(filtered.education).toEqual([]);
		});

		it("should handle all profiles hidden", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("resume-123", resume);
			visibility.personalDetails.fields.profiles.GitHub = false;
			visibility.personalDetails.fields.profiles.LinkedIn = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.profiles).toEqual([]);
		});
	});
});
