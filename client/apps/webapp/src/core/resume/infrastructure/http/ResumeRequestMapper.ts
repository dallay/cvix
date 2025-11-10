import type { Resume } from "@/core/resume/domain/Resume.ts";

/**
 * Backend DTO structure for resume generation request.
 * Maps the frontend JSON Resume schema to the backend's expected format.
 */
export interface GenerateResumeRequest {
	personalInfo: PersonalInfoDto;
	workExperience?: WorkExperienceDto[];
	education?: EducationDto[];
	skills?: SkillCategoryDto[];
	languages?: LanguageDto[];
	projects?: ProjectDto[];
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
		personalInfo: {
			fullName: resume.basics.name,
			email: resume.basics.email,
			phone: resume.basics.phone,
			location: resume.basics.location?.city,
			linkedin: linkedinProfile?.url,
			github: githubProfile?.url,
			website: resume.basics.url,
			summary: resume.basics.summary,
		},
		workExperience:
			resume.work.length > 0
				? resume.work.map((work) => ({
						company: work.name,
						position: work.position,
						startDate: work.startDate,
						endDate: work.endDate || undefined,
						location: undefined, // Not in JSON Resume work schema
						description: work.summary,
					}))
				: undefined,
		education:
			resume.education.length > 0
				? resume.education.map((edu) => ({
						institution: edu.institution,
						degree: edu.studyType,
						startDate: edu.startDate,
						endDate: edu.endDate || undefined,
						location: undefined, // Not in JSON Resume education schema
						gpa: edu.score || undefined,
						description: undefined, // Could map from courses if needed
					}))
				: undefined,
		skills:
			resume.skills.length > 0
				? resume.skills.map((skill) => ({
						name: skill.name,
						keywords: [...skill.keywords],
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
	};
}
