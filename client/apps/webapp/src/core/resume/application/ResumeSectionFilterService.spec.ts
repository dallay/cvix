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

	describe("filterVisibleItems - Performance Optimization (Bolt)", () => {
		describe("early return optimization when all items visible", () => {
			it("should return shallow copy when all items are explicitly true", () => {
				const items = [
					{ id: "1", company: "Company A" },
					{ id: "2", company: "Company B" },
					{ id: "3", company: "Company C" },
				];
				const visibility: ArraySectionVisibility = {
					section: "work",
					enabled: true,
					expanded: false,
					items: [true, true, true],
				};

				const result = service["filterVisibleItems"](items, visibility);

				expect(result).toEqual(items);
				expect(result).not.toBe(items); // Must be a new array instance
				expect(result.length).toBe(3);
			});

			it("should return shallow copy when all items are undefined (default visible)", () => {
				const items = [
					{ id: "1", skill: "JavaScript" },
					{ id: "2", skill: "TypeScript" },
					{ id: "3", skill: "React" },
				];
				const visibility: ArraySectionVisibility = {
					section: "skills",
					enabled: true,
					expanded: false,
					items: [undefined, undefined, undefined] as unknown as boolean[],
				};

				const result = service["filterVisibleItems"](items, visibility);

				expect(result).toEqual(items);
				expect(result).not.toBe(items);
				expect(result.length).toBe(3);
			});

			it('should return shallow copy when mix of true and undefined', () => {
				const items = [
					{ id: '1', degree: 'Bachelor' },
					{ id: '2', degree: 'Master' },
					{ id: '3', degree: 'PhD' },
				];
				const visibility: ArraySectionVisibility = {
					section: 'education',
					enabled: true,
					expanded: false,
					items: [true, undefined, true] as unknown as boolean[],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual(items);
				expect(result).not.toBe(items);
			});

			it('should skip filtering for large arrays when all items visible', () => {
				// Test performance optimization with large dataset
				const items = Array.from({ length: 100 }, (_, i) => ({
					id: `item-${i}`,
					value: `Value ${i}`,
				}));
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: Array(100).fill(true),
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual(items);
				expect(result).not.toBe(items);
				expect(result.length).toBe(100);
			});

			it('should preserve object references in shallow copy', () => {
				const item1 = { id: '1', name: 'Item 1' };
				const item2 = { id: '2', name: 'Item 2' };
				const items = [item1, item2];
				const visibility: ArraySectionVisibility = {
					section: 'projects',
					enabled: true,
					expanded: false,
					items: [true, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result[0]).toBe(item1);
				expect(result[1]).toBe(item2);
			});
		});

		describe('filtering behavior when hidden items exist', () => {
			it('should filter when at least one item is explicitly false', () => {
				const items = [
					{ id: '1', title: 'Award 1' },
					{ id: '2', title: 'Award 2' },
					{ id: '3', title: 'Award 3' },
				];
				const visibility: ArraySectionVisibility = {
					section: 'awards',
					enabled: true,
					expanded: false,
					items: [true, false, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual([
					{ id: '1', title: 'Award 1' },
					{ id: '3', title: 'Award 3' },
				]);
				expect(result.length).toBe(2);
			});

			it('should filter when multiple items are false', () => {
				const items = [
					{ id: '1', name: 'Project A' },
					{ id: '2', name: 'Project B' },
					{ id: '3', name: 'Project C' },
					{ id: '4', name: 'Project D' },
				];
				const visibility: ArraySectionVisibility = {
					section: 'projects',
					enabled: true,
					expanded: false,
					items: [true, false, false, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result.length).toBe(2);
				expect(result[0].id).toBe('1');
				expect(result[1].id).toBe('4');
			});

			it('should return empty array when all items are false', () => {
				const items = [
					{ id: '1', cert: 'Cert A' },
					{ id: '2', cert: 'Cert B' },
				];
				const visibility: ArraySectionVisibility = {
					section: 'certificates',
					enabled: true,
					expanded: false,
					items: [false, false],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual([]);
			});

			it('should handle single false in large array', () => {
				const items = Array.from({ length: 50 }, (_, i) => ({
					id: `${i}`,
					data: `Data ${i}`,
				}));
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: Array.from({ length: 50 }, (_, i) => i !== 25), // Only index 25 is false
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result.length).toBe(49);
				expect(result.find((item) => item.id === '25')).toBeUndefined();
			});
		});

		describe('performance characteristics and edge cases', () => {
			it('should handle empty items array efficiently', () => {
				const items: unknown[] = [];
				const visibility: ArraySectionVisibility = {
					section: 'languages',
					enabled: true,
					expanded: false,
					items: [],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual([]);
			});

			it('should handle mismatched visibility array lengths', () => {
				const items = [
					{ id: '1' },
					{ id: '2' },
					{ id: '3' },
				];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [false, true], // Fewer visibility flags than items
				};

				const result = service['filterVisibleItems'](items, visibility);

				// First item hidden, others visible by default
				expect(result.length).toBe(2);
				expect(result[0].id).toBe('2');
				expect(result[1].id).toBe('3');
			});

			it('should handle more visibility flags than items', () => {
				const items = [{ id: '1' }, { id: '2' }];
				const visibility: ArraySectionVisibility = {
					section: 'interests',
					enabled: true,
					expanded: false,
					items: [true, false, true, false], // More flags than items
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result.length).toBe(1);
				expect(result[0].id).toBe('1');
			});

			it('should not mutate original items array', () => {
				const items = [
					{ id: '1', mutable: true },
					{ id: '2', mutable: true },
				];
				const originalItems = JSON.parse(JSON.stringify(items));
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [true, false],
				};

				service['filterVisibleItems'](items, visibility);

				expect(items).toEqual(originalItems);
			});

			it('should handle complex nested objects', () => {
				const items = [
					{
						id: '1',
						company: 'Tech Corp',
						position: { title: 'Engineer', level: 'Senior' },
						skills: ['TypeScript', 'React'],
					},
					{
						id: '2',
						company: 'Design Inc',
						position: { title: 'Designer', level: 'Lead' },
						skills: ['Figma', 'Sketch'],
					},
				];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [false, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result.length).toBe(1);
				expect(result[0]).toEqual(items[1]);
			});
		});

		describe('boolean comparison strictness', () => {
			it('should treat only explicit false as hidden', () => {
				const items = [{ id: '1' }, { id: '2' }, { id: '3' }];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [null, 0, ''] as unknown as boolean[], // Falsy but not false
				};

				const result = service['filterVisibleItems'](items, visibility);

				// Only explicit false should hide items
				expect(result.length).toBe(3);
			});

			it('should correctly identify when hasHiddenItems is false', () => {
				// This tests the optimization condition
				const items = [{ id: '1' }, { id: '2' }];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [true, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				// Should use shallow copy optimization
				expect(result).toEqual(items);
				expect(result).not.toBe(items);
			});

			it('should correctly identify when hasHiddenItems is true', () => {
				const items = [{ id: '1' }, { id: '2' }, { id: '3' }];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: true,
					expanded: false,
					items: [true, false, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				// Should use filtering logic
				expect(result.length).toBe(2);
			});
		});

		describe('integration with disabled sections', () => {
			it('should return empty array when section is disabled regardless of visibility', () => {
				const items = [{ id: '1' }, { id: '2' }];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: false,
					expanded: false,
					items: [true, true],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual([]);
			});

			it('should return empty array when disabled even with all false', () => {
				const items = [{ id: '1' }];
				const visibility: ArraySectionVisibility = {
					section: 'work',
					enabled: false,
					expanded: false,
					items: [false],
				};

				const result = service['filterVisibleItems'](items, visibility);

				expect(result).toEqual([]);
			});
		});
	});
});
