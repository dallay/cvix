import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useResumeGeneration } from "../composables/useResumeGeneration";
import type { Resume } from "../types/resume";

// Mock fetch API
const mockFetch = vi.fn();
global.fetch = mockFetch;

// Mock file download
const mockCreateObjectURL = vi.fn(() => "blob:mock-url");
const mockRevokeObjectURL = vi.fn();
global.URL.createObjectURL = mockCreateObjectURL;
global.URL.revokeObjectURL = mockRevokeObjectURL;

describe("useResumeGeneration", () => {
	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();
		mockFetch.mockReset();
	});

	const createMockResume = (): Resume => ({
		basics: {
			name: "John Doe",
			email: "john@example.com",
			phone: "+1234567890",
		},
		work: [
			{
				company: "Tech Corp",
				position: "Developer",
				startDate: "2020-01-01",
			},
		],
		skills: [
			{
				name: "Programming",
				category: "Programming",
				keywords: ["Kotlin", "Java"],
			},
		],
	});

	it("should initialize with default state", () => {
		const { isGenerating, error, progress } = useResumeGeneration();

		expect(isGenerating.value).toBe(false);
		expect(error.value).toBeNull();
		expect(progress.value).toBe(0);
	});

	it("should generate resume successfully", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		const { generateResume, isGenerating, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(mockFetch).toHaveBeenCalledWith(
			"/api/resumes",
			expect.objectContaining({
				method: "POST",
				headers: expect.objectContaining({
					"Content-Type": "application/json",
					"Accept-Language": "en",
				}),
				body: JSON.stringify(resume),
			}),
		);
		expect(isGenerating.value).toBe(false);
		expect(error.value).toBeNull();
	});

	it("should generate resume with Spanish locale", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		const { generateResume } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "es");

		expect(mockFetch).toHaveBeenCalledWith(
			"/api/resumes",
			expect.objectContaining({
				headers: expect.objectContaining({
					"Accept-Language": "es",
				}),
			}),
		);
	});

	it("should set isGenerating to true during generation", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		let resolvePromise: ((value: unknown) => void) | undefined;
		const promise = new Promise((resolve) => {
			resolvePromise = resolve;
		});
		mockFetch.mockReturnValueOnce(promise);

		const { generateResume, isGenerating } = useResumeGeneration();
		const resume = createMockResume();

		const generationPromise = generateResume(resume, "en");

		// Should be generating
		expect(isGenerating.value).toBe(true);

		// Complete the request
		resolvePromise?.({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});
		await generationPromise;

		// Should no longer be generating
		expect(isGenerating.value).toBe(false);
	});

	it("should handle 400 validation error", async () => {
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 400,
			json: async () => ({
				error: {
					code: "invalid_request",
					message: "Invalid resume data",
					errors: [
						{
							field: "email",
							message: "Invalid email format",
						},
					],
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value).toEqual({
			code: "invalid_request",
			message: "Invalid resume data",
			errors: [
				{
					field: "email",
					message: "Invalid email format",
				},
			],
		});
	});

	it("should handle 422 business rule validation error", async () => {
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 422,
			json: async () => ({
				error: {
					code: "invalid_resume_data",
					message:
						"Resume must have at least one of work experience, education, or skills",
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value?.code).toBe("invalid_resume_data");
	});

	it("should handle 429 rate limit error", async () => {
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 429,
			json: async () => ({
				error: {
					code: "rate_limit_exceeded",
					message: "Too many requests. Please try again later.",
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value?.code).toBe("rate_limit_exceeded");
	});

	it("should handle 500 server error", async () => {
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 500,
			json: async () => ({
				error: {
					code: "internal_server_error",
					message: "An unexpected error occurred",
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value?.code).toBe("internal_server_error");
	});

	it("should handle 504 timeout error", async () => {
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 504,
			json: async () => ({
				error: {
					code: "pdf_generation_timeout",
					message: "PDF generation timed out",
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value?.code).toBe("pdf_generation_timeout");
	});

	it("should handle network error", async () => {
		mockFetch.mockRejectedValueOnce(new Error("Network error"));

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(error.value).toBeTruthy();
		expect(error.value?.message).toContain("Network error");
	});

	it("should download PDF when generation succeeds", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		// Mock document.createElement and appendChild
		const mockAnchor = {
			href: "",
			download: "",
			click: vi.fn(),
		};
		const createElementSpy = vi
			.spyOn(document, "createElement")
			.mockReturnValue(mockAnchor as unknown as HTMLElement);
		const appendChildSpy = vi
			.spyOn(document.body, "appendChild")
			.mockImplementation(() => mockAnchor as unknown as Node);
		const removeChildSpy = vi
			.spyOn(document.body, "removeChild")
			.mockImplementation(() => mockAnchor as unknown as Node);

		const { generateResume } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		// Should create download link
		expect(createElementSpy).toHaveBeenCalledWith("a");
		expect(mockCreateObjectURL).toHaveBeenCalledWith(mockBlob);
		expect(mockAnchor.href).toBe("blob:mock-url");
		expect(mockAnchor.download).toContain("resume");
		expect(mockAnchor.download).toContain(".pdf");
		expect(mockAnchor.click).toHaveBeenCalled();
		expect(appendChildSpy).toHaveBeenCalled();
		expect(removeChildSpy).toHaveBeenCalled();
		expect(mockRevokeObjectURL).toHaveBeenCalledWith("blob:mock-url");

		createElementSpy.mockRestore();
		appendChildSpy.mockRestore();
		removeChildSpy.mockRestore();
	});

	it("should update progress during generation", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		const { generateResume, progress } = useResumeGeneration();
		const resume = createMockResume();

		const generationPromise = generateResume(resume, "en");

		// Progress should update (implementation detail - may vary)
		// At minimum, should be 0 at start and 100 at end
		await generationPromise;

		expect(progress.value).toBe(100);
	});

	it("should clear error when starting new generation", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});

		// First request fails
		mockFetch.mockResolvedValueOnce({
			ok: false,
			status: 500,
			json: async () => ({
				error: {
					code: "internal_server_error",
					message: "An unexpected error occurred",
				},
			}),
		});

		const { generateResume, error } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");
		expect(error.value).toBeTruthy();

		// Second request succeeds
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		await generateResume(resume, "en");

		// Error should be cleared
		expect(error.value).toBeNull();
	});

	it("should include authentication token in request if available", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		mockFetch.mockResolvedValueOnce({
			ok: true,
			blob: async () => mockBlob,
			headers: new Headers({
				"content-type": "application/pdf",
			}),
		});

		// Mock auth token in localStorage
		const mockToken = "mock-auth-token";
		localStorage.setItem("auth_token", mockToken);

		const { generateResume } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(mockFetch).toHaveBeenCalledWith(
			"/api/resumes",
			expect.objectContaining({
				headers: expect.objectContaining({
					Authorization: `Bearer ${mockToken}`,
				}),
			}),
		);

		localStorage.removeItem("auth_token");
	});
});
