import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import { usePdf } from "./usePdf";

// Mock the resumeHttpClient
vi.mock("../../http/ResumeHttpClient", () => ({
	resumeHttpClient: {
		getTemplates: vi.fn(),
		generatePdf: vi.fn(),
	},
}));

// Import after mock to get mocked version
import { resumeHttpClient } from "../../http/ResumeHttpClient";

// Mock URL.createObjectURL and URL.revokeObjectURL
const mockCreateObjectURL = vi.fn();
const mockRevokeObjectURL = vi.fn();

describe("usePdf", () => {
	const mockResume: Resume = {
		basics: {
			name: "John Doe",
			label: "Software Engineer",
			image: "",
			email: "john@example.com",
			phone: "+1234567890",
			url: "",
			summary: "Experienced developer",
			location: {
				address: "",
				postalCode: "",
				city: "San Francisco",
				region: "CA",
				countryCode: "US",
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
		projects: [],
		references: [],
	};

	const mockTemplates: TemplateMetadata[] = [
		{
			id: "classic",
			name: "Classic",
			version: "1.0.0",
			description: "A classic professional template",
			supportedLocales: ["en", "es"],
			previewUrl: "/thumbnails/classic.png",
			params: {
				colorPalette: "professional",
				fontFamily: "serif",
			},
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

	beforeEach(() => {
		vi.clearAllMocks();

		// Setup URL mocks
		vi.stubGlobal("URL", {
			createObjectURL: mockCreateObjectURL,
			revokeObjectURL: mockRevokeObjectURL,
		});
		mockCreateObjectURL.mockReturnValue("blob:http://localhost/mock-url");
	});

	describe("initialization", () => {
		it("should initialize with default values", () => {
			const { isGenerating, isLoadingTemplates, error, templates, pdfUrl } =
				usePdf();

			expect(isGenerating.value).toBe(false);
			expect(isLoadingTemplates.value).toBe(true);
			expect(error.value).toBeNull();
			expect(templates.value).toEqual([]);
			expect(pdfUrl.value).toBeNull();
		});
	});

	describe("fetchTemplates", () => {
		it("should fetch templates successfully", async () => {
			vi.mocked(resumeHttpClient.getTemplates).mockResolvedValue(mockTemplates);

			const { fetchTemplates, templates, isLoadingTemplates, error } = usePdf();
			// workspaceId is now sent via X-Workspace-Id header automatically
			await fetchTemplates();

			expect(resumeHttpClient.getTemplates).toHaveBeenCalledOnce();
			expect(templates.value).toEqual(mockTemplates);
			expect(isLoadingTemplates.value).toBe(false);
			expect(error.value).toBeNull();
		});

		it("should handle fetch templates error with Error instance", async () => {
			const errorMessage = "Network error";
			vi.mocked(resumeHttpClient.getTemplates).mockRejectedValue(
				new Error(errorMessage),
			);

			const { fetchTemplates, templates, isLoadingTemplates, error } = usePdf();

			// workspaceId is now sent via X-Workspace-Id header automatically
			await fetchTemplates();

			expect(templates.value).toEqual([]);
			expect(isLoadingTemplates.value).toBe(false);
			expect(error.value).toBe(errorMessage);
		});

		it("should handle fetch templates error with non-Error", async () => {
			vi.mocked(resumeHttpClient.getTemplates).mockRejectedValue(
				"Unknown error",
			);

			const { fetchTemplates, error } = usePdf();

			// workspaceId is now sent via X-Workspace-Id header automatically
			await fetchTemplates();

			expect(error.value).toBe("Failed to load templates");
		});

		it("should set isLoadingTemplates to true during fetch", async () => {
			let resolvePromise!: (value: TemplateMetadata[]) => void;
			const pendingPromise = new Promise<TemplateMetadata[]>((resolve) => {
				resolvePromise = resolve;
			});

			vi.mocked(resumeHttpClient.getTemplates).mockReturnValue(pendingPromise);

			const { fetchTemplates, isLoadingTemplates } = usePdf();
			// workspaceId is now sent via X-Workspace-Id header automatically
			const fetchPromise = fetchTemplates();

			expect(isLoadingTemplates.value).toBe(true);

			resolvePromise(mockTemplates);
			await fetchPromise;
			expect(isLoadingTemplates.value).toBe(false);
		});

		it("should clear previous error on new fetch", async () => {
			vi.mocked(resumeHttpClient.getTemplates)
				.mockRejectedValueOnce(new Error("First error"))
				.mockResolvedValueOnce(mockTemplates);

			const { fetchTemplates, error } = usePdf();

			// workspaceId is now sent via X-Workspace-Id header automatically
			await fetchTemplates();
			expect(error.value).toBe("First error");

			await fetchTemplates();
			expect(error.value).toBeNull();
		});
	});

	describe("generatePdf", () => {
		const mockPdfBlob = new Blob(["PDF content"], { type: "application/pdf" });

		it("should generate PDF successfully with default locale", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockResolvedValue(mockPdfBlob);

			const { generatePdf, isGenerating, error, pdfUrl } = usePdf();

			const result = await generatePdf(mockResume, "classic", {});

			expect(resumeHttpClient.generatePdf).toHaveBeenCalledWith(
				"classic",
				mockResume,
				"en",
			);
			expect(result).toBe(mockPdfBlob);
			expect(isGenerating.value).toBe(false);
			expect(error.value).toBeNull();
			expect(pdfUrl.value).toBe("blob:http://localhost/mock-url");
			expect(mockCreateObjectURL).toHaveBeenCalledWith(mockPdfBlob);
		});

		it("should generate PDF with Spanish locale", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockResolvedValue(mockPdfBlob);

			const { generatePdf } = usePdf();

			await generatePdf(mockResume, "classic", { locale: "es" });

			expect(resumeHttpClient.generatePdf).toHaveBeenCalledWith(
				"classic",
				mockResume,
				"es",
			);
		});

		it("should default to en for invalid locale", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockResolvedValue(mockPdfBlob);

			const { generatePdf } = usePdf();

			await generatePdf(mockResume, "classic", { locale: "fr" });

			expect(resumeHttpClient.generatePdf).toHaveBeenCalledWith(
				"classic",
				mockResume,
				"en",
			);
		});

		it("should handle generate PDF error with Error instance", async () => {
			const errorMessage = "Generation failed";
			vi.mocked(resumeHttpClient.generatePdf).mockRejectedValue(
				new Error(errorMessage),
			);

			const { generatePdf, error, isGenerating } = usePdf();

			await expect(generatePdf(mockResume, "classic", {})).rejects.toThrow(
				errorMessage,
			);

			expect(error.value).toBe(errorMessage);
			expect(isGenerating.value).toBe(false);
		});

		it("should handle generate PDF error with non-Error", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockRejectedValue("Unknown");

			const { generatePdf, error } = usePdf();

			await expect(generatePdf(mockResume, "classic", {})).rejects.toBe(
				"Unknown",
			);

			expect(error.value).toBe("Failed to generate PDF");
		});

		it("should set isGenerating to true during generation", async () => {
			let resolvePromise!: (value: Blob) => void;
			const pendingPromise = new Promise<Blob>((resolve) => {
				resolvePromise = resolve;
			});

			vi.mocked(resumeHttpClient.generatePdf).mockReturnValue(pendingPromise);

			const { generatePdf, isGenerating } = usePdf();

			const genPromise = generatePdf(mockResume, "classic", {});

			expect(isGenerating.value).toBe(true);

			resolvePromise(mockPdfBlob);
			await genPromise;
			expect(isGenerating.value).toBe(false);
		});

		it("should revoke previous pdfUrl before generating new one", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockResolvedValue(mockPdfBlob);
			mockCreateObjectURL
				.mockReturnValueOnce("blob:http://localhost/first-url")
				.mockReturnValueOnce("blob:http://localhost/second-url");

			const { generatePdf, pdfUrl } = usePdf();

			await generatePdf(mockResume, "classic", {});
			expect(pdfUrl.value).toBe("blob:http://localhost/first-url");

			await generatePdf(mockResume, "modern", {});

			expect(mockRevokeObjectURL).toHaveBeenCalledWith(
				"blob:http://localhost/first-url",
			);
			expect(pdfUrl.value).toBe("blob:http://localhost/second-url");
		});

		it("should clear pdfUrl on error", async () => {
			vi.mocked(resumeHttpClient.generatePdf)
				.mockResolvedValueOnce(mockPdfBlob)
				.mockRejectedValueOnce(new Error("Failed"));

			const { generatePdf, pdfUrl } = usePdf();

			await generatePdf(mockResume, "classic", {});
			expect(pdfUrl.value).toBe("blob:http://localhost/mock-url");

			await expect(generatePdf(mockResume, "modern", {})).rejects.toThrow();
			expect(pdfUrl.value).toBeNull();
		});

		it("should clear error on successful generation", async () => {
			vi.mocked(resumeHttpClient.generatePdf)
				.mockRejectedValueOnce(new Error("First error"))
				.mockResolvedValueOnce(mockPdfBlob);

			const { generatePdf, error } = usePdf();

			await expect(generatePdf(mockResume, "classic", {})).rejects.toThrow();
			expect(error.value).toBe("First error");

			await generatePdf(mockResume, "classic", {});
			expect(error.value).toBeNull();
		});
	});

	describe("downloadPdf", () => {
		let mockLink: {
			href: string;
			download: string;
			click: ReturnType<typeof vi.fn>;
			remove: ReturnType<typeof vi.fn>;
		};
		let appendChildSpy: ReturnType<typeof vi.spyOn>;

		beforeEach(() => {
			mockLink = {
				href: "",
				download: "",
				click: vi.fn(),
				remove: vi.fn(),
			};

			vi.spyOn(document, "createElement").mockReturnValue(
				mockLink as unknown as HTMLAnchorElement,
			);
			appendChildSpy = vi
				.spyOn(document.body, "appendChild")
				.mockImplementation(() => mockLink as unknown as Node);
		});

		it("should download PDF with default filename", () => {
			const blob = new Blob(["PDF content"], { type: "application/pdf" });

			const { downloadPdf } = usePdf();

			downloadPdf(blob);

			expect(document.createElement).toHaveBeenCalledWith("a");
			expect(mockLink.href).toBe("blob:http://localhost/mock-url");
			expect(mockLink.download).toBe("resume.pdf");
			expect(appendChildSpy).toHaveBeenCalledWith(mockLink);
			expect(mockLink.click).toHaveBeenCalledOnce();
			expect(mockLink.remove).toHaveBeenCalledOnce();
			expect(mockRevokeObjectURL).toHaveBeenCalledWith(
				"blob:http://localhost/mock-url",
			);
		});

		it("should download PDF with custom filename", () => {
			const blob = new Blob(["PDF content"], { type: "application/pdf" });

			const { downloadPdf } = usePdf();

			downloadPdf(blob, "john-doe-resume.pdf");

			expect(mockLink.download).toBe("john-doe-resume.pdf");
		});
	});

	describe("cleanup on scope dispose", () => {
		it("should revoke pdfUrl when component is disposed", async () => {
			vi.mocked(resumeHttpClient.generatePdf).mockResolvedValue(
				new Blob(["PDF"], { type: "application/pdf" }),
			);

			// Mock effectScope to test cleanup
			const { effectScope } = await import("vue");
			const scope = effectScope();

			let pdfUrlValue: string | null = null;

			await scope.run(async () => {
				const { generatePdf, pdfUrl } = usePdf();
				await generatePdf(mockResume, "classic", {});
				pdfUrlValue = pdfUrl.value;
			});

			expect(pdfUrlValue).toBe("blob:http://localhost/mock-url");

			// Stop the scope to trigger cleanup
			scope.stop();

			expect(mockRevokeObjectURL).toHaveBeenCalledWith(
				"blob:http://localhost/mock-url",
			);
		});

		it("should not revoke if pdfUrl is null on dispose", async () => {
			const { effectScope } = await import("vue");
			const scope = effectScope();

			scope.run(() => {
				usePdf();
			});

			mockRevokeObjectURL.mockClear();
			scope.stop();

			expect(mockRevokeObjectURL).not.toHaveBeenCalled();
		});
	});
});
