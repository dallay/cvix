import { z } from "zod";

/**
 * Zod validation schemas for Resume Generator
 * Field length limits per spec.md FR-004:
 * - Names/titles: 100 characters
 * - Descriptions: 500 characters
 * - Skills: 50 characters
 */

// Date validation helper (YYYY-MM-DD format)
const isoDateString = z
	.string()
	.regex(/^\d{4}-\d{2}-\d{2}$/, "Must be in YYYY-MM-DD format");

// Location schema
export const locationSchema = z.object({
	address: z.string().max(100).optional(),
	postalCode: z.string().max(20).optional(),
	city: z.string().max(100).optional(),
	countryCode: z.string().length(2).optional(), // ISO 3166-1 alpha-2
	region: z.string().max(100).optional(),
});

// Social profile schema
export const socialProfileSchema = z.object({
	network: z.string().min(1).max(50),
	username: z.string().min(1).max(100),
	url: z.string().url().max(500),
});

// Personal info schema
export const personalInfoSchema = z.object({
	name: z
		.string()
		.min(1, "Full name is required")
		.max(100, "Name cannot exceed 100 characters"),
	label: z.string().max(100).optional(), // Job title
	email: z
		.string()
		.email("Must be a valid email address")
		.max(100, "Email cannot exceed 100 characters"),
	phone: z.string().max(30).optional(),
	url: z.string().url().max(500).optional(),
	summary: z.string().max(500).optional(),
	location: locationSchema.optional(),
	profiles: z.array(socialProfileSchema).optional(),
});

// Work experience schema
export const workExperienceSchema = z
	.object({
		company: z
			.string()
			.min(1, "Company name is required")
			.max(100, "Company name cannot exceed 100 characters"),
		position: z
			.string()
			.min(1, "Position is required")
			.max(100, "Position cannot exceed 100 characters"),
		startDate: isoDateString,
		endDate: isoDateString.optional(),
		location: z.string().max(100).optional(),
		summary: z.string().max(500).optional(),
		highlights: z.array(z.string().max(500)).optional(),
		url: z.string().url().max(500).optional(),
	})
	.refine(
		(data) => {
			if (data.endDate) {
				return new Date(data.startDate) <= new Date(data.endDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Education schema
export const educationSchema = z
	.object({
		institution: z
			.string()
			.min(1, "Institution name is required")
			.max(100, "Institution name cannot exceed 100 characters"),
		area: z
			.string()
			.min(1, "Field of study is required")
			.max(100, "Field of study cannot exceed 100 characters"),
		studyType: z
			.string()
			.min(1, "Degree type is required")
			.max(100, "Degree type cannot exceed 100 characters"),
		startDate: isoDateString,
		endDate: isoDateString.optional(),
		score: z.string().max(20).optional(),
		courses: z.array(z.string().max(100)).optional(),
	})
	.refine(
		(data) => {
			if (data.endDate) {
				return new Date(data.startDate) <= new Date(data.endDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Skill category schema
export const skillCategorySchema = z.object({
	name: z
		.string()
		.min(1, "Category name is required")
		.max(100, "Category name cannot exceed 100 characters"),
	level: z.enum(["Beginner", "Intermediate", "Advanced", "Expert"]).optional(),
	keywords: z
		.array(
			z
				.string()
				.min(1, "Skill cannot be empty")
				.max(50, "Skill cannot exceed 50 characters"),
		)
		.min(1, "At least one skill is required"),
});

// Language schema
export const languageSchema = z.object({
	language: z
		.string()
		.min(1, "Language name is required")
		.max(50, "Language name cannot exceed 50 characters"),
	fluency: z
		.string()
		.min(1, "Fluency level is required")
		.max(50, "Fluency level cannot exceed 50 characters"),
});

// Project schema
export const projectSchema = z
	.object({
		name: z
			.string()
			.min(1, "Project name is required")
			.max(100, "Project name cannot exceed 100 characters"),
		description: z.string().max(500).optional(),
		startDate: isoDateString.optional(),
		endDate: isoDateString.optional(),
		url: z.string().url().max(500).optional(),
		entity: z.string().max(100).optional(),
		type: z.string().max(50).optional(),
		highlights: z.array(z.string().max(500)).optional(),
		keywords: z.array(z.string().max(50)).optional(),
		roles: z.array(z.string().max(100)).optional(),
	})
	.refine(
		(data) => {
			if (data.startDate && data.endDate) {
				return new Date(data.startDate) <= new Date(data.endDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Complete resume schema
export const resumeSchema = z
	.object({
		basics: personalInfoSchema,
		work: z.array(workExperienceSchema).optional(),
		education: z.array(educationSchema).optional(),
		skills: z.array(skillCategorySchema).optional(),
		languages: z.array(languageSchema).optional(),
		projects: z.array(projectSchema).optional(),
	})
	.refine(
		(data) => {
			// Resume must have at least one content section (FR-001)
			const hasWork = data.work && data.work.length > 0;
			const hasEducation = data.education && data.education.length > 0;
			const hasSkills = data.skills && data.skills.length > 0;
			return hasWork || hasEducation || hasSkills;
		},
		{
			message:
				"Resume must have at least one of: work experience, education, or skills",
			path: ["basics"], // Show error at root level
		},
	);

// Export type inference
export type ResumeFormData = z.infer<typeof resumeSchema>;
