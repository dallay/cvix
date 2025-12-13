import type { Resume } from "@/core/resume/domain/Resume.ts";
import type {
	GenerateResumeRequest,
	ResumeRequest,
} from "@/core/resume/infrastructure/http/requests/ResumeRequest.ts";
import type { Reference } from "../../domain/Reference";

/**
 * Normalize an optional string value by trimming whitespace and
 * returning undefined for non-string inputs or empty strings.
 *
 * @param value - The input value to normalize
 * @returns The trimmed string, or undefined if input is not a string or is empty after trimming
 */
function normalizeOptionalString(value?: string): string | undefined {
	if (typeof value !== "string") return undefined;
	const trimmed = value.trim();
	return trimmed.length > 0 ? trimmed : undefined;
}

/**
 * Map the basics section of a Resume domain object to the backend DTO.
 * Ensures optional text fields are normalized and required fields are provided
 * in the expected shape for the API.
 *
 * @param resume - The Resume domain object
 * @returns An object containing mapped basics fields suitable for the backend
 */
const mapBasics = (resume: Resume) => {
	return {
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
	};
};

/**
 * Map the work section of a Resume domain object to the backend DTO.
 * Converts arrays and optional fields into the shape expected by the API.
 *
 * @param resume - The Resume domain object
 * @returns An array of mapped work entries or undefined when none are present
 */
const mapWork = (resume: Resume) => {
	return resume.work.length > 0
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
						: [],
			}))
		: undefined;
};

/**
 * Map the education section of a Resume domain object to the backend DTO.
 * Normalizes optional text fields and filters out empty course entries.
 *
 * @param resume - The Resume domain object
 * @returns An array of mapped education entries or undefined when none are present
 */
const mapEducation = (resume: Resume) => {
	return resume.education.length > 0
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
		: undefined;
};

/**
 * Map the skills section of a Resume domain object to the backend DTO.
 *
 * @param resume - The Resume domain object
 * @returns An array of mapped skills or undefined when none are present
 */
const mapSkills = (resume: Resume) => {
	return resume.skills.length > 0
		? resume.skills.map((skill) => ({
				name: skill.name,
				level: skill.level,
				keywords: Array.from(skill.keywords),
			}))
		: undefined;
};

/**
 * Map the languages section of a Resume domain object to the backend DTO.
 *
 * @param resume - The Resume domain object
 * @returns An array of mapped language entries or undefined when none are present
 */
const mapLanguages = (resume: Resume) => {
	return resume.languages.length > 0
		? resume.languages.map((lang) => ({
				language: lang.language,
				fluency: lang.fluency,
			}))
		: undefined;
};
/**
 * Maps the projects section of a Resume object.
 * @param resume - The Resume object containing projects
 * @returns An array of mapped projects or undefined if none exist
 */
const mapProjects = (resume: Resume) => {
	return resume.projects.length > 0
		? resume.projects.map((project) => ({
				name: project.name,
				description: project.description,
				url: project.url || undefined,
				startDate: project.startDate || undefined,
				endDate: project.endDate || undefined,
				highlights:
					project.highlights && project.highlights.length > 0
						? Array.from(project.highlights)
						: [],
			}))
		: undefined;
};
/**
 * Maps the volunteer section of a Resume object.
 * @param resume - The Resume object containing volunteer experiences
 * @returns An array of mapped volunteer experiences or undefined if none exist
 */
const mapVolunteer = (resume: Resume) => {
	return resume.volunteer.length > 0
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
						: [],
			}))
		: undefined;
};
/**
 * Maps the awards section of a Resume object.
 * @param resume - The Resume object containing awards
 * @returns An array of mapped awards or undefined if none exist
 */
const mapAwards = (resume: Resume) => {
	return resume.awards.length > 0
		? resume.awards.map((award) => ({
				title: award.title,
				date: award.date,
				awarder: award.awarder,
				summary: award.summary || undefined,
			}))
		: undefined;
};
/**
 * Maps the certificates section of a Resume object.
 * @param resume - The Resume object containing certificates
 * @returns An array of mapped certificates or undefined if none exist
 */
const mapCertificates = (resume: Resume) => {
	return resume.certificates.length > 0
		? resume.certificates.map((cert) => ({
				name: cert.name,
				issuer: cert.issuer,
				date: cert.date,
				url: normalizeOptionalString(cert.url),
			}))
		: undefined;
};
/**
 * Maps the publications section of a Resume object.
 * @param resume - The Resume object containing publications
 * @returns An array of mapped publications or undefined if none exist
 */
const mapPublications = (resume: Resume) => {
	return resume.publications.length > 0
		? resume.publications.map((pub) => ({
				name: pub.name,
				publisher: pub.publisher,
				releaseDate: pub.releaseDate,
				url: pub.url || undefined,
				summary: pub.summary || undefined,
			}))
		: undefined;
};
/**
 * Maps the interests section of a Resume object.
 * @param resume - The Resume object containing interests
 * @returns An array of mapped interests or undefined if none exist
 */
const mapInterests = (resume: Resume) => {
	return resume.interests.length > 0
		? resume.interests.map((interest) => ({
				name: interest.name,
				keywords: Array.from(interest.keywords),
			}))
		: undefined;
};
/**
 * Maps the references section of a Resume object.
 * @param resume - The Resume object containing references
 * @returns An array of mapped references or undefined if none exist
 */
const mapReferences = (resume: Resume) => {
	return resume.references.length > 0
		? resume.references.map((ref: Reference) => ({
				name: ref.name,
				reference: normalizeOptionalString(ref.reference),
			}))
		: undefined;
};

/**
 * Maps a Resume object (JSON Resume schema) to the backend's GenerateResumeRequest format.
 *
 * @param templateId - The template ID to be used for resume generation
 * @param resume - The frontend Resume object following JSON Resume schema
 * @returns The backend DTO in GenerateResumeRequest format
 */
export function mapResumeToGenerateResumeRequest(
	templateId: string,
	resume: Resume,
): GenerateResumeRequest {
	return {
		templateId,
		basics: mapBasics(resume),
		work: mapWork(resume),
		education: mapEducation(resume),
		skills: mapSkills(resume),
		languages: mapLanguages(resume),
		projects: mapProjects(resume),
		volunteer: mapVolunteer(resume),
		awards: mapAwards(resume),
		certificates: mapCertificates(resume),
		publications: mapPublications(resume),
		interests: mapInterests(resume),
		references: mapReferences(resume),
	};
}

/**
 * Maps a Resume object to a ResumeRequest DTO for backend communication.
 * @param resume - The Resume object to map
 * @returns The mapped ResumeRequest DTO
 */
export function mapResumeToResumeRequest(resume: Resume): ResumeRequest {
	return {
		basics: mapBasics(resume),
		work: mapWork(resume),
		education: mapEducation(resume),
		skills: mapSkills(resume),
		languages: mapLanguages(resume),
		projects: mapProjects(resume),
		volunteer: mapVolunteer(resume),
		awards: mapAwards(resume),
		certificates: mapCertificates(resume),
		publications: mapPublications(resume),
		interests: mapInterests(resume),
		references: mapReferences(resume),
	};
}
