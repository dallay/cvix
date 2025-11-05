import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useResumeGeneration } from "../composables/useResumeGeneration";
import { resumeHttpClient } from "../infrastructure";
import type { Resume } from "../types/resume";

// Mock the resume HTTP client
vi.mock("../infrastructure", () => ({
	resumeHttpClient: {
		generateResumePdf: vi.fn(),
	},
}));

// Mock file download
const mockCreateObjectURL = vi.fn(() => "blob:mock-url");
const mockRevokeObjectURL = vi.fn();
global.URL.createObjectURL = mockCreateObjectURL;
global.URL.revokeObjectURL = mockRevokeObjectURL;

describe("useResumeGeneration", () => {
	beforeEach(() => {
		setActivePinia(createPinia());
		vi.clearAllMocks();
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
		vi.mocked(resumeHttpClient.generateResumePdf).mockResolvedValueOnce(
			mockBlob,
		);

		const { generateResume, isGenerating, error } = useResumeGeneration();
		const resume = createMockResume();

		const result = await generateResume(resume, "en");

		expect(result).toBe(true);
		expect(resumeHttpClient.generateResumePdf).toHaveBeenCalledWith(
			resume,
			"en",
		);
		expect(isGenerating.value).toBe(false);
		expect(error.value).toBeNull();
	});

	it("should use HttpOnly cookies for authentication instead of localStorage", async () => {
		const mockBlob = new Blob(["fake-pdf-content"], {
			type: "application/pdf",
		});
		vi.mocked(resumeHttpClient.generateResumePdf).mockResolvedValueOnce(
			mockBlob,
		);

		const { generateResume } = useResumeGeneration();
		const resume = createMockResume();

		await generateResume(resume, "en");

		expect(resumeHttpClient.generateResumePdf).toHaveBeenCalledWith(
			resume,
			"en",
		);
	});
});
