import { z } from "zod";

/**
 * Zod validation schema for Resume (JSON Resume Schema v1.0.0)
 */

// Location schema
const locationSchema = z
	.object({
		address: z.string().optional(),
		postalCode: z.string().optional(),
		city: z.string().optional(),
		countryCode: z.string().optional(),
		region: z.string().optional(),
	})
	.optional();

// Social Profile schema
const socialProfileSchema = z.object({
	network: z.string().min(1),
	username: z.string().min(1),
	url: z.string().url(),
});

// Personal Info (basics) schema
const personalInfoSchema = z.object({
	name: z
		.string()
		.min(1, "Name is required")
		.max(100, "Name must be 100 characters or less"),
	label: z.string().max(100).optional(),
	email: z.string().email("Invalid email address"),
	phone: z.string().optional(),
	url: z.string().url("Invalid URL").optional().or(z.literal("")),
	summary: z
		.string()
		.max(500, "Summary must be 500 characters or less")
		.optional(),
	location: locationSchema,
	profiles: z.array(socialProfileSchema).optional(),
});

// Work Experience schema
const workExperienceSchema = z
	.object({
		company: z.string().min(1, "Company is required").max(100),
		position: z.string().min(1, "Position is required").max(100),
		startDate: z.string().min(1, "Start date is required"),
		endDate: z.string().optional(),
		location: z.string().optional(),
		summary: z.string().optional(),
		highlights: z.array(z.string()).optional(),
		url: z.string().url().optional().or(z.literal("")),
	})
	.refine(
		(data) => {
			if (data.endDate && data.startDate) {
				return new Date(data.endDate) >= new Date(data.startDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Education schema
const educationSchema = z
	.object({
		institution: z.string().min(1, "Institution is required").max(100),
		area: z.string().min(1, "Field of study is required").max(100),
		studyType: z.string().min(1, "Degree type is required").max(50),
		startDate: z.string().min(1, "Start date is required"),
		endDate: z.string().optional(),
		score: z.string().optional(),
		courses: z.array(z.string()).optional(),
	})
	.refine(
		(data) => {
			if (data.endDate && data.startDate) {
				return new Date(data.endDate) >= new Date(data.startDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Skill Category schema
const skillCategorySchema = z.object({
	name: z.string().min(1, "Category name is required").max(100),
	level: z.string().optional(),
	keywords: z
		.array(z.string().max(50, "Skill must be 50 characters or less"))
		.min(1, "At least one skill is required"),
});

// Language schema
const languageSchema = z.object({
	language: z.string().min(1, "Language name is required").max(50),
	fluency: z.string().optional(),
});

// Project schema
const projectSchema = z
	.object({
		name: z.string().min(1, "Project name is required").max(100),
		description: z.string().optional(),
		startDate: z.string().optional(),
		endDate: z.string().optional(),
		url: z.string().url().optional().or(z.literal("")),
		highlights: z.array(z.string()).optional(),
		keywords: z.array(z.string()).optional(),
	})
	.refine(
		(data) => {
			if (data.endDate && data.startDate) {
				return new Date(data.endDate) >= new Date(data.startDate);
			}
			return true;
		},
		{
			message: "End date must be after start date",
			path: ["endDate"],
		},
	);

// Main Resume schema
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
			// BR-001: Must have at least one of work, education, or skills
			return (
				(data.work && data.work.length > 0) ||
				(data.education && data.education.length > 0) ||
				(data.skills && data.skills.length > 0)
			);
		},
		{
			message:
				"Resume must have at least one work experience, education entry, or skill category",
			path: ["content"],
		},
	);

// Export individual schemas for field-level validation
export const schemas = {
	personalInfo: personalInfoSchema,
	workExperience: workExperienceSchema,
	education: educationSchema,
	skillCategory: skillCategorySchema,
	language: languageSchema,
	project: projectSchema,
};
