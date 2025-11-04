/**
 * TypeScript types for Resume Generator (JSON Resume Schema v1.0.0)
 * Based on: https://jsonresume.org/schema
 */

export type Resume = {
	basics: PersonalInfo;
	work?: WorkExperience[];
	education?: Education[];
	skills?: SkillCategory[];
	languages?: Language[];
	projects?: Project[];
};

// Alias for compatibility with component props
export type ResumeData = {
	personalInfo?: PersonalInfo;
	workExperience?: WorkExperience[];
	education?: Education[];
	skills?: SkillCategory[];
	languages?: Language[];
	projects?: Project[];
};

export type PersonalInfo = {
	name: string;
	label?: string; // Job title
	title?: string; // Alias for label (for compatibility)
	email: string;
	phone?: string;
	url?: string; // Personal website
	summary?: string;
	location?: Location;
	profiles?: SocialProfile[];
};

export type Location = {
	address?: string;
	postalCode?: string;
	city?: string;
	countryCode?: string;
	region?: string;
};

export type SocialProfile = {
	network: string; // e.g., "Twitter", "LinkedIn"
	username: string;
	url: string;
};

export type WorkExperience = {
	company: string;
	position: string;
	startDate: string; // ISO 8601 format (YYYY-MM-DD)
	endDate?: string; // ISO 8601 format or null if current
	location?: string;
	summary?: string;
	highlights?: string[];
	url?: string; // Company website
};

export type Education = {
	institution: string;
	area: string; // Field of study (e.g., "Computer Science")
	studyType: string; // Degree type (e.g., "Bachelor", "Master")
	startDate: string; // ISO 8601 format (YYYY-MM-DD)
	endDate?: string; // ISO 8601 format or null if ongoing
	score?: string; // GPA or grade
	courses?: string[];
};

export type SkillCategory = {
	category: string; // Category name (e.g., "Programming Languages", "Frameworks")
	name?: string; // Alias for category
	level?: string; // Proficiency level (e.g., "Beginner", "Intermediate", "Advanced", "Expert")
	keywords: string[]; // Individual skills (e.g., ["JavaScript", "TypeScript"])
};

export type Language = {
	language: string; // Language name (e.g., "English", "Spanish")
	fluency: string; // Proficiency level (e.g., "Native", "Fluent", "Intermediate")
};

export type Project = {
	name: string;
	description?: string;
	startDate?: string; // ISO 8601 format (YYYY-MM-DD)
	endDate?: string; // ISO 8601 format or null if ongoing
	url?: string; // Project website or repository
	entity?: string; // Organization or company (if applicable)
	type?: string; // Project type (e.g., "Open Source", "Commercial")
	highlights?: string[];
	keywords?: string[]; // Technologies used
	roles?: string[]; // Roles in the project
};

/**
 * API request/response types
 */

export type GenerateResumeRequest = {
	resumeData: Resume;
	locale?: string; // "en" or "es"
};

export type ResumeGenerationState = {
	isGenerating: boolean;
	progress?: number; // 0-100
	error?: ProblemDetail;
};

/**
 * RFC 7807 Problem Details for HTTP APIs
 * Standard format for API error responses
 */
export type ProblemDetail = {
	type?: string; // URI reference identifying the problem type
	title?: string; // Short, human-readable summary
	status: number; // HTTP status code
	detail?: string; // Human-readable explanation
	instance?: string; // URI reference identifying the specific occurrence
	timestamp?: string; // When the error occurred
	errorCategory?: string; // Category of the error (e.g., "VALIDATION", "INTERNAL_ERROR")
	fieldErrors?: Record<string, string>; // Field-level validation errors
	retryAfterSeconds?: number; // For rate limiting errors
	[key: string]: unknown; // Allow additional properties
};

/**
 * @deprecated Use ProblemDetail instead
 */
export type ApiError = ProblemDetail;

/**
 * @deprecated Use ProblemDetail.fieldErrors instead
 */
export type FieldError = {
	field: string;
	message: string;
	rejectedValue?: unknown;
};
