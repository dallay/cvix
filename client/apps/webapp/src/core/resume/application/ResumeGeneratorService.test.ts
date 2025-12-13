import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator";
import { createTestResume } from "@/core/resume/test-resume-factory.ts";
import { ResumeGeneratorService } from "./ResumeGeneratorService";

describe("ResumeGeneratorService", () => {
	let mockResumeGenerator: ResumeGenerator;
	let mockResumeValidator: ResumeValidator;
	let service: ResumeGeneratorService;
	let mockResume: Resume;

	beforeEach(() => {
		mockResume = createTestResume();

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

			const templateId = "template-123";
			const result = await service.generateResumePdf(templateId, mockResume);

			expect(mockResumeValidator.validate).toHaveBeenCalledWith(mockResume);
			expect(mockResumeGenerator.generatePdf).toHaveBeenCalledWith(
				templateId,
				mockResume,
				"en",
			);
			expect(result).toBe(mockBlob);
		});

		it("should generate PDF with custom locale", async () => {
			const mockBlob = new Blob(["fake pdf"], { type: "application/pdf" });
			vi.mocked(mockResumeValidator.validate).mockReturnValue(true);
			vi.mocked(mockResumeGenerator.generatePdf).mockResolvedValue(mockBlob);

			const templateId = "template-123";
			await service.generateResumePdf(templateId, mockResume, "es");

			expect(mockResumeGenerator.generatePdf).toHaveBeenCalledWith(
				templateId,
				mockResume,
				"es",
			);
		});

		it("should throw error when resume data is invalid", async () => {
			vi.mocked(mockResumeValidator.validate).mockReturnValue(false);

			await expect(
				service.generateResumePdf("template-123", mockResume),
			).rejects.toThrow("Invalid resume data");

			expect(mockResumeValidator.validate).toHaveBeenCalledWith(mockResume);
			expect(mockResumeGenerator.generatePdf).not.toHaveBeenCalled();
		});

		it("should propagate error from PDF generator", async () => {
			vi.mocked(mockResumeValidator.validate).mockReturnValue(true);
			vi.mocked(mockResumeGenerator.generatePdf).mockRejectedValue(
				new Error("PDF generation failed"),
			);

			await expect(
				service.generateResumePdf("template-123", mockResume),
			).rejects.toThrow("PDF generation failed");
		});
	});
});
