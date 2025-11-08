/**
 * Root model for the resume, based on the JSON Resume standard.
 *
 * @see https://jsonresume.org/schema/
 * @see https://docs.jsonresume.org/schema
 *
 * This interface defines all core sections of a professional resume.
 * Each property corresponds to a section in the JSON Resume schema.
 */
export interface Resume {
	/** Personal and contact information */
	basics: Basics;

	/** Work experience history */
	work: ReadonlyArray<Work>;

	/** Volunteer experience and nonprofit contributions */
	volunteer: ReadonlyArray<Volunteer>;

	/** Formal education background */
	education: ReadonlyArray<Education>;

	/** Awards and recognitions */
	awards: ReadonlyArray<Award>;

	/** Professional certificates and credentials */
	certificates: ReadonlyArray<Certificate>;

	/** Authored publications or media features */
	publications: ReadonlyArray<Publication>;

	/** Skills, technical expertise, and proficiencies */
	skills: ReadonlyArray<Skill>;

	/** Languages known and proficiency levels */
	languages: ReadonlyArray<Language>;

	/** Hobbies and personal interests */
	interests: ReadonlyArray<Interest>;

	/** References and recommendations */
	references: ReadonlyArray<Reference>;

	/** Professional or personal projects */
	projects: ReadonlyArray<Project>;
}

/**
 * Core personal and contact information.
 * @see https://jsonresume.org/schema/#basics
 */
export interface Basics {
	/** Full name of the individual */
	name: string;

	/** Professional title or short descriptor (e.g., “Software Engineer”) */
	label: string;

	/** Profile or avatar image URL */
	image: string;

	/** Primary email address */
	email: string;

	/** Contact phone number (international format preferred) */
	phone: string;

	/** Personal or portfolio website */
	url: string;

	/** Short professional summary */
	summary: string;

	/** Geographic location details */
	location: Location;

	/** Links to social or professional profiles */
	profiles: ReadonlyArray<Profile>;
}

/**
 * Location information.
 * @see https://jsonresume.org/schema/#location
 */
export interface Location {
	/** Street address */
	address: string;

	/** Postal or ZIP code */
	postalCode: string;

	/** City or locality */
	city: string;

	/** ISO 3166-1 alpha-2 country code (e.g., "US", "DE") */
	countryCode: string;

	/** State, province, or region */
	region: string;
}

/**
 * Social or professional networking profile.
 * @see https://jsonresume.org/schema/#profiles
 */
export interface Profile {
	/** Network name (e.g., LinkedIn, GitHub, Twitter) */
	network: string;

	/** Username or handle on the platform */
	username: string;

	/** Profile URL */
	url: string;
}

/**
 * Work experience entry.
 * @see https://jsonresume.org/schema/#work
 */
export interface Work {
	/** Company or employer name */
	name: string;

	/** Position or job title */
	position: string;

	/** Company or job URL */
	url: string;

	/** Start date in ISO 8601 format (YYYY-MM or YYYY-MM-DD) */
	startDate: string;

	/** End date in ISO 8601 format, or empty if ongoing */
	endDate: string;

	/** Summary of responsibilities and achievements */
	summary: string;

	/** Key highlights or notable achievements */
	highlights: ReadonlyArray<string>;
}

/**
 * Volunteer experience entry.
 * @see https://jsonresume.org/schema/#volunteer
 */
export interface Volunteer {
	/** Organization or nonprofit name */
	organization: string;

	/** Role or position held */
	position: string;

	/** Organization website or URL */
	url: string;

	/** Start date in ISO 8601 format */
	startDate: string;

	/** End date in ISO 8601 format, or empty if ongoing */
	endDate: string;

	/** Summary of contributions */
	summary: string;

	/** Key highlights or outcomes */
	highlights: ReadonlyArray<string>;
}

/**
 * Education background entry.
 * @see https://jsonresume.org/schema/#education
 */
export interface Education {
	/** Educational institution name */
	institution: string;

	/** Institution or program URL */
	url: string;

	/** Field of study (e.g., “Computer Science”) */
	area: string;

	/** Type of degree or certification (e.g., “Bachelor”, “Master”) */
	studyType: string;

	/** Start date in ISO 8601 format */
	startDate: string;

	/** End date in ISO 8601 format */
	endDate: string;

	/** Final score, GPA, or grade */
	score: string;

	/** Relevant courses completed */
	courses: ReadonlyArray<string>;
}

/**
 * Award or professional recognition.
 * @see https://jsonresume.org/schema/#awards
 */
export interface Award {
	/** Title of the award */
	title: string;

	/** Date received (ISO 8601) */
	date: string;

	/** Organization or entity that granted the award */
	awarder: string;

	/** Summary or description of the achievement */
	summary: string;
}

/**
 * Professional certificate or accreditation.
 * @see https://jsonresume.org/schema/#certificates
 */
export interface Certificate {
	/** Certificate name or title */
	name: string;

	/** Date obtained (ISO 8601) */
	date: string;

	/** Issuing organization */
	issuer: string;

	/** Verification or credential URL */
	url: string;
}

/**
 * Publication or article authored by the individual.
 * @see https://jsonresume.org/schema/#publications
 */
export interface Publication {
	/** Title of the publication */
	name: string;

	/** Publisher or platform name */
	publisher: string;

	/** Release date in ISO 8601 format */
	releaseDate: string;

	/** Publication URL */
	url: string;

	/** Short description or abstract */
	summary: string;
}

/**
 * Skill set or area of expertise.
 * @see https://jsonresume.org/schema/#skills
 */
export interface Skill {
	/** Skill category (e.g., “Web Development”, “Leadership”) */
	name: string;

	/** Self-assessed proficiency level (e.g., “Beginner”, “Expert”) */
	level: string;

	/** Related tools, technologies, or sub-skills */
	keywords: ReadonlyArray<string>;
}

/**
 * Spoken or written language proficiency.
 * @see https://jsonresume.org/schema/#languages
 */
export interface Language {
	/** Language name (e.g., “English”, “Spanish”) */
	language: string;

	/** Level of fluency (e.g., “Native”, “Professional”) */
	fluency: string;
}

/**
 * Personal interest or hobby.
 * @see https://jsonresume.org/schema/#interests
 */
export interface Interest {
	/** Interest name or category */
	name: string;

	/** Related keywords or topics */
	keywords: ReadonlyArray<string>;
}

/**
 * Personal or professional reference.
 * @see https://jsonresume.org/schema/#references
 */
export interface Reference {
	/** Name of the referee */
	name: string;

	/** Reference text or testimonial */
	reference: string;
}

/**
 * Project undertaken professionally or personally.
 * @see https://jsonresume.org/schema/#projects
 */
export interface Project {
	/** Project name or title */
	name: string;

	/** Start date in ISO 8601 format */
	startDate: string;

	/** End date in ISO 8601 format */
	endDate: string;

	/** Description or project summary */
	description: string;

	/** Notable results, metrics, or achievements */
	highlights: ReadonlyArray<string>;

	/** Project URL or repository link */
	url: string;
}

/**
 * Job description structure based on JSON Resume Job Schema.
 * Useful for job listings, templates, or parsing postings.
 *
 * @see https://jsonresume.org/job-description-schema
 */
export interface JobDescription {
	/** Job title (e.g., “Web Developer”) */
	title: string;

	/** Company or organization offering the position */
	company: string;

	/** Employment type (e.g., “Full-time”, “Part-time”, “Contract”) */
	type: string;

	/** Date posted or relevant date (ISO 8601) */
	date: string;

	/** Summary or job overview */
	description: string;

	/** Work location details */
	location: Location;

	/** Work arrangement (e.g., “Remote”, “Hybrid”, “On-site”) */
	remote: string;

	/** Annual or monthly salary range */
	salary: string;

	/** Experience level (e.g., “Entry-level”, “Mid-level”, “Senior”) */
	experience: string;

	/** Core responsibilities or daily tasks */
	responsibilities: ReadonlyArray<string>;

	/** Required qualifications or background */
	qualifications: ReadonlyArray<string>;

	/** Desired skills or technical stack */
	skills: ReadonlyArray<Skill>;
}
