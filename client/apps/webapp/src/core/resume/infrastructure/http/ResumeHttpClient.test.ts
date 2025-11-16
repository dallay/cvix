import { describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import { ResumeHttpClient } from "./ResumeHttpClient";

describe("ResumeHttpClient", () => {
	it("posts mapped payload to /resume/generate with locale header", async () => {
		const resume: Resume = {
			basics: {
				name: "Yuniel Acosta Pérez",
				label: "Senior Software Engineer",
				image: "",
				email: "yunielacosta738@gmail.com",
				phone: "+1-555-0000",
				url: "https://yap-cv.vercel.app/en",
				summary: "Some important text here",
				location: {
					address: "",
					postalCode: "",
					city: "San Francisco",
					countryCode: "US",
					region: "CA",
				},
				profiles: [
					{
						network: "LinkedIn",
						username: "yacosta738",
						url: "https://www.linkedin.com/in/yacosta738",
					},
				],
			},
			work: [
				{
					name: "GFT",
					position: "Senior Software Engineer",
					url: "",
					startDate: "2025-11-03",
					endDate: "2025-11-09",
					summary: "",
					highlights: [],
				},
			],
			education: [
				{
					institution: "UCLV",
					url: "",
					area: "Computer Science",
					studyType: "Bachelor's degree",
					startDate: "2025-11-03",
					endDate: "2025-11-09",
					score: "8.5",
					courses: ["Advanced Algorithms", "Logic"],
				},
			],
			skills: [
				{
					name: "Web Development",
					level: "",
					keywords: ["React", "Vue", "Angular"],
				},
				{
					name: "Backend Development",
					level: "",
					keywords: ["Kotlin", "Java"],
				},
			],
			languages: [
				{ language: "Spanish", fluency: "Native" },
				{ language: "English", fluency: "Fluency" },
			],
			projects: [
				{
					name: "cvix",
					startDate: "2025-11-03",
					endDate: "",
					description: "j gfgughjg ujghj gufgug jbvu",
					highlights: [],
					url: "https://github.com/dallay/cvix",
				},
			],
			volunteer: [],
			awards: [],
			certificates: [
				{
					name: "AWS Certificate",
					date: "2025-11-03",
					issuer: "Amazon Web Services",
					url: "",
				},
			],
			publications: [],
			interests: [{ name: "Open Source", keywords: ["Kotlin"] }],
			references: [],
		};

		const client = new ResumeHttpClient();
		const postSpy = vi.fn().mockResolvedValue({
			data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
		});
		// Override axios instance post method
		// biome-ignore lint/suspicious/noExplicitAny: overriding internal axios instance for testing
		(client as any).client.post = postSpy;

		const blob = await client.generatePdf(resume, "es");
		expect(blob).toBeInstanceOf(Blob);

		expect(postSpy).toHaveBeenCalledTimes(1);
		const callArgs = postSpy.mock.calls[0];
		if (!callArgs) throw new Error("Expected call args to exist");
		const [url, payload, config] = callArgs;
		expect(url).toBe("/resume/generate");
		// basics should align to backend DTO
		expect(payload.basics.name).toBe("Yuniel Acosta Pérez");
		expect(payload.basics.location).toEqual({
			address: undefined,
			postalCode: undefined,
			city: "San Francisco",
			countryCode: "US",
			region: "CA",
		});
		// header with locale and responseType
		expect(config.headers["Accept-Language"]).toBe("es");
		expect(config.responseType).toBe("blob");
	});
});
