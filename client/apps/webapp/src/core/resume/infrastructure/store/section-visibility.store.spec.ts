import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import { sectionVisibilityStorage } from "@/core/resume/infrastructure/storage/SectionVisibilityStorage";
import { useSectionVisibilityStore } from "./section-visibility.store";

// Mock the storage module
vi.mock(
	"@/core/resume/infrastructure/storage/SectionVisibilityStorage",
	() => ({
		sectionVisibilityStorage: {
			load: vi.fn(),
			save: vi.fn(),
		},
	}),
);

describe("useSectionVisibilityStore", () => {
	// Helper to create a test resume
	const createTestResume = (): Resume => ({
		basics: {
			name: "John Doe",
			label: "Software Engineer",
			image: "profile.jpg",
			email: "john@example.com",
			phone: "+1-555-0100",
			url: "https://johndoe.com",
			summary: "Experienced engineer",
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
				url: "",
				startDate: "2020-01-01",
				endDate: "2022-12-31",
				summary: "",
				highlights: [],
			},
			{
				name: "Company B",
				position: "Developer",
				url: "",
				startDate: "2018-01-01",
				endDate: "2019-12-31",
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
				startDate: "2014-09-01",
				endDate: "2018-06-01",
				score: "3.8",
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

	beforeEach(() => {
		// Create a new Pinia instance for each test
		setActivePinia(createPinia());
		// Clear all mocks
		vi.clearAllMocks();
		// Reset storage mock to return null by default
		vi.mocked(sectionVisibilityStorage.load).mockReturnValue(null);
	});

	describe("initialize", () => {
		it("should initialize with resume and create default visibility", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();

			store.initialize(resume, "test-resume-id");

			expect(store.visibility).not.toBeNull();
			expect(store.visibility?.resumeId).toBe("test-resume-id");
			expect(store.isLoading).toBe(false);
			expect(store.error).toBeNull();
		});

		it("should generate a UUID if resumeId is not provided", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();

			store.initialize(resume);

			expect(store.visibility).not.toBeNull();
			expect(store.visibility?.resumeId).toMatch(
				/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i,
			);
		});

		it("should load saved preferences if available", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			const savedVisibility = {
				resumeId: "saved-resume-id",
				personalDetails: {
					enabled: true as const,
					expanded: true,
					fields: {
						image: false,
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
						profiles: { GitHub: true, LinkedIn: true },
					},
				},
				work: { enabled: true, expanded: false, items: [true, false] },
				education: { enabled: true, expanded: false, items: [true] },
				skills: { enabled: false, expanded: false, items: [false, false] },
				projects: { enabled: true, expanded: false, items: [] },
				certifications: { enabled: true, expanded: false, items: [] },
				volunteer: { enabled: true, expanded: false, items: [] },
				awards: { enabled: true, expanded: false, items: [] },
				publications: { enabled: true, expanded: false, items: [] },
				languages: { enabled: true, expanded: false, items: [] },
				interests: { enabled: true, expanded: false, items: [] },
				references: { enabled: true, expanded: false, items: [] },
			};

			vi.mocked(sectionVisibilityStorage.load).mockReturnValue(savedVisibility);

			store.initialize(resume, "saved-resume-id");

			expect(store.visibility).toEqual(savedVisibility);
			expect(store.visibility?.personalDetails.fields.image).toBe(false);
			expect(store.visibility?.work.items).toEqual([true, false]);
		});

		it("should enable sections with data and disable empty sections", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();

			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.work.enabled).toBe(true);
			expect(store.visibility?.education.enabled).toBe(true);
			expect(store.visibility?.skills.enabled).toBe(true);
			expect(store.visibility?.projects.enabled).toBe(false); // Empty
			expect(store.visibility?.certifications.enabled).toBe(false); // Empty
		});
	});

	describe("toggleSection", () => {
		it("should toggle a section from enabled to disabled", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.work.enabled).toBe(true);

			store.toggleSection("work");

			expect(store.visibility?.work.enabled).toBe(false);
			expect(store.visibility?.work.expanded).toBe(false);
		});

		it("should toggle a section from disabled to enabled and enable all items", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Disable first
			store.toggleSection("work");
			expect(store.visibility?.work.enabled).toBe(false);

			// Re-enable
			store.toggleSection("work");

			expect(store.visibility?.work.enabled).toBe(true);
			expect(store.visibility?.work.items).toEqual([true, true]);
		});

		it("should not allow disabling Personal Details section (FR-007)", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.personalDetails.enabled).toBe(true);

			store.toggleSection("personalDetails");

			// Should still be enabled
			expect(store.visibility?.personalDetails.enabled).toBe(true);
		});
	});

	describe("toggleSectionExpanded", () => {
		it("should toggle expanded state for Personal Details", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.personalDetails.expanded).toBe(false);

			store.toggleSectionExpanded("personalDetails");

			expect(store.visibility?.personalDetails.expanded).toBe(true);

			store.toggleSectionExpanded("personalDetails");

			expect(store.visibility?.personalDetails.expanded).toBe(false);
		});

		it("should toggle expanded state for array sections when enabled", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.work.expanded).toBe(false);

			store.toggleSectionExpanded("work");

			expect(store.visibility?.work.expanded).toBe(true);
		});

		it("should not expand disabled sections", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Disable work section
			store.toggleSection("work");
			expect(store.visibility?.work.enabled).toBe(false);

			// Try to expand
			store.toggleSectionExpanded("work");

			// Should remain collapsed
			expect(store.visibility?.work.expanded).toBe(false);
		});
	});

	describe("toggleItem", () => {
		it("should toggle an item's visibility", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.work.items[0]).toBe(true);

			store.toggleItem("work", 0);

			expect(store.visibility?.work.items[0]).toBe(false);

			store.toggleItem("work", 0);

			expect(store.visibility?.work.items[0]).toBe(true);
		});

		it("should auto-disable section when all items are disabled (FR-017)", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.work.enabled).toBe(true);

			// Disable all work items
			store.toggleItem("work", 0);
			store.toggleItem("work", 1);

			// Section should auto-disable
			expect(store.visibility?.work.enabled).toBe(false);
			expect(store.visibility?.work.expanded).toBe(false);
		});

		it("should not modify item if index is out of bounds", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			const originalItems = [...(store.visibility?.work.items || [])];

			store.toggleItem("work", 999);

			expect(store.visibility?.work.items).toEqual(originalItems);
		});
	});

	describe("togglePersonalDetailsField", () => {
		it("should toggle simple boolean fields", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.personalDetails.fields.email).toBe(true);

			store.togglePersonalDetailsField("email");

			expect(store.visibility?.personalDetails.fields.email).toBe(false);
		});

		it("should not toggle name field (FR-013)", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Name field doesn't exist in fields, but ensure toggle doesn't crash
			store.togglePersonalDetailsField("name");

			// No assertion needed - just verifying it doesn't throw
		});

		it("should toggle all location fields together", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.personalDetails.fields.location.city).toBe(true);

			store.togglePersonalDetailsField("location");

			// All location fields should be toggled to false
			expect(store.visibility?.personalDetails.fields.location.address).toBe(
				false,
			);
			expect(store.visibility?.personalDetails.fields.location.city).toBe(
				false,
			);
			expect(store.visibility?.personalDetails.fields.location.postalCode).toBe(
				false,
			);
		});

		it("should toggle all profiles together", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			expect(store.visibility?.personalDetails.fields.profiles.GitHub).toBe(
				true,
			);

			store.togglePersonalDetailsField("profiles");

			// All profiles should be toggled to false
			expect(store.visibility?.personalDetails.fields.profiles.GitHub).toBe(
				false,
			);
			expect(store.visibility?.personalDetails.fields.profiles.LinkedIn).toBe(
				false,
			);
		});
	});

	describe("reset", () => {
		it("should reset visibility to defaults", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Make some changes
			store.toggleSection("work");
			store.toggleItem("skills", 0);
			store.togglePersonalDetailsField("email");

			expect(store.visibility?.work.enabled).toBe(false);
			expect(store.visibility?.skills.items[0]).toBe(false);
			expect(store.visibility?.personalDetails.fields.email).toBe(false);

			// Reset
			store.reset();

			// Should be back to defaults
			expect(store.visibility?.work.enabled).toBe(true);
			expect(store.visibility?.skills.items[0]).toBe(true);
			expect(store.visibility?.personalDetails.fields.email).toBe(true);
		});
	});

	describe("computed properties", () => {
		it("should compute section metadata correctly", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			const metadata = store.sectionMetadata;

			expect(metadata).toHaveLength(12);

			// Personal Details
			const personalDetails = metadata.find(
				(m) => m.type === "personalDetails",
			);
			expect(personalDetails?.hasData).toBe(true);
			expect(personalDetails?.labelKey).toBe("resume.sections.personalDetails");

			// Work (has 2 items)
			const work = metadata.find((m) => m.type === "work");
			expect(work?.hasData).toBe(true);
			expect(work?.itemCount).toBe(2);
			expect(work?.visibleItemCount).toBe(2);

			// Projects (empty)
			const projects = metadata.find((m) => m.type === "projects");
			expect(projects?.hasData).toBe(false);
			expect(projects?.itemCount).toBe(0);
		});

		it("should update metadata when items are toggled", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Initially all visible
			let work = store.sectionMetadata.find((m) => m.type === "work");
			expect(work?.visibleItemCount).toBe(2);

			// Toggle one item
			store.toggleItem("work", 0);

			work = store.sectionMetadata.find((m) => m.type === "work");
			expect(work?.visibleItemCount).toBe(1);
		});

		it("should compute filtered resume correctly", () => {
			const store = useSectionVisibilityStore();
			const resume = createTestResume();
			store.initialize(resume, "test-resume-id");

			// Initially all visible
			expect(store.filteredResume?.work).toHaveLength(2);

			// Disable work section
			store.toggleSection("work");

			expect(store.filteredResume?.work).toEqual([]);
		});
	});
});
