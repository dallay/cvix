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
	basics: PersonalInfoDto;
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

export interface PersonalInfoDto {
	fullName: string;
	email: string;
	phone: string;
	location?: string;
	linkedin?: string;
	github?: string;
	website?: string;
	summary?: string;
}

export interface WorkExperienceDto {
	company: string;
	position: string;
	startDate: string;
	endDate?: string;
	location?: string;
	description?: string;
}

export interface EducationDto {
	institution: string;
	degree: string;
	startDate: string;
	endDate?: string;
	location?: string;
	gpa?: string;
	description?: string;
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
	role: string;
	startDate?: string;
	endDate?: string;
	description?: string;
}

export interface AwardDto {
	title: string;
	date: string;
	awarder: string;
	description?: string;
}

export interface CertificateDto {
	name: string;
	issuer: string;
	date: string;
}

export interface PublicationDto {
	title: string;
	publisher: string;
	date: string;
	url?: string;
	description?: string;
}

export interface InterestDto {
	name: string;
	keywords?: string[];
}

export interface ReferenceDto {
	name: string;
	relation?: string;
	contact?: string;
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
	// Extract LinkedIn and GitHub URLs from profiles
	const linkedinProfile = resume.basics.profiles.find(
		(p) => p.network.toLowerCase() === "linkedin",
	);
	const githubProfile = resume.basics.profiles.find(
		(p) => p.network.toLowerCase() === "github",
	);

	return {
		basics: {
			fullName: normalizeOptionalString(resume.basics.name) ?? "",
			email: normalizeOptionalString(resume.basics.email) ?? "",
			phone: normalizeOptionalString(resume.basics.phone) ?? "",
			location: normalizeOptionalString(resume.basics.location?.city),
			linkedin: normalizeOptionalString(linkedinProfile?.url),
			github: normalizeOptionalString(githubProfile?.url),
			website: normalizeOptionalString(resume.basics.url),
			summary: normalizeOptionalString(resume.basics.summary),
		},
		work:
			resume.work.length > 0
				? resume.work.map((work) => ({
						company: work.name,
						position: work.position,
						startDate: work.startDate,
						endDate: work.endDate || undefined,
						// location not present in Work type; omit from DTO
						...(work.summary ? { description: work.summary } : {}),
					}))
				: undefined,
		education:
			resume.education.length > 0
				? resume.education.map((edu) => ({
						institution: edu.institution,
						degree: edu.studyType,
						startDate: edu.startDate,
						endDate: edu.endDate || undefined,
						// location not present in Education type; omit from DTO
						gpa: normalizeOptionalString(edu.score), // Preserve original GPA value without conversion
						// Map description: area + courses (joined), normalized
						description:
							normalizeOptionalString(
								[
									normalizeOptionalString(edu.area),
									edu.courses && edu.courses.length > 0
										? edu.courses
												.map(normalizeOptionalString)
												.filter(Boolean)
												.join(", ")
										: undefined,
								]
									.filter(Boolean)
									.join(" | "),
							) || undefined,
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
						role: vol.position, // Map domain's position field to DTO's role field
						startDate: vol.startDate || undefined,
						endDate: vol.endDate || undefined,
						description: vol.summary || undefined,
					}))
				: undefined,
		awards:
			resume.awards.length > 0
				? resume.awards.map((award) => ({
						title: award.title,
						date: award.date,
						awarder: award.awarder,
						description: award.summary || undefined,
					}))
				: undefined,
		certificates:
			resume.certificates.length > 0
				? resume.certificates.map((cert) => ({
						name: cert.name,
						issuer: cert.issuer,
						date: cert.date,
					}))
				: undefined,
		publications:
			resume.publications.length > 0
				? resume.publications.map((pub) => ({
						title: pub.name,
						publisher: pub.publisher,
						date: pub.releaseDate,
						url: pub.url || undefined,
						description: pub.summary || undefined,
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
				? resume.references.map((ref: Reference) => {
						const relation = normalizeOptionalString(ref.relation);
						const contact = normalizeOptionalString(ref.contact);
						const reference = normalizeOptionalString(ref.reference);
						return {
							name: ref.name,
							...(relation ? { relation } : {}),
							...(contact ? { contact } : {}),
							...(reference ? { reference } : {}),
						};
					})
				: undefined,
	};
}
