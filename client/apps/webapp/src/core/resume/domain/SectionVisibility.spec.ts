import { describe, expect, it } from "vitest";

import type { Resume } from "./Resume";
import {
	type ArraySectionVisibility,
	countVisibleItems,
	createDefaultVisibility,
	hasVisibleItems,
	SECTION_TYPES,
} from "./SectionVisibility";

describe("SectionVisibility Domain", () => {
	// Helper to create a minimal resume for testing
	const createMinimalResume = (): Resume => ({
		basics: {
			name: "Test User",
			label: "",
			image: "",
			email: "test@example.com",
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
			profiles: [
				{
					network: "GitHub",
					username: "testuser",
					url: "https://github.com/testuser",
				},
				{
					network: "LinkedIn",
					username: "testuser",
					url: "https://linkedin.com/in/testuser",
				},
			],
		},
		work: [
			{
				name: "Company A",
				position: "Developer",
				url: "",
				startDate: "2020-01-01",
				endDate: "2021-01-01",
				summary: "",
				highlights: [],
			},
			{
				name: "Company B",
				position: "Senior Developer",
				url: "",
				startDate: "2021-01-01",
				endDate: "",
				summary: "",
				highlights: [],
			},
		],
		education: [
			{
				institution: "University A",
				url: "",
				area: "Computer Science",
				studyType: "Bachelor",
				startDate: "2015-09-01",
				endDate: "2019-06-01",
				score: "",
				courses: [],
			},
		],
		skills: [
			{ name: "JavaScript", level: "Expert", keywords: [] },
			{ name: "TypeScript", level: "Advanced", keywords: [] },
		],
		projects: [],
		certificates: [],
		volunteer: [],
		awards: [],
		publications: [],
		languages: [],
		interests: [],
		references: [],
	});

	describe("SECTION_TYPES", () => {
		it("should define sections in correct order matching backend template", () => {
			expect(SECTION_TYPES).toEqual([
				"personalDetails",
				"work",
				"education",
				"skills",
				"projects",
				"certificates",
				"volunteer",
				"awards",
				"publications",
				"languages",
				"interests",
				"references",
			]);
		});

		it("should expose 12 typed section types; runtime array is not frozen", () => {
			expect(Object.isFrozen(SECTION_TYPES)).toBe(false); // 'as const' is TypeScript-only; runtime array is mutable
			expect(SECTION_TYPES.length).toBe(12);
		});
	});

	describe("createDefaultVisibility", () => {
		it("should create visibility with correct resumeId", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			expect(visibility.resumeId).toBe("resume-123");
		});

		it("should enable Personal Details by default", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			expect(visibility.personalDetails.enabled).toBe(true);
			expect(visibility.personalDetails.expanded).toBe(false);
		});

		it("should enable all personal details fields by default", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			const fields = visibility.personalDetails.fields;
			expect(fields.image).toBe(true);
			expect(fields.email).toBe(true);
			expect(fields.phone).toBe(true);
			expect(fields.url).toBe(true);
			expect(fields.summary).toBe(true);
			expect(fields.location.address).toBe(true);
			expect(fields.location.postalCode).toBe(true);
			expect(fields.location.city).toBe(true);
			expect(fields.location.countryCode).toBe(true);
			expect(fields.location.region).toBe(true);
		});

		it("should enable all profiles present in resume", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			expect(visibility.personalDetails.fields.profiles).toEqual({
				GitHub: true,
				LinkedIn: true,
			});
		});

		it("should enable sections with data and disable empty sections", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			// Sections with data should be enabled
			expect(visibility.work.enabled).toBe(true);
			expect(visibility.education.enabled).toBe(true);
			expect(visibility.skills.enabled).toBe(true);

			// Empty sections should be disabled
			expect(visibility.projects.enabled).toBe(false);
			expect(visibility.certificates.enabled).toBe(false);
			expect(visibility.volunteer.enabled).toBe(false);
			expect(visibility.awards.enabled).toBe(false);
			expect(visibility.publications.enabled).toBe(false);
			expect(visibility.languages.enabled).toBe(false);
			expect(visibility.interests.enabled).toBe(false);
			expect(visibility.references.enabled).toBe(false);
		});

		it("should create visibility for all items in array sections", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			// Work has 2 items
			expect(visibility.work.items).toEqual([true, true]);

			// Education has 1 item
			expect(visibility.education.items).toEqual([true]);

			// Skills has 2 items
			expect(visibility.skills.items).toEqual([true, true]);

			// Empty sections should have empty items array
			expect(visibility.projects.items).toEqual([]);
		});

		it("should not expand any sections by default", () => {
			const resume = createMinimalResume();
			const visibility = createDefaultVisibility("resume-123", resume);

			expect(visibility.personalDetails.expanded).toBe(false);
			expect(visibility.work.expanded).toBe(false);
			expect(visibility.education.expanded).toBe(false);
			expect(visibility.skills.expanded).toBe(false);
		});
	});

	describe("countVisibleItems", () => {
		it("should return 0 when section is disabled", () => {
			const visibility: ArraySectionVisibility = {
				enabled: false,
				expanded: false,
				items: [true, true, true],
			};

			expect(countVisibleItems(visibility)).toBe(0);
		});

		it("should count visible items when section is enabled", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [true, false, true, false, true],
			};

			expect(countVisibleItems(visibility)).toBe(3);
		});

		it("should return 0 when all items are hidden", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [false, false, false],
			};

			expect(countVisibleItems(visibility)).toBe(0);
		});

		it("should return total count when all items are visible", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [true, true, true, true],
			};

			expect(countVisibleItems(visibility)).toBe(4);
		});

		it("should handle empty items array", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [],
			};

			expect(countVisibleItems(visibility)).toBe(0);
		});
	});

	describe("hasVisibleItems", () => {
		it("should return false when section is disabled", () => {
			const visibility: ArraySectionVisibility = {
				enabled: false,
				expanded: false,
				items: [true, true],
			};

			expect(hasVisibleItems(visibility)).toBe(false);
		});

		it("should return true when at least one item is visible", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [false, false, true],
			};

			expect(hasVisibleItems(visibility)).toBe(true);
		});

		it("should return false when all items are hidden", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [false, false, false],
			};

			expect(hasVisibleItems(visibility)).toBe(false);
		});

		it("should return false for empty items array", () => {
			const visibility: ArraySectionVisibility = {
				enabled: true,
				expanded: false,
				items: [],
			};

			expect(hasVisibleItems(visibility)).toBe(false);
		});
	});
});
