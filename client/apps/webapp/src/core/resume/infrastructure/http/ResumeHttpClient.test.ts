import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import { createTestResume } from "@/core/resume/test-resume-factory.ts";
import {
	type ResumeDocumentResponse,
	ResumeHttpClient,
} from "./ResumeHttpClient";
import type { GenerateResumeRequest } from "./requests/ResumeRequest";

const verifyBasicsObject = (payload: GenerateResumeRequest) => {
	expect(payload.basics.name).toBe("John Doe");
	expect(payload.basics.label).toBe("Software Engineer");
	expect(payload.basics.location).toEqual({
		address: "123 Main St",
		postalCode: "12345",
		city: "San Francisco",
		countryCode: "US",
		region: "CA",
	});
	expect(payload.basics.image).toBe("profile.jpg");
	expect(payload.basics.email).toBe("john@example.com");
	expect(payload.basics.phone).toBe("+1-555-0100");
	expect(payload.basics.url).toBe("https://johndoe.com");
	expect(payload.basics.summary).toBe("Experienced software engineer");
	expect(payload.basics.profiles).toHaveLength(2);
	expect(payload.basics.profiles?.[0]).toEqual({
		network: "GitHub",
		username: "johndoe",
		url: "https://github.com/johndoe",
	});
	expect(payload.basics.profiles?.[1]).toEqual({
		network: "LinkedIn",
		username: "johndoe",
		url: "https://linkedin.com/in/johndoe",
	});
};

const verifyWorkObject = (payload: GenerateResumeRequest) => {
	expect(payload.work).not.toBeUndefined();
	expect(payload.work).toHaveLength(2);
	expect(payload.work?.[0]).toEqual({
		name: "Company A",
		position: "Senior Developer",
		url: "https://companya.com",
		startDate: "2020-01-01",
		endDate: "2022-12-31",
		summary: "Led development team",
		highlights: ["Achievement 1", "Achievement 2"],
	});
	expect(payload.work?.[1]).toEqual({
		name: "Company B",
		position: "Developer",
		url: "https://companyb.com",
		startDate: "2018-01-01",
		endDate: "2019-12-31",
		summary: "Developed features",
		highlights: [],
	});
};
const verifyEducationObject = (payload: GenerateResumeRequest) => {
	expect(payload.education).not.toBeUndefined();
	expect(payload.education).toHaveLength(1);
	expect(payload.education?.[0]).toEqual({
		institution: "University A",
		url: "https://university-a.edu",
		area: "Computer Science",
		studyType: "Bachelor",
		startDate: "2014-09-01",
		endDate: "2018-06-01",
		score: "3.8",
		courses: ["CS101", "CS201"],
	});
};
const verifySkillsObject = (payload: GenerateResumeRequest) => {
	expect(payload.skills).not.toBeUndefined();
	expect(payload.skills).toHaveLength(3);
	expect(payload.skills?.[0]).toEqual({
		name: "JavaScript",
		level: "Expert",
		keywords: ["ES6", "Node.js"],
	});
	expect(payload.skills?.[1]).toEqual({
		name: "TypeScript",
		level: "Advanced",
		keywords: ["Types", "Interfaces"],
	});
	expect(payload.skills?.[2]).toEqual({
		name: "Python",
		level: "Intermediate",
		keywords: ["Django", "Flask"],
	});
};
const verifyProjectsObject = (payload: GenerateResumeRequest) => {
	expect(payload.projects).not.toBeUndefined();
	expect(payload.projects).toHaveLength(1);
	expect(payload.projects?.[0]).toEqual({
		name: "Project Alpha",
		startDate: "2021-01-01",
		endDate: "2021-12-31",
		description: "A cool project",
		highlights: [],
		url: "https://project-alpha.com",
	});
};
const verifyCertificatesObject = (payload: GenerateResumeRequest) => {
	expect(payload.certificates).not.toBeUndefined();
	expect(payload.certificates).toHaveLength(1);
	expect(payload.certificates?.[0]).toEqual({
		name: "AWS Certified",
		date: "2021-06-01",
		url: "https://aws.amazon.com",
		issuer: "Amazon",
	});
};
const verifyVolunteerObject = (payload: GenerateResumeRequest) => {
	expect(payload.volunteer).not.toBeUndefined();
	expect(payload.volunteer).toHaveLength(1);
	expect(payload.volunteer?.[0]).toEqual({
		organization: "Code for Good",
		position: "Volunteer Developer",
		url: "https://codeforgood.org",
		startDate: "2020-01-01",
		endDate: undefined,
		summary: "Volunteering",
		highlights: [],
	});
};
const verifyAwardsObject = (payload: GenerateResumeRequest) => {
	expect(payload.awards).not.toBeUndefined();
	expect(payload.awards).toHaveLength(1);
	expect(payload.awards?.[0]).toEqual({
		title: "Best Developer 2021",
		date: "2021-12-01",
		awarder: "Company A",
		summary: "Excellence in development",
	});
};

const verifyPublicationsObject = (payload: GenerateResumeRequest) => {
	expect(payload.publications).not.toBeUndefined();
	expect(payload.publications).toHaveLength(1);
	expect(payload.publications?.[0]).toEqual({
		name: "Article on TypeScript",
		publisher: "Medium",
		releaseDate: "2022-01-01",
		url: "https://medium.com/article",
		summary: "How to use TypeScript",
	});
};

const verifyLanguagesObject = (payload: GenerateResumeRequest) => {
	expect(payload.languages).not.toBeUndefined();
	expect(payload.languages).toHaveLength(1);
	expect(payload.languages?.[0]).toEqual({
		language: "English",
		fluency: "Native",
	});
};
const verifyInterestsObject = (payload: GenerateResumeRequest) => {
	expect(payload.interests).not.toBeUndefined();
	expect(payload.interests).toHaveLength(1);
	expect(payload.interests?.[0]).toEqual({
		name: "Photography",
		keywords: ["Landscape", "Portrait"],
	});
};
const verifyReferencesObject = (payload: GenerateResumeRequest) => {
	expect(payload.references).not.toBeUndefined();
	expect(payload.references).toHaveLength(1);
	expect(payload.references?.[0]).toEqual({
		name: "Jane Smith",
		reference: "John is an excellent developer",
	});
};

describe("ResumeHttpClient", () => {
	const templateId = "classic";
	const mockResume: Resume = createTestResume();

	const mockResumeDocumentResponse: ResumeDocumentResponse = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		userId: "user-123",
		workspaceId: "workspace-456",
		title: "My Resume",
		content: mockResume,
		createdAt: "2025-01-01T00:00:00Z",
		updatedAt: "2025-01-02T00:00:00Z",
		createdBy: "user-123",
		updatedBy: "user-123",
	};

	let client: ResumeHttpClient;
	let getSpy: ReturnType<typeof vi.fn>;
	let postSpy: ReturnType<typeof vi.fn>;
	let putSpy: ReturnType<typeof vi.fn>;
	let deleteSpy: ReturnType<typeof vi.fn>;

	beforeEach(() => {
		client = new ResumeHttpClient();

		// Spy on the client methods after construction
		getSpy = vi.fn();
		postSpy = vi.fn();
		putSpy = vi.fn();
		deleteSpy = vi.fn();

		// Replace the client methods with spies
		// biome-ignore lint/suspicious/noExplicitAny: Testing requires accessing protected client
		(client as any).client.get = getSpy;
		// biome-ignore lint/suspicious/noExplicitAny: Testing requires accessing protected client
		(client as any).client.post = postSpy;
		// biome-ignore lint/suspicious/noExplicitAny: Testing requires accessing protected client
		(client as any).client.put = putSpy;
		// biome-ignore lint/suspicious/noExplicitAny: Testing requires accessing protected client
		(client as any).client.delete = deleteSpy;
	});
	describe("generatePdf", () => {
		it("posts mapped payload to /resume/generate with locale header", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			const blob = await client.generatePdf(templateId, mockResume, "es");
			expect(blob).toBeInstanceOf(Blob);

			expect(postSpy).toHaveBeenCalledTimes(1);
			const callArgs = postSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [url, payload, config] = callArgs;
			expect(url).toBe("/resume/generate");
			verifyBasicsObject(payload);
			verifyWorkObject(payload);
			verifyEducationObject(payload);
			verifySkillsObject(payload);
			verifyProjectsObject(payload);
			verifyCertificatesObject(payload);
			verifyVolunteerObject(payload);
			verifyAwardsObject(payload);
			verifyPublicationsObject(payload);
			verifyLanguagesObject(payload);
			verifyInterestsObject(payload);
			verifyReferencesObject(payload);
			// header with locale and responseType
			expect(config.headers["Accept-Language"]).toBe("es");
			expect(config.responseType).toBe("blob");
		});

		it("omits Accept-Language header when locale is not provided", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			await client.generatePdf(templateId, mockResume);

			const callArgs = postSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [, , config] = callArgs;
			expect(config.headers["Accept-Language"]).toBeUndefined();
		});

		it("sets Accept header to application/pdf", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			await client.generatePdf(templateId, mockResume, "en");

			const callArgs = postSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [, , config] = callArgs;
			expect(config.headers.Accept).toBe("application/pdf");
		});
	});

	describe("createResume", () => {
		it("creates a resume with PUT request", async () => {
			putSpy.mockResolvedValue({ data: mockResumeDocumentResponse });

			const result = await client.createResume(
				"550e8400-e29b-41d4-a716-446655440000",
				"workspace-456",
				mockResume,
				"My Resume",
			);

			expect(putSpy).toHaveBeenCalledTimes(1);
			const callArgs = putSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [url, payload] = callArgs;
			expect(url).toBe("/resume/550e8400-e29b-41d4-a716-446655440000");
			expect(payload.workspaceId).toBe("workspace-456");
			expect(payload.title).toBe("My Resume");
			expect(payload.content.basics.name).toBe("John Doe");
			expect(result).toEqual(mockResumeDocumentResponse);
		});

		it("creates a resume without title", async () => {
			putSpy.mockResolvedValue({ data: mockResumeDocumentResponse });

			await client.createResume(
				"550e8400-e29b-41d4-a716-446655440000",
				"workspace-456",
				mockResume,
			);

			const callArgs = putSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [, payload] = callArgs;
			expect(payload.title).toBeUndefined();
		});
	});

	describe("getResume", () => {
		it("fetches a resume by ID", async () => {
			getSpy.mockResolvedValue({ data: mockResumeDocumentResponse });

			const result = await client.getResume(
				"550e8400-e29b-41d4-a716-446655440000",
			);

			expect(getSpy).toHaveBeenCalledTimes(1);
			expect(getSpy).toHaveBeenCalledWith(
				"/resume/550e8400-e29b-41d4-a716-446655440000",
			);
			expect(result).toEqual(mockResumeDocumentResponse);
		});
	});

	describe("updateResume", () => {
		it("updates a resume with PUT request", async () => {
			putSpy.mockResolvedValue({ data: mockResumeDocumentResponse });

			const result = await client.updateResume(
				"550e8400-e29b-41d4-a716-446655440000",
				mockResume,
				"Updated Title",
			);

			expect(putSpy).toHaveBeenCalledTimes(1);
			const callArgs = putSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [url, payload] = callArgs;
			expect(url).toBe("/resume/550e8400-e29b-41d4-a716-446655440000/update");
			expect(payload.title).toBe("Updated Title");
			expect(payload.content.basics.name).toBe("John Doe");
			expect(result).toEqual(mockResumeDocumentResponse);
		});

		it("updates a resume without title", async () => {
			putSpy.mockResolvedValue({ data: mockResumeDocumentResponse });

			await client.updateResume(
				"550e8400-e29b-41d4-a716-446655440000",
				mockResume,
			);

			const callArgs = putSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [, payload] = callArgs;
			expect(payload.title).toBeUndefined();
		});
	});

	describe("deleteResume", () => {
		it("deletes a resume by ID", async () => {
			deleteSpy.mockResolvedValue({});

			await client.deleteResume("550e8400-e29b-41d4-a716-446655440000");

			expect(deleteSpy).toHaveBeenCalledTimes(1);
			expect(deleteSpy).toHaveBeenCalledWith(
				"/resume/550e8400-e29b-41d4-a716-446655440000",
			);
		});
	});

	describe("listResumes", () => {
		it("fetches list of resumes", async () => {
			const responseData = { data: [mockResumeDocumentResponse] };
			getSpy.mockResolvedValue({ data: responseData });

			const result = await client.listResumes();

			expect(getSpy).toHaveBeenCalledTimes(1);
			expect(getSpy).toHaveBeenCalledWith("/resume");
			expect(result).toEqual([mockResumeDocumentResponse]);
		});

		it("returns empty array when no resumes", async () => {
			getSpy.mockResolvedValue({ data: { data: [] } });

			const result = await client.listResumes();

			expect(result).toEqual([]);
		});
	});

	describe("getTemplates", () => {
		const mockTemplates: TemplateMetadata[] = [
			{
				id: "classic",
				name: "Classic",
				version: "1.0.0",
				description: "A classic professional template",
				supportedLocales: ["en", "es"],
				previewUrl: "/thumbnails/classic.png",
			},
			{
				id: "modern",
				name: "Modern",
				version: "1.0.0",
				description: "A modern clean template",
				supportedLocales: ["en"],
				previewUrl: "/thumbnails/modern.png",
			},
		];

		it("fetches list of templates", async () => {
			getSpy.mockResolvedValue({ data: { data: mockTemplates } });

			const result = await client.getTemplates(templateId);

			expect(getSpy).toHaveBeenCalledTimes(1);
			expect(getSpy).toHaveBeenCalledWith(
				`/templates?workspaceId=${templateId}`,
			);
			expect(result).toEqual(mockTemplates);
		});

		it("returns empty array when no templates", async () => {
			getSpy.mockResolvedValue({ data: { data: [] } });

			const result = await client.getTemplates(templateId);

			expect(result).toEqual([]);
		});
	});
});
