import { z } from "zod";

/**
 * Schema for user registration validation
 */
export const registerSchema = z
	.object({
		email: z
			.string()
			.min(1, "Email is required")
			.email("Invalid email format")
			.max(255, "Email must be less than 255 characters"),

		password: z
			.string()
			.min(8, "Password must be at least 8 characters")
			.max(100, "Password must be less than 100 characters")
			.regex(
				/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]/,
				"Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character",
			),

		confirmPassword: z.string().min(1, "Please confirm your password"),

		firstName: z
			.string()
			.min(1, "First name is required")
			.max(100, "First name must be less than 100 characters")
			.regex(
				/^[a-zA-Z\s'-]+$/,
				"First name can only contain letters, spaces, hyphens, and apostrophes",
			),

		lastName: z
			.string()
			.min(1, "Last name is required")
			.max(100, "Last name must be less than 100 characters")
			.regex(
				/^[a-zA-Z\s'-]+$/,
				"Last name can only contain letters, spaces, hyphens, and apostrophes",
			),

		acceptTerms: z
			.boolean()
			.refine(
				(val) => val === true,
				"You must accept the terms and conditions",
			),
	})
	.refine((data) => data.password === data.confirmPassword, {
		message: "Passwords do not match",
		path: ["confirmPassword"],
	});

/**
 * Schema for user login validation
 */
export const loginSchema = z.object({
	email: z.string().min(1, "Email is required").email("Invalid email format"),

	password: z.string().min(1, "Password is required"),

	rememberMe: z.boolean().optional().default(false),
});

/**
 * Type inference for register form
 */
export type RegisterFormData = z.infer<typeof registerSchema>;

/**
 * Type inference for login form
 */
export type LoginFormData = z.infer<typeof loginSchema>;
