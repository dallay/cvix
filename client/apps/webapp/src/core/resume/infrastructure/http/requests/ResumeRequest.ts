/**
 * Backend DTO structure for resume generation request.
 * Maps the frontend JSON Resume schema to the backend's expected format.
 */
export interface GenerateResumeRequest extends ResumeRequest {
	templateId: string;
}

/**
 * Shape of a resume payload sent to the backend.
 * Contains top-level sections mapped from the JSON Resume schema.
 */
export interface ResumeRequest {
	basics: BasicsDto;
	work?: WorkExperienceDto[];
	education?: EducationDto[];
	skills?: SkillCategoryDto[];
	languages?: LanguageDto[];
	projects?: ProjectDto[];
	volunteer?: VolunteerDto[];
	awards?: AwardDto[];
	certificates?: CertificateDto[];
	publications?: PublicationDto[];
	interests?: InterestDto[];
	references?: ReferenceDto[];
}

/**
 * DTO for creating a new resume resource.
 */
export interface CreateResumeRequest {
	workspaceId: string;
	title?: string;
	content: ResumeRequest;
}

/**
 * DTO for updating an existing resume resource.
 */
export interface UpdateResumeRequest {
	title?: string;
	content: ResumeRequest;
}

/**
 * Basics (personal / contact) section DTO.
 */
export interface BasicsDto {
	name: string;
	label?: string;
	image?: string;
	email: string;
	phone: string;
	url?: string;
	summary?: string;
	location?: LocationDto;
	profiles?: ProfileDto[];
}

/**
 * Structured location fields for the BasicsDto.
 */
export interface LocationDto {
	address?: string;
	postalCode?: string;
	city?: string;
	countryCode?: string;
	region?: string;
}

/**
 * Social profile entry (e.g., GitHub, LinkedIn).
 * Backend expects a non-empty `url` for published profiles.
 */
export interface ProfileDto {
	network: string;
	username?: string;
	url: string;
}

/**
 * Work / professional experience entry.
 */
export interface WorkExperienceDto {
	name: string;
	position: string;
	startDate: string;
	endDate?: string;
	summary?: string;
	url?: string;
}

/**
 * Education entry.
 */
export interface EducationDto {
	institution: string;
	area?: string;
	studyType?: string;
	startDate: string;
	endDate?: string;
	score?: string;
	url?: string;
	courses?: string[];
}

/**
 * Skill category (e.g., "Programming Languages") containing keywords.
 */
export interface SkillCategoryDto {
	name: string;
	keywords: string[];
}

/**
 * Language proficiency entry.
 */
export interface LanguageDto {
	language: string;
	fluency: string;
}

/**
 * Project entry (name + description + optional dates/URL).
 */
export interface ProjectDto {
	name: string;
	description: string;
	url?: string;
	startDate?: string;
	endDate?: string;
}

/**
 * Volunteer experience entry.
 */
export interface VolunteerDto {
	organization: string;
	position: string;
	startDate?: string;
	endDate?: string;
	summary?: string;
	url?: string;
	highlights?: string[];
}

/**
 * Award / recognition entry.
 */
export interface AwardDto {
	title: string;
	date: string;
	awarder: string;
	summary?: string;
}

/**
 * Certificate entry.
 */
export interface CertificateDto {
	name: string;
	issuer: string;
	date: string;
	url?: string;
}

/**
 * Publication entry.
 */
export interface PublicationDto {
	name: string;
	publisher: string;
	releaseDate: string;
	url?: string;
	summary?: string;
}

/**
 * Interest entry containing optional keywords.
 */
export interface InterestDto {
	name: string;
	keywords?: string[];
}

/**
 * Professional reference entry.
 */
export interface ReferenceDto {
	name: string;
	reference?: string;
}
