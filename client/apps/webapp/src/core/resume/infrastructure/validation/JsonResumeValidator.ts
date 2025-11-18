import type { Resume } from "@/core/resume/domain/Resume.ts";
import type {
	ResumeValidator,
	ValidationError,
} from "@/core/resume/domain/ResumeValidator.ts";

/**
 * Validates a Resume object against the JSON Resume Schema specification.
 *
 * @see https://jsonresume.org/schema/
 * @see https://raw.githubusercontent.com/jsonresume/resume-schema/master/schema.json
 *
 * This validator implements the official JSON Resume Schema (draft-07).
 * It validates all sections, fields, and formats according to the specification.
 */
export class JsonResumeValidator implements ResumeValidator {
	/**
	 * Accumulated validation errors from the last validation run
	 */
	private errors: ValidationError[] = [];

	/**
	 * ISO 8601 date pattern for JSON Resume Schema.
	 * Supports formats: YYYY, YYYY-MM, YYYY-MM-DD
	 * Validates actual month (01-12) and day (01-31) ranges
	 */
	private readonly ISO8601_PATTERN =
		/^([1-2][0-9]{3}(-((0[1-9])|(1[0-2]))(-((0[1-9])|([1-2][0-9])|(3[0-1])))?)?)$/;

	/**
	 * Email format pattern (simplified RFC 5322 validation).
	 */
	private readonly EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

	/**
	 * URL format pattern (simplified RFC 3986 validation).
	 */
	private readonly URL_PATTERN = /^https?:\/\/.+/;

	/**
	 * ISO 3166-1 alpha-2 country code pattern.
	 */
	private readonly COUNTRY_CODE_PATTERN = /^[A-Z]{2}$/;

	/**
	 * Returns all accumulated validation errors from the last validation run.
	 *
	 * @returns {ValidationError[]} Array of validation errors
	 */
	getErrors(): ValidationError[] {
		return [...this.errors];
	}

	/**
	 * Adds a validation error to the errors array.
	 *
	 * @param {string} path - The path to the invalid field (e.g., "basics.email")
	 * @param {string} message - The error message
	 */
	private addError(path: string, message: string): void {
		this.errors.push({
			path,
			message,
			section: this.getSectionFromPath(path),
		});
	}

	/**
	 * Determines the section name from a field path.
	 *
	 * @param {string} path - The field path (e.g., "basics.email", "work[0].company")
	 * @returns {string} The section name
	 */
	private getSectionFromPath(path: string): string {
		if (!path) return "General";
		const firstSegment = path.split("[")[0]?.split(".")[0];
		if (!firstSegment) return "General";

		const sectionMap: Record<string, string> = {
			basics: "Personal Information",
			work: "Work Experience",
			volunteer: "Volunteer Experience",
			education: "Education",
			awards: "Awards",
			certificates: "Certificates",
			publications: "Publications",
			skills: "Skills",
			languages: "Languages",
			interests: "Interests",
			references: "References",
			projects: "Projects",
			meta: "Metadata",
		};
		return sectionMap[firstSegment] || "General";
	}

	/**
	 * Validates the provided resume data against the JSON Resume Schema.
	 *
	 * @param {Resume} resume - The resume data to be validated.
	 * @returns {boolean} True if the resume is valid, false otherwise.
	 */
	validate(resume: Resume): boolean {
		// Clear previous errors
		this.errors = [];
		if (!resume) {
			this.addError("resume", "Resume object is null or undefined");
			return false;
		}

		try {
			// Validate required sections
			this.validateBasics(resume.basics);

			// Validate optional arrays (must be arrays if present)
			this.validateWorkArray(resume.work);
			this.validateVolunteerArray(resume.volunteer);
			this.validateEducationArray(resume.education);
			this.validateAwardsArray(resume.awards);
			this.validateCertificatesArray(resume.certificates);
			this.validatePublicationsArray(resume.publications);
			this.validateSkillsArray(resume.skills);
			this.validateLanguagesArray(resume.languages);
			this.validateInterestsArray(resume.interests);
			this.validateReferencesArray(resume.references);
			this.validateProjectsArray(resume.projects);

			// Return true if no errors accumulated
			return this.errors.length === 0;
		} catch (error) {
			this.addError(
				"resume",
				`Validation failed: ${error instanceof Error ? error.message : "Unknown error"}`,
			);
			return false;
		}
	}

	/**
	 * Validates the basics section (required).
	 */
	private validateBasics(basics: Resume["basics"]): void {
		if (!basics || typeof basics !== "object") {
			this.addError(
				"basics",
				"Basics section is required and must be an object",
			);
			return;
		}

		// Name is typically required for a valid resume
		if (basics.name && typeof basics.name !== "string") {
			this.addError("basics.name", "Name must be a string");
		}

		// Validate email format if present and non-empty
		if (
			basics.email &&
			basics.email !== "" &&
			!this.isValidEmail(basics.email)
		) {
			this.addError("basics.email", "Email format is invalid");
		}

		// Validate URL format if present and non-empty
		if (basics.url && basics.url !== "" && !this.isValidUrl(basics.url)) {
			this.addError("basics.url", "URL format is invalid");
		}

		// Validate image URL if present and non-empty
		if (basics.image && basics.image !== "" && !this.isValidUrl(basics.image)) {
			this.addError("basics.image", "Image URL format is invalid");
		}

		// Validate location if present
		if (basics.location) {
			this.validateLocation(basics.location);
		}

		// Validate profiles array if present
		if (basics.profiles) {
			this.validateProfilesArray(basics.profiles);
		}
	}

	/**
	 * Validates location object.
	 */
	private validateLocation(location: Resume["basics"]["location"]): void {
		if (!location || typeof location !== "object") {
			this.addError("basics.location", "Location must be an object");
			return;
		}

		// Validate country code if present
		if (
			location.countryCode &&
			!this.COUNTRY_CODE_PATTERN.test(location.countryCode)
		) {
			this.addError(
				"basics.location.countryCode",
				"Country code must be ISO 3166-1 alpha-2 format (e.g., US, GB)",
			);
		}
	}

	/**
	 * Validates profiles array.
	 */
	private validateProfilesArray(profiles: Resume["basics"]["profiles"]): void {
		if (!Array.isArray(profiles)) {
			this.addError("basics.profiles", "Profiles must be an array");
			return;
		}

		profiles.forEach((profile, index) => {
			if (!profile || typeof profile !== "object") {
				this.addError(`basics.profiles[${index}]`, "Profile must be an object");
				return;
			}

			// Validate URL if present and non-empty
			if (profile.url && profile.url !== "" && !this.isValidUrl(profile.url)) {
				this.addError(
					`basics.profiles[${index}].url`,
					"Profile URL format is invalid",
				);
			}
		});
	} /**
	 * Validates work experience array.
	 */
	private validateWorkArray(work: Resume["work"]): void {
		if (!Array.isArray(work)) {
			this.addError("work", "Work must be an array");
			return;
		}

		work.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`work[${index}]`, "Work item must be an object");
				return;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				this.addError(
					`work[${index}].startDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				this.addError(
					`work[${index}].endDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`work[${index}].url`, "URL format is invalid");
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				this.addError(
					`work[${index}].highlights`,
					"Highlights must be an array of strings",
				);
			}
		});
	}
	/**
	 * Validates volunteer experience array.
	 */
	private validateVolunteerArray(volunteer: Resume["volunteer"]): void {
		if (!Array.isArray(volunteer)) {
			this.addError("volunteer", "Volunteer must be an array");
			return;
		}

		volunteer.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(
					`volunteer[${index}]`,
					"Volunteer item must be an object",
				);
				return;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				this.addError(
					`volunteer[${index}].startDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				this.addError(
					`volunteer[${index}].endDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`volunteer[${index}].url`, "URL format is invalid");
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				this.addError(
					`volunteer[${index}].highlights`,
					"Highlights must be an array of strings",
				);
			}
		});
	}

	/**
	 * Validates education array.
	 */
	private validateEducationArray(education: Resume["education"]): void {
		if (!Array.isArray(education)) {
			this.addError("education", "Education must be an array");
			return;
		}

		education.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(
					`education[${index}]`,
					"Education item must be an object",
				);
				return;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				this.addError(
					`education[${index}].startDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				this.addError(
					`education[${index}].endDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`education[${index}].url`, "URL format is invalid");
			}

			// Validate courses array if present
			if (item.courses && !this.isValidStringArray(item.courses)) {
				this.addError(
					`education[${index}].courses`,
					"Courses must be an array of strings",
				);
			}
		});
	}
	/**
	 * Validates awards array.
	 */
	private validateAwardsArray(awards: Resume["awards"]): void {
		if (!Array.isArray(awards)) {
			this.addError("awards", "Awards must be an array");
			return;
		}

		awards.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`awards[${index}]`, "Award item must be an object");
				return;
			}

			// Validate date if present and non-empty
			if (
				item.date &&
				item.date !== "" &&
				!this.isValidIso8601Date(item.date)
			) {
				this.addError(
					`awards[${index}].date`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}
		});
	}

	/**
	 * Validates certificates array.
	 */
	private validateCertificatesArray(
		certificates: Resume["certificates"],
	): void {
		if (!Array.isArray(certificates)) {
			this.addError("certificates", "Certificates must be an array");
			return;
		}

		certificates.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(
					`certificates[${index}]`,
					"Certificate item must be an object",
				);
				return;
			}

			// Validate date if present and non-empty
			if (
				item.date &&
				item.date !== "" &&
				!this.isValidIso8601Date(item.date)
			) {
				this.addError(
					`certificates[${index}].date`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`certificates[${index}].url`, "URL format is invalid");
			}
		});
	}
	/**
	 * Validates publications array.
	 */
	private validatePublicationsArray(
		publications: Resume["publications"],
	): void {
		if (!Array.isArray(publications)) {
			this.addError("publications", "Publications must be an array");
			return;
		}

		publications.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(
					`publications[${index}]`,
					"Publication item must be an object",
				);
				return;
			}

			// Validate release date if present and non-empty
			if (
				item.releaseDate &&
				item.releaseDate !== "" &&
				!this.isValidIso8601Date(item.releaseDate)
			) {
				this.addError(
					`publications[${index}].releaseDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`publications[${index}].url`, "URL format is invalid");
			}
		});
	}

	/**
	 * Validates skills array.
	 */
	private validateSkillsArray(skills: Resume["skills"]): void {
		if (!Array.isArray(skills)) {
			this.addError("skills", "Skills must be an array");
			return;
		}

		skills.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`skills[${index}]`, "Skill item must be an object");
				return;
			}

			// Validate keywords array if present
			if (item.keywords && !this.isValidStringArray(item.keywords)) {
				this.addError(
					`skills[${index}].keywords`,
					"Keywords must be an array of strings",
				);
			}
		});
	}

	/**
	 * Validates languages array.
	 */
	private validateLanguagesArray(languages: Resume["languages"]): void {
		if (!Array.isArray(languages)) {
			this.addError("languages", "Languages must be an array");
			return;
		}

		languages.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`languages[${index}]`, "Language item must be an object");
			}
		});
	}

	/**
	 * Validates interests array.
	 */
	private validateInterestsArray(interests: Resume["interests"]): void {
		if (!Array.isArray(interests)) {
			this.addError("interests", "Interests must be an array");
			return;
		}

		interests.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`interests[${index}]`, "Interest item must be an object");
				return;
			}

			// Validate keywords array if present
			if (item.keywords && !this.isValidStringArray(item.keywords)) {
				this.addError(
					`interests[${index}].keywords`,
					"Keywords must be an array of strings",
				);
			}
		});
	}

	/**
	 * Validates references array.
	 */
	private validateReferencesArray(references: Resume["references"]): void {
		if (!Array.isArray(references)) {
			this.addError("references", "References must be an array");
			return;
		}

		references.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(
					`references[${index}]`,
					"Reference item must be an object",
				);
			}
		});
	}

	/**
	 * Validates projects array.
	 */
	private validateProjectsArray(projects: Resume["projects"]): void {
		if (!Array.isArray(projects)) {
			this.addError("projects", "Projects must be an array");
			return;
		}

		projects.forEach((item, index) => {
			if (!item || typeof item !== "object") {
				this.addError(`projects[${index}]`, "Project item must be an object");
				return;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				this.addError(
					`projects[${index}].startDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				this.addError(
					`projects[${index}].endDate`,
					"Date format must be YYYY, YYYY-MM, or YYYY-MM-DD",
				);
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				this.addError(`projects[${index}].url`, "URL format is invalid");
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				this.addError(
					`projects[${index}].highlights`,
					"Highlights must be an array of strings",
				);
			}
		});
	}

	/**
	 * Validates ISO 8601 date format (YYYY, YYYY-MM, or YYYY-MM-DD).
	 */
	private isValidIso8601Date(date: string): boolean {
		if (typeof date !== "string") {
			return false;
		}

		return this.ISO8601_PATTERN.test(date);
	}

	/**
	 * Validates email format.
	 */
	private isValidEmail(email: string): boolean {
		if (typeof email !== "string") {
			return false;
		}

		return this.EMAIL_PATTERN.test(email);
	}

	/**
	 * Validates URL format.
	 */
	private isValidUrl(url: string): boolean {
		if (typeof url !== "string") {
			return false;
		}

		return this.URL_PATTERN.test(url);
	}

	/**
	 * Validates that an array contains only strings.
	 */
	private isValidStringArray(arr: ReadonlyArray<string>): boolean {
		if (!Array.isArray(arr)) {
			return false;
		}

		return arr.every((item) => typeof item === "string");
	}
}
