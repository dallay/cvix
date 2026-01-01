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

			expect(filtered.basics.location.address).toBe("123 Main St");
			expect(filtered.basics.location.city).toBe("");
			expect(filtered.basics.location.postalCode).toBe("");
			expect(filtered.basics.location.countryCode).toBe("US");
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

	describe("filterArray performance optimization", () => {
		it("should return a new array when all items are visible (optimization path)", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// All items are visible by default

			const filtered = service.filterResume(resume, visibility);

			// Should return new arrays (not the same reference)
			expect(filtered.work).not.toBe(resume.work);
			expect(filtered.education).not.toBe(resume.education);
			expect(filtered.skills).not.toBe(resume.skills);
			// But content should be equal
			expect(filtered.work).toEqual(resume.work);
			expect(filtered.education).toEqual(resume.education);
			expect(filtered.skills).toEqual(resume.skills);
		});

		it("should use optimization path when all visibility items are true", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Explicitly set all items to true
			visibility.work.items = [true, true];
			visibility.education.items = [true];
			visibility.skills.items = [true, true, true];

			const filtered = service.filterResume(resume, visibility);

			// All items should be preserved
			expect(filtered.work).toHaveLength(2);
			expect(filtered.education).toHaveLength(1);
			expect(filtered.skills).toHaveLength(3);
			// Arrays should be new references (shallow copies)
			expect(filtered.work).not.toBe(resume.work);
		});

		it("should use optimization path when visibility items are undefined", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Set items to undefined (no visibility preferences set)
			visibility.work.items = [];
			visibility.education.items = [];

			const filtered = service.filterResume(resume, visibility);

			// Undefined items should be treated as visible
			expect(filtered.work).toHaveLength(2);
			expect(filtered.education).toHaveLength(1);
		});

		it("should use filtering path when at least one item is false", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			visibility.work.items = [true, false]; // One item hidden

			const filtered = service.filterResume(resume, visibility);

			// Should filter out the hidden item
			expect(filtered.work).toHaveLength(1);
			expect(filtered.work[0]?.name).toBe("Company A");
		});

		it("should use filtering path when items have mixed true/false/undefined", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			visibility.work.items = [true, false]; // Mixed visibility
			visibility.skills.items = [true, undefined as any, false]; // Mixed with undefined

			const filtered = service.filterResume(resume, visibility);

			// Work: second item hidden
			expect(filtered.work).toHaveLength(1);
			expect(filtered.work[0]?.name).toBe("Company A");
			// Skills: third item hidden, undefined treated as visible
			expect(filtered.skills).toHaveLength(2);
			expect(filtered.skills[0]?.name).toBe("JavaScript");
			expect(filtered.skills[1]?.name).toBe("TypeScript");
		});

		it("should return empty array when section is disabled (short-circuit)", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			visibility.work.enabled = false;

			const filtered = service.filterResume(resume, visibility);

			// Should short-circuit before checking visibility.items
			expect(filtered.work).toEqual([]);
		});

		it("should handle empty items array with optimization", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			visibility.work.items = []; // No visibility preferences

			const filtered = service.filterResume(resume, visibility);

			// Should use optimization path (no false values found)
			expect(filtered.work).toHaveLength(2);
			expect(filtered.work).not.toBe(resume.work);
		});

		it("should preserve array immutability in optimization path", () => {
			const resume = createTestResume();
			const originalWork = resume.work;
			const visibility = createDomainVisibility("test-resume", resume);

			const filtered = service.filterResume(resume, visibility);

			// Original should be unchanged
			expect(resume.work).toBe(originalWork);
			expect(resume.work).toEqual(originalWork);
			// Filtered should be a new array
			expect(filtered.work).not.toBe(originalWork);
			expect(filtered.work).toEqual(originalWork);
		});

		it("should handle all sections with optimization when no items are hidden", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);

			const filtered = service.filterResume(resume, visibility);

			// All sections should use optimization path
			expect(filtered.work).not.toBe(resume.work);
			expect(filtered.education).not.toBe(resume.education);
			expect(filtered.skills).not.toBe(resume.skills);
			expect(filtered.projects).not.toBe(resume.projects);
			expect(filtered.certificates).not.toBe(resume.certificates);
			expect(filtered.volunteer).not.toBe(resume.volunteer);
			expect(filtered.awards).not.toBe(resume.awards);
			expect(filtered.publications).not.toBe(resume.publications);
			expect(filtered.languages).not.toBe(resume.languages);
			expect(filtered.interests).not.toBe(resume.interests);
			expect(filtered.references).not.toBe(resume.references);
			// But all should have same content
			expect(filtered.work).toEqual(resume.work);
			expect(filtered.education).toEqual(resume.education);
		});

		it("should correctly detect hidden items with explicit false values", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			visibility.skills.items = [true, true, false]; // Last item explicitly false

			const filtered = service.filterResume(resume, visibility);

			// Should use filtering path, not optimization
			expect(filtered.skills).toHaveLength(2);
			expect(filtered.skills[0]?.name).toBe("JavaScript");
			expect(filtered.skills[1]?.name).toBe("TypeScript");
			// Python should be filtered out
			expect(filtered.skills.find((s) => s.name === "Python")).toBeUndefined();
		});

		it("should handle visibility with only undefined values (new items)", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Simulate newly added items without visibility preferences
			visibility.work.items = [undefined, undefined];

			const filtered = service.filterResume(resume, visibility);

			// Should use optimization path (no false values)
			expect(filtered.work).toHaveLength(2);
			expect(filtered.work).toEqual(resume.work);
		});

		it("should handle large arrays efficiently with optimization", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Simulate a large array with all items visible
			const largeArray = Array(1000).fill(true);
			visibility.work.items = largeArray;

			// Add corresponding work items to resume (for realistic test)
			const largeWorkArray = Array(1000)
				.fill(null)
				.map((_, i) => ({
					name: `Company ${i}`,
					position: `Position ${i}`,
					url: "",
					startDate: "2020-01-01",
					endDate: "2021-01-01",
					summary: "",
					highlights: [],
				}));
			const modifiedResume = { ...resume, work: largeWorkArray };

			const filtered = service.filterResume(modifiedResume, visibility);

			// Should use optimization path (all true, no filtering needed)
			expect(filtered.work).toHaveLength(1000);
			expect(filtered.work).not.toBe(modifiedResume.work);
		});

		it("should handle sections with zero-length arrays", () => {
			const resume: Resume = {
				...createTestResume(),
				work: [],
				education: [],
			};
			const visibility = createDomainVisibility("test-resume", resume);

			const filtered = service.filterResume(resume, visibility);

			// Empty arrays should remain empty
			expect(filtered.work).toEqual([]);
			expect(filtered.education).toEqual([]);
			// But should be new array references
			expect(filtered.work).not.toBe(resume.work);
		});

		it("should treat null and undefined differently from false", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Mix of null, undefined, true - no explicit false
			visibility.work.items = [true, null as any, undefined];

			const filtered = service.filterResume(resume, visibility);

			// Should use optimization path (no false values)
			// null and undefined are not === false
			expect(filtered.work).toHaveLength(2);
		});

		it("should handle Boolean false vs falsy values correctly", () => {
			const resume = createTestResume();
			const visibility = createDomainVisibility("test-resume", resume);
			// Only boolean false should trigger filtering
			visibility.work.items = [true, false]; // Explicit false
			visibility.skills.items = [true, 0 as any, "" as any]; // Falsy but not false

			const filtered = service.filterResume(resume, visibility);

			// Work should filter (has explicit false)
			expect(filtered.work).toHaveLength(1);
			// Skills should use optimization (falsy values !== false)
			expect(filtered.skills).toHaveLength(3);
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