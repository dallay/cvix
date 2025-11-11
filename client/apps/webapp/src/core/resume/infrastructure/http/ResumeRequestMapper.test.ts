import { describe, expect, it } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume.ts";
import {
	type GenerateResumeRequest,
	mapResumeToBackendRequest,
} from "./ResumeRequestMapper.ts";

describe("mapResumeToBackendRequest", () => {
	it("should map a complete Resume to GenerateResumeRequest", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "John Doe",
				label: "Software Engineer",
				image: "https://example.com/avatar.jpg",
				email: "john.doe@example.com",
				phone: "+1-555-0100",
				url: "https://johndoe.com",
				summary: "Experienced software engineer",
				location: {
					address: "123 Main St",
					postalCode: "12345",
					city: "San Francisco",
					countryCode: "US",
					region: "CA",
				},
				profiles: [
					{
						network: "LinkedIn",
						username: "johndoe",
						url: "https://linkedin.com/in/johndoe",
					},
					{
						network: "GitHub",
						username: "johndoe",
						url: "https://github.com/johndoe",
					},
				],
			},
			work: [
				{
					name: "Tech Corp",
					position: "Senior Developer",
					url: "https://techcorp.com",
					startDate: "2020-01-01",
					endDate: "2023-12-31",
					summary: "Led development of core features",
					highlights: ["Improved performance by 50%"],
				},
			],
			volunteer: [],
			education: [
				{
					institution: "State University",
					url: "https://stateuniv.edu",
					area: "Computer Science",
					studyType: "Bachelor",
					startDate: "2015-09-01",
					endDate: "2019-06-01",
					score: "3.8",
					courses: ["Data Structures", "Algorithms"],
				},
			],
			awards: [],
			certificates: [],
			publications: [],
			skills: [
				{
					name: "Programming",
					level: "Expert",
					keywords: ["JavaScript", "TypeScript", "Python"],
				},
			],
			languages: [
				{
					language: "English",
					fluency: "NATIVE",
				},
			],
			interests: [],
			references: [],
			projects: [
				{
					name: "Open Source Library",
					startDate: "2022-01-01",
					endDate: "2023-06-01",
					description: "A popular open source library",
					highlights: ["1000+ stars on GitHub"],
					url: "https://github.com/johndoe/library",
				},
			],
		};

		// Act
		const result: GenerateResumeRequest = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo).toEqual({
			fullName: "John Doe",
			email: "john.doe@example.com",
			phone: "+1-555-0100",
			location: "San Francisco",
			linkedin: "https://linkedin.com/in/johndoe",
			github: "https://github.com/johndoe",
			website: "https://johndoe.com",
			summary: "Experienced software engineer",
		});

		expect(result.workExperience).toHaveLength(1);
		expect(result.workExperience?.[0]).toEqual({
			company: "Tech Corp",
			position: "Senior Developer",
			startDate: "2020-01-01",
			endDate: "2023-12-31",
			location: undefined,
			description: "Led development of core features",
		});

		expect(result.education).toHaveLength(1);
		expect(result.education?.[0]).toEqual({
			institution: "State University",
			degree: "Bachelor",
			startDate: "2015-09-01",
			endDate: "2019-06-01",
			location: undefined,
			gpa: "3.8",
			description: "Computer Science | Data Structures, Algorithms",
		});

		expect(result.skills).toHaveLength(1);
		expect(result.skills?.[0]).toEqual({
			name: "Programming",
			keywords: ["JavaScript", "TypeScript", "Python"],
		});

		expect(result.languages).toHaveLength(1);
		expect(result.languages?.[0]).toEqual({
			language: "English",
			fluency: "NATIVE",
		});

		expect(result.projects).toHaveLength(1);
		expect(result.projects?.[0]).toEqual({
			name: "Open Source Library",
			description: "A popular open source library",
			url: "https://github.com/johndoe/library",
			startDate: "2022-01-01",
			endDate: "2023-06-01",
		});
	});

	it("should handle empty optional arrays by setting them to undefined", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Jane Doe",
				label: "",
				image: "",
				email: "jane@example.com",
				phone: "+1-555-0200",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Boston",
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.workExperience).toBeUndefined();
		expect(result.education).toBeUndefined();
		expect(result.skills).toBeUndefined();
		expect(result.languages).toBeUndefined();
		expect(result.projects).toBeUndefined();
	});

	it("should handle missing LinkedIn and GitHub profiles", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Bob Smith",
				label: "",
				image: "",
				email: "bob@example.com",
				phone: "+1-555-0300",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "NYC",
					countryCode: "US",
					region: "",
				},
				profiles: [
					{
						network: "Twitter",
						username: "bobsmith",
						url: "https://twitter.com/bobsmith",
					},
				],
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.linkedin).toBeUndefined();
		expect(result.personalInfo.github).toBeUndefined();
	});

	it("should handle required string fields set to empty strings", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "", // Empty required string
				label: "",
				image: "",
				email: "", // Empty required string
				phone: "", // Empty required string
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.fullName).toBe("");
		expect(result.personalInfo.email).toBe("");
		expect(result.personalInfo.phone).toBe("");
		expect(result.personalInfo.location).toBeUndefined(); // Empty city is normalized to undefined
	});

	it("should handle null vs undefined in optional fields - null location", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Alice Cooper",
				label: "",
				image: "",
				email: "alice@example.com",
				phone: "+1-555-0400",
				url: "",
				summary: "",
				// @ts-expect-error Testing runtime null handling
				location: null, // Null instead of object
				profiles: [],
			},
			work: [
				{
					name: "Company A",
					position: "Developer",
					url: "",
					startDate: "2020-01-01",
					endDate: "", // Empty string for optional
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.location).toBeUndefined(); // null.city should be handled gracefully
		expect(result.workExperience).toHaveLength(1);
		expect(result.workExperience?.[0]?.endDate).toBeUndefined();
	});

	it("should handle undefined optional fields in work experience", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Charlie Brown",
				label: "",
				image: "",
				email: "charlie@example.com",
				phone: "+1-555-0500",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Seattle",
					countryCode: "US",
					region: "",
				},
				profiles: [],
			},
			work: [
				{
					name: "Company B",
					position: "Engineer",
					url: "", // Empty string instead of undefined
					startDate: "2021-01-01",
					endDate: "", // Empty string for optional
					summary: "", // Empty string instead of undefined
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.workExperience).toHaveLength(1);
		expect(result.workExperience?.[0]?.description).toBeUndefined();
		expect(result.workExperience?.[0]?.endDate).toBeUndefined();
	});

	it("should handle profile network casing variations - LinkedIn", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "David Lee",
				label: "",
				image: "",
				email: "david@example.com",
				phone: "+1-555-0600",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Austin",
					countryCode: "US",
					region: "",
				},
				profiles: [
					{
						network: "LINKEDIN", // All uppercase
						username: "davidlee",
						url: "https://linkedin.com/in/davidlee",
					},
					{
						network: "GitHub", // Mixed case
						username: "davidlee",
						url: "https://github.com/davidlee",
					},
				],
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.linkedin).toBe(
			"https://linkedin.com/in/davidlee",
		);
		expect(result.personalInfo.github).toBe("https://github.com/davidlee");
	});

	it("should handle profile network casing variations - lowercase", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Eva Martinez",
				label: "",
				image: "",
				email: "eva@example.com",
				phone: "+1-555-0700",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Portland",
					countryCode: "US",
					region: "",
				},
				profiles: [
					{
						network: "linkedin", // All lowercase
						username: "evamartinez",
						url: "https://linkedin.com/in/evamartinez",
					},
					{
						network: "github", // All lowercase
						username: "evamartinez",
						url: "https://github.com/evamartinez",
					},
				],
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.linkedin).toBe(
			"https://linkedin.com/in/evamartinez",
		);
		expect(result.personalInfo.github).toBe("https://github.com/evamartinez");
	});

	it("should handle profile network casing variations - mixed case", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Frank Wilson",
				label: "",
				image: "",
				email: "frank@example.com",
				phone: "+1-555-0800",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Denver",
					countryCode: "US",
					region: "",
				},
				profiles: [
					{
						network: "LiNkEdIn", // Mixed case
						username: "frankwilson",
						url: "https://linkedin.com/in/frankwilson",
					},
					{
						network: "GiThUb", // Mixed case
						username: "frankwilson",
						url: "https://github.com/frankwilson",
					},
				],
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.linkedin).toBe(
			"https://linkedin.com/in/frankwilson",
		);
		expect(result.personalInfo.github).toBe("https://github.com/frankwilson");
	});

	it("should handle missing nested location object entirely", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Grace Kim",
				label: "",
				image: "",
				email: "grace@example.com",
				phone: "+1-555-0900",
				url: "",
				summary: "",
				// @ts-expect-error Testing runtime undefined handling
				location: undefined, // Completely missing
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

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.personalInfo.location).toBeUndefined();
		expect(result.personalInfo.fullName).toBe("Grace Kim");
		expect(result.personalInfo.email).toBe("grace@example.com");
	});

	it("should handle optional fields as null in projects", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Henry Adams",
				label: "",
				image: "",
				email: "henry@example.com",
				phone: "+1-555-1000",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Phoenix",
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
			projects: [
				{
					name: "Test Project",
					// @ts-expect-error Testing runtime null handling
					startDate: null, // Null instead of string
					// @ts-expect-error Testing runtime null handling
					endDate: null,
					description: "A test project",
					highlights: [],
					// @ts-expect-error Testing runtime null handling
					url: null,
				},
			],
		};

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.projects).toHaveLength(1);
		expect(result.projects?.[0]?.startDate).toBeUndefined();
		expect(result.projects?.[0]?.endDate).toBeUndefined();
		expect(result.projects?.[0]?.url).toBeUndefined();
		expect(result.projects?.[0]?.name).toBe("Test Project");
	});

	it("should handle education with null optional fields", () => {
		// Arrange
		const resume: Resume = {
			basics: {
				name: "Isabel Torres",
				label: "",
				image: "",
				email: "isabel@example.com",
				phone: "+1-555-1100",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "Miami",
					countryCode: "US",
					region: "",
				},
				profiles: [],
			},
			work: [],
			volunteer: [],
			education: [
				{
					institution: "Tech University",
					url: "",
					area: "Computer Science",
					studyType: "Master",
					startDate: "2018-09-01",
					// @ts-expect-error Testing runtime null handling
					endDate: null, // Null endDate
					// @ts-expect-error Testing runtime null handling
					score: null, // Null score
					courses: [],
				},
			],
			awards: [],
			certificates: [],
			publications: [],
			skills: [],
			languages: [],
			interests: [],
			references: [],
			projects: [],
		};

		// Act
		const result = mapResumeToBackendRequest(resume);

		// Assert
		expect(result.education).toHaveLength(1);
		expect(result.education?.[0]?.endDate).toBeUndefined();
		expect(result.education?.[0]?.gpa).toBeUndefined();
		expect(result.education?.[0]?.institution).toBe("Tech University");
		expect(result.education?.[0]?.degree).toBe("Master");
	});
});
