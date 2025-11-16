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
					startDate: "2020-01-01",
					endDate: "2023-12-31",
					summary: "Led development of core features",
					highlights: ["Improved performance by 50%"],
					url: "https://techcorp.com",
				},
			],
			volunteer: [
				{
					organization: "Nonprofit Org",
					position: "Volunteer",
					startDate: "2021-01-01",
					endDate: "2022-01-01",
					summary: "Assisted in organizing community events.",
					url: "https://nonprofit.org",
					highlights: ["Organized events", "Raised funds"],
				},
			],
			education: [
				{
					institution: "University of Example", // Fixed institution name
					url: "https://university.example.edu",
					area: "Computer Science",
					studyType: "Bachelor",
					startDate: "2015-09-01",
					endDate: "2019-06-01",
					score: "3.8",
					courses: ["Data Structures", "Algorithms"],
				},
			],
			awards: [
				{
					title: "Best Developer",
					date: "2021-06-01",
					awarder: "Tech Awards",
					summary: "Recognized for outstanding contributions",
				},
			],
			certificates: [
				{
					name: "Certified Kubernetes Administrator",
					issuer: "CNCF",
					date: "2022-03-01",
					url: "https://cncf.io/certification",
				},
			],
			publications: [
				{
					name: "Scaling Microservices",
					publisher: "Tech Journal",
					releaseDate: "2020-09-01",
					url: "https://techjournal.com/scaling-microservices",
					summary: "Exploring best practices for scalability",
				},
			],
			interests: [
				{
					name: "Open Source",
					keywords: ["Contributions", "Community"],
				},
			],
			references: [
				{
					name: "Jane Smith",
					reference: "John is an excellent developer.",
				},
			],
			projects: [
				{
					name: "Open Source Library",
					description: "A popular open source library",
					url: "https://github.com/johndoe/library",
					startDate: "2022-01-01",
					endDate: "2023-06-01",
					highlights: [], // Add empty highlights array to satisfy type requirements
				},
			],
			skills: [
				{
					name: "Programming Languages",
					level: "Expert",
					keywords: ["JavaScript", "TypeScript", "Kotlin"],
				},
			],
			languages: [
				{
					language: "English",
					fluency: "Native",
				},
			],
		};

		// Act
		const result: GenerateResumeRequest = mapResumeToBackendRequest(resume);

		// Assert basics mapped to backend shape
		expect(result.basics).toEqual({
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
		});

		expect(result.work).toHaveLength(1);
		expect(result.work?.[0]).toEqual({
			name: "Tech Corp",
			position: "Senior Developer",
			startDate: "2020-01-01",
			endDate: "2023-12-31",
			summary: "Led development of core features",
			url: "https://techcorp.com",
		});

		expect(result.education).toHaveLength(1);
		expect(result.education?.[0]).toEqual({
			institution: "University of Example",
			area: "Computer Science",
			studyType: "Bachelor",
			startDate: "2015-09-01",
			endDate: "2019-06-01",
			score: "3.8",
			url: "https://university.example.edu",
			courses: ["Data Structures", "Algorithms"],
		});

		expect(result.skills).toHaveLength(1);
		expect(result.skills?.[0]).toEqual({
			name: "Programming Languages",
			keywords: ["JavaScript", "TypeScript", "Kotlin"],
		});

		expect(result.languages).toHaveLength(1);
		expect(result.languages?.[0]).toEqual({
			language: "English",
			fluency: "Native",
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
		expect(result.work).toBeUndefined();
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

		// Assert: profiles preserved, but no LinkedIn/GitHub
		expect(result.basics.profiles).toEqual([
			{
				network: "Twitter",
				username: "bobsmith",
				url: "https://twitter.com/bobsmith",
			},
		]);
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
		expect(result.basics.name).toBe("");
		expect(result.basics.email).toBe("");
		expect(result.basics.phone).toBe("");
		expect(result.basics.location).toEqual({
			address: undefined,
			postalCode: undefined,
			city: undefined,
			countryCode: undefined,
			region: undefined,
		});
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
		expect(result.basics.location).toBeUndefined(); // null.city should be handled gracefully
		expect(result.work).toHaveLength(1);
		expect(result.work?.[0]?.endDate).toBeUndefined();
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
		expect(result.work).toHaveLength(1);
		expect(result.work?.[0]?.summary).toBeUndefined();
		expect(result.work?.[0]?.endDate).toBeUndefined();
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

		// Assert: profiles are passed through
		expect(result.basics.profiles).toEqual([
			{
				network: "LINKEDIN",
				username: "davidlee",
				url: "https://linkedin.com/in/davidlee",
			},
			{
				network: "GitHub",
				username: "davidlee",
				url: "https://github.com/davidlee",
			},
		]);
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
		expect(result.basics.profiles).toEqual([
			{
				network: "linkedin",
				username: "evamartinez",
				url: "https://linkedin.com/in/evamartinez",
			},
			{
				network: "github",
				username: "evamartinez",
				url: "https://github.com/evamartinez",
			},
		]);
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
		expect(result.basics.profiles).toEqual([
			{
				network: "LiNkEdIn",
				username: "frankwilson",
				url: "https://linkedin.com/in/frankwilson",
			},
			{
				network: "GiThUb",
				username: "frankwilson",
				url: "https://github.com/frankwilson",
			},
		]);
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
		expect(result.basics.location).toBeUndefined();
		expect(result.basics.name).toBe("Grace Kim");
		expect(result.basics.email).toBe("grace@example.com");
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
		expect(result.education?.[0]?.score).toBeUndefined();
		expect(result.education?.[0]?.institution).toBe("Tech University");
		expect(result.education?.[0]?.studyType).toBe("Master");
	});

	it("should map all JSON Resume sections to GenerateResumeRequest", () => {
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
					startDate: "2020-01-01",
					endDate: "2023-12-31",
					summary: "Led development of core features",
					highlights: ["Improved performance by 50%"],
					url: "https://techcorp.com",
				},
			],
			volunteer: [
				{
					organization: "Nonprofit Org",
					position: "Volunteer",
					startDate: "2021-01-01",
					endDate: "2022-01-01",
					summary: "Assisted in organizing community events.",
					url: "https://nonprofit.org",
					highlights: ["Organized events", "Raised funds"],
				},
			],
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
			awards: [
				{
					title: "Best Developer",
					date: "2021-06-01",
					awarder: "Tech Awards",
					summary: "Recognized for outstanding contributions",
				},
			],
			certificates: [
				{
					name: "Certified Kubernetes Administrator",
					issuer: "CNCF",
					date: "2022-03-01",
					url: "https://cncf.io/certification",
				},
			],
			publications: [
				{
					name: "Scaling Microservices",
					publisher: "Tech Journal",
					releaseDate: "2020-09-01",
					url: "https://techjournal.com/scaling-microservices",
					summary: "Exploring best practices for scalability",
				},
			],
			interests: [
				{
					name: "Open Source",
					keywords: ["Contributions", "Community"],
				},
			],
			references: [
				{
					name: "Jane Smith",
					reference: "John is an excellent developer.",
				},
			],
			projects: [
				{
					name: "Open Source Library",
					description: "A popular open source library",
					url: "https://github.com/johndoe/library",
					startDate: "2022-01-01",
					endDate: "2023-06-01",
					highlights: [], // Add empty highlights array to satisfy type requirements
				},
			],
			skills: [
				{
					name: "Programming Languages",
					level: "Expert",
					keywords: ["JavaScript", "TypeScript", "Kotlin"],
				},
			],
			languages: [
				{
					language: "English",
					fluency: "Native",
				},
			],
		};

		// Act
		const result: GenerateResumeRequest = mapResumeToBackendRequest(resume);

		// Assert
		// volunteer.position must be preserved
		expect(result.volunteer?.[0]?.position).toBe("Volunteer");
	});
});
