import { describe, expect, it } from "vitest";
import type { Resume } from "../domain/Resume";
import type {
	ArraySectionVisibility,
	SectionVisibility,
} from "../domain/SectionVisibility";
import { ResumeSectionFilterService } from "./ResumeSectionFilterService";

describe("ResumeSectionFilterService", () => {
	const service = new ResumeSectionFilterService();

	// Helper to create a complete test resume
	const createTestResume = (): Resume => ({
		basics: {
			name: "John Doe",
			label: "Software Engineer",
			image: "profile.jpg",
			email: "john@example.com",
			phone: "+1-555-0100",
			url: "https://johndoe.com",
			summary: "Experienced software engineer",
			location: {
				address: "123 Main St",
				postalCode: "12345",
				city: "San Francisco",
				countryCode: "US",
				region: "CA",
			},
			profiles: [
				{
					network: "GitHub",
					username: "johndoe",
					url: "https://github.com/johndoe",
				},
				{
					network: "LinkedIn",
					username: "johndoe",
					url: "https://linkedin.com/in/johndoe",
				},
			],
		},
		work: [
			{
				name: "Company A",
				position: "Senior Developer",
				url: "https://companya.com",
				startDate: "2020-01-01",
				endDate: "2022-12-31",
				summary: "Led development team",
				highlights: ["Achievement 1", "Achievement 2"],
			},
			{
				name: "Company B",
				position: "Developer",
				url: "https://companyb.com",
				startDate: "2018-01-01",
				endDate: "2019-12-31",
				summary: "Developed features",
				highlights: [],
			},
		],
		education: [
			{
				institution: "University A",
				url: "https://university-a.edu",
				area: "Computer Science",
				studyType: "Bachelor",
				startDate: "2014-09-01",
				endDate: "2018-06-01",
				score: "3.8",
				courses: ["CS101", "CS201"],
			},
		],
		skills: [
			{ name: "JavaScript", level: "Expert", keywords: ["ES6", "Node.js"] },
			{
				name: "TypeScript",
				level: "Advanced",
				keywords: ["Types", "Interfaces"],
			},
			{ name: "Python", level: "Intermediate", keywords: ["Django", "Flask"] },
		],
		projects: [
			{
				name: "Project Alpha",
				startDate: "2021-01-01",
				endDate: "2021-12-31",
				description: "A cool project",
				highlights: [],
				url: "https://project-alpha.com",
			},
		],
		certificates: [
			{
				name: "AWS Certified",
				date: "2021-06-01",
				url: "https://aws.amazon.com",
				issuer: "Amazon",
			},
		],
		volunteer: [
			{
				organization: "Code for Good",
				position: "Volunteer Developer",
				url: "https://codeforgood.org",
				startDate: "2020-01-01",
				endDate: "",
				summary: "Volunteering",
				highlights: [],
			},
		],
		awards: [
			{
				title: "Best Developer 2021",
				date: "2021-12-01",
				awarder: "Company A",
				summary: "Excellence in development",
			},
		],
		publications: [
			{
				name: "Article on TypeScript",
				publisher: "Medium",
				releaseDate: "2022-01-01",
				url: "https://medium.com/article",
				summary: "How to use TypeScript",
			},
		],
		languages: [{ language: "English", fluency: "Native" }],
		interests: [{ name: "Photography", keywords: ["Landscape", "Portrait"] }],
		references: [
			{
				name: "Jane Smith",
				reference: "John is an excellent developer",
			},
		],
	});

	// Helper to create default visibility (all enabled)
	const createDefaultVisibility = (resumeId: string): SectionVisibility => ({
		resumeId,
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
				profiles: {
					GitHub: true,
					LinkedIn: true,
				},
			},
		},
		work: { enabled: true, expanded: false, items: [true, true] },
		education: { enabled: true, expanded: false, items: [true] },
		skills: { enabled: true, expanded: false, items: [true, true, true] },
		projects: { enabled: true, expanded: false, items: [true] },
		certifications: { enabled: true, expanded: false, items: [true] },
		volunteer: { enabled: true, expanded: false, items: [true] },
		awards: { enabled: true, expanded: false, items: [true] },
		publications: { enabled: true, expanded: false, items: [true] },
		languages: { enabled: true, expanded: false, items: [true] },
		interests: { enabled: true, expanded: false, items: [true] },
		references: { enabled: true, expanded: false, items: [true] },
	});

	describe("filterResume", () => {
		it("should return unmodified resume when all sections and items are visible", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");

			const filtered = service.filterResume(resume, visibility);

			expect(filtered).toEqual(resume);
		});

		it("should preserve name (always visible)", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.name).toBe("John Doe");
		});

		it("should filter out hidden personal details fields", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
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
			const visibility = createDefaultVisibility("resume-123");
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
			const visibility = createDefaultVisibility("resume-123");
			visibility.personalDetails.fields.profiles.LinkedIn = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.profiles).toHaveLength(1);
			expect(filtered.basics.profiles[0]?.network).toBe("GitHub");
		});

		it("should return empty array when section is disabled", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
			visibility.work.enabled = false;
			visibility.education.enabled = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toEqual([]);
			expect(filtered.education).toEqual([]);
		});

		it("should filter out hidden items in work section", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
			visibility.work.items[1] = false; // Hide second work item

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.work).toHaveLength(1);
			expect(filtered.work[0]?.name).toBe("Company A");
		});

		it("should filter out hidden items in education section", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
			visibility.education.items[0] = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.education).toEqual([]);
		});

		it("should filter out hidden items in skills section", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
			visibility.skills.items[0] = false; // Hide JavaScript
			visibility.skills.items[2] = false; // Hide Python

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.skills).toHaveLength(1);
			expect(filtered.skills[0]?.name).toBe("TypeScript");
		});

		it("should handle multiple sections disabled", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
			visibility.projects.enabled = false;
			visibility.certifications.enabled = false;
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
			const visibility = createDefaultVisibility("resume-123");
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
			const visibility = createDefaultVisibility("resume-123");
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
			const visibility = createDefaultVisibility("resume-123");
			// Remove some items from visibility to test undefined handling
			visibility.work.items = [true]; // Only first item defined

			const filtered = service.filterResume(resume, visibility);

			// Both work items should be visible (undefined !== false)
			expect(filtered.work).toHaveLength(2);
		});

		it("should filter all array sections independently", () => {
			const resume = createTestResume();
			const visibility = createDefaultVisibility("resume-123");
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
				certifications: { enabled: true, expanded: false, items: [] },
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
			const visibility = createDefaultVisibility("resume-123");
			visibility.personalDetails.fields.profiles.GitHub = false;
			visibility.personalDetails.fields.profiles.LinkedIn = false;

			const filtered = service.filterResume(resume, visibility);

			expect(filtered.basics.profiles).toEqual([]);
		});
	});
});
