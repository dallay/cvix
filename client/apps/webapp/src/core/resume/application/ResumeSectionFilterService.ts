import type { Basics, Resume } from "../domain/Resume";
import {
	type ArraySectionVisibility,
	countVisibleItems,
	hasVisibleItems,
	type PersonalDetailsVisibility,
	type SectionVisibility,
} from "../domain/SectionVisibility";

/**
 * Service for filtering a resume based on visibility preferences.
 * Produces a new Resume object with hidden sections/items removed.
 *
 * ⚠️ SECTION ORDER PRESERVATION (FR-009, US4):
 * The filterResume method MUST maintain the order of sections as defined in the Resume type,
 * which matches SECTION_TYPES and the backend template (engineering.stg).
 *
 * Order: basics → work → education → skills → projects → certificates → volunteer →
 *        awards → publications → languages → interests → references
 *
 * Do NOT add any sorting logic or allow section reordering in this service.
 * See: client/apps/webapp/src/core/resume/domain/SectionVisibility.ts (SECTION_TYPES)
 * See: specs/005-pdf-section-selector/plan.md (FR-009)
 */
export class ResumeSectionFilterService {
	/**
	 * Applies visibility preferences to filter a resume.
	 * Returns a new Resume object containing only visible content.
	 *
	 * @param resume - The complete resume to filter
	 * @param visibility - The visibility preferences to apply
	 * @returns A new Resume with only visible sections/items
	 */
	filterResume(resume: Resume, visibility: SectionVisibility): Resume {
		return {
			basics: this.filterBasics(resume.basics, visibility.personalDetails),
			work: this.filterArray(resume.work, visibility.work),
			education: this.filterArray(resume.education, visibility.education),
			skills: this.filterArray(resume.skills, visibility.skills),
			projects: this.filterArray(resume.projects, visibility.projects),
			certificates: this.filterArray(
				resume.certificates,
				visibility.certificates,
			),
			volunteer: this.filterArray(resume.volunteer, visibility.volunteer),
			awards: this.filterArray(resume.awards, visibility.awards),
			publications: this.filterArray(
				resume.publications,
				visibility.publications,
			),
			languages: this.filterArray(resume.languages, visibility.languages),
			interests: this.filterArray(resume.interests, visibility.interests),
			references: this.filterArray(resume.references, visibility.references),
		};
	}

	/**
	 * Filters the Basics (Personal Details) section based on field visibility.
	 */
	private filterBasics(
		basics: Basics,
		visibility: PersonalDetailsVisibility,
	): Basics {
		const fields = visibility.fields;
		return {
			name: basics.name, // Always visible
			label: basics.label,
			image: fields.image ? basics.image : "",
			email: fields.email ? basics.email : "",
			phone: fields.phone ? basics.phone : "",
			url: fields.url ? basics.url : "",
			summary: fields.summary ? basics.summary : "",
			location: {
				address: fields.location.address ? basics.location.address : "",
				postalCode: fields.location.postalCode
					? basics.location.postalCode
					: "",
				city: fields.location.city ? basics.location.city : "",
				countryCode: fields.location.countryCode
					? basics.location.countryCode
					: "",
				region: fields.location.region ? basics.location.region : "",
			},
			profiles: basics.profiles.filter((profile) => {
				// If the profile network is explicitly marked false, filter it out
				// We use !== false to allow undefined (new profiles) to be visible by default
				return fields.profiles[profile.network] !== false;
			}),
		};
	}

	/**
	 * Filters an array section (work, education, etc.) based on item visibility.
	 */
	private filterArray<T>(
		items: ReadonlyArray<T>,
		visibility: ArraySectionVisibility,
	): ReadonlyArray<T> {
		// If the section is disabled, return empty array
		if (!visibility.enabled) {
			return [];
		}

		// ⚡ Bolt: Performance Optimization
		// If all items are visible, return a shallow copy of the original array to avoid unnecessary filtering.
		// This preserves the performance benefit while ensuring the function's contract (always return a new array).
		const hasHiddenItems = visibility.items.some((isVisible) => isVisible === false);
		if (!hasHiddenItems) {
			return [...items];
		}

		// Filter items based on their visibility flags.
		// Items without a corresponding visibility entry (e.g., newly added)
		// are shown by default (undefined !== false).
		// We explicitly use !== false to allow undefined values to pass through
		return items.filter((_, index) => {
			return visibility.items[index] !== false;
		});
	}

	/**
	 * Counts the number of visible items in a section.
	 */
	countVisibleItems(visibility: ArraySectionVisibility): number {
		return countVisibleItems(visibility);
	}

	/**
	 * Checks if a section has any visible items.
	 */
	hasVisibleItems(visibility: ArraySectionVisibility): boolean {
		return hasVisibleItems(visibility);
	}
}
