import type { Resume } from "./Resume";

/**
 * Enumeration of all resume section types.
 * Order defines standard resume section ordering (FR-009).
 *
 * ⚠️ CRITICAL: This order MUST match the backend LaTeX template rendering order.
 * Template location: server/engine/src/main/resources/templates/resume/engineering/engineering.stg
 * Template order: header → about → experience → education → skills → projects → publications →
 *                 certificates → awards → volunteer → languages → interests → references
 *
 * Mapping:
 * - personalDetails → header + about (always enabled, FR-007)
 * - work → experience
 * - education → education
 * - skills → skills
 * - projects → projects
 * - certificates → certificates
 * - volunteer → volunteer
 * - awards → awards
 * - publications → publications
 * - languages → languages
 * - interests → interests
 * - references → references
 *
 * Do NOT reorder this array without coordinating with backend template changes.
 * See: specs/005-pdf-section-selector/plan.md (FR-009)
 */
export const SECTION_TYPES = [
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
	resumeId: string;
	personalDetails: PersonalDetailsVisibility;
	work: ArraySectionVisibility;
	education: ArraySectionVisibility;
	skills: ArraySectionVisibility;
	projects: ArraySectionVisibility;
	certificates: ArraySectionVisibility;
	volunteer: ArraySectionVisibility;
	awards: ArraySectionVisibility;
	publications: ArraySectionVisibility;
	languages: ArraySectionVisibility;
	interests: ArraySectionVisibility;
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
	image: boolean;
	email: boolean;
	phone: boolean;
	location: {
		address: boolean;
		postalCode: boolean;
		city: boolean;
		countryCode: boolean;
		region: boolean;
	};
	summary: boolean;
	url: boolean;
	profiles: { [profile: string]: boolean };
}

/**
 * Visibility settings for a section containing multiple items.
 * Used for Work Experience, Education, Skills, Projects, etc.
 */
export interface ArraySectionVisibility {
	enabled: boolean;
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
	type: SectionType;
	labelKey: string;
	hasData: boolean;
	itemCount: number;
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
		certificates: createArrayVisibility(resume.certificates.length),
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
		items: Array.from({ length: itemCount }, () => true),
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
	return visibility.items.filter(Boolean).length;
}

/**
 * Returns true if the section has at least one visible item.
 */
export function hasVisibleItems(visibility: ArraySectionVisibility): boolean {
	return countVisibleItems(visibility) > 0;
}
