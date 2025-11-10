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
			description: undefined,
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
});
