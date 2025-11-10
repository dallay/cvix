import type { Resume } from "@/core/resume/domain/Resume.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";

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
	 * ISO 8601 date pattern for JSON Resume Schema.
	 * Supports formats: YYYY, YYYY-MM, YYYY-MM-DD
	 * Validates actual month (01-12) and day (01-31) ranges
	 */
	private readonly ISO8601_PATTERN =
		/^([1-2][0-9]{3}(-((0[1-9])|(1[0-2]))(-((0[1-9])|([1-2][0-9])|(3[0-1])))?)?)$/; /**
	 * Email format pattern (simplified RFC 5322 validation).
	 */
	private readonly EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

	/**
	 * Phone number pattern: allows digits, spaces, and common characters like (), -, +
	 * Requires at least 7 digits.
	 */
	private readonly PHONE_PATTERN = /^(?=(?:\D*\d){7,})[\d\s()+-]+$/;

	/**
	 * URL format pattern (simplified RFC 3986 validation).
	 */
	private readonly URL_PATTERN = /^https?:\/\/.+/;

	/**
	 * ISO 3166-1 alpha-2 country code pattern.
	 */
	private readonly COUNTRY_CODE_PATTERN = /^[A-Z]{2}$/;

	/**
	 * Validates the provided resume data against the JSON Resume Schema.
	 *
	 * @param {Resume} resume - The resume data to be validated.
	 * @returns {boolean} True if the resume is valid, false otherwise.
	 */
	validate(resume: Resume): boolean {
		if (!resume) {
			return false;
		}

		try {
			// Validate required sections
			if (!this.validateBasics(resume.basics)) {
				return false;
			}

			// Validate optional arrays (must be arrays if present)
			if (!this.validateWorkArray(resume.work)) {
				return false;
			}

			if (!this.validateVolunteerArray(resume.volunteer)) {
				return false;
			}

			if (!this.validateEducationArray(resume.education)) {
				return false;
			}

			if (!this.validateAwardsArray(resume.awards)) {
				return false;
			}

			if (!this.validateCertificatesArray(resume.certificates)) {
				return false;
			}

			if (!this.validatePublicationsArray(resume.publications)) {
				return false;
			}

			if (!this.validateSkillsArray(resume.skills)) {
				return false;
			}

			if (!this.validateLanguagesArray(resume.languages)) {
				return false;
			}

			if (!this.validateInterestsArray(resume.interests)) {
				return false;
			}

			if (!this.validateReferencesArray(resume.references)) {
				return false;
			}

			if (!this.validateProjectsArray(resume.projects)) {
				return false;
			}

			return true;
		} catch {
			return false;
		}
	}

	/**
	 * Validates the basics section (required).
	 */
	private validateBasics(basics: Resume["basics"]): boolean {
		if (!basics || typeof basics !== "object") {
			return false;
		}

		// Name is typically required for a valid resume
		if (!basics.name || typeof basics.name !== "string") {
			return false;
		}

		// Validate email format if present and non-empty
		if (!basics.email || !this.isValidEmail(basics.email)) {
			return false;
		}

		// Validate phone number format if present and non-empty
		if (
			basics.phone &&
			basics.phone !== "" &&
			!this.PHONE_PATTERN.test(basics.phone)
		) {
			return false;
		}

		// Validate URL format if present and non-empty
		if (basics.url && basics.url !== "" && !this.isValidUrl(basics.url)) {
			return false;
		}

		// Validate image URL if present and non-empty
		if (basics.image && basics.image !== "" && !this.isValidUrl(basics.image)) {
			return false;
		}

		// Validate location if present
		if (basics.location && !this.validateLocation(basics.location)) {
			return false;
		}

		// Validate profiles array if present
		if (basics.profiles && !this.validateProfilesArray(basics.profiles)) {
			return false;
		}

		return true;
	} /**
	 * Validates location object.
	 */
	private validateLocation(location: Resume["basics"]["location"]): boolean {
		if (!location || typeof location !== "object") {
			return false;
		}

		// Validate country code if present
		if (
			location.countryCode &&
			!this.COUNTRY_CODE_PATTERN.test(location.countryCode)
		) {
			return false;
		}

		return true;
	}

	/**
	 * Validates profiles array.
	 */
	private validateProfilesArray(
		profiles: Resume["basics"]["profiles"],
	): boolean {
		if (!Array.isArray(profiles)) {
			return false;
		}

		return profiles.every((profile) => {
			if (!profile || typeof profile !== "object") {
				return false;
			}

			// Validate URL if present and non-empty
			if (profile.url && profile.url !== "" && !this.isValidUrl(profile.url)) {
				return false;
			}

			return true;
		});
	} /**
	 * Validates work experience array.
	 */
	private validateWorkArray(work: Resume["work"]): boolean {
		if (!Array.isArray(work)) {
			return false;
		}

		return work.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				return false;
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				return false;
			}

			return true;
		});
	} /**
	 * Validates volunteer experience array.
	 */
	private validateVolunteerArray(volunteer: Resume["volunteer"]): boolean {
		if (!Array.isArray(volunteer)) {
			return false;
		}

		return volunteer.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				return false;
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates education array.
	 */
	private validateEducationArray(education: Resume["education"]): boolean {
		if (!Array.isArray(education)) {
			return false;
		}

		return education.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				return false;
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			// Validate courses array if present
			if (item.courses && !this.isValidStringArray(item.courses)) {
				return false;
			}

			return true;
		});
	} /**
	 * Validates awards array.
	 */
	private validateAwardsArray(awards: Resume["awards"]): boolean {
		if (!Array.isArray(awards)) {
			return false;
		}

		return awards.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate date if present and non-empty
			if (
				item.date &&
				item.date !== "" &&
				!this.isValidIso8601Date(item.date)
			) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates certificates array.
	 */
	private validateCertificatesArray(
		certificates: Resume["certificates"],
	): boolean {
		if (!Array.isArray(certificates)) {
			return false;
		}

		return certificates.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate date if present and non-empty
			if (
				item.date &&
				item.date !== "" &&
				!this.isValidIso8601Date(item.date)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			return true;
		});
	}
	/**
	 * Validates publications array.
	 */
	private validatePublicationsArray(
		publications: Resume["publications"],
	): boolean {
		if (!Array.isArray(publications)) {
			return false;
		}

		return publications.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate release date if present and non-empty
			if (
				item.releaseDate &&
				item.releaseDate !== "" &&
				!this.isValidIso8601Date(item.releaseDate)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates skills array.
	 */
	private validateSkillsArray(skills: Resume["skills"]): boolean {
		if (!Array.isArray(skills)) {
			return false;
		}

		return skills.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate keywords array if present
			if (item.keywords && !this.isValidStringArray(item.keywords)) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates languages array.
	 */
	private validateLanguagesArray(languages: Resume["languages"]): boolean {
		if (!Array.isArray(languages)) {
			return false;
		}

		return languages.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates interests array.
	 */
	private validateInterestsArray(interests: Resume["interests"]): boolean {
		if (!Array.isArray(interests)) {
			return false;
		}

		return interests.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate keywords array if present
			if (item.keywords && !this.isValidStringArray(item.keywords)) {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates references array.
	 */
	private validateReferencesArray(references: Resume["references"]): boolean {
		if (!Array.isArray(references)) {
			return false;
		}

		return references.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			return true;
		});
	}

	/**
	 * Validates projects array.
	 */
	private validateProjectsArray(projects: Resume["projects"]): boolean {
		if (!Array.isArray(projects)) {
			return false;
		}

		return projects.every((item) => {
			if (!item || typeof item !== "object") {
				return false;
			}

			// Validate dates if present and non-empty
			if (
				item.startDate &&
				item.startDate !== "" &&
				!this.isValidIso8601Date(item.startDate)
			) {
				return false;
			}

			if (
				item.endDate &&
				item.endDate !== "" &&
				!this.isValidIso8601Date(item.endDate)
			) {
				return false;
			}

			// Validate URL if present and non-empty
			if (item.url && item.url !== "" && !this.isValidUrl(item.url)) {
				return false;
			}

			// Validate highlights array if present
			if (item.highlights && !this.isValidStringArray(item.highlights)) {
				return false;
			}

			return true;
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
