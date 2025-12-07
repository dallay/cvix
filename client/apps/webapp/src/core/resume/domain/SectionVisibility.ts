import type { Resume } from "./Resume";

/**
 * Enumeration of all resume section types.
 * Order defines standard resume section ordering (FR-009).
 * This order must match the backend LaTeX template (engineering.stg).
 */
export const SECTION_TYPES = [
	"personalDetails",
	"work",
	"education",
	"skills",
	"projects",
	"certifications",
	"volunteer",
	"awards",
	"publications",
	"languages",
	"interests",
	"references",
] as const;

export type SectionType = (typeof SECTION_TYPES)[number];

/**
 * Sections that contain arrays of items (excludes personalDetails)
 */
export type ArraySectionType = Exclude<SectionType, "personalDetails">;

/**
 * Root visibility preferences for a resume.
 * Controls which sections and items appear in the PDF export.
 */
export interface SectionVisibility {
	/** Unique identifier for the resume these preferences apply to */
	resumeId: string;

	/** Personal details section (always enabled, individual fields toggleable) */
	personalDetails: PersonalDetailsVisibility;

	/** Work experience section visibility */
	work: ArraySectionVisibility;

	/** Education section visibility */
	education: ArraySectionVisibility;

	/** Skills section visibility */
	skills: ArraySectionVisibility;

	/** Projects section visibility */
	projects: ArraySectionVisibility;

	/** Certifications section visibility */
	certifications: ArraySectionVisibility;

	/** Volunteer experience section visibility */
	volunteer: ArraySectionVisibility;

	/** Awards section visibility */
	awards: ArraySectionVisibility;

	/** Publications section visibility */
	publications: ArraySectionVisibility;

	/** Languages section visibility */
	languages: ArraySectionVisibility;

	/** Interests section visibility */
	interests: ArraySectionVisibility;

	/** References section visibility */
	references: ArraySectionVisibility;
}

/**
 * Visibility settings for the Personal Details section.
 * This section cannot be fully disabled (FR-007).
 * Individual fields can be toggled except for name (FR-013).
 */
export interface PersonalDetailsVisibility {
	/** Always true - Personal Details cannot be disabled */
	readonly enabled: true;

	/** Whether the section is expanded to show field toggles */
	expanded: boolean;

	/** Individual field visibility */
	fields: PersonalDetailsFieldVisibility;
}

/**
 * Individual field visibility within Personal Details.
 * Note: 'name' is always visible and not included here.
 */
export interface PersonalDetailsFieldVisibility {
	/** Profile image visibility */
	image: boolean;

	/** Email address visibility */
	email: boolean;

	/** Phone number visibility */
	phone: boolean;

	/** Location/address visibility */
	location: {
		address: boolean;
		postalCode: boolean;
		city: boolean;
		countryCode: boolean;
		region: boolean;
	};

	/** Professional summary visibility */
	summary: boolean;

	/** Website URL visibility */
	url: boolean;

	/** Social profiles visibility */
	profiles: { [profile: string]: boolean };
}

/**
 * Visibility settings for a section containing multiple items.
 * Used for Work Experience, Education, Skills, Projects, etc.
 */
export interface ArraySectionVisibility {
	/** Whether the entire section is enabled */
	enabled: boolean;

	/** Whether the section is expanded to show item toggles */
	expanded: boolean;

	/**
	 * Visibility state for each item in the section.
	 * Index corresponds to item index in the resume array.
	 * true = visible, false = hidden
	 */
	items: boolean[];
}

/**
 * Metadata for rendering a section toggle pill.
 */
export interface SectionMetadata {
	/** Section type identifier */
	type: SectionType;

	/** Internationalized display label key */
	labelKey: string;

	/** Whether the section has data in the resume */
	hasData: boolean;

	/** Number of items in the section (0 for personalDetails) */
	itemCount: number;

	/** Number of currently visible items */
	visibleItemCount: number;
}

/**
 * Creates default visibility preferences with all sections/items enabled.
 *
 * @param resumeId - Unique identifier for the resume
 * @param resume - The resume to generate defaults for
 * @returns Default SectionVisibility with all content visible
 */
export function createDefaultVisibility(
	resumeId: string,
	resume: Resume,
): SectionVisibility {
	return {
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
				profiles: createProfilesVisibility(resume.basics.profiles),
			},
		},
		work: createArrayVisibility(resume.work.length),
		education: createArrayVisibility(resume.education.length),
		skills: createArrayVisibility(resume.skills.length),
		projects: createArrayVisibility(resume.projects.length),
		certifications: createArrayVisibility(resume.certificates.length),
		volunteer: createArrayVisibility(resume.volunteer.length),
		awards: createArrayVisibility(resume.awards.length),
		publications: createArrayVisibility(resume.publications.length),
		languages: createArrayVisibility(resume.languages.length),
		interests: createArrayVisibility(resume.interests.length),
		references: createArrayVisibility(resume.references.length),
	};
}

/**
 * Creates default visibility for an array section.
 */
function createArrayVisibility(itemCount: number): ArraySectionVisibility {
	return {
		enabled: itemCount > 0,
		expanded: false,
		items: Array(itemCount).fill(true),
	};
}

/**
 * Creates visibility record for profiles based on what exists in the resume.
 */
function createProfilesVisibility(
	profiles: ReadonlyArray<{ network: string }>,
): { [profile: string]: boolean } {
	const result: { [profile: string]: boolean } = {};
	profiles.forEach((profile) => {
		result[profile.network] = true;
	});
	return result;
}

/**
 * Returns the number of visible items in an array section.
 */
export function countVisibleItems(visibility: ArraySectionVisibility): number {
	if (!visibility.enabled) {
		return 0;
	}
	return visibility.items.filter((visible) => visible).length;
}

/**
 * Returns true if the section has at least one visible item.
 */
export function hasVisibleItems(visibility: ArraySectionVisibility): boolean {
	return countVisibleItems(visibility) > 0;
}
