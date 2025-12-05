/**
 * Normalizes an optional string field: trims and returns undefined if empty.
 */
function normalizeOptionalString(value?: string): string | undefined {
	if (typeof value !== "string") return undefined;
	const trimmed = value.trim();
	return trimmed.length > 0 ? trimmed : undefined;
}

import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { Reference } from "../../domain/Reference";

/**
 * Backend DTO structure for resume generation request.
 * Maps the frontend JSON Resume schema to the backend's expected format.
 */
export interface GenerateResumeRequest {
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

export interface LocationDto {
	address?: string;
	postalCode?: string;
	city?: string;
	countryCode?: string;
	region?: string;
}

export interface ProfileDto {
	network: string;
	username?: string;
	url: string;
}

export interface WorkExperienceDto {
	name: string;
	position: string;
	startDate: string;
	endDate?: string;
	summary?: string;
	url?: string;
}

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

export interface SkillCategoryDto {
	name: string;
	keywords: string[];
}

export interface LanguageDto {
	language: string;
	fluency: string;
}

export interface ProjectDto {
	name: string;
	description: string;
	url?: string;
	startDate?: string;
	endDate?: string;
}

export interface VolunteerDto {
	organization: string;
	position: string;
	startDate?: string;
	endDate?: string;
	summary?: string;
	url?: string;
	highlights?: string[];
}

export interface AwardDto {
	title: string;
	date: string;
	awarder: string;
	summary?: string;
}

export interface CertificateDto {
	name: string;
	issuer: string;
	date: string;
	url?: string;
}

export interface PublicationDto {
	name: string;
	publisher: string;
	releaseDate: string;
	url?: string;
	summary?: string;
}

export interface InterestDto {
	name: string;
	keywords?: string[];
}

export interface ReferenceDto {
	name: string;
	reference?: string;
}

/**
 * Maps a Resume object (JSON Resume schema) to the backend's GenerateResumeRequest format.
 *
 * @param resume - The frontend Resume object following JSON Resume schema
 * @returns The backend DTO in GenerateResumeRequest format
 */
export function mapResumeToBackendRequest(
	resume: Resume,
): GenerateResumeRequest {
	return {
		basics: {
			name: normalizeOptionalString(resume.basics.name) ?? "",
			label: normalizeOptionalString(resume.basics.label),
			image: normalizeOptionalString(resume.basics.image),
			email: normalizeOptionalString(resume.basics.email) ?? "",
			phone: normalizeOptionalString(resume.basics.phone) ?? "",
			url: normalizeOptionalString(resume.basics.url),
			summary: normalizeOptionalString(resume.basics.summary),
			location: resume.basics.location
				? {
						address: normalizeOptionalString(resume.basics.location.address),
						postalCode: normalizeOptionalString(
							resume.basics.location.postalCode,
						),
						city: normalizeOptionalString(resume.basics.location.city),
						countryCode: normalizeOptionalString(
							resume.basics.location.countryCode,
						),
						region: normalizeOptionalString(resume.basics.location.region),
					}
				: undefined,
			profiles:
				resume.basics.profiles
					.map((p) => ({
						network: p.network,
						username: normalizeOptionalString(p.username),
						url: normalizeOptionalString(p.url) ?? "",
					}))
					// Backend requires url NotBlank; drop entries with empty url
					.filter((p) => !!p.url) || undefined,
		},
		work:
			resume.work.length > 0
				? resume.work.map((work) => ({
						name: work.name,
						position: work.position,
						startDate: work.startDate,
						endDate: work.endDate || undefined,
						summary: work.summary || undefined,
						url: normalizeOptionalString(work.url),
						highlights:
							work.highlights && work.highlights.length > 0
								? Array.from(work.highlights)
								: undefined,
					}))
				: undefined,
		education:
			resume.education.length > 0
				? resume.education.map((edu) => ({
						institution: edu.institution,
						area: normalizeOptionalString(edu.area),
						studyType: normalizeOptionalString(edu.studyType),
						startDate: edu.startDate,
						endDate: edu.endDate || undefined,
						score: normalizeOptionalString(edu.score),
						url: normalizeOptionalString(edu.url),
						courses:
							edu.courses && edu.courses.length > 0
								? (edu.courses
										.map(normalizeOptionalString)
										.filter(Boolean) as string[])
								: undefined,
					}))
				: undefined,
		skills:
			resume.skills.length > 0
				? resume.skills.map((skill) => ({
						name: skill.name,
						keywords: Array.from(skill.keywords),
					}))
				: undefined,
		languages:
			resume.languages.length > 0
				? resume.languages.map((lang) => ({
						language: lang.language,
						fluency: lang.fluency,
					}))
				: undefined,
		projects:
			resume.projects.length > 0
				? resume.projects.map((project) => ({
						name: project.name,
						description: project.description,
						url: project.url || undefined,
						startDate: project.startDate || undefined,
						endDate: project.endDate || undefined,
					}))
				: undefined,
		volunteer:
			resume.volunteer.length > 0
				? resume.volunteer.map((vol) => ({
						organization: vol.organization,
						position: vol.position,
						startDate: vol.startDate || undefined,
						endDate: vol.endDate || undefined,
						summary: vol.summary || undefined,
						url: normalizeOptionalString(vol.url),
						highlights:
							vol.highlights && vol.highlights.length > 0
								? Array.from(vol.highlights)
								: undefined,
					}))
				: undefined,
		awards:
			resume.awards.length > 0
				? resume.awards.map((award) => ({
						title: award.title,
						date: award.date,
						awarder: award.awarder,
						summary: award.summary || undefined,
					}))
				: undefined,
		certificates:
			resume.certificates.length > 0
				? resume.certificates.map((cert) => ({
						name: cert.name,
						issuer: cert.issuer,
						date: cert.date,
						url: normalizeOptionalString(cert.url),
					}))
				: undefined,
		publications:
			resume.publications.length > 0
				? resume.publications.map((pub) => ({
						name: pub.name,
						publisher: pub.publisher,
						releaseDate: pub.releaseDate,
						url: pub.url || undefined,
						summary: pub.summary || undefined,
					}))
				: undefined,
		interests:
			resume.interests.length > 0
				? resume.interests.map((interest) => ({
						name: interest.name,
						keywords: Array.from(interest.keywords),
					}))
				: undefined,
		references:
			resume.references.length > 0
				? resume.references.map((ref: Reference) => ({
						name: ref.name,
						reference: normalizeOptionalString(ref.reference),
					}))
				: undefined,
	};
}
