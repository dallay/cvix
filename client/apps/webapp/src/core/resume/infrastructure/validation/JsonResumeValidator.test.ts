import { describe, expect, it } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import { JsonResumeValidator } from "./JsonResumeValidator.ts";

describe("JsonResumeValidator", () => {
	const validator = new JsonResumeValidator();

	describe("validate", () => {
		it("should return false for null or undefined resume", () => {
			expect(validator.validate(null as unknown as Resume)).toBe(false);
			expect(validator.validate(undefined as unknown as Resume)).toBe(false);
		});

		it("should return false when basics is missing", () => {
			const resume = {
				basics: null,
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			} as unknown as Resume;

			expect(validator.validate(resume)).toBe(false);
		});

		it("should validate a minimal valid resume", () => {
			const resume: Resume = {
				basics: {
					name: "John Doe",
					label: "Software Engineer",
					image: "https://example.com/photo.jpg",
					email: "john@example.com",
					phone: "+1-555-0100",
					url: "https://johndoe.com",
					summary: "Experienced software engineer",
					location: {
						address: "123 Main St",
						postalCode: "12345",
						city: "San Francisco",
						countryCode: "US",
						region: "California",
					},
					profiles: [],
				},
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};

			expect(validator.validate(resume)).toBe(true);
		});

		it("should validate a complete resume with all sections", () => {
			const resume: Resume = {
				basics: {
					name: "Jane Smith",
					label: "Full Stack Developer",
					image: "https://example.com/jane.jpg",
					email: "jane@example.com",
					phone: "+1-555-0200",
					url: "https://janesmith.dev",
					summary: "Passionate developer with 10 years of experience",
					location: {
						address: "456 Tech Avenue",
						postalCode: "94105",
						city: "San Francisco",
						countryCode: "US",
						region: "California",
					},
					profiles: [
						{
							network: "GitHub",
							username: "janesmith",
							url: "https://github.com/janesmith",
						},
						{
							network: "LinkedIn",
							username: "jane-smith",
							url: "https://linkedin.com/in/jane-smith",
						},
					],
				},
				work: [
					{
						name: "Tech Corp",
						position: "Senior Developer",
						url: "https://techcorp.com",
						startDate: "2020-01",
						endDate: "2023-12",
						summary: "Led development of cloud-native applications",
						highlights: [
							"Reduced deployment time by 50%",
							"Mentored 5 junior developers",
						],
					},
				],
				volunteer: [
					{
						organization: "Code for Good",
						position: "Volunteer Developer",
						url: "https://codeforgood.org",
						startDate: "2019-06",
						endDate: "2021-12",
						summary: "Built web applications for nonprofits",
						highlights: [
							"Developed donation platform",
							"Improved website accessibility",
						],
					},
				],
				education: [
					{
						institution: "University of Technology",
						url: "https://utech.edu",
						area: "Computer Science",
						studyType: "Bachelor",
						startDate: "2010-09",
						endDate: "2014-06",
						score: "3.8/4.0",
						courses: ["Data Structures", "Algorithms", "Web Development"],
					},
				],
				awards: [
					{
						title: "Developer of the Year",
						date: "2022-12",
						awarder: "Tech Awards",
						summary: "Recognized for outstanding contributions",
					},
				],
				certificates: [
					{
						name: "AWS Certified Solutions Architect",
						date: "2021-03",
						issuer: "Amazon Web Services",
						url: "https://aws.amazon.com/certification/",
					},
				],
				publications: [
					{
						name: "Modern Web Architecture",
						publisher: "Tech Publications",
						releaseDate: "2022-05",
						url: "https://techpub.com/modern-web",
						summary: "A guide to building scalable web applications",
					},
				],
				skills: [
					{
						name: "Web Development",
						level: "Expert",
						keywords: ["JavaScript", "TypeScript", "Vue.js", "React"],
					},
					{
						name: "Backend Development",
						level: "Advanced",
						keywords: ["Node.js", "Python", "PostgreSQL"],
					},
				],
				languages: [
					{
						language: "English",
						fluency: "Native",
					},
					{
						language: "Spanish",
						fluency: "Professional",
					},
				],
				interests: [
					{
						name: "Open Source",
						keywords: ["Contributing", "Maintainer"],
					},
					{
						name: "Photography",
						keywords: ["Landscape", "Portrait"],
					},
				],
				references: [
					{
						name: "John Manager",
						reference: "Jane is an exceptional developer and team player",
					},
				],
				projects: [
					{
						name: "E-Commerce Platform",
						startDate: "2021-01",
						endDate: "2021-12",
						description: "Built a scalable e-commerce solution",
						highlights: [
							"Handled 100k+ daily users",
							"Integrated payment gateways",
						],
						url: "https://github.com/janesmith/ecommerce",
					},
				],
			};

			expect(validator.validate(resume)).toBe(true);
		});

		it("should return false for a resume with empty strings", () => {
			const resume: Resume = {
				basics: {
					name: "",
					label: "",
					image: "",
					email: "",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "",
						region: "",
					},
					profiles: [],
				},
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};

			expect(validator.validate(resume)).toBe(false);
		});
	});

	describe("email validation", () => {
		it("should reject invalid email formats", () => {
			const resume: Resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "invalid-email",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "US",
						region: "",
					},
					profiles: [],
				},
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};

			expect(validator.validate(resume)).toBe(false);
		});

		it("should accept valid email formats", () => {
			const validEmails = [
				"user@example.com",
				"test.user@domain.co.uk",
				"firstname+lastname@company.org",
			];

			for (const email of validEmails) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email,
						phone: "",
						url: "",
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode: "US",
							region: "",
						},
						profiles: [],
					},
					work: [],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(true);
			}
		});
	});

	describe("URL validation", () => {
		it("should reject invalid URL formats", () => {
			const resume: Resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "test@example.com",
					phone: "",
					url: "not-a-url",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "US",
						region: "",
					},
					profiles: [],
				},
				work: [],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};

			expect(validator.validate(resume)).toBe(false);
		});

		it("should accept valid URL formats", () => {
			const validUrls = [
				"https://example.com",
				"http://subdomain.example.org/path",
				"https://example.com:8080/path?query=value",
			];

			for (const url of validUrls) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email: "test@example.com",
						phone: "",
						url,
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode: "US",
							region: "",
						},
						profiles: [],
					},
					work: [],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(true);
			}
		});
	});

	describe("ISO 8601 date validation", () => {
		it("should reject invalid date formats", () => {
			const invalidDates = [
				"2023/01/15",
				"15-01-2023",
				"2023-13-01",
				"2023-01-32",
				"23-01",
			];

			for (const date of invalidDates) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email: "test@example.com",
						phone: "",
						url: "",
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode: "US",
							region: "",
						},
						profiles: [],
					},
					work: [
						{
							name: "Company",
							position: "Developer",
							url: "",
							startDate: date,
							endDate: "",
							summary: "",
							highlights: [],
						},
					],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(false);
			}
		});

		it("should accept valid ISO 8601 date formats", () => {
			const validDates = ["2023", "2023-01", "2023-01-15"];

			for (const date of validDates) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email: "test@example.com",
						phone: "",
						url: "",
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode: "US",
							region: "",
						},
						profiles: [],
					},
					work: [
						{
							name: "Company",
							position: "Developer",
							url: "",
							startDate: date,
							endDate: "",
							summary: "",
							highlights: [],
						},
					],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(true);
			}
		});
	});

	describe("country code validation", () => {
		it("should reject invalid country codes", () => {
			const invalidCodes = ["USA", "U", "us", "123"];

			for (const countryCode of invalidCodes) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email: "test@example.com",
						phone: "",
						url: "",
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode,
							region: "",
						},
						profiles: [],
					},
					work: [],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(false);
			}
		});

		it("should accept valid ISO 3166-1 alpha-2 country codes", () => {
			const validCodes = ["US", "GB", "DE", "FR", "JP"];

			for (const countryCode of validCodes) {
				const resume: Resume = {
					basics: {
						name: "Test User",
						label: "",
						image: "",
						email: "test@example.com",
						phone: "",
						url: "",
						summary: "",
						location: {
							address: "",
							postalCode: "",
							city: "",
							countryCode,
							region: "",
						},
						profiles: [],
					},
					work: [],
					volunteer: [],
					education: [],
					awards: [],
					certificates: [],
					publications: [],
					skills: [],
					languages: [],
					interests: [],
					references: [],
					projects: [],
				};

				expect(validator.validate(resume)).toBe(true);
			}
		});
	});

	describe("array validation", () => {
		it("should reject non-array values for array fields", () => {
			const resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "test@example.com",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "US",
						region: "",
					},
					profiles: [],
				},
				work: "not-an-array",
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			} as unknown as Resume;

			expect(validator.validate(resume)).toBe(false);
		});

		it("should validate string arrays in highlights, keywords, and courses", () => {
			const resume: Resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "test@example.com",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "US",
						region: "",
					},
					profiles: [],
				},
				work: [
					{
						name: "Company",
						position: "Developer",
						url: "",
						startDate: "2020-01",
						endDate: "2023-01",
						summary: "",
						highlights: ["Achievement 1", "Achievement 2"],
					},
				],
				volunteer: [],
				education: [
					{
						institution: "University",
						url: "",
						area: "CS",
						studyType: "Bachelor",
						startDate: "2016-09",
						endDate: "2020-06",
						score: "3.8",
						courses: ["Course 1", "Course 2"],
					},
				],
				awards: [],
				certificates: [],
				publications: [],
				skills: [
					{
						name: "Programming",
						level: "Expert",
						keywords: ["JavaScript", "TypeScript"],
					},
				],
				languages: [],
				interests: [
					{
						name: "Technology",
						keywords: ["AI", "Machine Learning"],
					},
				],
				references: [],
				projects: [],
			};

			expect(validator.validate(resume)).toBe(true);
		});

		it("should reject invalid string arrays", () => {
			const resume = {
				basics: {
					name: "Test User",
					label: "",
					image: "",
					email: "test@example.com",
					phone: "",
					url: "",
					summary: "",
					location: {
						address: "",
						postalCode: "",
						city: "",
						countryCode: "US",
						region: "",
					},
					profiles: [],
				},
				work: [
					{
						name: "Company",
						position: "Developer",
						url: "",
						startDate: "2020-01",
						endDate: "2023-01",
						summary: "",
						highlights: ["Valid string", 123, "Another string"],
					},
				],
				volunteer: [],
				education: [],
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			} as unknown as Resume;

			expect(validator.validate(resume)).toBe(false);
		});
	});
});
