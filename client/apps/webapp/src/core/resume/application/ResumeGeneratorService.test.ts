import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator";
import { ResumeGeneratorService } from "./ResumeGeneratorService";

describe("ResumeGeneratorService", () => {
	let mockResumeGenerator: ResumeGenerator;
	let mockResumeValidator: ResumeValidator;
	let service: ResumeGeneratorService;
	let mockResume: Resume;

	beforeEach(() => {
		mockResume = {
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

		mockResumeGenerator = {
			generatePdf: vi.fn(),
		};

		mockResumeValidator = {
			validate: vi.fn(),
			getErrors: vi.fn(() => []),
		};

		service = new ResumeGeneratorService(
			mockResumeGenerator,
			mockResumeValidator,
		);
	});

	describe("generateResumePdf", () => {
		it("should generate PDF when resume data is valid", async () => {
			const mockBlob = new Blob(["fake pdf"], { type: "application/pdf" });
			vi.mocked(mockResumeValidator.validate).mockReturnValue(true);
			vi.mocked(mockResumeGenerator.generatePdf).mockResolvedValue(mockBlob);

			const result = await service.generateResumePdf(mockResume);

			expect(mockResumeValidator.validate).toHaveBeenCalledWith(mockResume);
			expect(mockResumeGenerator.generatePdf).toHaveBeenCalledWith(
				mockResume,
				"en",
			);
			expect(result).toBe(mockBlob);
		});

		it("should generate PDF with custom locale", async () => {
			const mockBlob = new Blob(["fake pdf"], { type: "application/pdf" });
			vi.mocked(mockResumeValidator.validate).mockReturnValue(true);
			vi.mocked(mockResumeGenerator.generatePdf).mockResolvedValue(mockBlob);

			await service.generateResumePdf(mockResume, "es");

			expect(mockResumeGenerator.generatePdf).toHaveBeenCalledWith(
				mockResume,
				"es",
			);
		});

		it("should throw error when resume data is invalid", async () => {
			vi.mocked(mockResumeValidator.validate).mockReturnValue(false);

			await expect(service.generateResumePdf(mockResume)).rejects.toThrow(
				"Invalid resume data",
			);

			expect(mockResumeValidator.validate).toHaveBeenCalledWith(mockResume);
			expect(mockResumeGenerator.generatePdf).not.toHaveBeenCalled();
		});

		it("should propagate error from PDF generator", async () => {
			vi.mocked(mockResumeValidator.validate).mockReturnValue(true);
			vi.mocked(mockResumeGenerator.generatePdf).mockRejectedValue(
				new Error("PDF generation failed"),
			);

			await expect(service.generateResumePdf(mockResume)).rejects.toThrow(
				"PDF generation failed",
			);
		});
	});
});
