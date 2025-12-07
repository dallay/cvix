import type { Basics, Resume } from "../domain/Resume";
import type {
	ArraySectionVisibility,
	PersonalDetailsVisibility,
	SectionVisibility,
} from "../domain/SectionVisibility";

/**
 * Service for filtering a resume based on visibility preferences.
 * Produces a new Resume object with hidden sections/items removed.
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
				visibility.certifications,
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

		// Filter items based on their visibility flags.
		// Items without a corresponding visibility entry (e.g., newly added)
		// are shown by default (undefined !== false).
		return items.filter((_, index) => {
			return visibility.items[index] !== false;
		});
	}

	/**
	 * Counts the number of visible items in a section.
	 */
	countVisibleItems(visibility: ArraySectionVisibility): number {
		if (!visibility.enabled) {
			return 0;
		}
		return visibility.items.filter((visible) => visible).length;
	}

	/**
	 * Checks if a section has any visible items.
	 */
	hasVisibleItems(visibility: ArraySectionVisibility): boolean {
		return this.countVisibleItems(visibility) > 0;
	}
}
