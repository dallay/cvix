import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import {
	type ResumeDocumentResponse,
	ResumeHttpClient,
} from "./ResumeHttpClient";

describe("ResumeHttpClient", () => {
	const mockResume: Resume = {
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
		getSpy = vi.fn();
		postSpy = vi.fn();
		putSpy = vi.fn();
		deleteSpy = vi.fn();

		// Override axios instance methods
		// biome-ignore lint/suspicious/noExplicitAny: overriding internal axios instance for testing
		(client as any).client.get = getSpy;
		// biome-ignore lint/suspicious/noExplicitAny: overriding internal axios instance for testing
		(client as any).client.post = postSpy;
		// biome-ignore lint/suspicious/noExplicitAny: overriding internal axios instance for testing
		(client as any).client.put = putSpy;
		// biome-ignore lint/suspicious/noExplicitAny: overriding internal axios instance for testing
		(client as any).client.delete = deleteSpy;
	});

	describe("generatePdf", () => {
		it("posts mapped payload to /resume/generate with locale header", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			const blob = await client.generatePdf(mockResume, "es");
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

		it("generates PDF with default English locale", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			await client.generatePdf(mockResume);

			const callArgs = postSpy.mock.calls[0];
			if (!callArgs) throw new Error("Expected call args to exist");
			const [, , config] = callArgs;
			expect(config.headers["Accept-Language"]).toBeUndefined();
		});

		it("sets Accept header to application/pdf", async () => {
			postSpy.mockResolvedValue({
				data: new Blob([new Uint8Array([1])], { type: "application/pdf" }),
			});

			await client.generatePdf(mockResume, "en");

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
			expect(payload.content.basics.name).toBe("Yuniel Acosta Pérez");
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
			expect(payload.content.basics.name).toBe("Yuniel Acosta Pérez");
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

			const result = await client.getTemplates();

			expect(getSpy).toHaveBeenCalledTimes(1);
			expect(getSpy).toHaveBeenCalledWith("/templates");
			expect(result).toEqual(mockTemplates);
		});

		it("returns empty array when no templates", async () => {
			getSpy.mockResolvedValue({ data: { data: [] } });

			const result = await client.getTemplates();

			expect(result).toEqual([]);
		});
	});
});
